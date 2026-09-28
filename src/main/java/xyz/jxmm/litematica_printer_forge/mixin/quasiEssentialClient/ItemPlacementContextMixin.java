/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package xyz.jxmm.litematica_printer_forge.mixin.quasiEssentialClient;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.utils.FakeAccurateBlockPlacement;

@Mixin(value={BlockPlaceContext.class}, priority=1200)
public class ItemPlacementContextMixin {
    @Inject(method={"getNearestLookingDirection"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void onGetDirection(CallbackInfoReturnable<Direction> cir) {
        if (FakeAccurateBlockPlacement.fakeDirection != null && FakeAccurateBlockPlacement.requestedTicks > -3) {
            cir.setReturnValue(FakeAccurateBlockPlacement.getFacingOrder()[0]);
        }
    }

    @Inject(method={"getNearestLookingVerticalDirection"}, at={@At(value="HEAD")}, cancellable=true, require=0)
    private void onGetVerticalDirection(CallbackInfoReturnable<Direction> cir) {
        if (FakeAccurateBlockPlacement.fakeDirection != null && FakeAccurateBlockPlacement.requestedTicks > -3 && FakeAccurateBlockPlacement.fakeDirection.m_122434_() == Direction.Axis.Y) {
            cir.setReturnValue(FakeAccurateBlockPlacement.fakeDirection);
        }
    }

    @Redirect(method={"getNearestLookingDirections"}, at=@At(value="INVOKE", target="Lnet/minecraft/core/Direction;orderedByNearest(Lnet/minecraft/world/entity/Entity;)[Lnet/minecraft/core/Direction;"), require=0)
    private Direction[] onGetArrayDirections(Entity entity) {
        if (!LitematicaMixinMod.DISABLE_SINGLEPLAYER_HANDLE.getBooleanValue() && FakeAccurateBlockPlacement.fakeDirection != null && FakeAccurateBlockPlacement.requestedTicks > -3) {
            return FakeAccurateBlockPlacement.getFacingOrder();
        }
        return Direction.m_122382_((Entity)entity);
    }
}

