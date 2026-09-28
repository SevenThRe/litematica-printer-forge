/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  fi.dy.masa.litematica.util.WorldUtils
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionResult
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package xyz.jxmm.litematica_printer_forge.mixin.Litematica;

import fi.dy.masa.litematica.util.WorldUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.utils.MessageHolder;
import xyz.jxmm.litematica_printer_forge.utils.Printer;

@Mixin(value={WorldUtils.class}, remap=false, priority=1010)
public class WorldUtilsMixin {
    private static boolean hasSent = false;

    @Inject(method={"doEasyPlaceAction"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onDoEasyPlaceAction(Minecraft mc, CallbackInfoReturnable<InteractionResult> cir) {
        if (mc.f_91074_ == null) {
            return;
        }
        System.out.println("[PRINTER-DEBUG] WorldUtilsMixin fired! PRINTER_OFF=" + LitematicaMixinMod.PRINTER_OFF.getBooleanValue() + " FAKE_ROTATION_ONLY=" + LitematicaMixinMod.PRINTER_ONLY_FAKE_ROTATION_MODE.getBooleanValue());
        if (LitematicaMixinMod.PRINTER_ONLY_FAKE_ROTATION_MODE.getBooleanValue()) {
            LitematicaMixinMod.USE_INVENTORY_CACHE.setBooleanValue(false);
            cir.setReturnValue(Printer.doEasyPlaceFakeRotation(mc));
        } else {
            if (LitematicaMixinMod.PRINTER_OFF.getBooleanValue()) {
                return;
            }
            InteractionResult defaultResult = InteractionResult.SUCCESS;
            try {
                defaultResult = Printer.doPrinterAction(mc);
            }
            catch (NullPointerException e) {
                MessageHolder.sendMessageUncheckedUnique(mc.f_91074_, e.getMessage());
                if (!hasSent && mc.f_91074_ != null) {
                    mc.f_91074_.m_5661_(Component.m_130674_((String)"Null pointer exception has occured, please upload log at https://github.com/aria1th/litematica-printer/issues"), false);
                    hasSent = true;
                }
            }
            catch (AssertionError e) {
                MessageHolder.sendOrderMessage("Order error happened " + ((Throwable)((Object)e)).getMessage());
                MessageHolder.sendMessageUncheckedUnique(mc.f_91074_, "Order Error Happened " + ((Throwable)((Object)e)).getMessage());
                cir.setReturnValue(InteractionResult.FAIL);
                return;
            }
            cir.setReturnValue(defaultResult);
        }
    }
}

