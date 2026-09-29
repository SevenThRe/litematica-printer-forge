package xyz.jxmm.litematica_printer_forge.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic block replacement utility: maps schematic blocks to replacement blocks per the config list.
 * Config format: one from=to pair per line (e.g. minecraft:horn_coral_block=minecraft:sponge)
 */
public class BlockReplacer {

    private static volatile Map<Item, Item> cache = null;
    private static volatile long cacheTimestamp = 0;
    private static final long CACHE_TTL_MS = 5000;

    public static Item resolve(Item original) {
        if (!LitematicaMixinMod.CORAL_REPLACE_ENABLED.getBooleanValue()) {
            return original;
        }
        Map<Item, Item> map = getMappings();
        Item replacement = map.get(original);
        return replacement != null ? replacement : original;
    }

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
     * True when the client state is exactly what the replacement mapping asks for:
     * its block is the resolved target block and every property shared with the
     * schematic state has the same value. Always false when no replacement applies.
     */
    public static boolean clientMatchesReplacement(BlockState schematic, BlockState client) {
        Block original = schematic.m_60734_();
        Block expected = resolveBlock(original);
        return expected != original && client.m_60734_() == expected && sharedPropertiesMatch(schematic, client);
    }

    /**
     * Every property shared between the two states must have the same value;
     * properties absent on the client block (e.g. oxidation level) are ignored.
     */
    public static boolean sharedPropertiesMatch(BlockState schematic, BlockState client) {
        for (Property<?> p : schematic.m_61147_()) {
            if (client.m_61138_(p) && !schematic.m_61143_(p).equals(client.m_61143_(p))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the replacement mapping table (cached).
     * Built-in defaults (1.20.1 blocks that change after placement) are applied first;
     * user config lines can override entries with the same key.
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
     * Built-in default replacements: unwaxed copper family → waxed counterparts (prevents oxidation),
     * horn coral block → yellow glazed terracotta. User config lines can override.
     */
    private static void applyDefaults(Map<Item, Item> map) {
        // NOTE: 1.20.1 registry names for oxidized full copper blocks have no "_block" suffix
        // (minecraft:exposed_copper), only the base one is "copper_block".
        putDefault(map, "copper_block", "waxed_copper_block");
        putDefault(map, "exposed_copper", "waxed_exposed_copper");
        putDefault(map, "weathered_copper", "waxed_weathered_copper");
        putDefault(map, "oxidized_copper", "waxed_oxidized_copper");
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

    /**
     * Built-in default replacement lines (config format), seeded into the config list
     * when blockReplaceMappings is empty so users can see/edit them in the config GUI.
     */
    public static java.util.List<String> getDefaultMappingLines() {
        java.util.List<String> lines = new java.util.ArrayList<>();
        // Explicit pairs: 1.20.1 oxidized copper names lack the "_block" suffix
        String[][] defaults = {
                {"copper_block", "waxed_copper_block"},
                {"exposed_copper", "waxed_exposed_copper"},
                {"weathered_copper", "waxed_weathered_copper"},
                {"oxidized_copper", "waxed_oxidized_copper"},
                {"cut_copper", "waxed_cut_copper"},
                {"exposed_cut_copper", "waxed_exposed_cut_copper"},
                {"weathered_cut_copper", "waxed_weathered_cut_copper"},
                {"oxidized_cut_copper", "waxed_oxidized_cut_copper"},
                {"cut_copper_stairs", "waxed_cut_copper_stairs"},
                {"exposed_cut_copper_stairs", "waxed_exposed_cut_copper_stairs"},
                {"weathered_cut_copper_stairs", "waxed_weathered_cut_copper_stairs"},
                {"oxidized_cut_copper_stairs", "waxed_oxidized_cut_copper_stairs"},
                {"cut_copper_slab", "waxed_cut_copper_slab"},
                {"exposed_cut_copper_slab", "waxed_exposed_cut_copper_slab"},
                {"weathered_cut_copper_slab", "waxed_weathered_cut_copper_slab"},
                {"oxidized_cut_copper_slab", "waxed_oxidized_cut_copper_slab"},
                {"lightning_rod", "waxed_lightning_rod"},
                {"exposed_lightning_rod", "waxed_exposed_lightning_rod"},
                {"weathered_lightning_rod", "waxed_weathered_lightning_rod"},
                {"oxidized_lightning_rod", "waxed_oxidized_lightning_rod"},
                {"horn_coral_block", "yellow_glazed_terracotta"}
        };
        for (String[] pair : defaults) {
            lines.add("minecraft:" + pair[0] + "=minecraft:" + pair[1]);
        }
        return lines;
    }

    private static void putDefault(Map<Item, Item> map, String from, String to) {
        Item fromItem = parseItem("minecraft:" + from);
        Item toItem = parseItem("minecraft:" + to);
        if (fromItem != null && fromItem != Items.f_41852_ && toItem != null && toItem != Items.f_41852_) {
            map.put(fromItem, toItem);
        }
    }

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
