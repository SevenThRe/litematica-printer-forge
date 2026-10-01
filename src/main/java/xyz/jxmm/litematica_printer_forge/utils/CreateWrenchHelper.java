package xyz.jxmm.litematica_printer_forge.utils;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Create-only helper (never load this class unless Create is on the classpath).
 * See {@link CreateWrenchFix}.
 *
 * Create's wrench logic is trusted on the network layer: the server accepts a
 * normal use-item-on packet while a wrench item is held and runs
 * IWrenchable.onWrenched -> KineticBlockEntity.switchToBlockState, which sets the
 * rotated state server side. No block breaking involved.
 */
final class CreateWrenchHelper {
    private CreateWrenchHelper() {
    }

    static boolean isWrench(ItemStack stack) {
        if (stack.m_41619_()) {
            return false;
        }
        try {
            if (AllItems.WRENCH.isIn(stack)) {
                return true;
            }
            Item item = stack.m_41720_();
            return AllItemTags.WRENCH.matches(item);
        } catch (Throwable t) {
            return false;
        }
    }

    static ItemStack findWrench(Minecraft mc) {
        LocalPlayer player = mc.f_91074_;
        if (player == null) {
            return null;
        }
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(player.m_21205_());
        stacks.add(player.m_21206_());
        for (int i = 0; i < player.m_150109_().m_6643_(); i++) {
            stacks.add(player.m_150109_().m_8020_(i));
        }
        for (ItemStack st : stacks) {
            if (isWrench(st)) {
                return st;
            }
        }
        return null;
    }

    private static Direction findRotationFace(IWrenchable wrenchable, BlockState schematic, BlockState clientState) {
        // getRotatedBlockState is a pure client-side computation (no level access),
        // so we can brute force which clicked face produces the schematic state.
        // One click always suffices for axis-style blocks (the new AXIS equals the
        // clicked face's axis), so a null here means "not wrench-fixable".
        for (Direction face : Direction.values()) {
            try {
                BlockState rotated = wrenchable.getRotatedBlockState(clientState, face);
                if (rotated != null && (rotated == schematic || rotated.equals(schematic))) {
                    return face;
                }
            } catch (Throwable ignored) {
                // some combinations are invalid for the block (e.g. horizontal-axis
                // blocks clicked on top) and throw inside setValue; just skip them
            }
        }
        return null;
    }

    static boolean run(BlockState schematic, BlockState clientState, BlockPos pos, Minecraft mc) {
        if (mc.f_91074_ == null || mc.f_91073_ == null) {
            return false;
        }
        if (clientState.m_60734_() != schematic.m_60734_()) {
            return false;
        }
        if (clientState == schematic || clientState.equals(schematic)) {
            return false;
        }
        if (!(clientState.m_60734_() instanceof IWrenchable)) {
            return false;
        }
        IWrenchable wrenchable = (IWrenchable) clientState.m_60734_();
        // never sneak-wrench: sneaking REMOVES the block instead of rotating it
        if (mc.f_91074_.m_6144_()) {
            return false;
        }
        Direction face = findRotationFace(wrenchable, schematic, clientState);
        if (face == null) {
            return false;
        }
        ItemStack wrench = findWrench(mc);
        if (wrench == null) {
            return false;
        }
        // A wrench click was sent recently; report handled so the caller neither
        // spams more clicks nor starts breaking the block while waiting for the
        // server to apply the rotation.
        if (Printer.isPositionCached(pos, false)) {
            return true;
        }
        if (!InventoryUtils.swapToItem(mc, wrench)) {
            return false;
        }
        Vec3 hit = Vec3.m_82528_((BlockPos) pos).m_82520_(0.5, 0.5, 0.5)
                .m_82549_(Vec3.m_82528_((BlockPos) face.m_122436_()).m_82542_(0.45, 0.45, 0.45));
        InteractionResult result = mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND,
                new BlockHitResult(hit, face, pos, false));
        if (result.m_19077_()) {
            // short cooldown so the scan does not spam more clicks while the
            // server applies the rotation
            Printer.cacheEasyPlacePosition(pos, false, 500);
            return true;
        }
        return false;
    }
}
