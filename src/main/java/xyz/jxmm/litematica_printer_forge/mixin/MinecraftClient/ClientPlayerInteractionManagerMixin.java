/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$ItemPickerMenu
 *  net.minecraft.client.multiplayer.MultiPlayerGameMode
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ClickType
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package xyz.jxmm.litematica_printer_forge.mixin.MinecraftClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MultiPlayerGameMode.class})
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private Minecraft f_105189_;

    @Inject(method={"handleInventoryMouseClick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V")}, require=1)
    private void getNextRevision(int syncId, int slotId, int button, ClickType actionType, Player player, CallbackInfo ci) {
        player.f_36096_.m_182425_();
    }

    @Inject(method={"handleCreativeModeItemAdd"}, at={@At(value="TAIL")}, require=1)
    private void getNextRevision(ItemStack stack, int slotId, CallbackInfo ci) {
        LocalPlayer player = this.f_105189_.f_91074_;
        if (player != null && !(player.f_36096_ instanceof CreativeModeInventoryScreen.ItemPickerMenu) && !player.m_150109_().m_8020_(slotId).equals(stack)) {
            player.f_36096_.m_182425_();
            player.f_36096_.m_182406_(slotId, player.f_36096_.m_182425_(), stack);
        }
    }
}

