package xyz.jxmm.litematica_printer_forge.utils;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltPart;
import com.simibubi.create.content.schematics.SchematicAndQuillItem;
import com.simibubi.create.content.schematics.SchematicInstances;
import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.content.schematics.cannon.MaterialChecklist;
import com.simibubi.create.content.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.utility.BlockHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;

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
 *   <li><b>Deployed blueprint</b> (Create exposes a SchematicWorld): the material list is built
 *       with Create's own {@link MaterialChecklist} + {@link ItemRequirement} and the
 *       schematicannon's per-position placement filter, so it matches the cannon GUI's 缺少物品
 *       list 1:1 (exact blockstate diff against the world, structural parts ignored, entities
 *       such as Super Glue included).</li>
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

    private static final int DIAG_MAX = 6;

    /** Every Create material source carried by the player (inventory + open GUI + backpacks, then the quill clipboard). */
    public static List<BlueprintEntry> collectBlueprints(Minecraft mc) {
        List<BlueprintEntry> out = new ArrayList<>();
        if (mc == null || mc.f_91074_ == null) return out;
        boolean dbg = LitematicaMixinMod.DEBUG_MESSAGE.getBooleanValue();
        // dedupe: the same blueprint can sit in the player inventory AND in the open
        // container menu (every menu repeats the player inventory slots) or in a backpack
        java.util.Set<String> seen = new java.util.HashSet<>();

        // ---- 1) player inventory (main slots + armor + offhand through the Container API) ----
        Inventory inv = mc.f_91074_.m_150109_();
        int size = inv.m_6643_();
        int invNonEmpty = 0;
        int invCount = 0;
        StringBuilder invProbe = new StringBuilder();
        int[] invProbeN = {0};
        for (int i = 0; i < size; i++) {
            ItemStack st = inv.m_8020_(i);
            if (st == null || st.m_41619_()) continue;
            invNonEmpty++;
            probe(invProbe, invProbeN, i, st);
            if (isBlueprint(st) && addEntry(out, seen, entryOf(st, "物品栏#" + i))) invCount++;
        }
        if (dbg) {
            say("蓝图扫描1/3 物品栏(无界面) " + size + "格 非空" + invNonEmpty
                    + " 蓝图" + invCount + " | 含schematic类:" + text(invProbe));
        }

        // ---- 2) the currently open container menu (schematicannon / schematic table / backpack GUI) ----
        int menuCount = 0;
        net.minecraft.world.inventory.AbstractContainerMenu openMenu = mc.f_91074_.f_36096_;
        StringBuilder menuProbe = new StringBuilder();
        int[] menuProbeN = {0};
        int menuNonEmpty = 0;
        int menuSlots = 0;
        boolean menuIsSelf = false;
        if (openMenu != null) {
            menuSlots = openMenu.f_38839_.size();
            for (int i = 0; i < menuSlots; i++) {
                net.minecraft.world.inventory.Slot slot = openMenu.f_38839_.get(i);
                ItemStack st = slot.m_7993_();
                // a menu always repeats the player inventory: label those correctly instead of
                // pretending they came out of the container.
                boolean fromPlayer = slot.f_40218_ == inv;
                if (fromPlayer) menuIsSelf = true;
                if (st == null || st.m_41619_()) continue;
                menuNonEmpty++;
                probe(menuProbe, menuProbeN, i, st);
                if (isBlueprint(st) && addEntry(out, seen, entryOf(st, (fromPlayer ? "物品栏(界面)#" : "容器#") + i))) {
                    menuCount++;
                }
            }
        }
        if (dbg) {
            say("蓝图扫描2/3 界面 " + (openMenu == null ? "无" : openMenu.getClass().getSimpleName()
                    + " " + menuSlots + "格 非空" + menuNonEmpty + " 含玩家物品栏=" + menuIsSelf)
                    + " 蓝图(新增)" + menuCount + " | 含schematic类:" + text(menuProbe));
        }

        // ---- 3) backpacks carried in the inventory (read through the capability, UI not needed) ----
        int bagCount = 0;
        int bagsSeen = 0;
        int bagSlots = 0;
        for (int i = 0; i < size; i++) {
            ItemStack bag = inv.m_8020_(i);
            if (bag.m_41619_() || !BackpackInjector.isBackpackStack(bag)) continue;
            bagsSeen++;
            bagSlots += BackpackInjector.backpackSlotCount(bag);
            BackpackInjector.forEachBackpackStack(bag, (slot, st) -> {
                if (isBlueprint(st)) addEntry(out, seen, entryOf(st, "背包#" + slot));
            });
        }
        bagCount = out.size() - invCount - menuCount;
        if (dbg) {
            say("蓝图扫描3/3 背包物品" + bagsSeen + "个 内部槽" + bagSlots + " 蓝图(新增)" + Math.max(bagCount, 0));
        }

        if (out.isEmpty()) addQuillSelection(mc, out);

        if (dbg) {
            for (int k = 0; k < out.size(); k++) {
                say("  #" + (k + 1) + " " + out.get(k).name + " ← " + out.get(k).detail);
            }
            say("蓝图扫描合计: 物品栏" + invCount + " 界面" + menuCount + " 背包" + Math.max(bagCount, 0)
                    + " 笔选区" + quillCount(out) + " → 共" + out.size() + "个来源");
        }
        return out;
    }

    /** Collects {@code index=itemId} for items that look like a Create blueprint/quill/clipboard. */
    private static void probe(StringBuilder sb, int[] n, int index, ItemStack st) {
        if (!looksSchematic(st)) return;
        if (n[0] >= DIAG_MAX) {
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '…') sb.append(" …");
            return;
        }
        if (sb.length() > 0) sb.append(' ');
        sb.append(index).append('=').append(itemId(st));
        n[0]++;
    }

    private static String text(StringBuilder sb) {
        return sb.length() == 0 ? "无" : sb.toString();
    }

    private static boolean looksSchematic(ItemStack st) {
        if (st == null || st.m_41619_()) return false;
        String cls = st.m_41720_().getClass().getName().toLowerCase();
        if (cls.contains("schematic") || cls.contains("clipboard")) return true;
        String id = itemId(st).toLowerCase();
        return id.contains("schematic") || id.contains("clipboard") || id.contains("blueprint");
    }

    private static String itemId(ItemStack st) {
        try {
            net.minecraft.resources.ResourceLocation rl =
                    net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(st.m_41720_());
            return rl == null ? st.m_41720_().getClass().getSimpleName() : rl.toString();
        } catch (Throwable t) {
            return st.m_41720_().getClass().getSimpleName();
        }
    }

    private static void say(String msg) {
        MessageHolder.sendMessageUnchecked("[BII-DEBUG] " + msg);
    }

    private static int quillCount(List<BlueprintEntry> out) {
        int n = 0;
        for (BlueprintEntry e : out) if (e.isSelection()) n++;
        return n;
    }

    /** Dedupes by the blueprint's structure file (falls back to the display name). */
    private static boolean addEntry(List<BlueprintEntry> out, java.util.Set<String> seen, BlueprintEntry e) {
        String key = e.stack.m_41783_() != null && e.stack.m_41783_().m_128441_("File")
                ? e.stack.m_41783_().m_128461_("File") : e.name;
        if (!seen.add(key)) return false;
        out.add(e);
        return true;
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

        // Mode 0: quill selection - the region of the world the blueprint will be made from.
        if (entry.isSelection()) {
            countSelection(mc, entry.selMin, entry.selMax, needed, minY);
            return true;
        }
        if (entry.stack.m_41619_()) return false;

        // Mode A: the blueprint is deployed -> build the list exactly like the schematicannon
        // does (Create's own MaterialChecklist + ItemRequirement + the cannon's place filter),
        // so the result matches the cannon GUI's 缺少物品 list 1:1.
        SchematicWorld schematicWorld = null;
        try {
            schematicWorld = SchematicInstances.get(mc.f_91073_, entry.stack);
        } catch (Throwable ignored) {
            schematicWorld = null;
        }
        if (schematicWorld != null && schematicWorld.getBlockMap() != null
                && !schematicWorld.getBlockMap().isEmpty()) {
            countLikeCannon(mc, schematicWorld, needed, minY);
            return true;
        }

        // Mode B: plain material list of the whole blueprint (no deployment anchor to diff
        // against, so nothing can be filtered - Create's requirement rules still apply).
        CompoundTag tag = templateTagOf(mc, entry.stack);
        if (tag == null) return false;
        countTotals(mc, tag, needed, minY);
        return true;
    }

    /**
     * Rebuilds the schematicannon's material checklist for a deployed blueprint.
     *
     * <p>Rule source is Create itself - {@link MaterialChecklist} + {@link ItemRequirement} - so
     * hand-mirrored rule tables can never drift again. The per-position filter is copied from the
     * cannon's bytecode ({@code SchematicPrinter.shouldPlaceBlock} + the cannon's own
     * {@code shouldPlace} predicate at its default replace mode 2):</p>
     * <ul>
     *   <li>chunk not loaded -&gt; skipped (the cannon skips it too and just warns),</li>
     *   <li>world state <b>equal to the schematic state</b> (exact state, age zeroed) -&gt; skipped,
     *       which is why a shaft that only differs in axis still shows up as 缺少,</li>
     *   <li>unbreakable blocks (bedrock) skipped,</li>
     *   <li>structural parts (structure void, double-block upper half, bed head, piston head,
     *       belt middle) and blocks without a requirement ignored,</li>
     *   <li>entities (Super Glue, item frames, armor stands) via {@code ItemRequirement.of(Entity)},
     *       DAMAGE items (glue) land in {@code damageRequired} and are injected too.</li>
     * </ul>
     */
    private static void countLikeCannon(Minecraft mc, SchematicWorld world,
                                        Map<Item, Integer> needed, Map<Item, Integer> minY) {
        MaterialChecklist checklist = new MaterialChecklist();
        BlockPos anchor = world.anchor;
        int unloaded = 0;
        for (Map.Entry<BlockPos, BlockState> e : world.getBlockMap().entrySet()) {
            BlockState schemState = BlockHelper.setZeroAge(e.getValue());
            if (schemState == null) continue;
            BlockPos rel = e.getKey();
            BlockPos worldPos = anchor == null ? rel : rel.m_121955_(anchor);
            if (!mc.f_91073_.m_46805_(worldPos)) {              // m_46805_ = isLoaded
                unloaded++;                                      // cannon: shouldPlaceBlock -> false
                continue;
            }
            BlockEntity schemBe = world.m_7702_(worldPos);      // getBlockEntity (schematic world)
            ItemRequirement requirement = ItemRequirement.of(schemState, schemBe);
            if (requirement.isEmpty() || requirement.isInvalid()) continue;
            if (cannonIgnores(schemState)) continue;
            BlockState worldState = mc.f_91073_.m_8055_(worldPos);  // getBlockState
            if (worldState == schemState) continue;                  // exact state already in place
            if (worldState.m_60800_(mc.f_91073_, worldPos) == -1.0F) continue;  // getDestroySpeed: bedrock
            checklist.require(requirement);
        }
        world.getEntityStream().forEach(entity -> {
            if (entity != null) checklist.require(ItemRequirement.of(entity));
        });
        drainChecklist(checklist, needed, minY);
        if (unloaded > 0) {
            say("加农炮比对: " + unloaded + " 个位置区块未加载，与加农炮一致未计入"
                    + "（打印那部分前先让客户端加载该区域再按一次注入）");
        }
    }

    /** The schematicannon's {@code shouldIgnoreBlockState} list, copied from its bytecode. */
    private static boolean cannonIgnores(BlockState state) {
        if (state.m_60734_() == Blocks.f_50454_) return true;                    // STRUCTURE_VOID
        if (state.m_61138_(BlockStateProperties.f_61401_)                        // DOUBLE_BLOCK_HALF
                && state.m_61143_(BlockStateProperties.f_61401_) == DoubleBlockHalf.UPPER) return true;
        if (state.m_61138_(BlockStateProperties.f_61391_)                        // BED_PART
                && state.m_61143_(BlockStateProperties.f_61391_) == BedPart.HEAD) return true;
        if (state.m_60734_() instanceof PistonHeadBlock) return true;
        return AllBlocks.BELT.has(state)
                && state.m_61143_(BeltBlock.PART) == BeltPart.MIDDLE;
    }

    /** Moves a filled checklist into BII's material maps. DAMAGE items (glue) come along. */
    private static void drainChecklist(MaterialChecklist checklist,
                                       Map<Item, Integer> needed, Map<Item, Integer> minY) {
        drainMap(checklist.required, needed, minY);
        drainMap(checklist.damageRequired, needed, minY);
    }

    private static void drainMap(Object2IntMap<Item> src, Map<Item, Integer> needed, Map<Item, Integer> minY) {
        for (Object2IntMap.Entry<Item> e : src.object2IntEntrySet()) {
            Item item = e.getKey();
            if (item == null || item == Items.f_41852_ || e.getIntValue() <= 0) continue;
            needed.merge(item, e.getIntValue(), Integer::sum);
            minY.merge(item, Integer.MAX_VALUE, Math::min);
        }
    }

    /** Counts the block materials inside an (inclusive) region of the world - the quill clipboard. */
    private static void countSelection(Minecraft mc, BlockPos min, BlockPos max,
                                       Map<Item, Integer> needed, Map<Item, Integer> minY) {
        MaterialChecklist checklist = new MaterialChecklist();
        for (BlockPos pos : BlockPos.m_121940_(min, max)) {
            if (!mc.f_91073_.m_46805_(pos)) continue; // cannot know unloaded chunks
            BlockState state = mc.f_91073_.m_8055_(pos);
            if (state == null || state.m_60795_()) continue;
            checklist.require(ItemRequirement.of(state, mc.f_91073_.m_7702_(pos)));
        }
        drainChecklist(checklist, needed, minY);
    }

    /**
     * Counts the whole blueprint from the canonical structure NBT: the "blocks" list holds one
     * entry per block whose "state" is an index into the palette. There is no deployment anchor,
     * so nothing can be diffed against the world - Create's requirement rules still decide what
     * each block costs.
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
        MaterialChecklist checklist = new MaterialChecklist();
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] <= 0) continue;
            BlockState state;
            try {
                state = NbtUtils.m_247651_(lookup, palette.m_128728_(i));
            } catch (Throwable t) {
                continue;
            }
            if (state == null) continue;
            ItemRequirement requirement = ItemRequirement.of(state, null);
            if (requirement.isEmpty() || requirement.isInvalid()) continue;
            for (int n = 0; n < counts[i]; n++) {
                checklist.require(requirement);
            }
        }

        // Entities: Create keeps Super Glue (plus item frames / armor stands) in the structure's
        // "entities" list and its material list asks for the matching item for each of them.
        ListTag entities = tag.m_128437_("entities", TAG_COMPOUND);
        for (int i = 0; i < entities.size(); i++) {
            CompoundTag entityTag = entities.m_128728_(i);
            if (entityTag == null || !entityTag.m_128441_("nbt")) continue;
            BackpackInjector.addEntityMaterialFor(needed, minY, entityTag.m_128469_("nbt"), Integer.MAX_VALUE);
        }
        drainChecklist(checklist, needed, minY);
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
