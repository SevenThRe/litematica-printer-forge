/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  fi.dy.masa.litematica.config.Configs$Generic
 *  fi.dy.masa.litematica.config.Hotkeys
 *  fi.dy.masa.litematica.data.DataManager
 *  fi.dy.masa.litematica.tool.ToolMode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
 *  net.minecraft.network.protocol.game.ClientboundDisconnectPacket
 *  net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package xyz.jxmm.litematica_printer_forge.mixin.MinecraftClient;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.tool.ToolMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.utils.Printer;

@Mixin(value={ClientPacketListener.class})
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    @Final
    private Minecraft f_104888_;
    private static boolean isSynced = false;

    @Inject(method={"handleContainerSetSlot"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void onUpdateSlots(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        LocalPlayer player = this.f_104888_.f_91074_;
        if (!isSynced && player != null) {
            isSynced = true;
            while (packet.m_182716_() != player.f_36096_.m_182424_()) {
                player.f_36096_.m_182425_();
            }
            return;
        }
        if (player != null && LitematicaMixinMod.DEBUG_PACKET_SYNC.getBooleanValue()) {
            int rev = player.f_36096_.m_182424_();
            if (LitematicaMixinMod.DISABLE_SYNC.getBooleanValue()) {
                if (!(this.f_104888_.f_91080_ instanceof CreativeModeInventoryScreen) && ClientPlayNetworkHandlerMixin.shouldCancel(rev, packet.m_182716_())) {
                    ci.cancel();
                } else {
                    while (packet.m_182716_() != player.f_36096_.m_182424_()) {
                        player.f_36096_.m_182425_();
                    }
                    if (packet.m_131994_() == -1) {
                        if (packet.m_131991_() == -1 && !(this.f_104888_.f_91080_ instanceof CreativeModeInventoryScreen)) {
                            this.f_104888_.execute(() -> player.f_36096_.m_142503_(packet.m_131995_()));
                        }
                        return;
                    }
                    this.f_104888_.execute(() -> player.f_36096_.m_182406_(packet.m_131994_(), packet.m_182716_(), packet.m_131995_()));
                }
            }
            return;
        }
        if (Printer.isSleeping) {
            return;
        }
        if (DataManager.getToolMode() != ToolMode.REBUILD && Configs.Generic.EASY_PLACE_MODE.getBooleanValue() && Configs.Generic.EASY_PLACE_HOLD_ENABLED.getBooleanValue() && Hotkeys.EASY_PLACE_ACTIVATION.getKeybind().isKeybindHeld() && LitematicaMixinMod.DISABLE_SYNC.getBooleanValue()) {
            ci.cancel();
        }
    }

    @Inject(method={"handleDisconnect"}, at={@At(value="HEAD")})
    private void handleDisconnect(ClientboundDisconnectPacket p_104954_, CallbackInfo ci) {
        isSynced = false;
    }

    @Inject(method={"handleSetCarriedItem"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void onUpdateSelectSlots(ClientboundSetCarriedItemPacket packet, CallbackInfo ci) {
        if (Printer.isSleeping) {
            return;
        }
        if (DataManager.getToolMode() != ToolMode.REBUILD && Configs.Generic.EASY_PLACE_MODE.getBooleanValue() && Configs.Generic.EASY_PLACE_HOLD_ENABLED.getBooleanValue() && Hotkeys.EASY_PLACE_ACTIVATION.getKeybind().isKeybindHeld() && LitematicaMixinMod.DISABLE_SYNC.getBooleanValue()) {
            ci.cancel();
        }
    }

    private static boolean shouldCancel(int current, int packet) {
        if (current == packet) {
            return false;
        }
        int abs = Math.abs(current - packet);
        if (abs > 1024 && abs < 32760) {
            return false;
        }
        return Math.abs(current - packet) > 32760 == current < packet;
    }
}

