package xyz.jxmm.litematica_printer_forge.autobuild.bridge;

import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/**
 * Reflection-only soft bridge to Baritone (expected 1.10.2 API surface).
 *
 * Produces zero compile-time references to baritone packages (verified via
 * javap: only string constants). Every entry point degrades to a safe
 * false / no-op when Baritone is absent or its API differs from expectations.
 */
public final class BaritoneBridge {
    private static final String TAG = "[AUTOBUILD] ";

    private static boolean initAttempted;
    private static boolean installed;

    private static Object providerObject;
    private static Method getPrimaryBaritone;
    private static Method getCustomGoalProcess;
    private static Method getPathingBehavior;
    private static Method setGoalAndPath;
    private static Method setGoalFallback;
    private static Method cancelGoal;
    private static Method onLostControl;
    private static Method getGoal;
    private static Method isPathingMethod;
    private static Constructor<?> goalBlockPosCtor;
    private static Constructor<?> goalBlockXyzCtor;

    private static Object settingsObject;
    private static Field allowBreakField;
    private static Field allowPlaceField;
    private static Field allowParkourField;
    private static Field allowSprintField;
    private static Field settingValueField;

    private static final Map<Field, Object> settingsSnapshot = new HashMap<>();
    private static boolean snapshotTaken;

    private BaritoneBridge() {
    }

    public static synchronized boolean isInstalled() {
        if (!initAttempted) {
            initAttempted = true;
            installed = init();
            LitematicaMixinMod.LOGGER.info(TAG + (installed
                    ? "Baritone detected, navigation bridge ready"
                    : "Baritone not present or incompatible, auto build navigation disabled"));
        }
        return installed;
    }

    private static boolean init() {
        try {
            Class<?> api = Class.forName("baritone.api.BaritoneAPI");
            providerObject = api.getMethod("getProvider").invoke(null);
            if (providerObject == null) return false;

            getPrimaryBaritone = findMethod(providerObject.getClass(), "getPrimaryBaritone", 0);
            Object baritone = primaryBaritone();
            if (getPrimaryBaritone == null || baritone == null) return false;

            getCustomGoalProcess = findMethod(baritone.getClass(), "getCustomGoalProcess", 0);
            getPathingBehavior = findMethod(baritone.getClass(), "getPathingBehavior", 0);
            Object goalProcess = getCustomGoalProcess != null ? getCustomGoalProcess.invoke(baritone) : null;
            Object pathing = getPathingBehavior != null ? getPathingBehavior.invoke(baritone) : null;
            if (goalProcess == null || pathing == null) return false;

            setGoalAndPath = findMethod(goalProcess.getClass(), "setGoalAndPath", 1);
            setGoalFallback = findMethod(goalProcess.getClass(), "setGoal", 1);
            // 1.10.2 dropped ICustomGoalProcess.cancel(); cancellation is setGoal(null)
            // or onLostControl(). Accept any of them.
            cancelGoal = findMethod(goalProcess.getClass(), "cancel", 0);
            onLostControl = findMethod(goalProcess.getClass(), "onLostControl", 0);
            getGoal = findMethod(goalProcess.getClass(), "getGoal", 0);
            isPathingMethod = findMethod(pathing.getClass(), "isPathing", 0);
            if ((setGoalAndPath == null && setGoalFallback == null)
                    || (cancelGoal == null && setGoalFallback == null && onLostControl == null)
                    || isPathingMethod == null) {
                return false;
            }

            Class<?> goalBlock = Class.forName("baritone.api.pathing.goals.GoalBlock");
            try {
                goalBlockPosCtor = goalBlock.getConstructor(BlockPos.class);
            } catch (NoSuchMethodException e) {
                goalBlockXyzCtor = goalBlock.getConstructor(int.class, int.class, int.class);
            }

            settingsObject = api.getMethod("getSettings").invoke(null);
            if (settingsObject == null) return false;
            allowBreakField = findField(settingsObject.getClass(), "allowBreak");
            allowPlaceField = findField(settingsObject.getClass(), "allowPlace");
            allowParkourField = findField(settingsObject.getClass(), "allowParkour");
            allowSprintField = findField(settingsObject.getClass(), "allowSprint");
            if (allowBreakField == null || allowPlaceField == null) return false;

            Object sampleSetting = allowBreakField.get(settingsObject);
            settingValueField = sampleSetting != null ? findField(sampleSetting.getClass(), "value") : null;
            return settingValueField != null;
        } catch (Throwable t) {
            LitematicaMixinMod.LOGGER.info(TAG + "Baritone reflection init failed: " + t);
            return false;
        }
    }

    private static Object primaryBaritone() {
        try {
            return providerObject != null && getPrimaryBaritone != null
                    ? getPrimaryBaritone.invoke(providerObject)
                    : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object goalProcess() {
        try {
            Object baritone = primaryBaritone();
            return baritone != null && getCustomGoalProcess != null
                    ? getCustomGoalProcess.invoke(baritone)
                    : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object pathingBehavior() {
        try {
            Object baritone = primaryBaritone();
            return baritone != null && getPathingBehavior != null
                    ? getPathingBehavior.invoke(baritone)
                    : null;
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Saves current Baritone movement settings so they can be restored on stop.
     */
    public static boolean snapshotSettings() {
        if (!isInstalled()) return false;
        try {
            settingsSnapshot.clear();
            snapshotField(allowBreakField);
            snapshotField(allowPlaceField);
            snapshotField(allowParkourField);
            snapshotField(allowSprintField);
            snapshotTaken = true;
            return true;
        } catch (Throwable t) {
            LitematicaMixinMod.LOGGER.info(TAG + "settings snapshot failed: " + t);
            return false;
        }
    }

    private static void snapshotField(Field settingField) throws Exception {
        if (settingField == null) return;
        Object setting = settingField.get(settingsObject);
        if (setting != null) {
            settingsSnapshot.put(settingField, settingValueField.get(setting));
        }
    }

    /**
     * Applies the director's desired pathing settings. Caller is expected to
     * have taken a snapshot first.
     */
    public static boolean applyPathingSettings(boolean allowBreak, boolean allowPlace,
                                               boolean allowParkour, boolean allowSprint) {
        if (!isInstalled()) return false;
        try {
            setSetting(allowBreakField, allowBreak);
            setSetting(allowPlaceField, allowPlace);
            setSetting(allowParkourField, allowParkour);
            setSetting(allowSprintField, allowSprint);
            return true;
        } catch (Throwable t) {
            LitematicaMixinMod.LOGGER.info(TAG + "apply pathing settings failed: " + t);
            return false;
        }
    }

    private static void setSetting(Field settingField, Object value) throws Exception {
        if (settingField == null) return;
        Object setting = settingField.get(settingsObject);
        if (setting != null) {
            settingValueField.set(setting, value);
        }
    }

    /**
     * Restores settings captured by {@link #snapshotSettings()}.
     */
    public static boolean restoreSettings() {
        if (!isInstalled() || !snapshotTaken) return false;
        try {
            for (Map.Entry<Field, Object> entry : settingsSnapshot.entrySet()) {
                setSetting(entry.getKey(), entry.getValue());
            }
            settingsSnapshot.clear();
            snapshotTaken = false;
            return true;
        } catch (Throwable t) {
            LitematicaMixinMod.LOGGER.info(TAG + "settings restore failed: " + t);
            return false;
        }
    }

    /**
     * Orders Baritone to path to the given block position (GoalBlock).
     */
    public static boolean goTo(BlockPos pos) {
        if (!isInstalled() || pos == null) return false;
        try {
            Object process = goalProcess();
            if (process == null) return false;
            Object goal = goalBlockPosCtor != null
                    ? goalBlockPosCtor.newInstance(pos)
                    : goalBlockXyzCtor.newInstance(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
            if (setGoalAndPath != null) {
                setGoalAndPath.invoke(process, goal);
            } else {
                setGoalFallback.invoke(process, goal);
            }
            return true;
        } catch (Throwable t) {
            LitematicaMixinMod.LOGGER.info(TAG + "goTo [" + pos.m_123341_() + "," + pos.m_123342_() + "," + pos.m_123343_() + "] failed: " + t);
            return false;
        }
    }

    /**
     * Cancels the current custom goal / path. Prefers cancel(), falls back to
     * setGoal(null) (1.10.2 API), then onLostControl().
     */
    public static boolean cancel() {
        if (!isInstalled()) return false;
        try {
            Object process = goalProcess();
            if (process == null) return false;
            if (cancelGoal != null) {
                cancelGoal.invoke(process);
            } else if (setGoalFallback != null) {
                setGoalFallback.invoke(process, new Object[]{null});
            } else {
                onLostControl.invoke(process);
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * True while Baritone is actively calculating or following a path.
     */
    public static boolean isPathing() {
        if (!isInstalled()) return false;
        try {
            Object pathing = pathingBehavior();
            return pathing != null && Boolean.TRUE.equals(isPathingMethod.invoke(pathing));
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * True while a custom goal is set (not yet cancelled/completed).
     */
    public static boolean hasGoal() {
        if (!isInstalled()) return false;
        try {
            Object process = goalProcess();
            return process != null && getGoal != null && getGoal.invoke(process) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Client-side arrival check: player feet within Chebyshev tolerance of the
     * target on every axis. Does not depend on Baritone.
     */
    public static boolean isArrived(BlockPos target, int tolerance) {
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ == null || target == null) return false;
        BlockPos feet = mc.f_91074_.m_20183_();
        int dx = Math.abs(feet.m_123341_() - target.m_123341_());
        int dy = Math.abs(feet.m_123342_() - target.m_123342_());
        int dz = Math.abs(feet.m_123343_() - target.m_123343_());
        return dx <= tolerance && dy <= tolerance && dz <= tolerance;
    }

    private static Method findMethod(Class<?> cls, String name, int paramCount) {
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                    m.setAccessible(true);
                    return m;
                }
            }
        }
        for (Method m : cls.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == paramCount) {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }

    private static Field findField(Class<?> cls, String name) {
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }
}
