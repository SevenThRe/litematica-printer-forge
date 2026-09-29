package xyz.jxmm.litematica_printer_forge.autobuild.director;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.autobuild.bridge.BaritoneBridge;
import xyz.jxmm.litematica_printer_forge.autobuild.planner.AutoBuildPlanner;
import xyz.jxmm.litematica_printer_forge.autobuild.scaffold.ScaffoldManager;
import xyz.jxmm.litematica_printer_forge.autobuild.supplier.MaterialBroker;

/**
 * Auto Build Director state machine v1.
 *
 * Main loop: PLANNING -> NAVIGATING -> PRINTING -> PLANNING ...
 * Missing materials or repeated failures drop to PAUSED.
 *
 * While RUNNING the director owns PRINTER_OFF and Baritone settings;
 * manual V key presses are overwritten on the next tick. On stop both
 * are restored to their pre-start values.
 */
public final class AutoBuildDirector {
    public enum State {
        IDLE, PLANNING, NAVIGATING, PRINTING, SUPPLYING, SCAFFOLDING, CLEANUP, PAUSED
    }

    /** Why the director is currently paused. NONE while running. */
    public enum PauseReason {
        NONE, MISSING, LOW_HEALTH, UNREACHABLE, STUCK, DEATH, DISCONNECT, EXTERNAL_GUI, MENU_TIMEOUT
    }

    private static final String TAG = "[AUTOBUILD] ";
    private static final int PRINT_STUCK_THRESHOLD = 100;
    private static final int HUD_INTERVAL = 20;
    /** Max times we may bounce (un-skip -> re-skip identical set) before pausing. */
    private static final int MAX_SKIP_STALL = 3;

    private static boolean running = false;
    private static boolean userPrinterOff = false;
    private static State currentState = State.IDLE;
    private static PauseReason pauseReason = PauseReason.NONE;

    /** Packed positions (AutoBuildPlanner.packPos) of blocks skipped for missing material. */
    private static final Set<Long> skipSet = new HashSet<>();
    private static int skipStall = 0;
    private static int lastSkipSize = 0;

    /** Transient yield while a foreign GUI is open (not one opened by the supply chain). */
    private static boolean guiYield = false;
    private static State yieldReturnState = null;

    private static int currentLayerY = Integer.MIN_VALUE;
    private static BlockPos targetStand = null;
    private static List<BlockPos> targetBlocks = Collections.emptyList();
    private static List<AutoBuildPlanner.StandCluster> currentClusters = Collections.emptyList();
    private static int clusterIdx = 0;

    private static int navigateTicks = 0;
    private static int arriveTolerance = 1;
    private static int printStuckTicks = 0;
    private static int unreachableRetries = 0;
    private static int lastMissingCount = -1;
    private static int lastMissingVersion = -1;

    private static int hudTick = 0;
    private static String hudLine = "";

    private AutoBuildDirector() {
    }

    public static State getState() {
        return currentState;
    }

    public static boolean isRunning() {
        return running;
    }

    public static void toggle() {
        if (running) {
            stop();
        } else {
            BuildCommand.startBuildFlow(Minecraft.m_91087_());
        }
    }

    public static void pauseOrResume() {
        if (!running) return;
        if (currentState == State.PAUSED) {
            resumeFromPause();
        } else {
            setState(State.PAUSED);
            pauseReason = PauseReason.NONE;
            LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
            BaritoneBridge.cancel();
            System.out.println(TAG + "Paused by user");
        }
    }

    public static void markTerminal() {
        xyz.jxmm.litematica_printer_forge.autobuild.supplier.TerminalLocator.markLookedAt(
                net.minecraft.client.Minecraft.m_91087_());
    }

    public static void tick(Minecraft mc) {
        if (!running) return;
        if (mc.f_91074_ == null) {
            // Player entity gone (quit / world switch): cannot hold state, stop cleanly.
            stop();
            return;
        }
        if (mc.f_91073_ == null) {
            pauseWithReason(PauseReason.DISCONNECT, "[AUTOBUILD] 与服务器断开，暂停");
            return;
        }

        if (checkDeath(mc)) return;
        if (checkLowHealth(mc)) return;
        if (checkExternalGui(mc)) return;

        xyz.jxmm.litematica_printer_forge.autobuild.supplier.TerminalLocator.autoRecordTick(mc);
        AutoBuildPlanner.tick(mc);

        switch (currentState) {
            case PLANNING:
                tickPlanning(mc);
                break;
            case NAVIGATING:
                tickNavigating(mc);
                break;
            case PRINTING:
                tickPrinting(mc);
                break;
            case SUPPLYING:
                tickSupplying(mc);
                break;
            case SCAFFOLDING:
                tickScaffolding(mc);
                break;
            case CLEANUP:
                tickCleanup(mc);
                break;
            case PAUSED:
                tickPaused(mc);
                break;
            default:
                break;
        }

        sendHud(mc);
    }

    // ===== Lifecycle =====

    /**
     * Starts the auto build director targeting a specific schematic placement.
     * Called by BuildCommand after the user picks one from the chat list,
     * or directly when only one enabled placement exists.
     */
    public static void start(fi.dy.masa.litematica.schematic.placement.SchematicPlacement placement) {
        AutoBuildPlanner.setTargetPlacement(placement);
        start();
    }

    private static void start() {
        running = true;
        userPrinterOff = LitematicaMixinMod.PRINTER_OFF.getBooleanValue();
        BaritoneBridge.snapshotSettings();
        BaritoneBridge.applyPathingSettings(
                LitematicaMixinMod.AUTO_BUILD_ALLOW_PATH_BREAK.getBooleanValue(),
                true,
                true,
                true
        );
        AutoBuildPlanner.invalidate();
        resetRunState();
        setState(State.PLANNING);
        System.out.println(TAG + "Auto build started");
    }

    private static void stop() {
        running = false;
        BaritoneBridge.cancel();
        BaritoneBridge.restoreSettings();
        ScaffoldManager.cancelJob();
        MaterialBroker.abort(Minecraft.m_91087_());
        LitematicaMixinMod.PRINTER_OFF.setBooleanValue(userPrinterOff);
        resetRunState();
        setState(State.IDLE);
        System.out.println(TAG + "Auto build stopped");
    }

    private static void resumeFromPause() {
        pauseReason = PauseReason.NONE;
        resetRunState();
        setState(State.PLANNING);
        System.out.println(TAG + "Resumed");
    }

    public static PauseReason getPauseReason() {
        return pauseReason;
    }

    /** Pause with a recorded reason and (optionally) a chat message to the player.
     *  The chat message and log line are emitted only on the transition, not every tick. */
    private static void pauseWithReason(PauseReason reason, String chat) {
        boolean first = currentState != State.PAUSED || pauseReason != reason;
        pauseReason = reason;
        setState(State.PAUSED);
        LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
        BaritoneBridge.cancel();
        if (first && chat != null && !chat.isEmpty() && Minecraft.m_91087_().f_91074_ != null) {
            Minecraft.m_91087_().f_91074_.m_5661_(Component.m_237113_(chat), false);
        }
        if (first) {
            System.out.println(TAG + "Paused (" + reason + ")" + (chat != null ? ": " + chat : ""));
        }
    }

    private static void resetRunState() {
        currentLayerY = Integer.MIN_VALUE;
        targetStand = null;
        targetBlocks = Collections.emptyList();
        currentClusters = Collections.emptyList();
        clusterIdx = 0;
        navigateTicks = 0;
        printStuckTicks = 0;
        unreachableRetries = 0;
        lastMissingCount = -1;
        lastMissingVersion = -1;
        skipSet.clear();
        skipStall = 0;
        lastSkipSize = 0;
        guiYield = false;
        yieldReturnState = null;
        pauseReason = PauseReason.NONE;
    }

    private static void setState(State next) {
        if (currentState != next) {
            currentState = next;
            System.out.println(TAG + "State -> " + next);
        }
    }

    // ===== State ticks =====

    private static void tickPlanning(Minecraft mc) {
        if (!AutoBuildPlanner.isScanComplete()) {
            hudLine = "Planning...";
            return;
        }

        Integer lowestY = AutoBuildPlanner.getLowestMissingY(skipSet);
        if (lowestY == null) {
            // No non-skipped missing blocks in loaded chunks.
            if (!skipSet.isEmpty()) {
                // We previously skipped some blocks for lack of material.
                // Re-attempt the full restock chain for them now: the player
                // (or SB/RS) may have restocked. If they still cannot be sourced
                // we re-skip and (after repeated stalls) finally pause.
                lastSkipSize = skipSet.size();
                skipSet.clear();
                Integer reY = AutoBuildPlanner.getLowestMissingY(skipSet);
                if (reY == null) {
                    if (ScaffoldManager.getMarkedCount() > 0) {
                        beginCleanup();
                        return;
                    }
                    System.out.println(TAG + "All schematic placements complete (skipped recovered)");
                    stop();
                    return;
                }
                lowestY = reY;
            } else {
                // Nothing skipped either. Force-load unbuilt chunks by walking,
                // or finish if everything is truly done.
                Integer unloadedY = AutoBuildPlanner.getLowestUnloadedY();
                if (unloadedY == null) {
                    if (ScaffoldManager.getMarkedCount() > 0) {
                        beginCleanup();
                        return;
                    }
                    System.out.println(TAG + "All schematic placements complete");
                    stop();
                    return;
                }
                for (AutoBuildPlanner.MissingEntry e : AutoBuildPlanner.getMissing()) {
                    if (e.unloaded) {
                        currentLayerY = unloadedY;
                        targetStand = e.pos;
                        targetBlocks = Collections.emptyList();
                        arriveTolerance = 12;
                        BaritoneBridge.goTo(e.pos);
                        navigateTicks = 0;
                        setState(State.NAVIGATING);
                        System.out.println(TAG + "Exploring towards unloaded area [" + e.pos.m_123341_() + "," + e.pos.m_123342_() + "," + e.pos.m_123343_() + "]");
                        return;
                    }
                }
                return;
            }
        }

        currentLayerY = lowestY;

        // Restock check before walking anywhere.
        Map<Item, Integer> needs = AutoBuildPlanner.getMaterialNeeds(currentLayerY, skipSet);
        if (MaterialBroker.hasShortage(mc, needs)) {
            BaritoneBridge.cancel();
            MaterialBroker.begin(mc, needs);
            setState(State.SUPPLYING);
            return;
        }

        AutoBuildPlanner.StandPlan plan = AutoBuildPlanner.computeStandClusters(mc, currentLayerY, skipSet);
        currentClusters = plan.clusters;
        clusterIdx = 0;

        if (currentClusters.isEmpty()) {
            if (LitematicaMixinMod.AUTO_BUILD_ALLOW_SCAFFOLD.getBooleanValue()
                    && !plan.uncovered.isEmpty()) {
                // No reachable stand for this layer: pillar up to the lowest
                // uncovered block so it enters the printer range (FR-16).
                BlockPos low = lowestUncovered(plan.uncovered);
                if (low != null && ScaffoldManager.requestPillarTo(mc, low, currentLayerY, "")) {
                    LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
                    setState(State.SCAFFOLDING);
                    return;
                }
                if (low != null) {
                    pauseWithReason(PauseReason.UNREACHABLE,
                            "[AUTOBUILD] 无可用站位且脚手架材料不足，暂停");
                    return;
                }
            }
            targetStand = null;
            targetBlocks = plan.uncovered;
            setState(State.PRINTING);
            printStuckTicks = 0;
            return;
        }

        AutoBuildPlanner.StandCluster best = currentClusters.get(0);
        targetStand = best.stand;
        targetBlocks = best.blocks;
        arriveTolerance = 1;
        BaritoneBridge.goTo(targetStand);
        navigateTicks = 0;
        setState(State.NAVIGATING);
        System.out.println(TAG + "Navigating to [" + targetStand.m_123341_() + "," + targetStand.m_123342_() + "," + targetStand.m_123343_()
                + "] layerY=" + currentLayerY + " blocks=" + targetBlocks.size());
    }

    private static void tickNavigating(Minecraft mc) {
        boolean allowPrintWhileMoving = LitematicaMixinMod.AUTO_BUILD_PRINT_WHILE_MOVING.getBooleanValue();
        LitematicaMixinMod.PRINTER_OFF.setBooleanValue(!allowPrintWhileMoving);

        navigateTicks++;

        boolean arrived = targetStand != null && BaritoneBridge.isArrived(targetStand, arriveTolerance);
        if (arrived) {
            System.out.println(TAG + "Arrived at stand [" + targetStand.m_123341_() + "," + targetStand.m_123342_() + "," + targetStand.m_123343_() + "]");
            BaritoneBridge.cancel();
            LitematicaMixinMod.PRINTER_OFF.setBooleanValue(false);
            printStuckTicks = 0;
            setState(targetBlocks.isEmpty() ? State.PLANNING : State.PRINTING);
            return;
        }

        int timeout = LitematicaMixinMod.AUTO_BUILD_ARRIVE_TIMEOUT.getIntegerValue();
        if (navigateTicks > timeout) {
            BaritoneBridge.cancel();
            unreachableRetries++;
            int maxRetries = LitematicaMixinMod.AUTO_BUILD_UNREACHABLE_RETRIES.getIntegerValue();
            System.out.println(TAG + "Arrival timeout at [" + targetStand.m_123341_() + "," + targetStand.m_123342_() + "," + targetStand.m_123343_()
                    + "] retry=" + unreachableRetries + "/" + maxRetries);

            if (unreachableRetries > maxRetries) {
                pauseWithReason(PauseReason.UNREACHABLE,
                        "[AUTOBUILD] 目标不可达超过重试上限，暂停");
                return;
            }

            clusterIdx++;
            if (clusterIdx < currentClusters.size()) {
                AutoBuildPlanner.StandCluster next = currentClusters.get(clusterIdx);
                targetStand = next.stand;
                targetBlocks = next.blocks;
                arriveTolerance = 1;
                BaritoneBridge.goTo(targetStand);
                navigateTicks = 0;
            } else {
                targetStand = null;
                setState(State.PRINTING);
                printStuckTicks = 0;
            }
        }

        hudLine = "Navigating Y=" + currentLayerY + " d=" + navigateTicks;
    }

    private static void tickSupplying(Minecraft mc) {
        LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
        hudLine = MaterialBroker.getStatusLine();

        if (!MaterialBroker.tick(mc)) {
            return;
        }

        Map<Item, Integer> residual = MaterialBroker.getResidual();
        if (residual.isEmpty()) {
            skipStall = 0;
            setState(State.PLANNING);
            return;
        }

        // Describe what is still missing for the player.
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Item, Integer> e : residual.entrySet()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(new net.minecraft.world.item.ItemStack(e.getKey()).m_41786_().getString())
                    .append(" x").append(e.getValue());
        }

        if (!LitematicaMixinMod.AUTO_BUILD_SKIP_MISSING.getBooleanValue()) {
            pauseWithReason(PauseReason.MISSING,
                    "[AUTOBUILD] 缺料暂停: " + sb + " (resume 补料后继续 / stop 终止)");
            return;
        }

        // Skip-missing mode: record the affected blocks and continue with the rest.
        int added = 0;
        for (AutoBuildPlanner.MissingEntry e : AutoBuildPlanner.getMissing()) {
            if (residual.containsKey(e.item) && skipSet.add(AutoBuildPlanner.packPos(e.pos))) {
                added++;
            }
        }
        // Stall guard: if we keep re-skipping the same set with no progress, pause
        // rather than looping forever (materials genuinely unavailable).
        if (lastSkipSize > 0 && skipSet.size() >= lastSkipSize) {
            skipStall++;
        } else {
            skipStall = 0;
        }
        lastSkipSize = skipSet.size();
        if (skipStall > MAX_SKIP_STALL) {
            pauseWithReason(PauseReason.MISSING,
                    "[AUTOBUILD] 缺料跳过多次无进展，暂停: " + sb);
            return;
        }
        System.out.println(TAG + "Skipped " + added + " block(s) for missing materials: " + sb
                + " (skipSet=" + skipSet.size() + ", will retry)");
        setState(State.PLANNING);
    }

    private static void tickPrinting(Minecraft mc) {
        LitematicaMixinMod.PRINTER_OFF.setBooleanValue(false);

        if (!AutoBuildPlanner.isScanComplete()) {
            hudLine = "Scanning...";
            return;
        }

        if (AutoBuildPlanner.isLayerComplete(currentLayerY, skipSet)) {
            System.out.println(TAG + "Layer Y=" + currentLayerY + " complete");
            setState(State.PLANNING);
            return;
        }

        // FR-18: if the printer needs a cell we scaffolded, break that
        // scaffold first (the shared Breaker mutex pauses the printer loop
        // for the few ticks this takes).
        if (!targetBlocks.isEmpty()) {
            for (BlockPos tb : targetBlocks) {
                if (ScaffoldManager.isMarked(tb)) {
                    ScaffoldManager.breakMarkedAt(mc, tb);
                    break;
                }
            }
        }

        if (targetStand != null && !BaritoneBridge.isArrived(targetStand, 2)) {
            BaritoneBridge.goTo(targetStand);
            navigateTicks = 0;
            setState(State.NAVIGATING);
            return;
        }

        int currentMissing = AutoBuildPlanner.countLoadedAtOrBelow(currentLayerY, skipSet);
        int version = AutoBuildPlanner.getDataVersion();
        if (lastMissingVersion == version && currentMissing == lastMissingCount) {
            printStuckTicks++;
        } else {
            printStuckTicks = 0;
            lastMissingCount = currentMissing;
            lastMissingVersion = version;
        }

        if (printStuckTicks > PRINT_STUCK_THRESHOLD) {
            pauseWithReason(PauseReason.STUCK,
                    "[AUTOBUILD] 打印卡住（" + PRINT_STUCK_THRESHOLD + " tick 无进展），暂停");
            return;
        }

        hudLine = "Printing Y=" + currentLayerY + " left=" + currentMissing;
    }

    private static void tickPaused(Minecraft mc) {
        LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
        BaritoneBridge.cancel();
        hudLine = "PAUSED (" + pauseReason + ")";
    }

    private static void tickScaffolding(Minecraft mc) {
        LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
        ScaffoldManager.tick(mc);
        if (ScaffoldManager.isJobFailed()) {
            pauseWithReason(PauseReason.UNREACHABLE,
                    "[AUTOBUILD] 脚手架搭建失败，暂停");
            return;
        }
        if (!ScaffoldManager.isJobActive()) {
            // Pillar finished (or no longer needed) -> re-plan.
            setState(State.PLANNING);
            return;
        }
        hudLine = "Scaffolding, marked=" + ScaffoldManager.getMarkedCount();
    }

    private static void tickCleanup(Minecraft mc) {
        LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
        if (ScaffoldManager.teardownTick(mc)) {
            System.out.println(TAG + "Cleanup finished");
            stop();
            return;
        }
        hudLine = "Cleanup, left=" + ScaffoldManager.getMarkedCount();
    }

    private static void beginCleanup() {
        System.out.println(TAG + "Entering cleanup, scaffolds=" + ScaffoldManager.getMarkedCount());
        ScaffoldManager.beginTeardown();
        LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
        setState(State.CLEANUP);
    }

    private static BlockPos lowestUncovered(List<BlockPos> list) {
        BlockPos best = null;
        for (BlockPos p : list) {
            if (best == null || p.m_123342_() < best.m_123342_()) best = p;
        }
        return best;
    }

    // ===== Safety =====

    private static boolean checkDeath(Minecraft mc) {
        if (mc.f_91074_.m_21223_() <= 0.0F) {
            pauseWithReason(PauseReason.DEATH,
                    "[AUTOBUILD] 玩家死亡，暂停（重生后按恢复热键继续）");
            return true;
        }
        return false;
    }

    /**
     * Temporarily yield while a screen we did not open is in front (FR-22).
     * The supply chain is exempt: it opens SB/RS menus itself while SUPPLYING.
     * When the GUI closes, the previous state is restored (navigation re-issued).
     */
    private static boolean checkExternalGui(Minecraft mc) {
        if (currentState == State.SUPPLYING) return false;
        if (currentState == State.PAUSED && !guiYield) return false;

        if (mc.f_91080_ == null) {
            if (guiYield) {
                guiYield = false;
                pauseReason = PauseReason.NONE;
                System.out.println(TAG + "Foreign GUI closed, resuming");
                State back = yieldReturnState == null ? State.PLANNING : yieldReturnState;
                if (back == State.NAVIGATING && targetStand != null) {
                    BaritoneBridge.goTo(targetStand);
                    navigateTicks = 0;
                }
                setState(back);
            }
            return false;
        }

        if (!guiYield) {
            guiYield = true;
            yieldReturnState = currentState == State.PAUSED ? State.PLANNING : currentState;
            pauseReason = PauseReason.EXTERNAL_GUI;
            LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
            BaritoneBridge.cancel();
            System.out.println(TAG + "Foreign GUI opened ("
                    + mc.f_91080_.getClass().getSimpleName() + "), yielding");
        }
        hudLine = "Yielding to GUI";
        return true;
    }

    private static boolean checkLowHealth(Minecraft mc) {
        int threshold = LitematicaMixinMod.AUTO_BUILD_LOW_HEALTH.getIntegerValue();
        if (threshold <= 0) return false;
        if (mc.f_91074_.m_21223_() <= threshold) {
            if (currentState != State.PAUSED) {
                pauseReason = PauseReason.LOW_HEALTH;
                setState(State.PAUSED);
                LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
                BaritoneBridge.cancel();
                System.out.println(TAG + "Low health (" + mc.f_91074_.m_21223_() + " <= " + threshold + "), paused");
            }
            hudLine = "PAUSED (low health)";
            return true;
        }
        return false;
    }

    // ===== HUD =====

    private static void sendHud(Minecraft mc) {
        hudTick++;
        if (hudTick % HUD_INTERVAL != 0) return;
        if (mc.f_91074_ == null) return;
        String msg = "[" + currentState + "] " + hudLine;
        mc.f_91074_.m_5661_(Component.m_237113_(msg), true);
    }
}
