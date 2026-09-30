/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  fi.dy.masa.litematica.world.SchematicWorldHandler
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.ContainerScreen
 *  net.minecraft.client.gui.screens.inventory.DispenserScreen
 *  net.minecraft.client.gui.screens.inventory.HopperScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BaseEntityBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.DispenserBlock
 *  net.minecraft.world.level.block.HopperBlock
 */
package xyz.jxmm.litematica_printer_forge.utils;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HopperBlock;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.utils.InventoryUtils;
import xyz.jxmm.litematica_printer_forge.utils.MessageHolder;

public class ItemInputs {
    private static long handling = new Date().getTime();
    private static final HashSet<Long> handledPos = new HashSet();
    private static Map.Entry<Long, Long> entry;
    public static BlockPos clickedPos;

    public static void clear() {
        handledPos.clear();
    }

    public static boolean canHandle() {
        return new Date().getTime() > handling + (long)LitematicaMixinMod.INVENTORY_OPERATIONS_WAIT.getIntegerValue();
    }

    private static void handle() {
        handling = new Date().getTime();
    }

    public static boolean matchStacks(List<ItemStack> required, List<Slot> current, LocalPlayer player, boolean allowNamed) {
        if (required.isEmpty()) {
            return false;
        }
        ArrayList<ItemStack> copy = new ArrayList<ItemStack>();
        for (int i = 0; i < required.size(); ++i) {
            ItemStack copiedStack = required.get(i).m_41777_();
            copiedStack.m_41774_(current.get(i).m_7993_().m_41613_());
            copy.add(copiedStack);
        }
        int[] countArray = player.m_150109_().f_35974_.stream().mapToInt(ItemStack::m_41613_).toArray();
        for (ItemStack itemStack : copy) {
            if (itemStack.m_41619_()) continue;
            int requiredAmount = itemStack.m_41613_();
            int i = 0;
            for (ItemStack playerStacks : player.m_150109_().f_35974_) {
                if (countArray[i] <= 0) {
                    ++i;
                    continue;
                }
                if (InventoryUtils.areItemsExact(itemStack, playerStacks, allowNamed)) {
                    if (countArray[i] >= requiredAmount) {
                        int n = i;
                        countArray[n] = countArray[n] - requiredAmount;
                        requiredAmount = 0;
                    } else {
                        requiredAmount -= countArray[i];
                        countArray[i] = 0;
                    }
                }
                if (requiredAmount <= 0) break;
                ++i;
            }
            if (requiredAmount <= 0) continue;
            return false;
        }
        return true;
    }

    private static int getPreference(Minecraft client, AbstractContainerMenu screen, ItemStack itemStack, boolean allowNamed) {
        if (screen == null || itemStack.m_41619_()) {
            return -1;
        }
        for (int i = 0; i < screen.f_38839_.size(); ++i) {
            ItemStack playerStacks;
            if (!(screen.m_38853_((int)i).f_40218_ instanceof Inventory) || !InventoryUtils.areItemsExact(itemStack, playerStacks = screen.m_38853_(i).m_7993_(), allowNamed)) continue;
            return i;
        }
        return -1;
    }

    private static List<Slot> getNonPlayerSlots(Minecraft client, AbstractContainerMenu screen) {
        ArrayList<Slot> retVal = new ArrayList<Slot>();
        for (int i = 0; i < screen.f_38839_.size(); ++i) {
            if (screen.m_38853_((int)i).f_40218_ instanceof Inventory) continue;
            retVal.add(screen.m_38853_(i));
        }
        return retVal;
    }

    private static void clearCursor(Minecraft client) {
        LocalPlayer player = client.f_91074_;
        if (player.f_36096_ != null && !player.f_36096_.m_142621_().m_41619_()) {
            ItemStack cursorStack = player.f_36096_.m_142621_();
            AbstractContainerMenu handler = player.f_36096_;
            for (int i = 0; i < handler.f_38839_.size(); ++i) {
                if (!ItemStack.m_150942_((ItemStack)cursorStack, (ItemStack)handler.m_38853_(i).m_7993_())) continue;
                client.f_91072_.m_171799_(handler.f_38840_, handler.m_38853_((int)i).f_40219_, 0, ClickType.PICKUP, (Player)player);
                return;
            }
            client.f_91072_.m_171799_(handler.f_38840_, -999, 1, ClickType.THROW, (Player)player);
        }
    }

    public static boolean execute(Minecraft client) {
        if (!ItemInputs.canHandle()) {
            MessageHolder.sendUniqueMessageActionBar(client.f_91074_, "Cooldown....");
            return false;
        }
        ItemInputs.handle();
        boolean allowNamed = LitematicaMixinMod.INVENTORY_OPERATIONS_FILTER_ALLOW_NAMED.getBooleanValue();
        BlockPos where = ItemInputs.rayCast(client);
        if (where == null) {
            MessageHolder.sendUniqueMessageActionBar(client.f_91074_, "Failed to raycast");
            return false;
        }
        if (handledPos.contains(where.m_121878_())) {
            MessageHolder.sendUniqueMessageActionBar(client.f_91074_, "Position is already handled");
            clickedPos = null;
            client.f_91074_.m_242612_();
            return false;
        }
        if (client.f_91074_.f_36096_ == client.f_91074_.f_36095_) {
            MessageHolder.sendUniqueMessageActionBar(client.f_91074_, "Screen is not extra screen");
            return false;
        }
        client.f_91074_.f_36096_.m_150444_();
        client.f_91074_.f_36096_.m_150429_();
        List<ItemStack> requiredStacks = ItemInputs.getRaycastRequiredItemStacks(client);
        if (requiredStacks.isEmpty()) {
            MessageHolder.sendUniqueDebugMessage("required stacks were empty for " + where.m_123344_());
            client.f_91074_.m_242612_();
            return false;
        }
        MessageHolder.sendUniqueDebugMessage("Handled pos " + where.m_123344_());
        List<Slot> nonPlayerSlot = ItemInputs.getNonPlayerSlots(client, client.f_91074_.f_36096_);
        if (ItemInputs.matchStacks(requiredStacks, nonPlayerSlot, client.f_91074_, allowNamed)) {
            if (requiredStacks.size() != nonPlayerSlot.size()) {
                MessageHolder.sendMessageUncheckedUnique(client.f_91074_, "Sizes differ as " + requiredStacks.size() + " but non-player slot size : " + nonPlayerSlot.size());
                return false;
            }
            if (entry == null || entry.getKey().longValue() != where.m_121878_()) {
                entry = Map.entry(where.m_121878_(), new Date().getTime() + (long)LitematicaMixinMod.INVENTORY_OPERATIONS_WAIT.getIntegerValue());
                return true;
            }
            if (entry.getValue() > new Date().getTime()) {
                return true;
            }
            entry = null;
            boolean allCorrect = true;
            for (int j = 0; j < LitematicaMixinMod.INVENTORY_OPERATIONS_RETRY.getIntegerValue(); ++j) {
                for (int i = 0; i < requiredStacks.size(); ++i) {
                    if (InventoryUtils.areItemsExactCount(nonPlayerSlot.get(i).m_7993_(), requiredStacks.get(i), allowNamed)) continue;
                    ItemInputs.sendItem(client, nonPlayerSlot.get(i), requiredStacks.get(i), allowNamed);
                    if (InventoryUtils.areItemsExactCount(nonPlayerSlot.get(i).m_7993_(), requiredStacks.get(i), allowNamed)) continue;
                    allCorrect = false;
                }
            }
            if (allCorrect) {
                handledPos.add(where.m_121878_());
                MessageHolder.sendUniqueDebugMessage("Successfully done operation at " + where.m_123344_());
                if (LitematicaMixinMod.INVENTORY_OPERATIONS_CLOSE_SCREEN.getBooleanValue()) {
                    client.f_91074_.m_242612_();
                }
                return true;
            }
            MessageHolder.sendUniqueDebugMessage("Partially failed to send all items at " + where.m_123344_() + ", will retry");
            return true;
        }
        MessageHolder.sendUniqueDebugMessage("Does not have enough item for " + where.m_123344_() + "!");
        if (LitematicaMixinMod.INVENTORY_OPERATIONS_CLOSE_SCREEN.getBooleanValue()) {
            client.f_91074_.m_242612_();
        }
        return false;
    }

    private static void sendItem(Minecraft client, int targetSlot, ItemStack stack, boolean allowNamed) {
        ItemInputs.clearCursor(client);
        ItemInputs.clearUnmatchTargetSlot(client, targetSlot, stack, allowNamed);
        int holding = ItemInputs.getPreference(client, client.f_91074_.f_36096_, stack, allowNamed);
        if (holding == -1) {
            return;
        }
        AbstractContainerMenu screenHandler = client.f_91074_.f_36096_;
        ItemInputs.leftClickSlot(screenHandler, holding);
        for (int i = 0; i < stack.m_41613_() - screenHandler.m_38853_(targetSlot).m_7993_().m_41613_(); ++i) {
            ItemInputs.rightClickSlot(screenHandler, targetSlot);
        }
        ItemInputs.leftClickSlot(screenHandler, holding);
        MessageHolder.sendUniqueDebugMessage("Sent item from " + holding + " to " + targetSlot);
    }

    private static void sendItem(Minecraft client, Slot targetSlot, ItemStack stack, boolean allowNamed) {
        ItemInputs.sendItem(client, targetSlot.f_40219_, stack, allowNamed);
    }

    private static void clearUnmatchTargetSlot(Minecraft client, int targetSlot, ItemStack wantedItem, boolean allowNamed) {
        ItemStack slotStack = client.f_91074_.f_36096_.m_38853_(targetSlot).m_7993_();
        if (InventoryUtils.areItemsExact(slotStack, wantedItem, allowNamed) && slotStack.m_41613_() <= wantedItem.m_41613_()) {
            return;
        }
        AbstractContainerMenu gui = client.f_91074_.f_36096_;
        ItemInputs.leftClickSlot(gui, targetSlot);
        ItemInputs.clearCursor(client);
    }

    private static void leftClickSlot(AbstractContainerMenu gui, int slotNum) {
        ItemInputs.clickSlot(gui, slotNum, 0, ClickType.PICKUP);
    }

    private static void rightClickSlot(AbstractContainerMenu gui, int slotNum) {
        ItemInputs.clickSlot(gui, slotNum, 1, ClickType.PICKUP);
    }

    private static void shiftClickSlot(AbstractContainerMenu gui, int slotNum) {
        ItemInputs.clickSlot(gui, slotNum, 0, ClickType.QUICK_MOVE);
    }

    public static void clickSlot(AbstractContainerMenu gui, int slotNum, int button, ClickType action) {
        if (slotNum >= 0 && slotNum < gui.f_38839_.size()) {
            Slot slot = gui.m_38853_(slotNum);
            ItemInputs.clickSlot(gui, slot, button, action);
        }
    }

    public static void clickSlot(AbstractContainerMenu gui, Slot slot, int button, ClickType action) {
        try {
            Minecraft.m_91087_().f_91072_.m_171799_(gui.f_38840_, slot.f_40219_, button, action, (Player)Minecraft.m_91087_().f_91074_);
        }
        catch (Exception e) {
            MessageHolder.sendMessageUncheckedUnique(Minecraft.m_91087_().f_91074_, "Clicking slot failed ");
            MessageHolder.sendMessageUncheckedUnique(Minecraft.m_91087_().f_91074_, e.getMessage());
        }
    }

    public static List<ItemStack> getRaycastRequiredItemStacks(Minecraft minecraftClient) {
        ArrayList<ItemStack> retVal = new ArrayList<ItemStack>();
        Screen screen = minecraftClient.f_91080_;
        if (screen == null) {
            return retVal;
        }
        if (screen instanceof InventoryScreen) {
            MessageHolder.sendUniqueDebugMessage(minecraftClient.f_91074_, "Screen was InventoryScreen");
            return retVal;
        }
        BlockPos context = ItemInputs.rayCast(minecraftClient);
        if (context == null) {
            return retVal;
        }
        if (handledPos.contains(context.m_121878_())) {
            MessageHolder.sendUniqueDebugMessage(minecraftClient.f_91074_, "Screen was already registered");
            return retVal;
        }
        if (!(screen instanceof ContainerScreen || screen instanceof DispenserScreen || screen instanceof HopperScreen)) {
            return retVal;
        }
        return InventoryUtils.getRequiredStackInSchematic((Level)SchematicWorldHandler.getSchematicWorld(), minecraftClient, context);
    }

    private static BlockPos rayCast(Minecraft minecraftClient) {
        if (clickedPos == null) {
            MessageHolder.sendUniqueMessageActionBar(minecraftClient.f_91074_, "Current raycast is set to null");
            return null;
        }
        MessageHolder.sendUniqueMessageActionBar(minecraftClient.f_91074_, "Current raycast is set to " + clickedPos.m_123344_());
        BlockPos castedPos = clickedPos;
        Block block = minecraftClient.f_91073_.m_8055_(castedPos).m_60734_();
        if (block instanceof BaseEntityBlock && (block instanceof HopperBlock || block instanceof ChestBlock || block instanceof DispenserBlock)) {
            return castedPos;
        }
        return null;
    }

    static {
        clickedPos = null;
    }
}

