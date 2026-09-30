package xyz.jxmm.litematica_printer_forge.utils;

import net.minecraft.world.item.ItemStack;

/**
 * Material plan entry for the backpack ghost-injection feature (BII).
 *
 * Extracted from {@code BackpackInjector} as a top-level class so Forge's
 * ModuleClassLoader resolves it reliably (nested classes of mixin-processed
 * classes can fail to load at runtime with NoClassDefFoundError).
 */
public class InjectPlan {
    public final ItemStack stack;
    public int amount;
    /** World Y of this material's lowest missing position (used for Y-layer sorted injection) */
    public int lowestY = Integer.MAX_VALUE;

    public InjectPlan(ItemStack stack, int amount) {
        this.stack = stack;
        this.amount = amount;
    }

    public InjectPlan(ItemStack stack, int amount, int lowestY) {
        this.stack = stack;
        this.amount = amount;
        this.lowestY = lowestY;
    }
}
