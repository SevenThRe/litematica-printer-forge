package xyz.jxmm.litematica_printer_forge.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

/**
 * Optional Create integration: instead of breaking a wrongly-oriented Create block
 * and placing it again (which reproduces the same wrong state), use Create's own
 * wrench mechanics to rotate the block into the schematic state.
 *
 * This outer class carries NO Create imports so it is safe to load on instances
 * without Create; the actual Create-touching logic lives in {@link CreateWrenchHelper},
 * which is only loaded after the mod-present check below.
 */
public final class CreateWrenchFix {
    private CreateWrenchFix() {
    }

    public static boolean isCreateLoaded() {
        try {
            return ModList.get() != null && ModList.get().isLoaded("create");
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * If the client state can be rotated to the schematic state with a single wrench click
     * and the player owns any wrench (Create's wrench or any item in Create's wrench item
     * tag), swaps it in and right clicks the block. Returns true when a wrench click was
     * performed (the caller should treat the block as handled this tick).
     */
    public static boolean tryWrench(BlockState schematic, BlockState clientState, BlockPos pos, Minecraft mc) {
        if (!isCreateLoaded()) {
            return false;
        }
        try {
            return CreateWrenchHelper.run(schematic, clientState, pos, mc);
        } catch (Throwable t) {
            return false;
        }
    }

    public static ItemStack findWrench(Minecraft mc) {
        if (!isCreateLoaded()) {
            return null;
        }
        try {
            return CreateWrenchHelper.findWrench(mc);
        } catch (Throwable t) {
            return null;
        }
    }
}
