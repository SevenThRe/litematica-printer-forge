package xyz.jxmm.litematica_printer_forge.autobuild.planner;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement;
import fi.dy.masa.litematica.util.PositionUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockState;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.utils.BlockReplacer;

/**
 * Layered build planner for the auto build director.
 *
 * Runs an independent sharded scan (same world-coordinate formula and missing
 * criterion as MissingBlockEsp, replacement-aware via BlockReplacer) and produces:
 *  - missing block table sorted by Y ascending (unloaded chunks tracked separately,
 *    never counted toward layer completion),
 *  - per-layer material requirements (Item -> count),
 *  - stand-position candidates grouped into scored clusters,
 *  - a schematic-occupancy index used for scaffold legality checks.
 *
 * Passive component: the director drives {@link #tick(Minecraft)} while active.
 */
public final class AutoBuildPlanner {
    private static final int BLOCK_BUDGET = 16384;
    private static final int PER_TASK_BUDGET = 4096;
    private static final int DROP_SEARCH_MAX = 8;
    private static final int CLUSTER_ENTRY_CAP = 256;
    private static final Direction[] HORIZONTALS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    public static final class MissingEntry {
        public final BlockPos pos;
        public final Item item;
        public final Block block;
        public final Block expected;
        public final boolean unloaded;

        MissingEntry(BlockPos pos, Item item, Block block, boolean unloaded) {
            this.pos = pos;
            this.item = item;
            this.block = block;
            this.expected = BlockReplacer.resolveBlock(block);
            this.unloaded = unloaded;
        }
    }

    public static final class PlacementStats {
        public int nonAirTotal;
        public int missingLoaded;
        public int missingUnloaded;
        public BlockPos min;
        public BlockPos max;
    }

    public static final class StandCluster {
        public final BlockPos stand;
        public final List<BlockPos> blocks;
        public final double score;

        StandCluster(BlockPos stand, List<BlockPos> blocks, double score) {
            this.stand = stand;
            this.blocks = blocks;
            this.score = score;
        }
    }

    public static final class StandPlan {
        public final List<StandCluster> clusters;
        public final List<BlockPos> uncovered;

        StandPlan(List<StandCluster> clusters, List<BlockPos> uncovered) {
            this.clusters = clusters;
            this.uncovered = uncovered;
        }
    }

    private static final class ScanTask {
        final SchematicPlacement placement;
        final SubRegionPlacement sub;
        final LitematicaBlockStateContainer container;
        final BlockPos regionTransformed;
        final BlockPos origin;

        ScanTask(SchematicPlacement placement, SubRegionPlacement sub, LitematicaBlockStateContainer container,
                 BlockPos regionTransformed, BlockPos origin) {
            this.placement = placement;
            this.sub = sub;
            this.container = container;
            this.regionTransformed = regionTransformed;
            this.origin = origin;
        }
    }

    private static List<ScanTask> tasks = Collections.emptyList();
    private static int taskIdx;
    private static int bx, by, bz;
    private static ArrayList<MissingEntry> accum = new ArrayList<>();
    private static HashSet<Long> schematicPosAccum = new HashSet<>();
    private static LinkedHashMap<String, PlacementStats> statsAccum = new LinkedHashMap<>();
    private static String signature = "";
    private static boolean scanComplete = false;
    private static int dataVersion = 0;

    private static List<MissingEntry> missingSorted = Collections.emptyList();
    private static Set<Long> schematicPositions = Collections.emptySet();
    private static Map<String, PlacementStats> placementStats = Collections.emptyMap();

    private static SchematicPlacement targetPlacement = null;

    private AutoBuildPlanner() {
    }

    // Vanilla BlockPos.asLong layout, packed manually to avoid SRG name dependence.
    public static long packPos(BlockPos pos) {
        return ((long) (pos.m_123341_() & 0x3FFFFFF) << 38)
                | ((long) (pos.m_123343_() & 0x3FFFFFF) << 12)
                | (long) (pos.m_123342_() & 0xFFF);
    }

    public static void setTargetPlacement(SchematicPlacement placement) {
        if (targetPlacement != placement) {
            targetPlacement = placement;
            invalidate();
        }
    }

    public static SchematicPlacement getTargetPlacement() {
        return targetPlacement;
    }

    public static boolean isScanComplete() {
        return scanComplete;
    }

    public static int getDataVersion() {
        return dataVersion;
    }

    public static void invalidate() {
        signature = "";
        scanComplete = false;
        missingSorted = Collections.emptyList();
        schematicPositions = Collections.emptySet();
        placementStats = Collections.emptyMap();
        tasks = Collections.emptyList();
        accum = new ArrayList<>();
        schematicPosAccum = new HashSet<>();
        statsAccum = new LinkedHashMap<>();
    }

    public static void reset() {
        targetPlacement = null;
        invalidate();
    }

    public static void tick(Minecraft mc) {
        if (mc.f_91074_ == null || mc.f_91073_ == null) {
            reset();
            return;
        }
        String sig = buildSignature();
        if (!sig.equals(signature)) {
            signature = sig;
            initScan(mc);
        }
        if (tasks.isEmpty()) {
            if (!scanComplete) {
                finishScan(mc);
            }
            return;
        }
        scanStep(mc);
    }

    private static String buildSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(LitematicaMixinMod.AUTO_BUILD_SELECTED_ONLY.getBooleanValue()).append('|');
        sb.append(targetPlacement == null ? "-" : targetPlacement.getName()).append('|');
        List<SchematicPlacement> all = DataManager.getSchematicPlacementManager().getAllSchematicsPlacements();
        sb.append(all.size()).append(';');
        for (SchematicPlacement p : all) {
            sb.append(p.isEnabled()).append(',')
                    .append(p.getName()).append(',')
                    .append(p.getOrigin()).append(',')
                    .append(p.getRotation()).append(',')
                    .append(p.getMirror()).append(';');
        }
        return sb.toString();
    }

    private static List<SchematicPlacement> effectivePlacements() {
        if (targetPlacement != null) {
            return targetPlacement.isEnabled() ? List.of(targetPlacement) : List.of();
        }
        if (LitematicaMixinMod.AUTO_BUILD_SELECTED_ONLY.getBooleanValue()) {
            SchematicPlacement sel = DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();
            return sel != null && sel.isEnabled() ? List.of(sel) : List.of();
        }
        return DataManager.getSchematicPlacementManager().getAllSchematicsPlacements();
    }

    private static void initScan(Minecraft mc) {
        ArrayList<ScanTask> list = new ArrayList<>();
        for (SchematicPlacement placement : effectivePlacements()) {
            if (!placement.isEnabled()) continue;
            LitematicaSchematic schematic = placement.getSchematic();
            if (schematic == null) continue;
            for (Map.Entry<String, SubRegionPlacement> entry
                    : placement.getEnabledRelativeSubRegionPlacements().entrySet()) {
                SubRegionPlacement sub = entry.getValue();
                LitematicaBlockStateContainer container = schematic.getSubRegionContainer(entry.getKey());
                if (container == null) continue;
                BlockPos regionTransformed = PositionUtils
                        .getTransformedBlockPos(sub.getPos(), placement.getMirror(), placement.getRotation());
                list.add(new ScanTask(placement, sub, container, regionTransformed, placement.getOrigin()));
            }
        }
        tasks = list;
        taskIdx = 0;
        bx = by = bz = 0;
        accum = new ArrayList<>();
        schematicPosAccum = new HashSet<>();
        statsAccum = new LinkedHashMap<>();
    }

    private static void scanStep(Minecraft mc) {
        Level world = mc.f_91073_;
        WorldSchematic schemWorld = SchematicWorldHandler.getSchematicWorld();
        int worldBottomY = world.m_141937_();
        int worldTopY = world.m_151558_();
        int budget = BLOCK_BUDGET;

        while (budget > 0 && taskIdx < tasks.size()) {
            ScanTask t = tasks.get(taskIdx);
            Vec3i size = t.container.getSize();
            int perTask = Math.min(budget, PER_TASK_BUDGET);
            int processed = 0;
            while (processed < perTask) {
                BlockState schemState = t.container.get(bx, by, bz);
                boolean advanced = false;
                if (!schemState.m_60795_()) {
                    BlockPos worldPos = PositionUtils
                            .getTransformedPlacementPosition(new BlockPos(bx, by, bz), t.placement, t.sub)
                            .m_121955_(t.regionTransformed)
                            .m_121955_(t.origin);
                    recordSchematicPosition(t.placement, worldPos);
                    int y = worldPos.m_123342_();
                    if (y >= worldBottomY && y <= worldTopY) {
                        MissingEntry entry = classify(world, schemWorld, schemState, worldPos);
                        if (entry != null) {
                            accum.add(entry);
                            PlacementStats st = statsAccum.get(t.placement.getName());
                            if (st != null) {
                                if (entry.unloaded) st.missingUnloaded++; else st.missingLoaded++;
                            }
                        }
                    }
                }
                if (++bx >= size.m_123341_()) {
                    bx = 0;
                    if (++by >= size.m_123342_()) {
                        by = 0;
                        if (++bz >= size.m_123343_()) {
                            bz = 0;
                            advanced = true;
                        }
                    }
                }
                processed++;
                budget--;
                if (advanced) {
                    taskIdx++;
                    bx = by = bz = 0;
                    break;
                }
            }
        }

        if (taskIdx >= tasks.size()) {
            finishScan(mc);
            initScan(mc);
        }
    }

    private static void recordSchematicPosition(SchematicPlacement placement, BlockPos worldPos) {
        schematicPosAccum.add(packPos(worldPos));
        PlacementStats st = statsAccum.computeIfAbsent(placement.getName(), k -> new PlacementStats());
        st.nonAirTotal++;
        if (st.min == null) {
            st.min = worldPos.m_7949_();
            st.max = worldPos.m_7949_();
        } else {
            st.min = new BlockPos(Math.min(st.min.m_123341_(), worldPos.m_123341_()),
                    Math.min(st.min.m_123342_(), worldPos.m_123342_()),
                    Math.min(st.min.m_123343_(), worldPos.m_123343_()));
            st.max = new BlockPos(Math.max(st.max.m_123341_(), worldPos.m_123341_()),
                    Math.max(st.max.m_123342_(), worldPos.m_123342_()),
                    Math.max(st.max.m_123343_(), worldPos.m_123343_()));
        }
    }

    private static void finishScan(Minecraft mc) {
        accum.sort(Comparator.comparingInt((MissingEntry e) -> e.pos.m_123342_())
                .thenComparingInt(e -> e.pos.m_123341_())
                .thenComparingInt(e -> e.pos.m_123343_()));
        missingSorted = Collections.unmodifiableList(accum);
        schematicPositions = Collections.unmodifiableSet(schematicPosAccum);
        placementStats = Collections.unmodifiableMap(statsAccum);
        scanComplete = true;
        dataVersion++;
        LitematicaMixinMod.LOGGER.info("[AUTOBUILD] Planner scan pass complete: missing=" + missingSorted.size()
                + " schematicBlocks=" + schematicPositions.size() + " version=" + dataVersion);
    }

    /**
     * Shared missing-block criterion, identical to MissingBlockEsp / the verifier:
     * loaded chunk -> missing when the world block is air, replaceable, or neither the
     * schematic block nor its replacement target; falling blocks without possible
     * support are skipped. Unloaded chunk -> counted missing with the unloaded flag.
     */
    private static MissingEntry classify(Level world, WorldSchematic schemWorld, BlockState schemState, BlockPos worldPos) {
        if (world.m_46805_(worldPos)) {
            BlockState worldState = world.m_8055_(worldPos);
            if (!(worldState.m_60795_() || worldState.m_247087_()
                    || (worldState.m_60734_() != schemState.m_60734_()
                        && worldState.m_60734_() != BlockReplacer.resolveBlock(schemState.m_60734_())))) {
                return null;
            }
            BlockState belowWorld = world.m_8055_(worldPos.m_7495_());
            BlockState belowSchem = schemWorld == null ? null : schemWorld.m_8055_(worldPos.m_7495_());
            boolean neverBuildable = schemState.m_60734_() instanceof FallingBlock
                    && belowSchem != null && belowSchem.m_60795_()
                    && (belowWorld.m_60795_() || !belowWorld.m_60819_().m_76178_());
            if (neverBuildable) {
                return null;
            }
            Item item = itemFor(schemState);
            return item == null ? null : new MissingEntry(worldPos.m_7949_(), item, schemState.m_60734_(), false);
        }
        Item item = itemFor(schemState);
        return item == null ? null : new MissingEntry(worldPos.m_7949_(), item, schemState.m_60734_(), true);
    }

    private static Item itemFor(BlockState schemState) {
        // Potted plants have no item form of their own (asItem() == AIR); represent them in the
        // missing list by the empty flower pot. getMaterialNeeds() expands them into pot + flower.
        if (schemState.m_60734_() instanceof net.minecraft.world.level.block.FlowerPotBlock) {
            return Items.f_42618_;
        }
        Item item = schemState.m_60734_().m_5456_();
        if (item == Items.f_41852_) {
            return null;
        }
        item = BlockReplacer.resolve(item);
        return item == Items.f_41852_ ? null : item;
    }

    // ===== Query API (last completed snapshot) =====

    public static List<MissingEntry> getMissing() {
        return missingSorted;
    }

    public static Map<String, PlacementStats> getPlacementStats() {
        return placementStats;
    }

    /** Lowest Y with at least one loaded, non-skipped missing entry; null when none. */
    public static Integer getLowestMissingY(Set<Long> skipSet) {
        for (MissingEntry e : missingSorted) {
            if (e.unloaded) continue;
            if (skipSet != null && skipSet.contains(packPos(e.pos))) continue;
            return e.pos.m_123342_();
        }
        return null;
    }

    /** Lowest Y with at least one missing entry in an unloaded chunk; null when none. */
    public static Integer getLowestUnloadedY() {
        for (MissingEntry e : missingSorted) {
            if (e.unloaded) {
                return e.pos.m_123342_();
            }
        }
        return null;
    }

    public static int countLoadedAtOrBelow(int y, Set<Long> skipSet) {
        int count = 0;
        for (MissingEntry e : missingSorted) {
            int ey = e.pos.m_123342_();
            if (ey > y) break;
            if (e.unloaded) continue;
            if (skipSet != null && skipSet.contains(packPos(e.pos))) continue;
            count++;
        }
        return count;
    }

    public static int countUnloadedAtOrBelow(int y) {
        int count = 0;
        for (MissingEntry e : missingSorted) {
            if (e.pos.m_123342_() > y) break;
            if (e.unloaded) count++;
        }
        return count;
    }

    /** Layer completion criterion: no loaded, non-skipped missing block at or below y. */
    public static boolean isLayerComplete(int y, Set<Long> skipSet) {
        return countLoadedAtOrBelow(y, skipSet) == 0;
    }

    public static List<MissingEntry> getLayerEntries(int layerY, Set<Long> skipSet) {
        List<MissingEntry> out = new ArrayList<>();
        for (MissingEntry e : missingSorted) {
            int ey = e.pos.m_123342_();
            if (ey < layerY) continue;
            if (ey > layerY) break;
            if (e.unloaded) continue;
            if (skipSet != null && skipSet.contains(packPos(e.pos))) continue;
            out.add(e);
        }
        return out;
    }

    /** Material demand for one layer: replacement-aware item -> count (loaded, non-skipped only). */
    public static Map<Item, Integer> getMaterialNeeds(int layerY, Set<Long> skipSet) {
        Map<Item, Integer> out = new HashMap<>();
        for (MissingEntry e : getLayerEntries(layerY, skipSet)) {
            // Potted plants (itemFor() maps them to the empty pot) consume BOTH the pot and
            // the plant - count each so the restock/injection chain stocks both materials.
            if (e.block instanceof net.minecraft.world.level.block.FlowerPotBlock) {
                out.merge(Items.f_42618_, 1, Integer::sum);
                Item flower = xyz.jxmm.litematica_printer_forge.utils.BackpackInjector.pottedFlowerItem(
                        e.block.m_49966_());
                if (flower != null) {
                    out.merge(flower, 1, Integer::sum);
                }
                continue;
            }
            out.merge(e.item, 1, Integer::sum);
        }
        return out;
    }

    /** Scaffold legality query: is pos a non-air schematic position at or below maxY. */
    public static boolean isSchematicBlockAtOrBelow(BlockPos pos, int maxY) {
        return pos.m_123342_() <= maxY && schematicPositions.contains(packPos(pos));
    }

    // ===== Stand candidates =====

    /**
     * Greedy set-cover clustering of stand candidates for one layer.
     * Candidate = position where feet+head are passable, the block below is a solid
     * opaque cube, the missing block lies inside the printer range box, the spot is
     * not dangerous (fire/lava/fluid) and does not overlap unbuilt schematic cells.
     * Score = covered block count / (1 + horizontal distance to player).
     */
    public static StandPlan computeStandClusters(Minecraft mc, int layerY, Set<Long> skipSet) {
        if (mc.f_91074_ == null || mc.f_91073_ == null) {
            return new StandPlan(List.of(), List.of());
        }
        List<MissingEntry> entries = new ArrayList<>(getLayerEntries(layerY, skipSet));
        if (entries.isEmpty()) {
            return new StandPlan(List.of(), List.of());
        }
        if (entries.size() > CLUSTER_ENTRY_CAP) {
            double px = mc.f_91074_.m_20185_();
            double py = mc.f_91074_.m_20186_();
            double pz = mc.f_91074_.m_20189_();
            entries.sort(Comparator.comparingDouble(e -> {
                double dx = e.pos.m_123341_() + 0.5 - px;
                double dy = e.pos.m_123342_() + 0.5 - py;
                double dz = e.pos.m_123343_() + 0.5 - pz;
                return dx * dx + dy * dy + dz * dz;
            }));
            entries = new ArrayList<>(entries.subList(0, CLUSTER_ENTRY_CAP));
        }
        int rx = LitematicaMixinMod.EASY_PLACE_MODE_RANGE_X.getIntegerValue();
        int ry = LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Y.getIntegerValue();
        int rz = LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Z.getIntegerValue();
        Level world = mc.f_91073_;
        int n = entries.size();

        Map<BlockPos, List<Integer>> coverMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            HashSet<BlockPos> stands = new HashSet<>();
            gatherStandCandidates(world, entries.get(i).pos, rx, ry, rz, stands);
            for (BlockPos s : stands) {
                coverMap.computeIfAbsent(s, k -> new ArrayList<>()).add(i);
            }
        }

        boolean[] covered = new boolean[n];
        int remaining = n;
        BlockPos playerPos = mc.f_91074_.m_20183_();
        List<StandCluster> clusters = new ArrayList<>();
        while (remaining > 0) {
            BlockPos best = null;
            int bestCount = 0;
            double bestDist = 0;
            for (Map.Entry<BlockPos, List<Integer>> e : coverMap.entrySet()) {
                int c = 0;
                for (int idx : e.getValue()) {
                    if (!covered[idx]) c++;
                }
                if (c == 0) continue;
                double d = distSq(e.getKey(), playerPos);
                if (best == null || c > bestCount || (c == bestCount && d < bestDist)) {
                    best = e.getKey();
                    bestCount = c;
                    bestDist = d;
                }
            }
            if (best == null || bestCount == 0) break;
            List<BlockPos> blocks = new ArrayList<>();
            for (int idx : coverMap.get(best)) {
                if (!covered[idx]) {
                    covered[idx] = true;
                    remaining--;
                    blocks.add(entries.get(idx).pos);
                }
            }
            double score = bestCount / (1.0 + Math.sqrt(bestDist));
            clusters.add(new StandCluster(best, blocks, score));
            coverMap.remove(best);
        }
        clusters.sort((a, b) -> Double.compare(b.score, a.score));

        List<BlockPos> uncovered = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!covered[i]) {
                uncovered.add(entries.get(i).pos);
            }
        }
        return new StandPlan(clusters, uncovered);
    }

    private static double distSq(BlockPos a, BlockPos b) {
        double dx = a.m_123341_() - b.m_123341_();
        double dy = a.m_123342_() - b.m_123342_();
        double dz = a.m_123343_() - b.m_123343_();
        return dx * dx + dy * dy + dz * dz;
    }

    private static void gatherStandCandidates(Level world, BlockPos p, int rx, int ry, int rz, Set<BlockPos> out) {
        for (Direction d : Direction.values()) {
            tryAddStand(world, p.m_121955_(d.m_122436_()), p, rx, ry, rz, out);
        }
        for (Direction d : HORIZONTALS) {
            BlockPos base = p.m_121955_(d.m_122436_());
            for (int dy = 0; dy <= DROP_SEARCH_MAX; dy++) {
                BlockPos s = new BlockPos(base.m_123341_(), p.m_123342_() - dy, base.m_123343_());
                if (tryAddStand(world, s, p, rx, ry, rz, out)) {
                    break;
                }
            }
        }
    }

    private static boolean tryAddStand(Level world, BlockPos s, BlockPos target, int rx, int ry, int rz, Set<BlockPos> out) {
        if (Math.abs(s.m_123341_() - target.m_123341_()) > rx
                || Math.abs(s.m_123342_() - target.m_123342_()) > ry
                || Math.abs(s.m_123343_() - target.m_123343_()) > rz) {
            return false;
        }
        if (!standable(world, s)) {
            return false;
        }
        out.add(s.m_7949_());
        return true;
    }

    private static boolean standable(Level world, BlockPos s) {
        BlockPos below = s.m_7495_();
        if (!world.m_46805_(s) || !world.m_46805_(below)) {
            return false;
        }
        // Never stand inside unbuilt schematic cells (feet or head).
        if (schematicPositions.contains(packPos(s)) || schematicPositions.contains(packPos(s.m_7494_()))) {
            return false;
        }
        BlockState ground = world.m_8055_(below);
        Block gb = ground.m_60734_();
        if (gb instanceof FireBlock || gb instanceof MagmaBlock || gb instanceof CampfireBlock) {
            return false;
        }
        // isRedstoneConductor: solid opaque cube, conservative ground check.
        if (!ground.m_60796_(world, below)) {
            return false;
        }
        return passable(world.m_8055_(s)) && passable(world.m_8055_(s.m_7494_()));
    }

    private static boolean passable(BlockState s) {
        if (!(s.m_60795_() || s.m_247087_())) {
            return false;
        }
        if (!s.m_60819_().m_76178_()) {
            return false;
        }
        return !(s.m_60734_() instanceof FireBlock);
    }

    /**
     * Immediate full statistics pass over a single placement (used by the build
     * command hover summary; runs once per invocation, not sharded).
     */
    public static PlacementStats summarizeNow(Minecraft mc, SchematicPlacement placement) {
        PlacementStats st = new PlacementStats();
        if (mc.f_91073_ == null || placement == null) {
            return st;
        }
        Level world = mc.f_91073_;
        WorldSchematic schemWorld = SchematicWorldHandler.getSchematicWorld();
        int worldBottomY = world.m_141937_();
        int worldTopY = world.m_151558_();
        LitematicaSchematic schematic = placement.getSchematic();
        if (schematic == null) {
            return st;
        }
        for (Map.Entry<String, SubRegionPlacement> entry
                : placement.getEnabledRelativeSubRegionPlacements().entrySet()) {
            LitematicaBlockStateContainer container = schematic.getSubRegionContainer(entry.getKey());
            if (container == null) continue;
            SubRegionPlacement sub = entry.getValue();
            BlockPos regionTransformed = PositionUtils
                    .getTransformedBlockPos(sub.getPos(), placement.getMirror(), placement.getRotation());
            BlockPos origin = placement.getOrigin();
            Vec3i size = container.getSize();
            for (int z = 0; z < size.m_123343_(); z++) {
                for (int y = 0; y < size.m_123342_(); y++) {
                    for (int x = 0; x < size.m_123341_(); x++) {
                        BlockState schemState = container.get(x, y, z);
                        if (schemState.m_60795_()) continue;
                        BlockPos worldPos = PositionUtils
                                .getTransformedPlacementPosition(new BlockPos(x, y, z), placement, sub)
                                .m_121955_(regionTransformed)
                                .m_121955_(origin);
                        st.nonAirTotal++;
                        if (st.min == null) {
                            st.min = worldPos.m_7949_();
                            st.max = worldPos.m_7949_();
                        } else {
                            st.min = new BlockPos(Math.min(st.min.m_123341_(), worldPos.m_123341_()),
                                    Math.min(st.min.m_123342_(), worldPos.m_123342_()),
                                    Math.min(st.min.m_123343_(), worldPos.m_123343_()));
                            st.max = new BlockPos(Math.max(st.max.m_123341_(), worldPos.m_123341_()),
                                    Math.max(st.max.m_123342_(), worldPos.m_123342_()),
                                    Math.max(st.max.m_123343_(), worldPos.m_123343_()));
                        }
                        int wy = worldPos.m_123342_();
                        if (wy < worldBottomY || wy > worldTopY) continue;
                        MissingEntry m = classify(world, schemWorld, schemState, worldPos);
                        if (m != null) {
                            if (m.unloaded) st.missingUnloaded++; else st.missingLoaded++;
                        }
                    }
                }
            }
        }
        return st;
    }
}
