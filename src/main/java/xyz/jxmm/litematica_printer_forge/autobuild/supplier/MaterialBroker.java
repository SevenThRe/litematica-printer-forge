package xyz.jxmm.litematica_printer_forge.autobuild.supplier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;

/**
 * Current-layer restock coordinator.
 *
 * Input: per-item material demand of the layer being printed (replacement-aware,
 * from AutoBuildPlanner.getMaterialNeeds). The broker first counts the player
 * inventory, then walks the supplier chain in fixed order:
 *   1) Sophisticated Backpacks real transfer (Task 7)
 *   2) Refined Storage extraction (Task 8)
 *   3) BII ghost injection (Task 9)
 * A supplier is only triggered when the previous ones left a residual shortage.
 *
 * Accounting is delta-based: inventory counts are snapshotted before a supplier
 * starts and re-counted SETTLE_TICKS after it finishes; the difference is the
 * verified amount obtained. Suppliers are tick-driven state machines; while the
 * broker runs the director stays in SUPPLYING (printing/navigation suppressed).
 */
public final class MaterialBroker {
    private static final String TAG = "[AUTOBUILD] ";
    private static final int SETTLE_TICKS = 15;
    private static final int SUPPLIER_TIMEOUT_TICKS = 1200;

    public enum Status {
        IDLE, RUNNING, DONE, EXHAUSTED
    }

    /**
     * A material source. start() begins an asynchronous fetch of the given
     * shortages into the player inventory; tick() returns true while work is
     * in progress. abort() must leave menus closed and state cleaned up.
     */
    public interface MaterialSupplier {
        String name();

        boolean isAvailable(Minecraft mc);

        void start(Minecraft mc, Map<Item, Integer> shortages);

        /** @return true while still working, false when finished or failed. */
        boolean tick(Minecraft mc);

        void abort(Minecraft mc);
    }

    private static Status status = Status.IDLE;
    private static Map<Item, Integer> residual = new LinkedHashMap<>();
    private static List<MaterialSupplier> chain = new ArrayList<>();
    private static int chainIdx = 0;
    private static Map<Item, Integer> countBefore = new HashMap<>();
    private static int settleTicksLeft = 0;
    private static int supplierTicks = 0;
    private static String statusLine = "";

    private MaterialBroker() {
    }

    public static Status getStatus() {
        return status;
    }

    /** Remaining unsatisfied demand; empty when fully supplied. */
    public static Map<Item, Integer> getResidual() {
        return residual;
    }

    public static String getStatusLine() {
        return statusLine;
    }

    /** Same counting semantics as BackpackInjector.countInInventory. */
    public static int countInInventory(Minecraft mc, Item item) {
        if (mc.f_91074_ == null) return 0;
        Inventory inv = mc.f_91074_.m_150109_();
        int count = 0;
        for (int i = 0; i < inv.m_6643_(); i++) {
            ItemStack s = inv.m_8020_(i);
            if (!s.m_41619_() && s.m_41720_() == item) {
                count += s.m_41613_();
            }
        }
        return count;
    }

    /** Demand minus what the player already carries; only positive entries. */
    public static Map<Item, Integer> computeShortages(Minecraft mc, Map<Item, Integer> needs) {
        Map<Item, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<Item, Integer> e : needs.entrySet()) {
            int shortage = e.getValue() - countInInventory(mc, e.getKey());
            if (shortage > 0) {
                out.put(e.getKey(), shortage);
            }
        }
        return out;
    }

    public static boolean hasShortage(Minecraft mc, Map<Item, Integer> needs) {
        return !computeShortages(mc, needs).isEmpty();
    }

    /**
     * Starts the restock chain for the given layer demand. When the player
     * already carries everything, completes immediately with status DONE.
     */
    public static void begin(Minecraft mc, Map<Item, Integer> needs) {
        abort(mc);
        residual = computeShortages(mc, needs);
        if (residual.isEmpty()) {
            status = Status.DONE;
            return;
        }
        System.out.println(TAG + "Restock started, shortages: " + describe(residual));
        chain = buildChain(mc);
        chainIdx = 0;
        settleTicksLeft = 0;
        status = Status.RUNNING;
        startNextSupplier(mc);
    }

    private static List<MaterialSupplier> buildChain(Minecraft mc) {
        List<MaterialSupplier> out = new ArrayList<>();
        if (LitematicaMixinMod.AUTO_BUILD_SB_TRANSFER.getBooleanValue()) {
            out.add(new SbTransferSupplier());
        }
        if (LitematicaMixinMod.AUTO_BUILD_RS_EXTRACT.getBooleanValue()) {
            out.add(new RsExtractSupplier());
        }
        if (LitematicaMixinMod.BII_ENABLED.getBooleanValue()) {
            out.add(new BiiInjectSupplier());
        }
        return out;
    }

    /**
     * Drives the active supplier. Returns true once the broker reached a
     * terminal state (DONE or EXHAUSTED).
     */
    public static boolean tick(Minecraft mc) {
        if (status != Status.RUNNING) return true;
        if (mc.f_91074_ == null || mc.f_91073_ == null) {
            abort(mc);
            status = Status.EXHAUSTED;
            return true;
        }

        if (settleTicksLeft > 0) {
            settleTicksLeft--;
            if (settleTicksLeft == 0) {
                finishSupplierAccounting(mc);
                if (residual.isEmpty()) {
                    status = Status.DONE;
                    statusLine = "";
                    System.out.println(TAG + "Restock complete");
                    return true;
                }
                chainIdx++;
                startNextSupplier(mc);
            }
            return false;
        }

        if (chainIdx >= chain.size()) {
            status = Status.EXHAUSTED;
            statusLine = "";
            System.out.println(TAG + "Restock exhausted, residual: " + describe(residual));
            return true;
        }

        MaterialSupplier active = chain.get(chainIdx);
        supplierTicks++;
        if (supplierTicks > SUPPLIER_TIMEOUT_TICKS) {
            System.out.println(TAG + "Supplier " + active.name() + " timed out, skipping");
            safeAbort(active, mc);
            chainIdx++;
            startNextSupplier(mc);
            return false;
        }

        boolean working;
        try {
            working = active.tick(mc);
        } catch (Throwable t) {
            System.out.println(TAG + "Supplier " + active.name() + " failed: " + t);
            safeAbort(active, mc);
            working = false;
        }
        if (!working) {
            settleTicksLeft = SETTLE_TICKS;
        }
        return false;
    }

    public static void abort(Minecraft mc) {
        if (status == Status.RUNNING && settleTicksLeft == 0 && chainIdx < chain.size()) {
            safeAbort(chain.get(chainIdx), mc);
        }
        status = Status.IDLE;
        residual = new LinkedHashMap<>();
        chain = new ArrayList<>();
        chainIdx = 0;
        countBefore = new HashMap<>();
        settleTicksLeft = 0;
        supplierTicks = 0;
        statusLine = "";
    }

    private static void startNextSupplier(Minecraft mc) {
        while (chainIdx < chain.size()) {
            MaterialSupplier s = chain.get(chainIdx);
            boolean avail;
            try {
                avail = s.isAvailable(mc);
            } catch (Throwable t) {
                System.out.println(TAG + "Supplier " + s.name() + " availability check failed: " + t);
                avail = false;
            }
            if (avail) {
                countBefore = countAll(mc, residual.keySet());
                supplierTicks = 0;
                statusLine = "Supplying via " + s.name();
                System.out.println(TAG + "Trying supplier " + s.name() + " for: " + describe(residual));
                try {
                    s.start(mc, new LinkedHashMap<>(residual));
                } catch (Throwable t) {
                    System.out.println(TAG + "Supplier " + s.name() + " start failed: " + t);
                    safeAbort(s, mc);
                    chainIdx++;
                    continue;
                }
                return;
            }
            chainIdx++;
        }
        statusLine = "";
    }

    private static void finishSupplierAccounting(Minecraft mc) {
        Map<Item, Integer> obtained = new LinkedHashMap<>();
        for (Item item : new ArrayList<>(residual.keySet())) {
            int before = countBefore.getOrDefault(item, 0);
            int after = countInInventory(mc, item);
            int got = Math.max(0, after - before);
            if (got > 0) {
                obtained.put(item, got);
                int left = residual.get(item) - got;
                if (left <= 0) {
                    residual.remove(item);
                } else {
                    residual.put(item, left);
                }
            }
        }
        System.out.println(TAG + "Supplier pass done, obtained: " + describe(obtained)
                + " residual: " + describe(residual));
    }

    private static Map<Item, Integer> countAll(Minecraft mc, Collection<Item> items) {
        Map<Item, Integer> out = new HashMap<>();
        for (Item it : items) {
            out.put(it, countInInventory(mc, it));
        }
        return out;
    }

    private static void safeAbort(MaterialSupplier s, Minecraft mc) {
        try {
            s.abort(mc);
        } catch (Throwable ignored) {
        }
    }

    private static String describe(Map<Item, Integer> map) {
        if (map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<Item, Integer> e : map.entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(new ItemStack(e.getKey()).m_41786_().getString()).append(" x").append(e.getValue());
        }
        return sb.append("}").toString();
    }
}
