package xyz.jxmm.litematica_printer_forge.autobuild.supplier;

import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;

/**
 * RS grid terminal memory. Position sources, in priority order:
 *   1) markTerminal hotkey on a looked-at grid block,
 *   2) auto-recorded when the player manually opens a GridContainerMenu
 *      (grid.getPos() via reflection, block-entity grids only),
 *   3) radius scan for a GridBlockEntity around the player.
 * The position persists in the AUTO_BUILD_TERMINAL_POS config string.
 */
public final class TerminalLocator {
    private static final String TAG = "[AUTOBUILD] ";
    private static final int SCAN_RADIUS = 24;

    private static Class<?> gridBlockEntityClass;
    private static Class<?> gridMenuClass;
    private static boolean initAttempted = false;
    private static boolean rsPresent = false;

    private TerminalLocator() {
    }

    private static synchronized boolean init() {
        if (initAttempted) return rsPresent;
        initAttempted = true;
        try {
            gridBlockEntityClass = Class.forName("com.refinedmods.refinedstorage.blockentity.grid.GridBlockEntity");
            gridMenuClass = Class.forName("com.refinedmods.refinedstorage.container.GridContainerMenu");
            rsPresent = true;
        } catch (Throwable t) {
            rsPresent = false;
        }
        return rsPresent;
    }

    public static boolean isRsPresent() {
        return init();
    }

    public static boolean isTerminalAt(Minecraft mc, BlockPos pos) {
        if (!init() || mc.f_91073_ == null || pos == null) return false;
        if (!mc.f_91073_.m_46805_(pos)) return false;
        BlockEntity be = mc.f_91073_.m_7702_(pos);
        return be != null && gridBlockEntityClass.isInstance(be);
    }

    /** Saved position from config, or null when unset/invalid. */
    public static BlockPos saved() {
        String s = LitematicaMixinMod.AUTO_BUILD_TERMINAL_POS.getStringValue().trim();
        if (s.isEmpty()) return null;
        try {
            String[] parts = s.split(",");
            return new BlockPos(Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()));
        } catch (Exception e) {
            return null;
        }
    }

    public static void save(BlockPos pos) {
        LitematicaMixinMod.AUTO_BUILD_TERMINAL_POS.setValueFromString(
                pos.m_123341_() + "," + pos.m_123342_() + "," + pos.m_123343_());
    }

    /**
     * The terminal position to use right now: valid saved position first,
     * otherwise a nearby grid block entity (which then gets remembered).
     */
    public static BlockPos findUsable(Minecraft mc) {
        BlockPos pos = saved();
        if (pos != null && isTerminalAt(mc, pos)) {
            return pos;
        }
        BlockPos found = scanNearby(mc, SCAN_RADIUS);
        if (found != null) {
            save(found);
            System.out.println(TAG + "Auto-remembered RS grid terminal at [" + found.m_123341_() + "," + found.m_123342_() + "," + found.m_123343_() + "]");
        }
        return found;
    }

    /** Mark hotkey handler: record the looked-at grid terminal. */
    public static void markLookedAt(Minecraft mc) {
        if (!init() || mc.f_91074_ == null) return;
        HitResult hit = mc.f_91077_;
        if (hit == null || hit.m_6662_() != HitResult.Type.BLOCK) {
            chat(mc, "RS terminal mark: not looking at a block");
            return;
        }
        BlockPos pos = ((BlockHitResult) hit).m_82425_();
        if (isTerminalAt(mc, pos)) {
            save(pos);
            chat(mc, "RS terminal marked at [" + pos.m_123341_() + "," + pos.m_123342_() + "," + pos.m_123343_() + "]");
        } else {
            chat(mc, "RS terminal mark failed: target is not a grid terminal");
        }
    }

    /**
     * Cheap per-tick hook: when the player manually opens a grid menu backed by
     * a block (not a wireless grid), remember its position.
     */
    public static void autoRecordTick(Minecraft mc) {
        if (!init() || mc.f_91074_ == null) return;
        Object menu = mc.f_91074_.f_36096_;
        if (!gridMenuClass.isInstance(menu)) return;
        if (saved() != null) return;
        try {
            Method getGrid = findMethod(menu.getClass(), "getGrid", 0);
            if (getGrid == null) return;
            Object grid = getGrid.invoke(menu);
            if (grid == null || !grid.getClass().getName().contains("blockentity")) return;
            Method getPos = findMethod(grid.getClass(), "getPos", 0);
            if (getPos == null) return;
            Object pos = getPos.invoke(grid);
            if (pos instanceof BlockPos bp) {
                save(bp);
                System.out.println(TAG + "Recorded RS grid terminal from open menu at [" + bp.m_123341_() + "," + bp.m_123342_() + "," + bp.m_123343_() + "]");
            }
        } catch (Throwable ignored) {
        }
    }

    private static BlockPos scanNearby(Minecraft mc, int radius) {
        if (mc.f_91074_ == null || mc.f_91073_ == null) return null;
        BlockPos center = mc.f_91074_.m_20183_();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = center.m_121955_(new net.minecraft.core.Vec3i(dx, dy, dz));
                    if (!mc.f_91073_.m_46805_(p)) continue;
                    BlockEntity be = mc.f_91073_.m_7702_(p);
                    if (be != null && gridBlockEntityClass.isInstance(be)) {
                        double d = (double) dx * dx + (double) dy * dy + (double) dz * dz;
                        if (d < bestDist) {
                            bestDist = d;
                            best = p.m_7949_();
                        }
                    }
                }
            }
        }
        return best;
    }

    private static void chat(Minecraft mc, String msg) {
        mc.f_91074_.m_5661_(Component.m_237113_(TAG + msg), false);
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
}
