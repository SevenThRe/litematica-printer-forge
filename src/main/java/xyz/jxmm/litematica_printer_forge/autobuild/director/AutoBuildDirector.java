package xyz.jxmm.litematica_printer_forge.autobuild.director;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.autobuild.bridge.BaritoneBridge;
import xyz.jxmm.litematica_printer_forge.autobuild.planner.AutoBuildPlanner;
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

    private static final String TAG = "[AUTOBUILD] ";
    private static final int PRINT_STUCK_THRESHOLD = 100;
    private static final int HUD_INTERVAL = 20;

    private static boolean running = false;
    private static boolean userPrinterOff = false;
    private static State currentState = State.IDLE;

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
        if (mc.f_91074_ == null || mc.f_91073_ == null) {
            stop();
            return;
        }

        if (checkLowHealth(mc)) return;

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
        MaterialBroker.abort(Minecraft.m_91087_());
        LitematicaMixinMod.PRINTER_OFF.setBooleanValue(userPrinterOff);
        resetRunState();
        setState(State.IDLE);
        System.out.println(TAG + "Auto build stopped");
    }

    private static void resumeFromPause() {
        resetRunState();
        setState(State.PLANNING);
        System.out.println(TAG + "Resumed");
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

        Integer lowestY = AutoBuildPlanner.getLowestMissingY(null);
        if (lowestY == null) {
            // No loaded-chunk missing blocks left. If unloaded chunks still hold
            // unbuilt blocks, walk towards the nearest one to force loading;
            // the next scan pass reclassifies them and normal flow resumes.
            Integer unloadedY = AutoBuildPlanner.getLowestUnloadedY();
            if (unloadedY == null) {
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

        currentLayerY = lowestY;

        // Restock check before walking anywhere.
        Map<Item, Integer> needs = AutoBuildPlanner.getMaterialNeeds(currentLayerY, null);
        if (MaterialBroker.hasShortage(mc, needs)) {
            BaritoneBridge.cancel();
            MaterialBroker.begin(mc, needs);
            setState(State.SUPPLYING);
            return;
        }

        AutoBuildPlanner.StandPlan plan = AutoBuildPlanner.computeStandClusters(mc, currentLayerY, null);
        currentClusters = plan.clusters;
        clusterIdx = 0;

        if (currentClusters.isEmpty()) {
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
                setState(State.PAUSED);
                LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
                System.out.println(TAG + "Max retries reached, paused");
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
            setState(State.PLANNING);
        } else {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Item, Integer> e : residual.entrySet()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(new net.minecraft.world.item.ItemStack(e.getKey()).m_41786_().getString())
                        .append(" x").append(e.getValue());
            }
            setState(State.PAUSED);
            LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
            System.out.println(TAG + "Missing materials, paused: " + sb);
        }
    }

    private static void tickPrinting(Minecraft mc) {
        LitematicaMixinMod.PRINTER_OFF.setBooleanValue(false);

        if (!AutoBuildPlanner.isScanComplete()) {
            hudLine = "Scanning...";
            return;
        }

        if (AutoBuildPlanner.isLayerComplete(currentLayerY, null)) {
            System.out.println(TAG + "Layer Y=" + currentLayerY + " complete");
            setState(State.PLANNING);
            return;
        }

        if (targetStand != null && !BaritoneBridge.isArrived(targetStand, 2)) {
            BaritoneBridge.goTo(targetStand);
            navigateTicks = 0;
            setState(State.NAVIGATING);
            return;
        }

        int currentMissing = AutoBuildPlanner.countLoadedAtOrBelow(currentLayerY, null);
        int version = AutoBuildPlanner.getDataVersion();
        if (lastMissingVersion == version && currentMissing == lastMissingCount) {
            printStuckTicks++;
        } else {
            printStuckTicks = 0;
            lastMissingCount = currentMissing;
            lastMissingVersion = version;
        }

        if (printStuckTicks > PRINT_STUCK_THRESHOLD) {
            setState(State.PAUSED);
            LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
            System.out.println(TAG + "Printing stuck (no progress for " + PRINT_STUCK_THRESHOLD + " ticks), paused");
            return;
        }

        hudLine = "Printing Y=" + currentLayerY + " left=" + currentMissing;
    }

    private static void tickPaused(Minecraft mc) {
        LitematicaMixinMod.PRINTER_OFF.setBooleanValue(true);
        BaritoneBridge.cancel();
        hudLine = "Paused";
    }

    // ===== Safety =====

    private static boolean checkLowHealth(Minecraft mc) {
        int threshold = LitematicaMixinMod.AUTO_BUILD_LOW_HEALTH.getIntegerValue();
        if (threshold <= 0) return false;
        if (mc.f_91074_.m_21223_() <= threshold) {
            if (currentState != State.PAUSED) {
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
