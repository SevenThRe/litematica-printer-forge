package xyz.jxmm.litematica_printer_forge.fastbuild;

import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.LitematicaSchematic;
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement;
import fi.dy.masa.litematica.util.PositionUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.utils.BlockReplacer;

/**
 * The OP fast builder: builds the difference between the enabled litematica placements and the
 * world by sending batched vanilla commands instead of walking around placing blocks.
 *
 * <p>Five stages, spread over ticks so nothing blocks the render thread:
 * <ol>
 *   <li><b>SCAN</b> - walk every enabled placement / sub region / container position, compute the
 *       world position with the same formula the ESP and the auto builder use
 *       ({@code PositionUtils.getTransformedPlacementPosition(local, placement, sub)} offset by the
 *       transformed sub region position and the placement origin), and keep the positions whose
 *       world block is not already what the schematic wants.</li>
 *   <li><b>DECOMPOSE</b> - group the missing positions per chunk column and greedily grow
 *       axis aligned boxes of one identical block state. A 16x16 chunk column is the natural unit
 *       because /fill fails with "not loaded" unless every chunk it touches is loaded, so keeping
 *       every box inside one chunk column means one loaded check per command.</li>
 *   <li><b>DRAIN</b> - send at most N fill / M setblock commands per tick (configurable; the OP
 *       reward is that the server exempts opped players from its command spam counter, see
 *       {@code ServerGamePacketListenerImpl.m_215251_}).</li>
 *   <li><b>SETTLE</b> - wait a little so the resulting block updates can travel back.</li>
 *   <li><b>VERIFY</b> - re-read the world at every position that was supposed to change, drop the
 *       ones that now match, and run another round for the rest. This is what makes the builder
 *       self healing: a command dropped by the server (block limit, an unloaded chunk, a
 *       momentarily refused command) simply shows up as still missing and is retried.</li>
 * </ol>
 *
 * <p>Block entity data is carried by /setblock. Verified from the 1.20.1 bytecode: both
 * {@code FillCommand} and {@code SetBlockCommand} end in {@code BlockInput.m_114670_(level, pos, 2)},
 * which does {@code level.setBlock(...)} and then {@code level.getBlockEntity(pos).load(tag)}.
 * So a smart chute's configured filter survives, exactly like the create schematicannon's own paste.
 * /fill cannot carry per-block data (the same tag would land on every block), so any position that
 * has block entity data in the schematic is placed with /setblock instead.
 *
 * <p>Entities are deliberately not handled yet: super glue, item frames and armour stands live in
 * the schematic's entity list, and rotating them correctly needs litematica's own
 * {@code EntityUtils} paste path. Blocks with block entity data already cover the bulk of the work.
 */
public final class FastBuildJob {
    private FastBuildJob() {
    }

    private enum Stage {
        IDLE, SCAN, DECOMPOSE, DRAIN, SETTLE, FINISHED
    }

    /** Positions examined per tick during the scan. */
    private static final int SCAN_BUDGET = 20000;
    /** Chunk columns decomposed per tick. */
    private static final int DECOMPOSE_BUDGET = 24;
    /** Ticks to wait after the last command before verifying. */
    private static final int SETTLE_TICKS = 20;
    /** In safe mode at most one command every this many ticks. */
    private static final int SAFE_MODE_INTERVAL = 20;
    /** Singleplayer moves no packets, so the same budget goes much further. */
    private static final int SINGLEPLAYER_MULTIPLIER = 4;
    private static final int HUD_INTERVAL = 10;

    // ---------------------------------------------------------------- scan state

    private static final class Region {
        final SchematicPlacement placement;
        final SubRegionPlacement sub;
        final LitematicaBlockStateContainer container;
        final BlockPos regionTransformed;
        final BlockPos origin;
        final Map<BlockPos, CompoundTag> beMap;

        Region(SchematicPlacement placement, SubRegionPlacement sub, LitematicaBlockStateContainer container,
               BlockPos regionTransformed, BlockPos origin, Map<BlockPos, CompoundTag> beMap) {
            this.placement = placement;
            this.sub = sub;
            this.container = container;
            this.regionTransformed = regionTransformed;
            this.origin = origin;
            this.beMap = beMap;
        }
    }

    /** One command: a fill box, or a single setblock (a == b). */
    private static final class Op {
        final int x1;
        final int y1;
        final int z1;
        final int x2;
        final int y2;
        final int z2;
        final BlockState state;
        final CompoundTag nbt;
        final boolean fill;

        Op(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state, CompoundTag nbt, boolean fill) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.state = state;
            this.nbt = nbt;
            this.fill = fill;
        }

        int volume() {
            return (x2 - x1 + 1) * (y2 - y1 + 1) * (z2 - z1 + 1);
        }
    }

    private static final class Stats {
        int schematicBlocks;
        int alreadyOk;
        int unloadedSkipped;
        int missingFound;
        int fillsSent;
        int setsSent;
        int blocksCovered;
        int nbtDropped;
        int commandsSkipped;
        int rounds;
        int remaining;

        String summary() {
            return "fills=" + fillsSent + " setblocks=" + setsSent + " blocks=" + blocksCovered
                    + " nbtDropped=" + nbtDropped + " skipped=" + commandsSkipped
                    + " unloaded=" + unloadedSkipped + " remaining=" + remaining;
        }
    }

    private static Stage stage = Stage.IDLE;
    private static Level boundLevel;
    private static boolean aborted;
    private static String abortReason = "";
    private static final ArrayList<Region> regions = new ArrayList<>();
    private static int regionIdx;
    private static int bx;
    private static int by;
    private static int bz;
    private static int scanBudget;
    private static int decomposeBudget;
    private static int settleTicks;
    private static int hudTicks;
    private static int round;
    private static int maxBox;
    private static int safeModeCooldown;

    private static final HashMap<Long, BlockState> missing = new HashMap<>();
    private static final HashMap<Long, CompoundTag> missingNbt = new HashMap<>();
    private static final LinkedHashMap<Long, ArrayList<Long>> byChunk = new LinkedHashMap<>();
    private static ArrayList<Long> chunkOrder = new ArrayList<>();
    private static int chunkIdx;
    private static final ArrayDeque<Op> fillQueue = new ArrayDeque<>();
    private static final ArrayDeque<Op> setQueue = new ArrayDeque<>();
    private static Stats stats = new Stats();

    // ---------------------------------------------------------------- public API

    public static boolean isRunning() {
        return stage != Stage.IDLE && stage != Stage.FINISHED;
    }

    public static boolean isAborted() {
        return aborted;
    }

    /**
     * Start a fast build of every enabled litematica placement. Silently does nothing when the
     * player may not run /fill + /setblock; the caller reports that via {@link OpGate}.
     */
    public static boolean start(Minecraft mc) {
        stop(mc, false);
        if (mc == null || mc.f_91073_ == null || mc.f_91074_ == null) {
            return false;
        }
        if (!LitematicaMixinMod.FAST_BUILD_ENABLED.getBooleanValue()) {
            say(mc, "fast builder disabled in config", false);
            return false;
        }
        if (!OpGate.canFastBuild(mc)) {
            say(mc, "no permission - need /fill and /setblock (" + OpGate.describe(mc) + ")", false);
            return false;
        }

        ArrayList<Region> found = new ArrayList<>();
        List<SchematicPlacement> all = DataManager.getSchematicPlacementManager().getAllSchematicsPlacements();
        for (SchematicPlacement p : all) {
            if (p == null || !p.isEnabled()) {
                continue;
            }
            LitematicaSchematic schematic = p.getSchematic();
            if (schematic == null) {
                continue;
            }
            for (Map.Entry<String, SubRegionPlacement> entry
                    : p.getEnabledRelativeSubRegionPlacements().entrySet()) {
                SubRegionPlacement sub = entry.getValue();
                LitematicaBlockStateContainer container = schematic.getSubRegionContainer(entry.getKey());
                if (container == null || sub == null) {
                    continue;
                }
                BlockPos regionTransformed = PositionUtils
                        .getTransformedBlockPos(sub.getPos(), p.getMirror(), p.getRotation());
                Map<BlockPos, CompoundTag> beMap = null;
                try {
                    beMap = schematic.getBlockEntityMapForRegion(entry.getKey());
                } catch (Throwable ignored) {
                    beMap = null;
                }
                found.add(new Region(p, sub, container, regionTransformed, p.getOrigin(), beMap));
            }
        }
        if (found.isEmpty()) {
            say(mc, "no enabled blueprint placement", false);
            return false;
        }

        regions.clear();
        regions.addAll(found);
        boundLevel = mc.f_91073_;
        aborted = false;
        abortReason = "";
        regionIdx = 0;
        bx = by = bz = 0;
        round = 1;
        maxBox = LitematicaMixinMod.FAST_BUILD_MAX_BOX.getIntegerValue();
        stats = new Stats();
        missing.clear();
        missingNbt.clear();
        byChunk.clear();
        chunkOrder.clear();
        fillQueue.clear();
        setQueue.clear();
        scanBudget = 0;
        decomposeBudget = 0;
        settleTicks = 0;
        hudTicks = 0;
        safeModeCooldown = 0;
        stage = Stage.SCAN;

        LitematicaMixinMod.LOGGER.info("[FASTBUILD] start: regions=" + regions.size()
                + " permission=" + OpGate.describe(mc) + " maxBox=" + maxBox);
        say(mc, "started: " + regions.size() + " region(s), " + OpGate.describe(mc), false);
        return true;
    }

    public static void stop(Minecraft mc, boolean notify) {
        if (stage != Stage.IDLE) {
            LitematicaMixinMod.LOGGER.info("[FASTBUILD] stop at stage " + stage + " " + stats.summary());
        }
        stage = Stage.IDLE;
        regions.clear();
        missing.clear();
        missingNbt.clear();
        byChunk.clear();
        chunkOrder.clear();
        fillQueue.clear();
        setQueue.clear();
        boundLevel = null;
        if (notify && mc != null) {
            say(mc, "stopped", false);
        }
    }

    /** Toggle for the hotkey. */
    public static void toggle(Minecraft mc) {
        if (isRunning()) {
            stop(mc, true);
        } else {
            start(mc);
        }
    }

    public static void tick(Minecraft mc) {
        if (stage == Stage.IDLE) {
            return;
        }
        if (mc == null || mc.f_91074_ == null || mc.f_91073_ == null
                || mc.m_91403_() == null || mc.f_91073_ != boundLevel) {
            stage = Stage.IDLE;
            aborted = true;
            abortReason = "world changed or connection lost";
            if (mc != null && mc.f_91074_ != null) {
                say(mc, "aborted: " + abortReason, false);
            }
            return;
        }
        if (aborted) {
            return;
        }

        switch (stage) {
            case SCAN:
                stepScan(mc);
                break;
            case DECOMPOSE:
                stepDecompose(mc);
                break;
            case DRAIN:
                stepDrain(mc);
                break;
            case SETTLE:
                if (--settleTicks <= 0) {
                    stepVerify(mc);
                }
                break;
            default:
                break;
        }
        if (stage != Stage.IDLE && stage != Stage.FINISHED && --hudTicks <= 0) {
            hudTicks = HUD_INTERVAL;
            say(mc, progressLine(), true);
        }
    }

    public static String progressLine() {
        switch (stage) {
            case SCAN:
                return "scanning regions " + (regionIdx + 1) + "/" + regions.size()
                        + " missing=" + missing.size();
            case DECOMPOSE:
                return "planning " + chunkIdx + "/" + chunkOrder.size()
                        + " fills=" + fillQueue.size() + " setblocks=" + setQueue.size();
            case DRAIN:
                return "building round " + round + " fills=" + fillQueue.size()
                        + " setblocks=" + setQueue.size() + " blocks=" + stats.blocksCovered;
            case SETTLE:
                return "verifying in " + settleTicks + "t";
            default:
                return "";
        }
    }

    public static String report() {
        return stats.summary();
    }

    // ---------------------------------------------------------------- stage: scan

    private static void stepScan(Minecraft mc) {
        Level world = mc.f_91073_;
        int bottom = world.m_141937_();
        int top = world.m_151558_();
        scanBudget = SCAN_BUDGET;

        while (scanBudget > 0 && regionIdx < regions.size()) {
            Region r = regions.get(regionIdx);
            Vec3i size = r.container.getSize();
            while (scanBudget > 0) {
                BlockState schemState = r.container.get(bx, by, bz);
                boolean advanced = false;
                if (!schemState.m_60795_()) {
                    stats.schematicBlocks++;
                    BlockPos local = new BlockPos(bx, by, bz);
                    BlockPos worldPos = PositionUtils
                            .getTransformedPlacementPosition(local, r.placement, r.sub)
                            .m_121955_(r.regionTransformed)
                            .m_121955_(r.origin);
                    int y = worldPos.m_123342_();
                    if (y < bottom || y > top) {
                        // outside the build height: nothing a command could do about it
                    } else if (!world.m_46805_(worldPos)) {
                        stats.unloadedSkipped++;
                    } else {
                        BlockState target = transformState(schemState, r);
                        if (alreadyPlaced(target, world.m_8055_(worldPos))) {
                            stats.alreadyOk++;
                        } else {
                            long key = pack(worldPos.m_123341_(), y, worldPos.m_123343_());
                            missing.put(key, target);
                            if (r.beMap != null) {
                                CompoundTag be = r.beMap.get(local);
                                if (be != null) {
                                    missingNbt.put(key, be.m_6426_());
                                }
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
                scanBudget--;
                if (advanced) {
                    regionIdx++;
                    bx = by = bz = 0;
                    break;
                }
            }
        }

        if (regionIdx >= regions.size()) {
            stats.missingFound = missing.size();
            LitematicaMixinMod.LOGGER.info("[FASTBUILD] scan done: schematicBlocks=" + stats.schematicBlocks
                    + " alreadyOk=" + stats.alreadyOk + " missing=" + missing.size()
                    + " withNbt=" + missingNbt.size() + " unloaded=" + stats.unloadedSkipped);
            if (missing.isEmpty()) {
                finish(mc, "nothing to build - world already matches the blueprint");
                return;
            }
            beginDecompose();
        }
    }

    /**
     * The state litematica would paste at that spot: the raw container state, mirrored and rotated
     * by the placement and then by the sub region - same order {@code PositionUtils} uses for the
     * block position itself.
     */
    private static BlockState transformState(BlockState state, Region r) {
        BlockState out = state;
        try {
            Mirror pm = r.placement.getMirror();
            if (pm != null && pm != Mirror.NONE) {
                out = out.m_60715_(pm);
            }
            Rotation pr = r.placement.getRotation();
            if (pr != null && pr != Rotation.NONE) {
                out = out.m_60717_(pr);
            }
            Mirror sm = r.sub.getMirror();
            if (sm != null && sm != Mirror.NONE) {
                out = out.m_60715_(sm);
            }
            Rotation sr = r.sub.getRotation();
            if (sr != null && sr != Rotation.NONE) {
                out = out.m_60717_(sr);
            }
        } catch (Throwable ignored) {
            return state;
        }
        return out;
    }

    /**
     * True when the world block is already acceptable. Exact state equality is the strict answer;
     * a block the user mapped through the block replacement config also counts, matching what the
     * printer, the ESP and the auto builder accept.
     */
    private static boolean alreadyPlaced(BlockState target, BlockState worldState) {
        if (worldState == null) {
            return false;
        }
        if (worldState == target) {
            return true;
        }
        try {
            return BlockReplacer.clientMatchesReplacement(target, worldState);
        } catch (Throwable ignored) {
            return false;
        }
    }

    // ---------------------------------------------------------------- stage: decompose

    private static void beginDecompose() {
        byChunk.clear();
        for (Map.Entry<Long, BlockState> e : missing.entrySet()) {
            long key = e.getKey();
            byChunk.computeIfAbsent(chunkKeyOf(key), k -> new ArrayList<>()).add(key);
        }
        chunkOrder = new ArrayList<>(byChunk.keySet());
        chunkOrder.sort((a, b) -> {
            int ax = chunkX(a);
            int bxv = chunkX(b);
            if (ax != bxv) {
                return Integer.compare(ax, bxv);
            }
            return Integer.compare(chunkZ(a), chunkZ(b));
        });
        chunkIdx = 0;
        decomposeBudget = 0;
        fillQueue.clear();
        setQueue.clear();
        stage = Stage.DECOMPOSE;
    }

    private static void stepDecompose(Minecraft mc) {
        growthTop = mc.f_91073_.m_151558_();
        decomposeBudget = DECOMPOSE_BUDGET;
        while (decomposeBudget > 0 && chunkIdx < chunkOrder.size()) {
            long chunkKey = chunkOrder.get(chunkIdx);
            ArrayList<Long> positions = byChunk.remove(chunkKey);
            if (positions != null) {
                decomposeChunk(positions);
            }
            chunkIdx++;
            decomposeBudget--;
        }
        if (chunkIdx >= chunkOrder.size()) {
            long blocks = 0;
            for (Op o : fillQueue) {
                blocks += o.volume();
            }
            blocks += setQueue.size();
            LitematicaMixinMod.LOGGER.info("[FASTBUILD] round " + round + " planned: fills=" + fillQueue.size()
                    + " setblocks=" + setQueue.size() + " blocks=" + blocks + " maxBox=" + maxBox);
            settleTicks = 0;
            stage = Stage.DRAIN;
        }
    }

    /**
     * Greedy cuboid decomposition inside one chunk column: grow along +X, then +Z, then +Y, and cut
     * the box as soon as the state changes, the column ends or the per command block budget is hit.
     * Positions that carry block entity data never join a box - /fill would smear one tag over the
     * whole region - so they are emitted as individual /setblock commands.
     */
    private static void decomposeChunk(ArrayList<Long> positions) {
        HashMap<Long, BlockState> cells = new HashMap<>(positions.size() * 2);
        ArrayList<Long> nbtKeys = null;
        for (long key : positions) {
            CompoundTag tag = missingNbt.get(key);
            if (tag != null) {
                if (nbtKeys == null) {
                    nbtKeys = new ArrayList<>();
                }
                nbtKeys.add(key);
            } else {
                BlockState state = missing.get(key);
                if (state != null) {
                    cells.put(key, state);
                }
            }
        }
        if (nbtKeys != null) {
            for (long key : nbtKeys) {
                BlockState state = missing.get(key);
                CompoundTag tag = missingNbt.get(key);
                if (state != null && tag != null) {
                    setQueue.add(new Op(px(key), py(key), pz(key), px(key), py(key), pz(key), state, tag, false));
                }
            }
        }
        if (cells.isEmpty()) {
            return;
        }

        positions.sort((a, b) -> {
            int ay = py(a);
            int byy = py(b);
            if (ay != byy) {
                return Integer.compare(ay, byy);
            }
            int az = pz(a);
            int bz2 = pz(b);
            if (az != bz2) {
                return Integer.compare(az, bz2);
            }
            return Integer.compare(px(a), px(b));
        });

        HashSet<Long> used = new HashSet<>(positions.size() * 2);
        for (long key : positions) {
            if (used.contains(key)) {
                continue;
            }
            BlockState state = cells.get(key);
            if (state == null) {
                // a block entity position - already handled above
                continue;
            }
            int x = px(key);
            int y = py(key);
            int z = pz(key);
            used.add(key);

            int dx = 1;
            int maxDx = 16 - (x & 15);
            while (dx < maxDx && same(cells, used, x + dx, y, z, state)) {
                dx++;
            }

            int dz = 1;
            int maxDz = 16 - (z & 15);
            while (dz < maxDz && (long) dx * (dz + 1) <= maxBox && slabX(cells, used, x, dx, y, z + dz, state)) {
                dz++;
            }

            int dy = 1;
            while (y + dy <= growthTop && (long) dx * dz * (dy + 1) <= maxBox
                    && slabXZ(cells, used, x, dx, y + dy, z, dz, state)) {
                dy++;
            }

            fillQueue.add(new Op(x, y, z, x + dx - 1, y + dy - 1, z + dz - 1, state, null, true));
            for (int ox = 0; ox < dx; ox++) {
                for (int oz = 0; oz < dz; oz++) {
                    for (int oy = 0; oy < dy; oy++) {
                        used.add(pack(x + ox, y + oy, z + oz));
                    }
                }
            }
        }
    }

    /** Block growth ceiling, refreshed once per decompose tick. */
    private static int growthTop = 320;

    private static boolean same(HashMap<Long, BlockState> cells, HashSet<Long> used,
                                int x, int y, int z, BlockState state) {
        long key = pack(x, y, z);
        if (used.contains(key)) {
            return false;
        }
        BlockState other = cells.get(key);
        return other == state;
    }

    private static boolean slabX(HashMap<Long, BlockState> cells, HashSet<Long> used,
                                 int x, int dx, int y, int z, BlockState state) {
        for (int i = 0; i < dx; i++) {
            if (!same(cells, used, x + i, y, z, state)) {
                return false;
            }
        }
        return true;
    }

    private static boolean slabXZ(HashMap<Long, BlockState> cells, HashSet<Long> used,
                                  int x, int dx, int y, int z, int dz, BlockState state) {
        for (int i = 0; i < dx; i++) {
            for (int j = 0; j < dz; j++) {
                if (!same(cells, used, x + i, y, z + j, state)) {
                    return false;
                }
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- stage: drain

    private static void stepDrain(Minecraft mc) {
        boolean singleplayer = false;
        try {
            singleplayer = mc.m_91092_() != null;
        } catch (Throwable ignored) {
            singleplayer = false;
        }
        int mult = (singleplayer && LitematicaMixinMod.FAST_BUILD_SINGLEPLAYER_BOOST.getBooleanValue())
                ? SINGLEPLAYER_MULTIPLIER : 1;
        int fillBudget = LitematicaMixinMod.FAST_BUILD_FILL_PER_TICK.getIntegerValue() * mult;
        int setBudget = LitematicaMixinMod.FAST_BUILD_SET_PER_TICK.getIntegerValue() * mult;

        if (LitematicaMixinMod.FAST_BUILD_SAFE_MODE.getBooleanValue()) {
            // A non opped player is disconnected once the server's command counter passes 200;
            // it gains 20 per command and loses 1 per tick, so the only truly safe pace is one
            // command per 20 ticks. Ops are exempt from that counter, which is why this is off by
            // default - but servers that hand out level 2 through a permissions plugin rather than
            // the op list do exist.
            if (safeModeCooldown > 0) {
                safeModeCooldown--;
                fillBudget = 0;
                setBudget = 0;
            } else {
                fillBudget = Math.min(fillBudget, 1);
                setBudget = 0;
                safeModeCooldown = SAFE_MODE_INTERVAL;
            }
        }

        boolean sent = false;
        while (fillBudget > 0 && !fillQueue.isEmpty()) {
            Op op = fillQueue.poll();
            if (emit(mc, op)) {
                stats.fillsSent++;
                stats.blocksCovered += op.volume();
                sent = true;
            } else {
                stats.commandsSkipped++;
                if (abortIfRefused(mc)) {
                    return;
                }
            }
            fillBudget--;
        }
        while (setBudget > 0 && !setQueue.isEmpty()) {
            Op op = setQueue.poll();
            if (emit(mc, op)) {
                stats.setsSent++;
                stats.blocksCovered++;
                sent = true;
            } else {
                stats.commandsSkipped++;
                if (abortIfRefused(mc)) {
                    return;
                }
            }
            setBudget--;
        }

        if (fillQueue.isEmpty() && setQueue.isEmpty()) {
            stage = Stage.SETTLE;
            settleTicks = SETTLE_TICKS;
        } else if (!sent) {
            // nothing went out this tick - don't spin forever on a stuck queue
            stats.commandsSkipped++;
        }
    }

    /**
     * A refused send is a hard verdict: {@code CommandSender} refuses exactly when the client
     * command tree (which is the server's own tree) would reject the command, so retrying or
     * spraying more packets cannot help. Bail out instead of spinning on a stuck queue.
     */
    private static boolean abortIfRefused(Minecraft mc) {
        String reason = CommandSender.lastReject;
        if (reason == null) {
            return false;
        }
        stop(mc, false);
        aborted = true;
        abortReason = reason;
        say(mc, "aborted: " + reason, false);
        return true;
    }

    private static boolean emit(Minecraft mc, Op op) {
        String stateText;
        try {
            stateText = BlockStateParser.m_116769_(op.state);
        } catch (Throwable t) {
            return false;
        }
        if (stateText == null || stateText.isEmpty()) {
            return false;
        }
        if (op.fill) {
            return CommandSender.send(mc, CommandSender.fill(op.x1, op.y1, op.z1, op.x2, op.y2, op.z2, stateText));
        }
        String snbt = null;
        if (op.nbt != null && LitematicaMixinMod.FAST_BUILD_INCLUDE_NBT.getBooleanValue()) {
            snbt = snbtOf(op.nbt);
        }
        String command = CommandSender.setblock(op.x1, op.y1, op.z1, stateText, snbt);
        if (command.length() > CommandSender.SAFE_LIMIT && snbt != null) {
            // The command packet caps its string at 256 characters, so a large block entity tag
            // has to go: place the block, keep the shape, lose the configured contents.
            stats.nbtDropped++;
            snbt = null;
            command = CommandSender.setblock(op.x1, op.y1, op.z1, stateText, null);
        }
        if (command.length() > CommandSender.SAFE_LIMIT) {
            return false;
        }
        return CommandSender.send(mc, command);
    }

    /** SNBT for the block entity, with the absolute coordinates stripped. */
    private static String snbtOf(CompoundTag tag) {
        try {
            CompoundTag copy = tag.m_6426_();
            copy.m_128473_("x");
            copy.m_128473_("y");
            copy.m_128473_("z");
            return copy.toString();
        } catch (Throwable t) {
            return null;
        }
    }

    // ---------------------------------------------------------------- stage: verify

    private static void stepVerify(Minecraft mc) {
        Level world = mc.f_91073_;
        int stillMissing = 0;
        int cleared = 0;
        Iterator<Map.Entry<Long, BlockState>> it = missing.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, BlockState> entry = it.next();
            long key = entry.getKey();
            BlockPos pos = new BlockPos(px(key), py(key), pz(key));
            if (!world.m_46805_(pos)) {
                stillMissing++;
                continue;
            }
            if (alreadyPlaced(entry.getValue(), world.m_8055_(pos))) {
                it.remove();
                missingNbt.remove(key);
                cleared++;
            } else {
                stillMissing++;
            }
        }
        stats.remaining = stillMissing;
        LitematicaMixinMod.LOGGER.info("[FASTBUILD] round " + round + " verified: cleared=" + cleared
                + " stillMissing=" + stillMissing + " " + stats.summary());

        if (stillMissing == 0) {
            finish(mc, "done - " + stats.summary());
            return;
        }

        int maxRounds = LitematicaMixinMod.FAST_BUILD_RETRIES.getIntegerValue();
        if (round >= maxRounds) {
            finish(mc, "stopped after " + round + " rounds, " + stillMissing
                    + " block(s) still missing (" + stats.summary() + ")");
            return;
        }
        round++;
        stats.rounds = round;
        // Something was refused - the usual cause is the server's commandModificationBlockLimit
        // gamerule being lower than our per command cap, so halve the box budget and try again.
        if (maxBox > 256) {
            maxBox = Math.max(256, maxBox / 2);
            LitematicaMixinMod.LOGGER.info("[FASTBUILD] shrinking per command block budget to " + maxBox);
        }
        beginDecompose();
    }

    private static void finish(Minecraft mc, String text) {
        stats.remaining = missing.size();
        say(mc, text, false);
        LitematicaMixinMod.LOGGER.info("[FASTBUILD] finished: " + text);
        stage = Stage.FINISHED;
        regions.clear();
        missing.clear();
        missingNbt.clear();
        byChunk.clear();
        chunkOrder.clear();
        fillQueue.clear();
        setQueue.clear();
        boundLevel = null;
        stage = Stage.IDLE;
    }

    // ---------------------------------------------------------------- helpers

    private static void say(Minecraft mc, String text, boolean actionBar) {
        if (mc == null || mc.f_91074_ == null) {
            return;
        }
        try {
            mc.f_91074_.m_5661_(Component.m_237113_("[FastBuild] " + text), actionBar);
        } catch (Throwable ignored) {
        }
    }

    /** Vanilla BlockPos.asLong layout, unpacked with sign extension round trips. */
    public static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (long) (y & 0xFFF);
    }

    public static int px(long key) {
        return (int) (key >> 38) << 6 >> 6;
    }

    public static int py(long key) {
        return (int) (key & 0xFFF) << 20 >> 20;
    }

    public static int pz(long key) {
        return (int) ((key >> 12) & 0x3FFFFFF) << 6 >> 6;
    }

    private static long chunkKeyOf(long packed) {
        return chunkKey(px(packed) >> 4, pz(packed) >> 4) ;
    }

    private static long chunkKey(int cx, int cz) {
        return ((long) cx & 0xFFFFFFFFL) | (((long) cz & 0xFFFFFFFFL) << 32);
    }

    private static int chunkX(long key) {
        return (int) (key & 0xFFFFFFFFL);
    }

    private static int chunkZ(long key) {
        return (int) ((key >>> 32) & 0xFFFFFFFFL);
    }
}
