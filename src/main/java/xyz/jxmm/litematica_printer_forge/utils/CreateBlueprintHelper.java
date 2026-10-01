package xyz.jxmm.litematica_printer_forge.utils;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.schematics.SchematicAndQuillItem;
import com.simibubi.create.content.schematics.SchematicInstances;
import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.content.schematics.client.SchematicAndQuillHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Create blueprint (create:schematic) material reader used by BII.
 *
 * Loaded only after {@link CreateBlueprintFix} confirmed Create is present.
 *
 * Create stores a blueprint as a vanilla structure template: the block data lives in
 * {@code <gameDir>/schematics/<File>.nbt} (downloaded by ClientSchematicLoader), and the item
 * NBT only carries the settings (File / Bounds / Anchor / Rotation / Mirror / Deployed).
 * {@link SchematicItem#loadSchematic} is Create's own loader for exactly that file.
 *
 * Two modes:
 * <ul>
 *   <li><b>Deployed blueprint</b> (Create exposes a SchematicWorld): diff against the world,
 *       exactly like the litematica path - positions in the block map are relative to the
 *       schematic anchor, so worldPos = anchor + relative.</li>
 *   <li><b>Blueprint as an item</b>: no anchor exists, so the whole blueprint material list is
 *       used (the caller still subtracts inventory + backpack stock).</li>
 * </ul>
 */
public final class CreateBlueprintHelper {

    /** NBT tag types (kept as literals: the game is compiled against SRG names). */
    private static final int TAG_INT = 3;
    private static final int TAG_DOUBLE = 6;
    private static final int TAG_LIST = 9;
    private static final int TAG_COMPOUND = 10;

    private CreateBlueprintHelper() {
    }

    // ===== discovery =====

    /** Every Create material source carried by the player (inventory + backpacks, then the quill clipboard). */
    public static List<BlueprintEntry> collectBlueprints(Minecraft mc) {
        List<BlueprintEntry> out = new ArrayList<>();
        if (mc == null || mc.f_91074_ == null) return out;
        Inventory inv = mc.f_91074_.m_150109_();
        int size = inv.m_6643_();
        for (int i = 0; i < size; i++) {
            ItemStack st = inv.m_8020_(i);
            if (isBlueprint(st)) out.add(entryOf(st, "物品栏"));
        }
        for (int i = 0; i < size; i++) {
            ItemStack bag = inv.m_8020_(i);
            if (bag.m_41619_() || !BackpackInjector.isBackpackStack(bag)) continue;
            BackpackInjector.forEachBackpackStack(bag, (slot, st) -> {
                if (isBlueprint(st)) out.add(entryOf(st, "背包"));
            });
        }
        if (out.isEmpty()) addQuillSelection(mc, out);
        return out;
    }

    /**
     * The schematic-and-quill "clipboard": an in-progress selection is not an item, its two
     * corners live in Create's client handler, so it is only offered when no saved blueprint
     * was found and the player is actually holding the quill.
     */
    private static void addQuillSelection(Minecraft mc, List<BlueprintEntry> out) {
        try {
            boolean holding = mc.f_91074_.m_21205_().m_41720_() instanceof SchematicAndQuillItem
                    || mc.f_91074_.m_21206_().m_41720_() instanceof SchematicAndQuillItem;
            if (!holding) return;
            SchematicAndQuillHandler handler = CreateClient.SCHEMATIC_AND_QUILL_HANDLER;
            if (handler == null || handler.firstPos == null || handler.secondPos == null) return;
            BlockPos a = handler.firstPos;
            BlockPos b = handler.secondPos;
            BlockPos min = new BlockPos(Math.min(a.m_123341_(), b.m_123341_()),
                    Math.min(a.m_123342_(), b.m_123342_()),
                    Math.min(a.m_123343_(), b.m_123343_()));
            BlockPos max = new BlockPos(Math.max(a.m_123341_(), b.m_123341_()),
                    Math.max(a.m_123342_(), b.m_123342_()),
                    Math.max(a.m_123343_(), b.m_123343_()));
            String size = (max.m_123341_() - min.m_123341_() + 1) + "×" + (max.m_123342_() - min.m_123342_() + 1)
                    + "×" + (max.m_123343_() - min.m_123343_() + 1);
            out.add(new BlueprintEntry(ItemStack.f_41583_, "笔选区(剪贴板)", "手持蓝图笔 · " + size, min, max));
        } catch (Throwable ignored) {
            // quill data unavailable: silently fall back to the other sources
        }
    }

    private static boolean isBlueprint(ItemStack st) {
        return st != null && !st.m_41619_() && st.m_41782_() && st.m_41720_() instanceof SchematicItem;
    }

    private static BlueprintEntry entryOf(ItemStack st, String where) {
        CompoundTag tag = st.m_41783_();
        String hover = st.m_41786_().getString();
        String name = null;
        if (tag != null && tag.m_128441_("display")) {
            name = hover; // renamed blueprint: the custom name is the most useful label
        }
        if (name == null || name.isEmpty()) {
            String file = tag == null ? null : tag.m_128461_("File");
            if (file != null && !file.isEmpty()) {
                name = file.endsWith(".nbt") ? file.substring(0, file.length() - 4) : file;
            }
        }
        if (name == null || name.isEmpty()) name = hover;

        String detail = where;
        String bounds = boundsTextOf(tag);
        if (bounds != null) detail = detail + " · " + bounds;
        if (tag != null && tag.m_128471_("Deployed")) detail = detail + " · 已部署(按世界比对)";
        return new BlueprintEntry(st.m_41777_(), name, detail);
    }

    private static String boundsTextOf(CompoundTag tag) {
        if (tag == null || !tag.m_128441_("Bounds")) return null;
        ListTag asInt = tag.m_128437_("Bounds", TAG_INT);
        if (asInt.size() >= 3) {
            return asInt.m_128763_(0) + "×" + asInt.m_128763_(1) + "×" + asInt.m_128763_(2);
        }
        ListTag asDouble = tag.m_128437_("Bounds", TAG_DOUBLE);
        if (asDouble.size() >= 3) {
            return (int) asDouble.m_128772_(0) + "×" + (int) asDouble.m_128772_(1) + "×" + (int) asDouble.m_128772_(2);
        }
        return null;
    }

    // ===== materials =====

    public static boolean buildMaterials(Minecraft mc, BlueprintEntry entry,
                                         Map<Item, Integer> needed, Map<Item, Integer> minY) {
        if (mc == null || mc.f_91073_ == null || entry == null) return false;

        // Mode 0: quill selection - count what the selected region of the world holds.
        if (entry.isSelection()) {
            countSelection(mc, entry.selMin, entry.selMax, needed, minY);
            return true;
        }
        if (entry.stack.m_41619_()) return false;

        // Mode A: the blueprint is deployed in the world -> use Create's own loaded schematic world.
        SchematicWorld schematicWorld = null;
        try {
            schematicWorld = SchematicInstances.get(mc.f_91073_, entry.stack);
        } catch (Throwable ignored) {
            schematicWorld = null;
        }
        if (schematicWorld != null && schematicWorld.getBlockMap() != null
                && !schematicWorld.getBlockMap().isEmpty()) {
            BlockPos anchor = schematicWorld.anchor;
            for (Map.Entry<BlockPos, BlockState> e : schematicWorld.getBlockMap().entrySet()) {
                BlockState schemState = e.getValue();
                if (schemState == null || schemState.m_60795_()) continue;
                BlockPos worldPos = anchor == null ? e.getKey() : anchor.m_121955_(e.getKey());
                if (!isMissing(mc, worldPos, schemState)) continue;
                BackpackInjector.addMaterialFor(needed, minY, schemState, worldPos.m_123342_());
            }
            return true;
        }

        // Mode B: plain material list of the whole blueprint.
        CompoundTag tag = templateTagOf(mc, entry.stack);
        if (tag == null) return false;
        countTotals(mc, tag, needed, minY);
        return true;
    }

    /** Counts the block materials inside an (inclusive) region of the world - the quill clipboard. */
    private static void countSelection(Minecraft mc, BlockPos min, BlockPos max,
                                       Map<Item, Integer> needed, Map<Item, Integer> minY) {
        for (BlockPos pos : BlockPos.m_121940_(min, max)) {
            if (!mc.f_91073_.m_46805_(pos)) continue; // cannot know unloaded chunks
            BlockState state = mc.f_91073_.m_8055_(pos);
            if (state == null || state.m_60795_()) continue;
            BackpackInjector.addMaterialFor(needed, minY, state, pos.m_123342_());
        }
    }

    /** Same missing criterion as the litematica based BII scan / the verifier. */
    private static boolean isMissing(Minecraft mc, BlockPos worldPos, BlockState schemState) {        if (!mc.f_91073_.m_46805_(worldPos)) return true; // unloaded: count as missing
        BlockState worldState = mc.f_91073_.m_8055_(worldPos);
        return worldState.m_60795_() || worldState.m_247087_()
                || (worldState.m_60734_() != schemState.m_60734_()
                    && worldState.m_60734_() != BlockReplacer.resolveBlock(schemState.m_60734_()));
    }

    /**
     * Structure template NBT of the blueprint: an embedded tag when the item carries one
     * (not the case in Create 0.5.1, kept as a safety net), otherwise Create's own loader
     * reads {@code schematics/<File>.nbt}.
     */
    private static CompoundTag templateTagOf(Minecraft mc, ItemStack stack) {
        CompoundTag root = stack.m_41783_();
        if (root != null && (root.m_128441_("palette") || root.m_128441_("palettes"))) {
            return root;
        }
        StructureTemplate template = SchematicItem.loadSchematic(
                mc.f_91073_.m_246945_(Registries.f_256747_), stack);
        if (template == null) return null;
        Vec3i size = template.m_163801_();
        if (size == null || size.equals(Vec3i.f_123288_)) return null; // file missing / empty
        return template.m_74618_(new CompoundTag());
    }

    /**
     * Counts the whole blueprint from the canonical structure NBT: the "blocks" list holds one
     * entry per block whose "state" is an index into the palette.
     */
    private static void countTotals(Minecraft mc, CompoundTag tag,
                                    Map<Item, Integer> needed, Map<Item, Integer> minY) {
        ListTag palette = paletteOf(tag);
        if (palette == null || palette.size() == 0) return;
        ListTag blocks = tag.m_128437_("blocks", TAG_COMPOUND);
        int[] counts = new int[palette.size()];
        for (int i = 0; i < blocks.size(); i++) {
            int state = blocks.m_128728_(i).m_128451_("state");
            if (state >= 0 && state < counts.length) counts[state]++;
        }
        net.minecraft.core.HolderGetter<net.minecraft.world.level.block.Block> lookup =
                mc.f_91073_.m_246945_(Registries.f_256747_);
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] <= 0) continue;
            BlockState state;
            try {
                state = NbtUtils.m_247651_(lookup, palette.m_128728_(i));
            } catch (Throwable t) {
                continue;
            }
            if (state == null) continue;
            for (int n = 0; n < counts[i]; n++) {
                BackpackInjector.addMaterialFor(needed, minY, state, Integer.MAX_VALUE);
            }
        }
    }

    /** "palettes" array (first entry) with "palette" fallback - mirrors StructureTemplate.load. */
    private static ListTag paletteOf(CompoundTag tag) {
        if (tag.m_128441_("palettes")) {
            ListTag palettes = tag.m_128437_("palettes", TAG_LIST);
            if (palettes.size() > 0) {
                ListTag first = palettes.m_128744_(0);
                if (first.size() > 0) return first;
            }
        }
        if (tag.m_128441_("palette")) {
            return tag.m_128437_("palette", TAG_COMPOUND);
        }
        return null;
    }
}
