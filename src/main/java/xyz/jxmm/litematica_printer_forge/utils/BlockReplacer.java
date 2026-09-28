package xyz.jxmm.litematica_printer_forge.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用方块替换工具：根据配置表将原理图中的方块映射为替代方块。
 * 配置格式：每行一条 from=to（如 minecraft:horn_coral_block=minecraft:sponge）
 */
public class BlockReplacer {

    private static volatile Map<Item, Item> cache = null;
    private static volatile long cacheTimestamp = 0;
    private static final long CACHE_TTL_MS = 5000; // 5秒缓存

    /**
     * 将原理图中的方块 Item 解析为实际应使用的替代 Item。
     * 若无映射或映射无效，返回原 Item。
     */
    public static Item resolve(Item original) {
        if (!LitematicaMixinMod.CORAL_REPLACE_ENABLED.getBooleanValue()) {
            return original;
        }
        Map<Item, Item> map = getMappings();
        Item replacement = map.get(original);
        return replacement != null ? replacement : original;
    }

    /**
     * Block 级解析：原理图方块 → 替代方块（用于放置/满足判定）。
     */
    public static net.minecraft.world.level.block.Block resolveBlock(net.minecraft.world.level.block.Block original) {
        if (!LitematicaMixinMod.CORAL_REPLACE_ENABLED.getBooleanValue()) {
            return original;
        }
        Item r = resolve(original.m_5456_());
        if (r instanceof net.minecraft.world.item.BlockItem bi) {
            return bi.m_40614_();
        }
        return original;
    }

    /**
     * 获取替换映射表（带缓存）。
     * 内置默认表（1.20.1 放置后会变化的方块）会先填入，用户配置的行可覆盖同名默认项。
     */
    public static Map<Item, Item> getMappings() {
        long now = System.currentTimeMillis();
        if (cache != null && (now - cacheTimestamp) < CACHE_TTL_MS) {
            return cache;
        }
        Map<Item, Item> map = new HashMap<>();
        applyDefaults(map);
        List<String> lines = LitematicaMixinMod.CORAL_REPLACE_MAPPINGS.getStrings();
        if (lines != null) {
            for (String line : lines) {
                if (line == null || line.isEmpty()) continue;
                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;
                Item from = parseItem(parts[0].trim());
                Item to = parseItem(parts[1].trim());
                if (from != null && from != Items.f_41852_ && to != null && to != Items.f_41852_) {
                    map.put(from, to);
                }
            }
        }
        cache = map;
        cacheTimestamp = now;
        return map;
    }

    /**
     * 内置默认替换表：未涂蜡铜系 → 对应涂蜡版本（防氧化），
     * 黄珊瑚块 → 黄色带釉陶瓦。用户配置行可覆盖。
     */
    private static void applyDefaults(Map<Item, Item> map) {
        putDefault(map, "copper_block", "waxed_copper_block");
        putDefault(map, "exposed_copper_block", "waxed_exposed_copper_block");
        putDefault(map, "weathered_copper_block", "waxed_weathered_copper_block");
        putDefault(map, "oxidized_copper_block", "waxed_oxidized_copper_block");
        putDefault(map, "cut_copper", "waxed_cut_copper");
        putDefault(map, "exposed_cut_copper", "waxed_exposed_cut_copper");
        putDefault(map, "weathered_cut_copper", "waxed_weathered_cut_copper");
        putDefault(map, "oxidized_cut_copper", "waxed_oxidized_cut_copper");
        putDefault(map, "cut_copper_stairs", "waxed_cut_copper_stairs");
        putDefault(map, "exposed_cut_copper_stairs", "waxed_exposed_cut_copper_stairs");
        putDefault(map, "weathered_cut_copper_stairs", "waxed_weathered_cut_copper_stairs");
        putDefault(map, "oxidized_cut_copper_stairs", "waxed_oxidized_cut_copper_stairs");
        putDefault(map, "cut_copper_slab", "waxed_cut_copper_slab");
        putDefault(map, "exposed_cut_copper_slab", "waxed_exposed_cut_copper_slab");
        putDefault(map, "weathered_cut_copper_slab", "waxed_weathered_cut_copper_slab");
        putDefault(map, "oxidized_cut_copper_slab", "waxed_oxidized_cut_copper_slab");
        putDefault(map, "lightning_rod", "waxed_lightning_rod");
        putDefault(map, "exposed_lightning_rod", "waxed_exposed_lightning_rod");
        putDefault(map, "weathered_lightning_rod", "waxed_weathered_lightning_rod");
        putDefault(map, "oxidized_lightning_rod", "waxed_oxidized_lightning_rod");
        putDefault(map, "horn_coral_block", "yellow_glazed_terracotta");
    }

    private static void putDefault(Map<Item, Item> map, String from, String to) {
        Item fromItem = parseItem("minecraft:" + from);
        Item toItem = parseItem("minecraft:" + to);
        if (fromItem != null && fromItem != Items.f_41852_ && toItem != null && toItem != Items.f_41852_) {
            map.put(fromItem, toItem);
        }
    }

    /**
     * 清除缓存（配置变更后调用）。
     */
    public static void clearCache() {
        cache = null;
    }

    private static Item parseItem(String registryName) {
        try {
            ResourceLocation rl = new ResourceLocation(registryName);
            Item item = ForgeRegistries.ITEMS.getValue(rl);
            return item != null ? item : null;
        } catch (Exception e) {
            return null;
        }
    }
}
