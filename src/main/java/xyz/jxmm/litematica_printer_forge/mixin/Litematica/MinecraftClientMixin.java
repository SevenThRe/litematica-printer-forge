package xyz.jxmm.litematica_printer_forge.mixin.Litematica;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.utils.BackpackInjector;
import xyz.jxmm.litematica_printer_forge.utils.BedrockBreaker;
import xyz.jxmm.litematica_printer_forge.utils.FakeAccurateBlockPlacement;
import xyz.jxmm.litematica_printer_forge.utils.InventoryUtils;
import xyz.jxmm.litematica_printer_forge.utils.ItemInputs;
import xyz.jxmm.litematica_printer_forge.utils.MessageHolder;
import xyz.jxmm.litematica_printer_forge.utils.Printer;

import java.util.List;

@Mixin(value = {Minecraft.class})
public abstract class MinecraftClientMixin {
    @Shadow
    public HitResult f_91077_;
    @Shadow
    public ClientLevel f_91073_;
    @Shadow
    public LocalPlayer f_91074_;

    @Shadow
    public abstract ClientPacketListener m_91403_();

    /*
     * PRINTER_HOTKEY is not registered in the MaFgLib keybind system, so isKeybindHeld() is always false.
     * Instead, poll the GLFW physical keys directly with edge detection: one press toggles the printerOff switch.
     */
    private static boolean wasHotkeyPressed = false;

    private static boolean isPrinterHotkeyJustPressed() {
        IKeybind keybind = LitematicaMixinMod.PRINTER_HOTKEY.getKeybind();
        List<Integer> keys = keybind.getKeys();
        Minecraft mc = Minecraft.m_91087_();
        if (keys == null || keys.isEmpty() || mc.f_91080_ != null) {
            wasHotkeyPressed = false;
            return false;
        }
        long handle = mc.m_91268_().m_85439_();
        boolean pressed = true;
        for (Integer keyCode : keys) {
            if (keyCode == null) {
                pressed = false;
                break;
            }
            boolean down = keyCode < 0
                    ? GLFW.glfwGetMouseButton(handle, keyCode + 100) == 1
                    : GLFW.glfwGetKey(handle, keyCode) == 1;
            if (!down) {
                pressed = false;
                break;
            }
        }
        boolean justPressed = pressed && !wasHotkeyPressed;
        wasHotkeyPressed = pressed;
        return justPressed;
    }

    @Inject(at = {@At(value = "HEAD")}, method = {"setLevel"})
    public void joinWorld(ClientLevel world, CallbackInfo ci) {
        Printer.worldBottomY = world.m_141937_();
        Printer.worldTopY = world.m_151558_();
        BackpackInjector.resetBatchState();
    }

    @Inject(at = {@At(value = "HEAD")}, method = {"tick"})
    public void onPrinterTickCount(CallbackInfo info) {
        try {
            BedrockBreaker.tick();
            InventoryUtils.tick();
            FakeAccurateBlockPlacement.tick(this.m_91403_(), this.f_91074_);
            BackpackInjector.tick(Minecraft.m_91087_());
            BackpackInjector.onClientTick(Minecraft.m_91087_());
        } catch (Exception e) {
            System.out.println("[PRINTER-DEBUG] Exception in pre-printer ticks: " + String.valueOf(e));
            e.printStackTrace();
        }
        if (this.f_91074_ != null && this.f_91073_ != null) {
            boolean hasSchematic = SchematicWorldHandler.getSchematicWorld() != null;

            if (isPrinterHotkeyJustPressed()) {
                boolean newState = !LitematicaMixinMod.PRINTER_OFF.getBooleanValue();
                LitematicaMixinMod.PRINTER_OFF.setBooleanValue(newState);
                if (this.f_91074_ != null) {
                    net.minecraft.network.chat.MutableComponent msg = net.minecraft.network.chat.Component.m_237113_(newState ? "Printer OFF" : "Printer ON");
                    msg.m_130940_(newState ? net.minecraft.ChatFormatting.RED : net.minecraft.ChatFormatting.GREEN);
                    this.f_91074_.m_5661_(msg, true);
                }
            }

            boolean printerOff = LitematicaMixinMod.PRINTER_OFF.getBooleanValue();
            if (!printerOff && hasSchematic) {
                if (LitematicaMixinMod.PRINTER_ONLY_FAKE_ROTATION_MODE.getBooleanValue()) {
                    LitematicaMixinMod.USE_INVENTORY_CACHE.setBooleanValue(false);
                    Printer.doEasyPlaceFakeRotation(Minecraft.m_91087_());
                } else {
                    Printer.doPrinterAction(Minecraft.m_91087_());
                }
            }
        }
    }

    @Inject(at = {@At(value = "HEAD")}, method = {"startUseItem"})
    public void getIfBlockEntity(CallbackInfo info) {
        ItemInputs.clickedPos = this.f_91077_ != null && this.f_91077_.m_6662_() == HitResult.Type.BLOCK && this.f_91073_.m_7702_(((BlockHitResult) this.f_91077_).m_82425_()) != null && this.f_91077_.m_6662_() == HitResult.Type.BLOCK && this.f_91073_.m_7702_(((BlockHitResult) this.f_91077_).m_82425_()) != null ? ((BlockHitResult) this.f_91077_).m_82425_() : null;
    }
}
