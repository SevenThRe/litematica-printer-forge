package xyz.jxmm.litematica_printer_forge.utils;

import net.minecraft.world.item.Item;

import java.util.Map;

/**
 * Callback reporting the verified deltas after a backpack ghost-injection cycle.
 *
 * Extracted from {@code BackpackInjector} as a top-level interface so Forge's
 * ModuleClassLoader resolves it reliably (nested types of mixin-processed
 * classes can fail to load at runtime with NoClassDefFoundError).
 */
public interface InjectResultListener {
    void onResult(Map<Item, Integer> gotInv, Map<Item, Integer> gotStored);
}
