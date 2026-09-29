package xyz.jxmm.litematica_printer_forge.autobuild.supplier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import xyz.jxmm.litematica_printer_forge.autobuild.bridge.BaritoneBridge;

/**
 * Refined Storage extraction supplier (wireless + fixed terminal).
 *
 * Reflection only; zero compile-time references to refinedstorage packages.
 * Extraction sends GridItemPullMessage(uuid, flags=4) per spike notes.
 * Grid opening mimics a plain right-click via Minecraft.startUseItem().
 */
public final class RsExtractSupplier implements MaterialBroker.MaterialSupplier {
    private static final String TAG = "[AUTOBUILD] ";
    private static final int SYNC_TICKS = 10;
    private static final int SETTLE_TICKS = 15;
    private static final int OPEN_TIMEOUT = 60;
    private static final int WALK_TIMEOUT = 400;

    private enum Phase {
        IDLE, WALK_TO_TERMINAL, OPEN, SYNC, EXTRACT, SETTLE, CLOSE, DONE
    }

    private static boolean reflectInit = false;
    private static boolean reflectOk = false;
    private static Class<?> gridScreenClass;
    private static Field gridScreenViewField;
    private static Method gridViewGetStacks;
    private static Method gridStackGetId;
    private static Method gridStackGetIngredient;
    private static Constructor<?> gridItemPullMessageCtor;
    private static Object networkHandlerInstance;
    private static Method networkHandlerSendToServer;
    private static Class<?> wirelessGridItemClass;
    private static Class<?> gridMenuClass;

    private Phase phase = Phase.IDLE;
    private int waitTicks = 0;
    private Map<Item, Integer> shortages = new LinkedHashMap<>();
    private int savedSelectedSlot = -1;
    private BlockPos terminalPos = null;
    private List<Object[]> pullQueue = new ArrayList<>();
    private int pullIdx = 0;

    @Override
    public String name() {
        return "RS-Extract";
    }

    @Override
    public boolean isAvailable(Minecraft mc) {
        if (!initReflection()) return false;
        if (findWirelessHotbarSlot(mc) >= 0) return true;
        return TerminalLocator.findUsable(mc) != null;
    }

    @Override
    public void start(Minecraft mc, Map<Item, Integer> shortages) {
        this.shortages = new LinkedHashMap<>(shortages);
        pullQueue = new ArrayList<>();
        pullIdx = 0;
        waitTicks = 0;
        savedSelectedSlot = -1;
        terminalPos = null;

        int slot = findWirelessHotbarSlot(mc);
        if (slot >= 0) {
            savedSelectedSlot = mc.f_91074_.m_150109_().f_35977_;
            selectHotbarSlot(mc, slot);
            mc.f_91072_.m_233721_(mc.f_91074_, net.minecraft.world.InteractionHand.MAIN_HAND);
            phase = Phase.OPEN;
            return;
        }

        BlockPos pos = TerminalLocator.findUsable(mc);
        if (pos != null) {
            terminalPos = pos;
            BaritoneBridge.goTo(pos);
            phase = Phase.WALK_TO_TERMINAL;
            return;
        }

        phase = Phase.DONE;
    }

    @Override
    public boolean tick(Minecraft mc) {
        switch (phase) {
            case WALK_TO_TERMINAL:
                waitTicks++;
                if (terminalPos == null || waitTicks > WALK_TIMEOUT) {
                    System.out.println(TAG + "RS: walk to terminal timeout");
                    phase = Phase.DONE;
                    return false;
                }
                if (BaritoneBridge.isArrived(terminalPos, 3)) {
                    BaritoneBridge.cancel();
                    faceBlock(mc, terminalPos);
                    net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                            new net.minecraft.world.phys.Vec3(terminalPos.m_123341_() + 0.5,
                                    terminalPos.m_123342_() + 0.5, terminalPos.m_123343_() + 0.5),
                            net.minecraft.core.Direction.UP, terminalPos, false);
                    mc.f_91072_.m_233732_(mc.f_91074_, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
                    phase = Phase.OPEN;
                    waitTicks = 0;
                }
                return true;
            case OPEN:
                waitTicks++;
                if (isGridMenuOpen(mc)) {
                    phase = Phase.SYNC;
                    waitTicks = 0;
                } else if (waitTicks > OPEN_TIMEOUT) {
                    System.out.println(TAG + "RS: grid open timeout");
                    phase = Phase.DONE;
                    return false;
                }
                return true;
            case SYNC:
                waitTicks++;
                if (waitTicks >= SYNC_TICKS) {
                    buildPullQueue(mc);
                    phase = Phase.EXTRACT;
                }
                return true;
            case EXTRACT:
                if (pullIdx >= pullQueue.size()) {
                    phase = Phase.SETTLE;
                    waitTicks = 0;
                    return true;
                }
                sendPull((UUID) pullQueue.get(pullIdx)[0], (Integer) pullQueue.get(pullIdx++)[1]);
                return true;
            case SETTLE:
                waitTicks++;
                if (waitTicks >= SETTLE_TICKS) {
                    phase = Phase.CLOSE;
                }
                return true;
            case CLOSE:
                mc.m_91152_(null);
                restoreSlot(mc);
                phase = Phase.DONE;
                return false;
            default:
                return false;
        }
    }

    @Override
    public void abort(Minecraft mc) {
        if (phase != Phase.IDLE && phase != Phase.DONE) {
            mc.m_91152_(null);
            restoreSlot(mc);
            BaritoneBridge.cancel();
        }
        phase = Phase.IDLE;
        pullQueue = new ArrayList<>();
        pullIdx = 0;
        shortages = new LinkedHashMap<>();
    }

    // ===== Reflection init =====

    private static synchronized boolean initReflection() {
        if (reflectInit) return reflectOk;
        reflectInit = true;
        try {
            Class<?> rsClass = Class.forName("com.refinedmods.refinedstorage.RS");
            networkHandlerInstance = rsClass.getField("NETWORK_HANDLER").get(null);
            networkHandlerSendToServer = findMethod(networkHandlerInstance.getClass(), "sendToServer", 1);

            Class<?> pullClass = Class.forName("com.refinedmods.refinedstorage.network.grid.GridItemPullMessage");
            gridItemPullMessageCtor = pullClass.getConstructor(UUID.class, int.class);

            gridScreenClass = Class.forName("com.refinedmods.refinedstorage.screen.grid.GridScreen");
            gridScreenViewField = findField(gridScreenClass, "view");

            gridMenuClass = Class.forName("com.refinedmods.refinedstorage.container.GridContainerMenu");
            wirelessGridItemClass = Class.forName("com.refinedmods.refinedstorage.item.WirelessGridItem");

            reflectOk = networkHandlerInstance != null && networkHandlerSendToServer != null
                    && gridItemPullMessageCtor != null && gridScreenViewField != null;
        } catch (Throwable t) {
            System.out.println(TAG + "RS reflection init failed: " + t);
            reflectOk = false;
        }
        return reflectOk;
    }

    // ===== Helpers =====

    /** Wireless terminal must sit on the hotbar; swap choreography is out of scope. */
    private static int findWirelessHotbarSlot(Minecraft mc) {
        if (mc.f_91074_ == null) return -1;
        Inventory inv = mc.f_91074_.m_150109_();
        for (int i = 0; i < 9; i++) {
            ItemStack s = inv.m_8020_(i);
            if (!s.m_41619_() && wirelessGridItemClass.isInstance(s.m_41720_())) {
                return i;
            }
        }
        return -1;
    }

    private static void selectHotbarSlot(Minecraft mc, int slot) {
        mc.f_91074_.m_150109_().f_35977_ = slot;
        mc.m_91403_().m_104955_(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(slot));
    }

    private static BlockPos parseTerminalPos() {
        return TerminalLocator.saved();
    }

    private static boolean isGridBlockEntity(Minecraft mc, BlockPos pos) {
        return TerminalLocator.isTerminalAt(mc, pos);
    }

    private static boolean isGridMenuOpen(Minecraft mc) {
        return mc.f_91074_ != null && gridMenuClass.isInstance(mc.f_91074_.f_36096_);
    }

    private static void faceBlock(Minecraft mc, BlockPos pos) {
        double dx = pos.m_123341_() + 0.5 - mc.f_91074_.m_20185_();
        double dy = pos.m_123342_() + 0.5 - (mc.f_91074_.m_20186_() + mc.f_91074_.m_20192_());
        double dz = pos.m_123343_() + 0.5 - mc.f_91074_.m_20189_();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        float pitch = dist == 0 ? 0 : (float) (-(Math.asin(dy / dist) * 180.0 / Math.PI));
        mc.f_91074_.m_146922_(yaw);
        mc.f_91074_.m_146926_(pitch);
    }

    private void buildPullQueue(Minecraft mc) {
        pullQueue = new ArrayList<>();
        if (mc.f_91080_ == null || !gridScreenClass.isInstance(mc.f_91080_)) {
            System.out.println(TAG + "RS: grid screen not open");
            return;
        }
        try {
            Object view = gridScreenViewField.get(mc.f_91080_);
            if (view == null) return;
            if (gridViewGetStacks == null) {
                gridViewGetStacks = findMethod(view.getClass(), "getStacks", 0);
            }
            List<?> stacks = (List<?>) gridViewGetStacks.invoke(view);
            if (stacks == null) return;

            Map<Item, Integer> remaining = new LinkedHashMap<>(shortages);
            for (Object gs : stacks) {
                if (gs == null) continue;
                if (gridStackGetId == null) gridStackGetId = findMethod(gs.getClass(), "getId", 0);
                if (gridStackGetIngredient == null) gridStackGetIngredient = findMethod(gs.getClass(), "getIngredient", 0);
                Object ing = gridStackGetIngredient.invoke(gs);
                if (!(ing instanceof ItemStack)) continue;
                Item item = ((ItemStack) ing).m_41720_();
                Integer need = remaining.get(item);
                if (need == null || need <= 0) continue;
                UUID id = (UUID) gridStackGetId.invoke(gs);
                pullQueue.add(new Object[]{id, 4});
                remaining.remove(item);
                if (remaining.isEmpty()) break;
            }
            System.out.println(TAG + "RS: queued " + pullQueue.size() + " pull requests");
        } catch (Throwable t) {
            System.out.println(TAG + "RS: build pull queue failed: " + t);
        }
    }

    private static void sendPull(UUID id, int flags) {
        try {
            Object msg = gridItemPullMessageCtor.newInstance(id, flags);
            networkHandlerSendToServer.invoke(networkHandlerInstance, msg);
        } catch (Throwable t) {
            System.out.println(TAG + "RS: send pull failed: " + t);
        }
    }

    private void restoreSlot(Minecraft mc) {
        if (savedSelectedSlot >= 0 && mc.f_91074_ != null) {
            selectHotbarSlot(mc, savedSelectedSlot);
        }
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
