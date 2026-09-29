package xyz.jxmm.litematica_printer_forge.autobuild.scaffold;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.jxmm.litematica_printer_forge.autobuild.planner.AutoBuildPlanner;
import xyz.jxmm.litematica_printer_forge.autobuild.supplier.MaterialBroker;
import xyz.jxmm.litematica_printer_forge.utils.FakeAccurateBlockPlacement;
import xyz.jxmm.litematica_printer_forge.utils.InventoryUtils;
import xyz.jxmm.litematica_printer_forge.utils.Printer;

/**
 * Scaffold manager (FR-16..FR-21).
 *
 * Built-in candidate materials, no config (FR-24): minecraft:scaffolding,
 * then minecraft:dirt, then minecraft:cobblestone. The first item the player
 * actually carries is used; columns may mix materials (each placed block is
 * recorded with its real item).
 *
 * Placement primitive is self-managed (FR-19): look-down jump-place pillars,
 * driven by this class, never routed through the printer main loop and never
 * counted in print statistics.
 *
 * Every block we place is recorded (pos + item + timestamp + placement name)
 * and persisted per-dimension to <gameDir>/autobuild_scaffolds_<dim>.json
 * (FR-17) so markers survive restarts and relogins.
 *
 * Legality (FR-18): a scaffold cell may never overlap a non-air schematic
 * position at or below the current working layer (AutoBuildPlanner query);
 * conflict cells discovered during printing are broken before the printer
 * needs them.
 *
 * Teardown (FR-20/21): top-down breaking with the shared Breaker (continuous
 * dig progress), the player walks to broken positions to collect drops; cells
 * that already contain the correct schematic block are released, foreign
 * residues are reported, never silently dropped. On completion the marker set
 * is cleared and the JSON file deleted.
 */
public final class ScaffoldManager {
    private static final String TAG = "[AUTOBUILD] ";

    /** Built-in candidate material ids, in priority order (FR-16, FR-24). */
    private static final String[] CANDIDATE_IDS = {
            "minecraft:scaffolding", "minecraft:dirt", "minecraft:cobblestone"
    };

    private static final int PLACE_VERIFY_TICKS = 3;
    private static final int MAX_PLACE_RETRIES = 24;
    private static final int SETTLE_MAX_TICKS = 30;
    private static final int MAINTENANCE_INTERVAL = 10;
    private static final int SAVE_DEBOUNCE_TICKS = 40;

    public static final class MarkedBlock {
        public final BlockPos pos;
        public final String itemId;
        public final long ts;
        public final String placement;

        MarkedBlock(BlockPos pos, String itemId, long ts, String placement) {
            this.pos = pos;
            this.itemId = itemId;
            this.ts = ts;
            this.placement = placement;
        }
    }

    // ===== Marker store =====
    private static final Map<Long, MarkedBlock> marked = new LinkedHashMap<>();
    private static String loadedDimKey = null;
    private static boolean dirty = false;
    private static int saveCountdown = 0;
    private static final List<BlockPos> residues = new ArrayList<>();

    // ===== Pillar job state machine =====
    private static boolean jobActive = false;
    private static boolean jobFailed = false;
    private static BlockPos pillarTarget = null;   // schematic block we want to reach
    private static int pillarLayerY = Integer.MIN_VALUE;
    private static String pillarPlacement = "";
    private static int phase = 0;                  // 0 idle,1 jump-wait,2 place,3 settle
    private static int phaseTicks = 0;
    private static int placeRetries = 0;
    private static Item activeItem = null;
    private static BlockPos pendingPlacePos = null;

    // ===== Teardown state =====
    private static boolean teardownActive = false;
    private static List<MarkedBlock> teardownQueue = null;
    private static BlockPos pickupTarget = null;
    private static int pickupTicks = 0;

    private ScaffoldManager() {
    }

    // ===== Public queries =====

    public static boolean isMarked(BlockPos pos) {
        return marked.containsKey(AutoBuildPlanner.packPos(pos));
    }

    public static int getMarkedCount() {
        return marked.size();
    }

    /** Snapshot of marked positions for the ESP renderer. */
    public static List<BlockPos> getMarkedPositions() {
        List<BlockPos> out = new ArrayList<>(marked.size());
        for (MarkedBlock mb : marked.values()) {
            out.add(mb.pos);
        }
        return out;
    }

    public static boolean isJobActive() {
        return jobActive;
    }

    public static boolean isJobFailed() {
        return jobFailed;
    }

    public static boolean isTeardownActive() {
        return teardownActive;
    }

    // ===== Lifecycle (called every client tick from LitematicaMixinMod) =====

    private static boolean wasInWorld = false;

    public static void onClientTick(Minecraft mc) {
        boolean inWorld = mc.f_91074_ != null && mc.f_91073_ != null;
        if (inWorld && !wasInWorld) {
            loadForDimension(mc);
        } else if (!inWorld && wasInWorld) {
            saveIfNeeded(mc, true);
            loadedDimKey = null;
            cancelJob();
            teardownActive = false;
        }
        wasInWorld = inWorld;
        if (!inWorld) return;

        // Debounced save.
        if (dirty) {
            if (saveCountdown > 0) {
                saveCountdown--;
            } else {
                saveIfNeeded(mc, false);
            }
        }

        // Maintenance: release markers whose cell became air (broken by anyone).
        if (!marked.isEmpty()) {
            maintenanceTick(mc);
        }
    }

    private static void maintenanceTick(Minecraft mc) {
        Iterator<Map.Entry<Long, MarkedBlock>> it = marked.entrySet().iterator();
        boolean changed = false;
        while (it.hasNext()) {
            MarkedBlock mb = it.next().getValue();
            if (mc.f_91073_.m_8055_(mb.pos).m_60795_()) {
                it.remove();
                changed = true;
                System.out.println(TAG + "Scaffold at [" + mb.pos.m_123341_() + "," + mb.pos.m_123342_()
                        + "," + mb.pos.m_123343_() + "] is gone, marker released");
            }
        }
        if (changed) {
            dirty = true;
            saveCountdown = SAVE_DEBOUNCE_TICKS;
        }
    }

    // ===== Pillar requests (called by the director) =====

    /**
     * Requests a pillar that raises the player so the schematic block at
     * target ends up inside the printer range (standing directly below it).
     * Returns false immediately when no scaffold is needed or material is
     * unavailable.
     */
    public static boolean requestPillarTo(Minecraft mc, BlockPos target, int layerY, String placementName) {
        LocalPlayer p = mc.f_91074_;
        if (p == null) return false;
        int feetY = p.m_20183_().m_123342_();
        int needed = (target.m_123342_() - 1) - feetY;
        if (needed <= 0) {
            return false;
        }
        if (chooseScaffoldItem(mc) == null) {
            System.out.println(TAG + "No scaffold material (scaffolding/dirt/cobblestone) in inventory");
            return false;
        }
        jobActive = true;
        jobFailed = false;
        pillarTarget = target;
        pillarLayerY = layerY;
        pillarPlacement = placementName == null ? "" : placementName;
        phase = 0;
        phaseTicks = 0;
        placeRetries = 0;
        pendingPlacePos = null;
        System.out.println(TAG + "Scaffold pillar requested to reach [" + target.m_123341_() + ","
                + target.m_123342_() + "," + target.m_123343_() + "], need " + needed + " block(s)");
        return true;
    }

    /** Aborts any active pillar job (does not touch the marker set). */
    public static void cancelJob() {
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91066_ != null) {
            mc.f_91066_.f_92089_.m_7249_(false);
        }
        jobActive = false;
        jobFailed = false;
        pillarTarget = null;
        phase = 0;
        phaseTicks = 0;
        pendingPlacePos = null;
    }

    /**
     * Drives the pillar state machine. Called from the director's SCAFFOLDING
     * state with the printer suppressed. jobFailed signals the caller to pause.
     */
    public static void tick(Minecraft mc) {
        if (!jobActive) return;
        LocalPlayer p = mc.f_91074_;
        if (p == null || mc.f_91073_ == null) {
            cancelJob();
            return;
        }

        // Done when the target block is directly above our feet (within range).
        int feetY = p.m_20183_().m_123342_();
        if (pillarTarget != null && feetY >= pillarTarget.m_123342_() - 1) {
            System.out.println(TAG + "Scaffold pillar done, feet at Y=" + feetY);
            cancelJob();
            return;
        }

        phaseTicks++;
        switch (phase) {
            case 0: { // ensure item in hand, then start a jump
                if (chooseScaffoldItem(mc) == null) {
                    System.out.println(TAG + "Scaffold material exhausted mid-pillar");
                    jobFailed = true;
                    cancelJob();
                    return;
                }
                mc.f_91066_.f_92089_.m_7249_(true);
                phase = 1;
                phaseTicks = 0;
                break;
            }
            case 1: { // wait for airborne (jump pressed)
                if (!p.m_20096_()) {
                    mc.f_91066_.f_92089_.m_7249_(false);
                    phase = 2;
                    phaseTicks = 0;
                } else if (phaseTicks > 8) {
                    // stuck on ground (head blocked?) -> retry a couple of times
                    mc.f_91066_.f_92089_.m_7249_(false);
                    placeRetries++;
                    if (placeRetries > MAX_PLACE_RETRIES) {
                        System.out.println(TAG + "Scaffold pillar cannot jump, giving up at Y=" + feetY);
                        jobFailed = true;
                        cancelJob();
                        return;
                    }
                    phase = 0;
                }
                break;
            }
            case 2: { // airborne: place a block into the cell we just vacated
                BlockPos feet = p.m_20183_();
                if (!isLegalScaffoldCell(mc, feet)) {
                    System.out.println(TAG + "Scaffold cell [" + feet.m_123341_() + "," + feet.m_123342_()
                            + "," + feet.m_123343_() + "] overlaps unbuilt schematic, aborting pillar");
                    jobFailed = true;
                    cancelJob();
                    return;
                }
                if (placeScaffoldBlock(mc, feet, activeItem)) {
                    pendingPlacePos = feet;
                    phase = 3;
                    phaseTicks = 0;
                } else {
                    placeRetries++;
                    if (placeRetries > MAX_PLACE_RETRIES) {
                        System.out.println(TAG + "Scaffold placement kept failing, giving up at Y=" + feetY);
                        jobFailed = true;
                        cancelJob();
                        return;
                    }
                    phase = 0;
                }
                break;
            }
            case 3: { // verify placement, then settle back on the new block
                if (pendingPlacePos != null) {
                    BlockState st = mc.f_91073_.m_8055_(pendingPlacePos);
                    if (st.m_60734_() == blockOf(activeItem)) {
                        mark(pendingPlacePos, activeItem, pillarPlacement);
                        pendingPlacePos = null;
                        placeRetries = 0;
                        phase = 4;
                        phaseTicks = 0;
                        break;
                    }
                    if (phaseTicks > PLACE_VERIFY_TICKS) {
                        pendingPlacePos = null;
                        phase = 0; // retry the whole cycle
                        break;
                    }
                } else {
                    phase = 0;
                }
                break;
            }
            case 4: { // settle: wait until we stand on the new block
                if (p.m_20096_() || phaseTicks > SETTLE_MAX_TICKS) {
                    phase = 0;
                    phaseTicks = 0;
                }
                break;
            }
            default:
                phase = 0;
                break;
        }
    }

    // ===== Conflict removal during printing =====

    /**
     * Breaks a marked scaffold cell that the printer now needs (FR-18:
     * "if a later print needs a scaffold cell, break that scaffold first").
     * Returns true when a break was started or is in progress.
     */
    public static boolean breakMarkedAt(Minecraft mc, BlockPos pos) {
        if (!isMarked(pos)) return false;
        if (Printer.breaker.isBreakingBlock()) return true;
        Printer.breaker.startBreakingBlock(pos, mc);
        return true;
    }

    // ===== Teardown (FR-20/21, CLEANUP state) =====

    public static void beginTeardown() {
        if (marked.isEmpty()) {
            teardownActive = false;
            return;
        }
        residues.clear();
        teardownQueue = new ArrayList<>(marked.values());
        // Top-down so the player descends the column safely (FR-20).
        teardownQueue.sort(Comparator.comparingInt((MarkedBlock mb) -> mb.pos.m_123342_()).reversed());
        teardownActive = true;
        pickupTarget = null;
        pickupTicks = 0;
        System.out.println(TAG + "Teardown started, " + teardownQueue.size() + " scaffold block(s)");
    }

    /**
     * Drives teardown. Returns true when everything is dismantled, collected
     * and the marker file deleted.
     */
    public static boolean teardownTick(Minecraft mc) {
        if (!teardownActive) return true;
        LocalPlayer p = mc.f_91074_;
        if (p == null || mc.f_91073_ == null) {
            return false;
        }

        // Walk to the last broken position first so drops get picked up (FR-20).
        if (pickupTarget != null) {
            pickupTicks++;
            if (!xyz.jxmm.litematica_printer_forge.autobuild.bridge.BaritoneBridge.isPathing()) {
                xyz.jxmm.litematica_printer_forge.autobuild.bridge.BaritoneBridge.goTo(pickupTarget);
            }
            if (xyz.jxmm.litematica_printer_forge.autobuild.bridge.BaritoneBridge.isArrived(pickupTarget, 1)
                    || pickupTicks > 200) {
                xyz.jxmm.litematica_printer_forge.autobuild.bridge.BaritoneBridge.cancel();
                pickupTarget = null;
                pickupTicks = 0;
            }
            return false;
        }

        if (teardownQueue == null || teardownQueue.isEmpty()) {
            finishTeardown(mc);
            return true;
        }

        // Shared breaker is busy on its current target -> let it finish.
        if (Printer.breaker.isBreakingBlock()) {
            return false;
        }

        MarkedBlock mb = teardownQueue.get(0);
        BlockState world = mc.f_91073_.m_8055_(mb.pos);
        Block expected = blockOf(itemOf(mb.itemId));
        if (expected == null || world.m_60795_()) {
            // Already gone (maintenance normally removed it, be safe).
            teardownQueue.remove(0);
            return false;
        }
        if (world.m_60734_() != expected) {
            // Cell now holds a foreign block. If it matches the schematic, keep
            // it (FR-21); otherwise report it as residue, never silently drop.
            if (AutoBuildPlanner.isSchematicBlockAtOrBelow(mb.pos, Integer.MAX_VALUE)) {
                System.out.println(TAG + "Scaffold cell now holds the schematic block, keeping it");
            } else {
                residues.add(mb.pos);
                System.out.println(TAG + "Residue at [" + mb.pos.m_123341_() + "," + mb.pos.m_123342_()
                        + "," + mb.pos.m_123343_() + "]: " + world.m_60734_());
            }
            marked.remove(AutoBuildPlanner.packPos(mb.pos));
            dirty = true;
            saveCountdown = SAVE_DEBOUNCE_TICKS;
            teardownQueue.remove(0);
            return false;
        }
        // Our block: break it (Breaker keeps dig progress across ticks).
        if (world.m_60800_(mc.f_91073_, mb.pos) == 0.0f
                || world.m_60625_(p, mc.f_91073_, mb.pos) >= 1.0f) {
            mc.f_91072_.m_105269_(mb.pos, Direction.UP);
        } else {
            Printer.breaker.startBreakingBlock(mb.pos, mc);
        }
        teardownQueue.remove(0);
        marked.remove(AutoBuildPlanner.packPos(mb.pos));
        dirty = true;
        saveCountdown = SAVE_DEBOUNCE_TICKS;
        pickupTarget = mb.pos;
        pickupTicks = 0;
        return false;
    }

    private static void finishTeardown(Minecraft mc) {
        teardownActive = false;
        marked.clear();
        deleteMarkerFile(mc);
        if (!residues.isEmpty()) {
            StringBuilder sb = new StringBuilder("[AUTOBUILD] 残留无法拆除: ");
            for (int i = 0; i < Math.min(residues.size(), 8); i++) {
                BlockPos rp = residues.get(i);
                if (i > 0) sb.append(", ");
                sb.append("[").append(rp.m_123341_()).append(",").append(rp.m_123342_())
                        .append(",").append(rp.m_123343_()).append("]");
            }
            if (residues.size() > 8) sb.append(" ...(+").append(residues.size() - 8).append(")");
            if (mc.f_91074_ != null) {
                mc.f_91074_.m_5661_(net.minecraft.network.chat.Component.m_237113_(sb.toString()), false);
            }
        }
        System.out.println(TAG + "Teardown complete, marker file deleted, residues=" + residues.size());
    }

    // ===== Internals =====

    private static boolean isLegalScaffoldCell(Minecraft mc, BlockPos pos) {
        // FR-18: never inside an unbuilt schematic cell at or below the layer.
        return !AutoBuildPlanner.isSchematicBlockAtOrBelow(pos, pillarLayerY);
    }

    private static boolean placeScaffoldBlock(Minecraft mc, BlockPos feetPos, Item item) {
        LocalPlayer p = mc.f_91074_;
        if (item == null || p == null) return false;
        BlockState existing = mc.f_91073_.m_8055_(feetPos);
        if (!existing.m_60795_() && !existing.m_247087_()) {
            return false;
        }
        ItemStack hand = p.m_21205_();
        if (hand.m_41720_() != item) {
            // (Re)select the scaffold item; placement continues next tick.
            return false;
        }
        // Support = the solid block we jumped off, right below the vacated cell.
        BlockPos support = new BlockPos(feetPos.m_123341_(), feetPos.m_123342_() - 1, feetPos.m_123343_());
        Vec3 hitVec = new Vec3(support.m_123341_() + 0.5, support.m_123342_() + 1.0, support.m_123343_() + 0.5);
        BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, support, false);
        // Human-like: tell the server we are looking straight down (pillaring).
        FakeAccurateBlockPlacement.fakeYaw = p.m_146908_();
        FakeAccurateBlockPlacement.fakePitch = 90.0f;
        FakeAccurateBlockPlacement.sendLookPacket(mc.m_91403_(), p);
        try {
            mc.f_91072_.m_233732_(p, InteractionHand.MAIN_HAND, hit);
            InventoryUtils.decrementCount(p.m_150110_().f_35937_);
            return true;
        } catch (Throwable t) {
            System.out.println(TAG + "Scaffold place failed: " + t);
            return false;
        }
    }

    private static Item chooseScaffoldItem(Minecraft mc) {
        // Prefer what is already in the main hand among the candidates.
        ItemStack hand = mc.f_91074_.m_21205_();
        Item handItem = hand.m_41720_();
        for (String id : CANDIDATE_IDS) {
            Item it = itemOf(id);
            if (it != null && it == handItem) {
                activeItem = it;
                return it;
            }
        }
        for (String id : CANDIDATE_IDS) {
            Item it = itemOf(id);
            if (it == null) continue;
            if (MaterialBroker.countInInventory(mc, it) > 0) {
                if (InventoryUtils.getAvailableSlot(it) >= 0) {
                    InventoryUtils.swapToItem(mc, new ItemStack(it));
                }
                activeItem = it;
                return it;
            }
        }
        activeItem = null;
        return null;
    }

    private static Item itemOf(String id) {
        try {
            return ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        } catch (Throwable t) {
            return null;
        }
    }

    private static Block blockOf(Item item) {
        if (item instanceof BlockItem) {
            return ((BlockItem) item).m_40614_();
        }
        return null;
    }

    private static void mark(BlockPos pos, Item item, String placement) {
        marked.put(AutoBuildPlanner.packPos(pos),
                new MarkedBlock(pos, idOf(item), System.currentTimeMillis(), placement));
        dirty = true;
        saveCountdown = SAVE_DEBOUNCE_TICKS;
        System.out.println(TAG + "Scaffold placed at [" + pos.m_123341_() + "," + pos.m_123342_()
                + "," + pos.m_123343_() + "] (" + idOf(item) + "), marked=" + marked.size());
    }

    private static String idOf(Item item) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(item);
        return rl == null ? "minecraft:dirt" : rl.toString();
    }

    // ===== Persistence (FR-17) =====

    private static File markerFile(Minecraft mc) {
        String dim = "unknown";
        try {
            dim = mc.f_91073_.m_220362_().m_135782_().toString();
        } catch (Throwable ignored) {
        }
        return new File(mc.f_91069_, "autobuild_scaffolds_" + dim.replace(':', '_') + ".json");
    }

    private static void loadForDimension(Minecraft mc) {
        String dim;
        try {
            dim = mc.f_91073_.m_220362_().m_135782_().toString();
        } catch (Throwable t) {
            return;
        }
        if (dim.equals(loadedDimKey)) return;
        loadedDimKey = dim;
        marked.clear();
        dirty = false;
        File f = markerFile(mc);
        if (!f.exists()) {
            System.out.println(TAG + "No scaffold marker file for " + dim);
            return;
        }
        try {
            String json = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            com.google.gson.JsonObject root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            com.google.gson.JsonArray arr = root.getAsJsonArray("blocks");
            int n = 0;
            if (arr != null) {
                for (int i = 0; i < arr.size(); i++) {
                    com.google.gson.JsonObject o = arr.get(i).getAsJsonObject();
                    int x = o.get("x").getAsInt();
                    int y = o.get("y").getAsInt();
                    int z = o.get("z").getAsInt();
                    String item = o.has("item") ? o.get("item").getAsString() : "minecraft:dirt";
                    long ts = o.has("ts") ? o.get("ts").getAsLong() : 0L;
                    String pl = o.has("placement") ? o.get("placement").getAsString() : "";
                    marked.put(AutoBuildPlanner.packPos(new BlockPos(x, y, z)),
                            new MarkedBlock(new BlockPos(x, y, z), item, ts, pl));
                    n++;
                }
            }
            System.out.println(TAG + "Loaded " + n + " scaffold marker(s) from " + f.getName());
        } catch (Throwable t) {
            System.out.println(TAG + "Failed to load scaffold markers: " + t);
        }
    }

    private static void saveIfNeeded(Minecraft mc, boolean force) {
        if (!dirty && !force) return;
        if (force && marked.isEmpty()) {
            deleteMarkerFile(mc);
            dirty = false;
            return;
        }
        File f = markerFile(mc);
        try {
            com.google.gson.JsonObject root = new com.google.gson.JsonObject();
            String dim;
            try {
                dim = mc.f_91073_.m_220362_().m_135782_().toString();
            } catch (Throwable t) {
                dim = loadedDimKey == null ? "unknown" : loadedDimKey;
            }
            root.addProperty("dimension", dim);
            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
            for (MarkedBlock mb : marked.values()) {
                com.google.gson.JsonObject o = new com.google.gson.JsonObject();
                o.addProperty("x", mb.pos.m_123341_());
                o.addProperty("y", mb.pos.m_123342_());
                o.addProperty("z", mb.pos.m_123343_());
                o.addProperty("item", mb.itemId);
                o.addProperty("ts", mb.ts);
                o.addProperty("placement", mb.placement);
                arr.add(o);
            }
            root.add("blocks", arr);
            Files.write(f.toPath(), root.toString().getBytes(StandardCharsets.UTF_8));
            dirty = false;
        } catch (IOException e) {
            System.out.println(TAG + "Failed to save scaffold markers: " + e);
        }
    }

    private static void deleteMarkerFile(Minecraft mc) {
        try {
            File f = markerFile(mc);
            if (f.exists() && f.delete()) {
                System.out.println(TAG + "Deleted " + f.getName());
            }
        } catch (Throwable t) {
            System.out.println(TAG + "Failed to delete marker file: " + t);
        }
    }
}
