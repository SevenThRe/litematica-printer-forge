package xyz.jxmm.litematica_printer_forge.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mod gate for the Create blueprint support of BII.
 *
 * This class deliberately references <b>no</b> Create class: it only checks
 * {@code ModList} and then delegates to {@link CreateBlueprintHelper}, which is
 * loaded lazily on first use. Instances without Create therefore never touch the
 * Create classes (same pattern as CreateWrenchFix / CreateWrenchHelper).
 */
public final class CreateBlueprintFix {
    private static Boolean createLoaded = null;

    public static boolean isCreateLoaded() {
        if (createLoaded == null) {
            try {
                createLoaded = ModList.get() != null && ModList.get().isLoaded("create");
            } catch (Throwable t) {
                createLoaded = false;
            }
        }
        return createLoaded;
    }

    /** All Create blueprints carried by the player (inventory + equipped backpacks). */
    public static List<BlueprintEntry> collectBlueprints(Minecraft mc) {
        if (!isCreateLoaded()) return Collections.emptyList();
        try {
            return CreateBlueprintHelper.collectBlueprints(mc);
        } catch (Throwable t) {
            MessageHolder.sendMessageUnchecked("[BII] 读取 Create 蓝图失败: " + t);
            return Collections.emptyList();
        }
    }

    /**
     * Fills {@code needed} (item -> amount) and {@code minY} (item -> lowest world Y) for the
     * given blueprint. Returns false when the blueprint data cannot be read at all.
     */
    public static boolean buildMaterials(Minecraft mc, BlueprintEntry entry,
                                        Map<Item, Integer> needed, Map<Item, Integer> minY) {
        if (!isCreateLoaded() || entry == null) return false;
        try {
            return CreateBlueprintHelper.buildMaterials(mc, entry, needed, minY);
        } catch (Throwable t) {
            MessageHolder.sendMessageUnchecked("[BII] 计算蓝图材料失败: " + t);
            return false;
        }
    }
}
