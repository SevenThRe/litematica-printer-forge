/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.world.item.context.UseOnContext
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package xyz.jxmm.litematica_printer_forge.mixin.quasiEssentialClient;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.jxmm.litematica_printer_forge.utils.FakeAccurateBlockPlacement;

@Mixin(value={UseOnContext.class}, priority=1200)
public class ItemUsageContextMixin {
    @Inject(method={"getHorizontalDirection"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void onGetFacing(CallbackInfoReturnable<Direction> cir) {
        Direction direction = FakeAccurateBlockPlacement.getPlayerFacing();
        if (direction != null && FakeAccurateBlockPlacement.fakeDirection != null && FakeAccurateBlockPlacement.requestedTicks > -3 && FakeAccurateBlockPlacement.fakeDirection.m_122434_() != Direction.Axis.Y) {
            cir.setReturnValue(direction);
        }
    }

    @Inject(method={"getRotation"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void onGetYaw(CallbackInfoReturnable<Float> cir) {
        if (FakeAccurateBlockPlacement.requestedTicks > -3 && FakeAccurateBlockPlacement.fakeDirection != null) {
            cir.setReturnValue(Float.valueOf(FakeAccurateBlockPlacement.fakeYaw));
        }
    }
}

