package xyz.jxmm.litematica_printer_forge.autobuild.supplier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.jxmm.litematica_printer_forge.utils.BackpackInjector;

/**
 * Sophisticated Backpacks real-transfer supplier.
 *
 * Opens the backpack via BackpackOpenMessage (same packet BII uses), waits for
 * the storage menu to sync, then moves materials into the player inventory with
 * standard PICKUP click pairs (backpack slot -> cursor -> empty player slot).
 * QUICK_MOVE is never used: the COY3 server silently rejects it.
 *
 * Obtained amounts are not self-reported; MaterialBroker measures the real
 * inventory delta after a settle window.
 */
public final class SbTransferSupplier implements MaterialBroker.MaterialSupplier {
    private static final String TAG = "[AUTOBUILD] ";
    private static final int MENU_SYNC_DELAY = 10;
    private static final int OPEN_TIMEOUT_TICKS = 60;
    private static final int PAIRS_PER_TICK = 8;

    private enum Phase {
        IDLE, OPENING, SYNC, TRANSFER, DONE
    }

    private Phase phase = Phase.IDLE;
    private int waitTicks = 0;
    private Map<Item, Integer> shortages = new LinkedHashMap<>();
    private List<int[]> plannedPairs = new ArrayList<>();
    private int pairIdx = 0;
    private boolean openedByUs = false;

    @Override
    public String name() {
        return "SB-Transfer";
    }

    @Override
    public boolean isAvailable(Minecraft mc) {
        if (mc.f_91074_ == null) return false;
        if (!BackpackInjector.isAvailable(mc)) return false;
        return BackpackInjector.findBackpackSlot(mc.f_91074_.m_150109_()) >= 0;
    }

    @Override
    public void start(Minecraft mc, Map<Item, Integer> shortages) {
        this.shortages = new LinkedHashMap<>(shortages);
        plannedPairs = new ArrayList<>();
        pairIdx = 0;
        waitTicks = 0;

        if (BackpackInjector.getBackpackMenu(mc) instanceof AbstractContainerMenu) {
            openedByUs = false;
            phase = Phase.SYNC;
            return;
        }

        Inventory inv = mc.f_91074_.m_150109_();
        int backpackSlot = BackpackInjector.findBackpackSlot(inv);
        if (backpackSlot < 0) {
            phase = Phase.DONE;
            return;
        }
        mc.m_91152_(null);
        BackpackInjector.sendBackpackOpenPacket(mc, backpackSlot);
        openedByUs = true;
        phase = Phase.OPENING;
    }

    @Override
    public boolean tick(Minecraft mc) {
        switch (phase) {
            case OPENING:
                waitTicks++;
                if (BackpackInjector.getBackpackMenu(mc) instanceof AbstractContainerMenu) {
                    phase = Phase.SYNC;
                    waitTicks = 0;
                } else if (waitTicks > OPEN_TIMEOUT_TICKS) {
                    System.out.println(TAG + "SB-Transfer: backpack open timeout");
                    phase = Phase.DONE;
                    return false;
                }
                return true;
            case SYNC:
                waitTicks++;
                if (waitTicks >= MENU_SYNC_DELAY) {
                    buildPlan(mc);
                    phase = Phase.TRANSFER;
                }
                return true;
            case TRANSFER: {
                Object menuObj = BackpackInjector.getBackpackMenu(mc);
                if (!(menuObj instanceof AbstractContainerMenu menu)) {
                    System.out.println(TAG + "SB-Transfer: menu lost during transfer");
                    phase = Phase.DONE;
                    return false;
                }
                int budget = PAIRS_PER_TICK;
                while (budget-- > 0 && pairIdx < plannedPairs.size()) {
                    int[] pair = plannedPairs.get(pairIdx++);
                    clickSlot(mc, menu, pair[0]);
                    clickSlot(mc, menu, pair[1]);
                }
                if (pairIdx >= plannedPairs.size()) {
                    depositCarried(mc, menu);
                    if (openedByUs) {
                        mc.m_91152_(null);
                    }
                    phase = Phase.DONE;
                    return false;
                }
                return true;
            }
            default:
                return false;
        }
    }

    @Override
    public void abort(Minecraft mc) {
        if (phase == Phase.SYNC || phase == Phase.TRANSFER) {
            Object menuObj = BackpackInjector.getBackpackMenu(mc);
            if (menuObj instanceof AbstractContainerMenu menu) {
                depositCarried(mc, menu);
                if (openedByUs) {
                    mc.m_91152_(null);
                }
            }
        }
        phase = Phase.IDLE;
        plannedPairs = new ArrayList<>();
        pairIdx = 0;
        shortages = new LinkedHashMap<>();
    }

    /**
     * Builds click pairs: for every shortage item, take whole stacks from
     * backpack storage slots and drop each into a distinct empty player
     * inventory slot. Slight over-fetch is acceptable (items stay with the
     * player); the broker's delta accounting records what really arrived.
     */
    private void buildPlan(Minecraft mc) {
        Object menuObj = BackpackInjector.getBackpackMenu(mc);
        if (!(menuObj instanceof AbstractContainerMenu menu)) {
            plannedPairs = new ArrayList<>();
            return;
        }
        Container playerInv = mc.f_91074_.m_150109_();
        List<Slot> slots = menu.f_38839_;
        Set<Integer> usedTargets = new HashSet<>();

        for (Map.Entry<Item, Integer> e : shortages.entrySet()) {
            Item item = e.getKey();
            int remaining = e.getValue();
            for (int i = 0; i < slots.size() && remaining > 0; i++) {
                Slot s = slots.get(i);
                if (s.f_40218_ == playerInv) continue;
                ItemStack stack = s.m_7993_();
                if (stack.m_41619_() || stack.m_41720_() != item) continue;
                int target = findEmptyPlayerSlot(slots, playerInv, usedTargets);
                if (target < 0) break;
                usedTargets.add(target);
                plannedPairs.add(new int[]{i, target});
                remaining -= stack.m_41613_();
            }
        }
        System.out.println(TAG + "SB-Transfer: planned " + plannedPairs.size() + " stack moves");
    }

    private static int findEmptyPlayerSlot(List<Slot> slots, Container playerInv, Set<Integer> usedTargets) {
        for (int i = 0; i < slots.size(); i++) {
            if (usedTargets.contains(i)) continue;
            Slot s = slots.get(i);
            if (s.f_40218_ == playerInv && s.m_7993_().m_41619_()) {
                return i;
            }
        }
        return -1;
    }

    /** Left-click PICKUP on a menu slot. slotNum is the index into menu slots. */
    private static void clickSlot(Minecraft mc, AbstractContainerMenu menu, int slotNum) {
        try {
            if (slotNum >= 0 && slotNum < menu.f_38839_.size()) {
                mc.f_91072_.m_171799_(menu.f_38840_, slotNum, 0, ClickType.PICKUP, mc.f_91074_);
            }
        } catch (Exception e) {
            System.out.println(TAG + "SB-Transfer: click failed: " + e.getMessage());
        }
    }

    /** If a place-click failed the cursor still carries items; park them anywhere empty. */
    private static void depositCarried(Minecraft mc, AbstractContainerMenu menu) {
        try {
            ItemStack carried = menu.m_142621_();
            if (carried.m_41619_()) return;
            Container playerInv = mc.f_91074_.m_150109_();
            List<Slot> slots = menu.f_38839_;
            for (int i = 0; i < slots.size(); i++) {
                Slot s = slots.get(i);
                if (s.f_40218_ == playerInv && s.m_7993_().m_41619_()) {
                    clickSlot(mc, menu, i);
                    return;
                }
            }
            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i).m_7993_().m_41619_()) {
                    clickSlot(mc, menu, i);
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println(TAG + "SB-Transfer: deposit carried failed: " + e.getMessage());
        }
    }
}
