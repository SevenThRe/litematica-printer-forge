package xyz.jxmm.litematica_printer_forge.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/**
 * One Create material source found while looking for injection sources.
 *
 * Two flavours:
 * <ul>
 *   <li><b>blueprint</b> - a {@code create:schematic} item; the stack is a <b>copy</b> that keeps
 *       the NBT (File / Bounds / Anchor / Rotation / Mirror / Deployed) the reader needs.</li>
 *   <li><b>quill selection</b> - the in-progress selection of a schematic-and-quill ("clipboard");
 *       it has no item data, the two selection corners are used instead.</li>
 * </ul>
 */
public class BlueprintEntry {
    /** Copy of the blueprint item (NBT preserved); empty for a quill selection. */
    public final ItemStack stack;
    /** Display name: custom name when present, otherwise the structure file name. */
    public final String name;
    /** Where it was found plus a size hint, shown as the second line in the picker. */
    public final String detail;
    /** Quill selection corners (inclusive), null for blueprint entries. */
    public final BlockPos selMin;
    public final BlockPos selMax;

    public BlueprintEntry(ItemStack stack, String name, String detail) {
        this(stack, name, detail, null, null);
    }

    public BlueprintEntry(ItemStack stack, String name, String detail, BlockPos selMin, BlockPos selMax) {
        this.stack = stack;
        this.name = name;
        this.detail = detail;
        this.selMin = selMin;
        this.selMax = selMax;
    }

    /** True when this entry describes a live quill selection rather than a saved blueprint. */
    public boolean isSelection() {
        return selMin != null && selMax != null;
    }

    @Override
    public String toString() {
        return name;
    }
}
