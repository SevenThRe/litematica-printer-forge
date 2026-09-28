/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  fi.dy.masa.litematica.config.Configs$Generic
 *  fi.dy.masa.litematica.config.Hotkeys
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket$Action
 *  net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TieredItem
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
 */
package xyz.jxmm.litematica_printer_forge.utils;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.config.Hotkeys;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.utils.MessageHolder;
import xyz.jxmm.litematica_printer_forge.utils.Printer;

public class InventoryUtils {
    private static int ptr = -1;
    public static int lastCount = 0;
    public static int itemChangeCount = 0;
    public static Item handlingItem = null;
    public static Item previousItem = null;
    public static int trackedSelectedSlot = -1;
    public static HashMap<Integer, Item> usedSlots = new LinkedHashMap<Integer, Item>();
    public static HashMap<Integer, Integer> slotCounts = new LinkedHashMap<Integer, Integer>();

    public static void tick() {
        if (!Printer.isSleeping && Configs.Generic.EASY_PLACE_MODE.getBooleanValue() && Configs.Generic.EASY_PLACE_HOLD_ENABLED.getBooleanValue() && Hotkeys.EASY_PLACE_ACTIVATION.getKeybind().isKeybindHeld()) {
            for (int i = 0; i < 9; ++i) {
                if (!usedSlots.containsKey(i) || slotCounts.get(i) > 0) continue;
                usedSlots.remove(i);
                slotCounts.remove(i);
            }
        } else {
            trackedSelectedSlot = -1;
            previousItem = null;
            handlingItem = null;
            usedSlots.clear();
            slotCounts.clear();
        }
    }

    public static void decrementCount() {
        if (lastCount > 0) {
            --lastCount;
            slotCounts.computeIfPresent(trackedSelectedSlot, (key, value) -> value - 1);
        }
    }

    public static void decrementCount(boolean isCreative) {
        if (isCreative) {
            lastCount = 65536;
        }
        if (lastCount > 0) {
            --lastCount;
            slotCounts.computeIfPresent(trackedSelectedSlot, (key, value) -> value - 1);
        }
    }

    private static int getPtr() {
        ++ptr;
        return ptr %= 9;
    }

    public static int getAvailableSlot(Item item) {
        if (usedSlots.containsValue(item)) {
            for (Integer i : usedSlots.keySet()) {
                if (usedSlots.get(i) != item) continue;
                return i;
            }
            return -1;
        }
        if (usedSlots.size() == 9) {
            return InventoryUtils.getPtr();
        }
        for (int i = 0; i < 9; ++i) {
            if (usedSlots.containsKey(i)) continue;
            return i;
        }
        return -1;
    }

    private static int searchSlot(Item item) {
        for (Integer i : usedSlots.keySet()) {
            if (usedSlots.get(i) != item || slotCounts.getOrDefault(i, 0) <= 0) continue;
            return i;
        }
        return -1;
    }

    public static boolean hasEmptyHotbar() {
        return usedSlots.size() < 9;
    }

    public static ItemStack getMainHandStack(LocalPlayer player) {
        return player.m_21205_();
    }

    public static boolean areItemsExact(ItemStack a, ItemStack b) {
        return ItemStack.m_41656_(a, b) && ItemStack.m_150942_(a, b);
    }

    public static boolean areItemsExact(ItemStack a, ItemStack b, boolean allowNamed) {
        if (allowNamed) {
            return InventoryUtils.areItemsExactAllowNamed(a, b);
        }
        return ItemStack.m_41656_(a, b) && ItemStack.m_150942_(a, b);
    }

    public static boolean areItemsExactCount(ItemStack a, ItemStack b, boolean allowNamed) {
        if (a.m_41613_() != b.m_41613_()) {
            return false;
        }
        if (allowNamed) {
            return InventoryUtils.areItemsExactAllowNamed(a, b);
        }
        return ItemStack.m_41656_(a, b) && ItemStack.m_150942_(a, b);
    }

    public static boolean areItemsExactAllowNamed(ItemStack a, ItemStack b) {
        if (a.m_41720_() instanceof TieredItem || b.m_41720_() instanceof TieredItem) {
            return false;
        }
        return ItemStack.m_41656_(a, b) || a.m_41741_() == b.m_41741_() && a.m_41788_() && b.m_41788_();
    }

    public static boolean requiresSwap(LocalPlayer player, ItemStack stack) {
        int selectedSlot = player.m_150109_().f_35977_;
        if (usedSlots.get(selectedSlot) != null) {
            return stack.m_41720_() != usedSlots.get(selectedSlot) || slotCounts.getOrDefault(selectedSlot, 0) <= 0;
        }
        return previousItem == null || lastCount == 0 ? !InventoryUtils.areItemsExact(InventoryUtils.getMainHandStack(player), stack) : !InventoryUtils.areItemsExact(previousItem.m_7968_(), stack);
    }

    public static boolean canSwap(LocalPlayer player, ItemStack stack) {
        if (player.m_150110_().f_35937_) {
            return true;
        }
        int slotNum = InventoryUtils.findSlotMatchingItem(player, stack);
        return slotNum != -1;
    }

    public static synchronized boolean swapToItem(Minecraft client, ItemStack stack) {
        int slot;
        MessageHolder.sendOrderMessage("Trying to swap item into " + String.valueOf(stack.m_41720_()));
        LocalPlayer player = client.f_91074_;
        int maxChange = LitematicaMixinMod.PRINTER_MAX_ITEM_CHANGES.getIntegerValue();
        if (player == null || client.f_91072_ == null) {
            return false;
        }
        if (stack.m_41720_() != handlingItem && maxChange != 0 && itemChangeCount > maxChange) {
            MessageHolder.sendOrderMessage("Exceeded item change count");
            return false;
        }
        if (!InventoryUtils.requiresSwap(player, stack)) {
            assert (trackedSelectedSlot == -1 || trackedSelectedSlot == player.m_150109_().f_35977_) : "Selected slot changed for external reason! : expected %s, current %s".formatted(new Object[]{trackedSelectedSlot, player.m_150109_().f_35977_});
            assert (previousItem == null || previousItem == stack.m_41720_()) : "Handling item :  " + String.valueOf(handlingItem) + " was not equal to " + String.valueOf(stack.m_41720_());
            MessageHolder.sendOrderMessage("Didn't require swap for item " + String.valueOf(stack.m_41720_()) + " previous handling item : " + String.valueOf(previousItem));
            int n = lastCount = player.m_150110_().f_35937_ ? 65536 : InventoryUtils.getMainHandStack(player).m_41613_();
            if (usedSlots.containsValue(stack.m_41720_()) && InventoryUtils.searchSlot(stack.m_41720_()) != trackedSelectedSlot) {
                MessageHolder.sendMessageUncheckedUnique("Hotbar has duplicate item references, which should not happen!");
            }
            trackedSelectedSlot = player.m_150109_().f_35977_;
            usedSlots.put(player.m_150109_().f_35977_, InventoryUtils.getMainHandStack(player).m_41720_());
            slotCounts.put(player.m_150109_().f_35977_, lastCount);
            previousItem = stack.m_41720_();
            return true;
        }
        if (usedSlots.containsValue(stack.m_41720_()) && (slot = InventoryUtils.searchSlot(stack.m_41720_())) != -1) {
            trackedSelectedSlot = player.m_150109_().f_35977_ = slot;
            usedSlots.put(trackedSelectedSlot, stack.m_41720_());
            slotCounts.put(trackedSelectedSlot, stack.m_41613_());
            lastCount = stack.m_41613_();
            handlingItem = previousItem = stack.m_41720_();
            MessageHolder.sendOrderMessage("Selected slot " + player.m_150109_().f_35977_ + " based on cache for " + String.valueOf(stack.m_41720_()));
            client.m_91403_().m_104955_((Packet)new ServerboundSetCarriedItemPacket(player.m_150109_().f_35977_));
            return !player.m_150109_().m_36056_().m_41619_();
        }
        if (InventoryUtils.survivalSwap(client, player, stack)) {
            usedSlots.put(player.m_150109_().f_35977_, stack.m_41720_());
            slotCounts.put(trackedSelectedSlot, InventoryUtils.getMainHandStack(player).m_41613_());
            MessageHolder.sendOrderMessage("Swapped to item " + String.valueOf(stack.m_41720_()));
            previousItem = handlingItem = stack.m_41720_();
            ++itemChangeCount;
            return true;
        }
        return InventoryUtils.creativeSwap(client, player, stack);
    }

    public static int findSlotMatchingItem(LocalPlayer player, ItemStack stack) {
        int slot = player.m_150109_().m_36030_(stack);
        if (slot != -1) {
            return slot;
        }
        Inventory inv = player.m_150109_();
        for (int i = 0; i < inv.f_35974_.size(); ++i) {
            if (!ItemStack.m_41656_((ItemStack)inv.f_35974_.get(i), stack)) continue;
            return i;
        }
        return -1;
    }

    private static boolean creativeSwap(Minecraft client, LocalPlayer player, ItemStack stack) {
        if (!player.m_150110_().f_35937_) {
            return false;
        }
        int selectedSlot = InventoryUtils.getAvailableSlot(stack.m_41720_());
        if (selectedSlot == -1) {
            return false;
        }
        MessageHolder.sendOrderMessage("Clicked creative stack " + String.valueOf(stack.m_41720_()) + " for slot " + selectedSlot);
        player.m_150109_().f_35977_ = selectedSlot;
        client.f_91072_.m_105241_(stack, 36 + selectedSlot);
        client.m_91403_().m_104955_((Packet)new ServerboundSetCarriedItemPacket(player.m_150109_().f_35977_));
        trackedSelectedSlot = selectedSlot;
        player.m_150109_().f_35974_.set(selectedSlot, stack);
        usedSlots.put(player.m_150109_().f_35977_, stack.m_41720_());
        slotCounts.put(trackedSelectedSlot, 65536);
        lastCount = 65536;
        previousItem = handlingItem = stack.m_41720_();
        ++itemChangeCount;
        return true;
    }

    private static boolean survivalSwap(Minecraft client, LocalPlayer player, ItemStack stack) {
        if (!InventoryUtils.canSwap(player, stack)) {
            return false;
        }
        if (InventoryUtils.areItemsExact(player.m_21206_(), stack) && !InventoryUtils.areItemsExact(InventoryUtils.getMainHandStack(player), stack)) {
            lastCount = client.f_91074_.m_150110_().f_35937_ ? 65536 : client.f_91074_.m_21206_().m_41613_();
            client.m_91403_().m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.f_121853_, Direction.DOWN));
            return true;
        }
        int slot = InventoryUtils.findSlotMatchingItem(player, stack);
        if (slot == -1) {
            return false;
        }
        if (Inventory.m_36045_((int)slot)) {
            if (usedSlots.get(slot) != null) {
                MessageHolder.sendOrderMessage("Hotbar slot should have been handled before, so it must be error!");
                MessageHolder.sendOrderMessage("Expected : " + String.valueOf(usedSlots.get(slot)) + " but current client handles : " + String.valueOf(stack.m_41720_()));
                return false;
            }
            player.m_150109_().f_35977_ = slot;
            trackedSelectedSlot = slot;
            MessageHolder.sendOrderMessage("Selected hotbar Slot " + slot);
            lastCount = player.m_150110_().f_35937_ ? 65536 : player.m_150109_().m_8020_(slot).m_41613_();
            client.m_91403_().m_104955_((Packet)new ServerboundSetCarriedItemPacket(slot));
        } else {
            int selectedSlot = InventoryUtils.getAvailableSlot(stack.m_41720_());
            if (selectedSlot == -1) {
                MessageHolder.sendOrderMessage("All hotbar slots are used");
                return false;
            }
            lastCount = player.m_150110_().f_35937_ ? 65536 : player.m_150109_().m_8020_(slot).m_41613_();
            MessageHolder.sendOrderMessage("Slot at " + slot + "(%s)".formatted(new Object[]{player.m_150109_().m_8020_(slot).m_41720_()}) + " is swapped with " + selectedSlot + "(%s)".formatted(new Object[]{player.m_150109_().f_35974_.get(selectedSlot)}));
            usedSlots.put(selectedSlot, stack.m_41720_());
            client.f_91072_.m_171799_(player.f_36095_.f_38840_, slot, selectedSlot, ClickType.SWAP, (Player)player);
            player.m_150109_().f_35977_ = selectedSlot;
            trackedSelectedSlot = selectedSlot;
        }
        try {
            assert (ItemStack.m_41656_(InventoryUtils.getMainHandStack(player), stack));
        }
        catch (Exception e) {
            MessageHolder.sendMessageUncheckedUnique(player, stack.toString() + " does not match with " + String.valueOf(player.m_21205_()) + "!");
        }
        return true;
    }

    public static List<ItemStack> getRequiredStackInSchematic(Level schematicWorld, Minecraft minecraftClient, BlockPos pos) {
        LocalPlayer player = minecraftClient.f_91074_;
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();
        BlockEntity blockEntity = schematicWorld.m_7702_(pos);
        if (blockEntity == null) {
            return result;
        }
        if (blockEntity instanceof RandomizableContainerBlockEntity) {
            RandomizableContainerBlockEntity containerBlockEntity = (RandomizableContainerBlockEntity)blockEntity;
            if (containerBlockEntity.m_7983_()) {
                return result;
            }
            if (containerBlockEntity.m_6542_((Player)player)) {
                for (int i = 0; i < containerBlockEntity.m_6643_(); ++i) {
                    result.add(containerBlockEntity.m_8020_(i));
                }
            } else {
                MessageHolder.sendMessageUncheckedUnique(player, "Container at " + pos.m_123344_() + "can't be opened by player!");
            }
        }
        return result;
    }

    public static boolean hasItemInSchematic(Level schematicWorld, BlockPos pos) {
        BlockEntity blockEntity = schematicWorld.m_7702_(pos);
        if (blockEntity == null) {
            return false;
        }
        if (blockEntity instanceof RandomizableContainerBlockEntity) {
            RandomizableContainerBlockEntity containerBlockEntity = (RandomizableContainerBlockEntity)blockEntity;
            if (containerBlockEntity.m_7983_()) {
                return false;
            }
            for (int i = 0; i < containerBlockEntity.m_6643_(); ++i) {
                if (containerBlockEntity.m_8020_(i).m_41619_()) continue;
                return true;
            }
        }
        return false;
    }
}

