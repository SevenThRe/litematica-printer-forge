package xyz.jxmm.litematica_printer_forge.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Create's blueprint material rules, mirrored 1:1 from
 * {@code com.simibubi.create.content.schematics.requirement.ItemRequirement}
 * (Create 0.5.1, official sources).
 *
 * <p>BII used to inject {@code block.asItem()} for every blueprint block, which disagrees with
 * what Create itself asks for. The visible symptom: a blueprint holding farmland shows
 * "Farmland" on the schematicannon but the clipboard material list says "Dirt", while BII
 * injected Farmland. Create deliberately maps several blocks to a different item:</p>
 *
 * <pre>
 * ItemRequirement#defaultOf(BlockState, BlockEntity):
 *   block == AIR                                  -> NONE
 *   item = block.asItem() == AIR                  -> INVALID (cannot be printed)
 *   SLAB_TYPE == DOUBLE                           -> 2x item
 *   TurtleEggBlock                                -> EGGS x item
 *   SeaPickleBlock                                -> PICKLES x item
 *   SnowLayerBlock                                -> LAYERS x item
 *   create:rich_soil_farmland                     -> 1x create:rich_soil
 *   FarmBlock | DirtPathBlock                     -> 1x minecraft:dirt
 *   AbstractBannerBlock (+ BannerBlockEntity)     -> 1x banner (NBT)
 *   Blocks.TALL_GRASS                             -> 2x minecraft:grass
 *   Blocks.LARGE_FERN                             -> 2x minecraft:fern
 *   otherwise                                     -> 1x item
 *
 * ItemRequirement#of(Entity) - the blueprint's entity list:
 *   create:super_glue       -> 1x create:super_glue   (DAMAGE: held, never consumed)
 *   create:crafting_blueprint -> 1x create:crafting_blueprint
 *   ItemFrame / GlowItemFrame -> 1x minecraft:item_frame
 *   ArmorStand              -> 1x minecraft:armor_stand
 *   anything else           -> INVALID
 * </pre>
 *
 * <p>Deliberately free of any compile-time Create reference (only registry lookups by name),
 * so it can be loaded even when Create is absent.</p>
 */
public final class CreateMaterialRules {

    private CreateMaterialRules() {
    }

    /** "No special rule here - fall back to block.asItem()". */
    public static final ItemStack[] DEFAULT = null;
    /** "This blueprint entry needs nothing (or cannot be printed)". */
    public static final ItemStack[] NONE = new ItemStack[0];

    private static ItemStack[] one(Item item, int count) {
        if (item == null || item == Items.f_41852_ || count <= 0) return NONE;
        return new ItemStack[]{new ItemStack(item, count)};
    }

    private static Item itemOf(String namespace, String path) {
        try {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation(namespace, path));
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean isBlock(Block block, String namespace, String path) {
        try {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            // m_135827_() = getNamespace(), m_135815_() = getPath()
            return id != null && namespace.equals(id.m_135827_()) && path.equals(id.m_135815_());
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Create's requirement for one blueprint block.
     *
     * @return {@link #DEFAULT} to use the plain {@code block.asItem()}, {@link #NONE} for entries
     * that need nothing, otherwise the exact stacks Create would ask for.
     */
    public static ItemStack[] forBlock(BlockState state) {
        if (state == null) return NONE;
        Block block = state.m_60734_();                     // getBlock()
        if (block == Blocks.f_50016_) return NONE;          // Blocks.AIR
        Item item = block.m_5456_();                        // asItem()
        if (item == Items.f_41852_) return NONE;            // no item form -> Create calls it INVALID

        if (state.m_61138_(BlockStateProperties.f_61397_)   // hasProperty(SLAB_TYPE)
                && state.m_61143_(BlockStateProperties.f_61397_) == SlabType.DOUBLE) {
            return one(item, 2);
        }
        if (block instanceof TurtleEggBlock) {
            return one(item, state.m_61143_(TurtleEggBlock.f_57754_));
        }
        if (block instanceof SeaPickleBlock) {
            return one(item, state.m_61143_(SeaPickleBlock.f_56074_));
        }
        if (block instanceof SnowLayerBlock) {
            return one(item, state.m_61143_(SnowLayerBlock.f_56581_));
        }
        if (isBlock(block, "create", "rich_soil_farmland")) {
            return one(itemOf("create", "rich_soil"), 1);
        }
        if (block instanceof FarmBlock || block instanceof DirtPathBlock) {
            return one(Items.f_42329_, 1);                  // Items.DIRT  <- the farmland case
        }
        if (block instanceof AbstractBannerBlock) {
            return one(item, 1);                            // banner NBT is lost, the item is not
        }
        if (block == Blocks.f_50359_) {                     // Blocks.TALL_GRASS
            return one(Items.f_41864_, 2);                  // 2x Items.GRASS
        }
        if (block == Blocks.f_50360_) {                     // Blocks.LARGE_FERN
            return one(Items.f_41865_, 2);                  // 2x Items.FERN
        }
        return DEFAULT;
    }

    /**
     * Create's requirement for one entity stored in the blueprint.
     *
     * @param nbt the entity's NBT (as saved inside the structure template's "entities" list).
     * @return the stacks to inject, or {@link #NONE} when the entity needs nothing.
     */
    public static ItemStack[] forEntity(CompoundTag nbt) {
        if (nbt == null) return NONE;
        String id = nbt.m_128461_("id");                    // getString("id")
        if (id == null || id.isEmpty()) return NONE;
        switch (id) {
            case "create:super_glue":
                // SuperGlueEntity#getRequiredItems -> ItemUseType.DAMAGE: the cannon holds the
                // glue tube, it is not used up. One is still required to print the glue.
                return one(itemOf("create", "super_glue"), 1);
            case "create:crafting_blueprint":
                return one(itemOf("create", "crafting_blueprint"), 1);
            case "minecraft:item_frame":
            case "minecraft:glow_item_frame":
                return one(Items.f_42617_, 1);              // Items.ITEM_FRAME (GlowItemFrame is an ItemFrame)
            case "minecraft:armor_stand":
                return one(Items.f_42650_, 1);              // Items.ARMOR_STAND
            default:
                return NONE;
        }
    }
}
