package xyz.jxmm.litematica_printer_forge.utils;

import com.google.common.collect.ImmutableMap;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.glfw.GLFW;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.gui.BackpackInjectScreen;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class BackpackInjector {
    private static boolean resolved = false;
    private static boolean available = false;
    private static Method mIsSophisticatedLoaded;
    private static Method mGetStorageMenu;
    private static Method mGetRealSlots;
    private static Method mSendSetGhostSlotMessage;
    private static Method mCountItemInBackpack;
    private static boolean wasTriggerPressed = false;
    // 自动打开背包注入的 pending 状态
    private static boolean autoOpenBackpack = false;
    private static int autoOpenTicks = 0;
    private static List<InjectPlan> pendingPlans = null;

    /** 注入排序模式：0=Y升序（底部→顶部，默认）1=Y倒序（顶部→底部）2=扫描顺序 */
    public static int lastSortMode = 0;

    public static void applySort(List<InjectPlan> plans, int sortMode) {
        if (sortMode == 0) {
            plans.sort(java.util.Comparator.comparingInt(p -> p.lowestY));
        } else if (sortMode == 1) {
            plans.sort((a, b) -> Integer.compare(b.lowestY, a.lowestY));
        }
        // sortMode == 2 保持扫描顺序
    }

    public static String sortModeName(int sortMode) {
        switch (sortMode) {
            case 0: return "排序: Y升序 (底部→顶部)";
            case 1: return "排序: Y倒序 (顶部→底部)";
            default: return "排序: 扫描顺序";
        }
    }

    public static class InjectPlan {
        public final ItemStack stack;
        public int amount;
        /** 该材料最低缺失位置的世界 Y（用于 Y 层级排序注入） */
        public int lowestY = Integer.MAX_VALUE;
        public InjectPlan(ItemStack stack, int amount) {
            this.stack = stack;
            this.amount = amount;
        }
        public InjectPlan(ItemStack stack, int amount, int lowestY) {
            this.stack = stack;
            this.amount = amount;
            this.lowestY = lowestY;
        }
    }

    private static void resolve(Minecraft mc) {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> backpackUtil = Class.forName("com.backpackinjector.backpack.BackpackUtil");
            mIsSophisticatedLoaded = backpackUtil.getMethod("isSophisticatedLoaded");
            mGetStorageMenu = backpackUtil.getMethod("getStorageMenu", Minecraft.class);
            // getRealInventorySlots 是 private static，必须用 getDeclaredMethod
            mGetRealSlots = backpackUtil.getDeclaredMethod("getRealInventorySlots", Object.class);
            mGetRealSlots.setAccessible(true);
            mSendSetGhostSlotMessage = backpackUtil.getMethod("sendSetGhostSlotMessage", ItemStack.class, Integer.TYPE);
            mCountItemInBackpack = backpackUtil.getMethod("countItemInBackpack", Object.class, ItemStack.class);
            available = true;
        } catch (Exception e) {
            available = false;
        }
    }

    public static boolean isAvailable(Minecraft mc) {
        resolve(mc);
        if (!available) return false;
        try {
            return (Boolean) mIsSophisticatedLoaded.invoke(null);
        } catch (Exception e) {
            return false;
        }
    }

    public static void tick(Minecraft mc) {
        if (!LitematicaMixinMod.BII_ENABLED.getBooleanValue()) return;
        if (!isTriggerJustPressed()) return;
        if (!isAvailable(mc)) return;
        openGui(mc);
    }

    public static void openGui(Minecraft mc) {
        List<SchematicPlacement> allPlacements = DataManager.getSchematicPlacementManager().getAllSchematicsPlacements();
        if (allPlacements == null || allPlacements.isEmpty()) {
            MessageHolder.sendMessageUnchecked("[BII] 未选择原理图");
            return;
        }
        // 收集所有启用的原理图
        List<SchematicPlacement> enabled = new ArrayList<>();
        for (SchematicPlacement p : allPlacements) {
            if (p != null && p.isEnabled()) {
                enabled.add(p);
            }
        }
        if (enabled.isEmpty()) {
            MessageHolder.sendMessageUnchecked("[BII] 没有启用的原理图");
            return;
        }
        // 只有一张则直接用，多张则弹出选择器
        if (enabled.size() == 1) {
            openGuiFor(mc, enabled.get(0));
        } else {
            mc.m_91152_(new xyz.jxmm.litematica_printer_forge.gui.SchematicPickerScreen(enabled));
        }
    }

    // 等待背包打开后显示 BII 界面的暂存
    private static SchematicPlacement pendingPlacement = null;
    private static int pendingShowGuiTicks = 0;
    private static boolean waitingToShowGui = false;
    // 检测到 menu 后还要等槽位数据同步（OpenMenu 包与 ContainerSetContent 包不是同一时刻到）
    private static int menuReadyDelayTicks = 0;
    private static final int MENU_SYNC_DELAY = 10;

    public static void openGuiFor(Minecraft mc, SchematicPlacement placement) {
        // 如果背包 GUI 已打开且数据已稳定（至少开过 MENU_SYNC_DELAY tick），直接计数显示
        Object menu = getBackpackMenu(mc);
        if (menu != null && menuReadyDelayTicks >= MENU_SYNC_DELAY) {
            showInjectGui(mc, placement, menu);
            return;
        }

        // 自动打开背包，等 GUI 打开且槽位同步后再计数
        Inventory inv = mc.f_91074_.m_150109_();
        int backpackSlot = findBackpackSlot(inv);
        if (backpackSlot < 0) {
            MessageHolder.sendMessageUnchecked("[BII] 背包中没有找到背包物品");
            return;
        }
        mc.m_91152_(null);
        pendingPlacement = placement;
        pendingShowGuiTicks = 0;
        waitingToShowGui = true;
        sendBackpackOpenPacket(mc, backpackSlot);
        MessageHolder.sendMessageUnchecked("[BII] 正在打开背包...");
    }

    /**
     * 背包 GUI 已打开后：扫描原理图 + 从 menu 计数 + 弹出 BII 界面
     */
    private static void showInjectGui(Minecraft mc, SchematicPlacement placement, Object menu) {
        Inventory inv = mc.f_91074_.m_150109_();

        // 直接从原理图容器读取（不依赖 world 加载范围）
        // 坐标公式必须与 LitematicaSchematic.placeBlocksToWorld 完全一致：
        // getTransformedPlacementPosition（两层旋转/镜像）+ 变换后的子区域偏移 + placement origin。
        // 漏掉后两步会扫到世界原点，导致算出的缺失材料与验证器完全不一致。
        fi.dy.masa.litematica.schematic.LitematicaSchematic schematic = placement.getSchematic();
        if (schematic == null) {
            MessageHolder.sendMessageUnchecked("[BII] 原理图数据不可用");
            return;
        }
        BlockPos placementOrigin = placement.getOrigin();
        Map<Item, Integer> missingMap = new LinkedHashMap<>();
        Map<Item, Integer> minYMap = new HashMap<>();
        for (java.util.Map.Entry<String, fi.dy.masa.litematica.schematic.placement.SubRegionPlacement> entry
                : placement.getEnabledRelativeSubRegionPlacements().entrySet()) {
            String regionName = entry.getKey();
            fi.dy.masa.litematica.schematic.placement.SubRegionPlacement subRegion = entry.getValue();
            fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer container =
                    schematic.getSubRegionContainer(regionName);
            if (container == null) continue;
            BlockPos regionPosTransformed = fi.dy.masa.litematica.util.PositionUtils
                    .getTransformedBlockPos(subRegion.getPos(), placement.getMirror(), placement.getRotation());
            net.minecraft.core.Vec3i size = container.getSize();
            for (int y = 0; y < size.m_123342_(); ++y) {
                for (int x = 0; x < size.m_123341_(); ++x) {
                    for (int z = 0; z < size.m_123343_(); ++z) {
                        BlockState schemState = container.get(x, y, z);
                        if (schemState.m_60795_()) continue;
                        BlockPos worldPos = fi.dy.masa.litematica.util.PositionUtils
                                .getTransformedPlacementPosition(new BlockPos(x, y, z), placement, subRegion)
                                .m_121955_((net.minecraft.core.Vec3i) regionPosTransformed)
                                .m_121955_((net.minecraft.core.Vec3i) placementOrigin);
                        // 未加载区块按"缺失"计入（备料语义：那些位置迟早要放）；
                        // 已加载区块与验证器同口径判定 MISSING + WRONG_BLOCK
                        boolean missing;
                        if (mc.f_91073_.m_46805_(worldPos)) {
                            BlockState worldState = mc.f_91073_.m_8055_(worldPos);
                            missing = worldState.m_60795_() || worldState.m_247087_()
                                    || (worldState.m_60734_() != schemState.m_60734_()
                                        && worldState.m_60734_() != BlockReplacer.resolveBlock(schemState.m_60734_()));
                        } else {
                            missing = true;
                        }
                        if (!missing) continue;
                        Item item = schemState.m_60734_().m_5456_();
                        if (item == Items.f_41852_) continue;
                        item = BlockReplacer.resolve(item);
                        missingMap.merge(item, 1, Integer::sum);
                        minYMap.merge(item, worldPos.m_123342_(), Math::min);
                    }
                }
            }
        }
        if (missingMap.isEmpty()) {
            MessageHolder.sendMessageUnchecked("[BII] 「" + placement.getName() + "」没有缺失材料");
            return;
        }

        // 从打开的背包 menu 计数。plans 包含全部短缺材料（与验证器缺失列表一致），
        // amount 默认=总缺口（needed - inInv），让用户直观看到每种还差多少；
        // 实际注入时在 doInject 中按背包实际存量截断（背包没货的自动跳过）。
        List<InjectPlan> plans = new ArrayList<>();
        int shortageTotal = 0;
        boolean debug = LitematicaMixinMod.DEBUG_MESSAGE.getBooleanValue();
        for (Map.Entry<Item, Integer> e : missingMap.entrySet()) {
            ItemStack stack = new ItemStack(e.getKey(), 1);
            int needed = e.getValue();
            int inInv = countInInventory(inv, e.getKey());
            int inBackpack = countInBackpack(menu, stack);
            int shortage = Math.max(0, needed - inInv - inBackpack);
            int planAmount = Math.max(0, needed - inInv);
            if (debug) {
                MessageHolder.sendMessageUnchecked("[BII] " + stack.m_41786_().getString()
                        + " 缺" + needed + " 背包" + inInv + " 存储" + inBackpack
                        + " → 计划" + planAmount + (inBackpack > 0 ? "（已有货先搬）" : "（凭空注入）"));
            }
            if (shortage <= 0) continue;
            shortageTotal += shortage;
            plans.add(new InjectPlan(stack, planAmount, minYMap.getOrDefault(e.getKey(), 0)));
        }

        if (plans.isEmpty()) {
            MessageHolder.sendMessageUnchecked("[BII] 「" + placement.getName() + "」材料已足够");
            return;
        }

        // 按上次选择的排序模式排列（默认 Y 升序：从建筑底部往上搭）
        applySort(plans, lastSortMode);

        mc.m_91152_(new BackpackInjectScreen(plans, placement.getName()));
    }

    public static void inject(List<InjectPlan> plans, int sortMode, Minecraft mc) {
        if (!isAvailable(mc)) {
            MessageHolder.sendMessageUnchecked("[BII] 背包模组不可用");
            return;
        }
        lastSortMode = sortMode;
        applySort(plans, sortMode);

        // 背包 GUI 已打开且数据稳定（BII 界面通常已导致背包关闭，这里只作快速路径）
        Object menu = getBackpackMenu(mc);
        if (menu != null && menuReadyDelayTicks >= MENU_SYNC_DELAY) {
            doInject(plans, mc, menu);
            mc.m_91152_(null);
            return;
        }

        // 用户可能手动关闭了背包，重新打开
        Inventory inv = mc.f_91074_.m_150109_();
        int backpackSlot = findBackpackSlot(inv);
        if (backpackSlot < 0) {
            MessageHolder.sendMessageUnchecked("[BII] 背包中没有找到背包物品");
            return;
        }

        mc.m_91152_(null);
        pendingPlans = plans;
        autoOpenBackpack = true;
        autoOpenTicks = 0;

        sendBackpackOpenPacket(mc, backpackSlot);
        MessageHolder.sendMessageUnchecked("[BII] 正在打开背包...");
    }

    private static void doInject(List<InjectPlan> plans, Minecraft mc, Object menu) {
        // "注入"语义 = 凭空创造缺失材料，全部走 SetGhostSlotMessage（服务端 Slot.set 直写）：
        // 1) 每种材料拿至多一组（maxStackSize）ghost 直写玩家物品栏空格
        // 2) 剩余缺口 ghost 注入背包存储空槽（填满背包备用）
        // 总创造量 = 缺口量。QUICK_MOVE 点击包方案已废弃（服务端静默拒绝，原因未明，ghost 已实证可靠）
        List<net.minecraft.world.inventory.Slot> slots = getRealSlots(menu);
        if (slots == null || slots.isEmpty()) {
            MessageHolder.sendMessageUnchecked("[BII] 无法读取背包槽位");
            return;
        }
        if (!(menu instanceof net.minecraft.world.inventory.AbstractContainerMenu absMenu)) {
            MessageHolder.sendMessageUnchecked("[BII] 背包 menu 类型异常");
            return;
        }
        net.minecraft.client.multiplayer.ClientPacketListener connection = mc.m_91403_();
        if (connection == null) {
            MessageHolder.sendMessageUnchecked("[BII] 未连接到服务器");
            return;
        }

        // 背包存储空槽（注入目标）
        List<Integer> emptySlotIds = new ArrayList<>();
        // 玩家物品栏在菜单中的空槽号（拿取目标）
        List<Integer> invSlotIds = new ArrayList<>();
        net.minecraft.world.Container playerInv = mc.f_91074_.m_150109_();
        for (net.minecraft.world.inventory.Slot s : absMenu.f_38839_) {
            if (s.m_7993_().m_41619_()) {
                if (s.f_40218_ == playerInv) {
                    invSlotIds.add(s.f_40219_);
                } else {
                    emptySlotIds.add(s.f_40219_);
                }
            }
        }
        if (emptySlotIds.isEmpty() && invSlotIds.isEmpty()) {
            MessageHolder.sendMessageUnchecked("[BII] 背包和物品栏都没有空槽位，无法注入");
            return;
        }

        boolean debug = LitematicaMixinMod.DEBUG_MESSAGE.getBooleanValue();
        int totalTaken = 0;
        int totalStored = 0;
        int takenKinds = 0;
        int invIdx = 0;
        int bpIdx = 0;

        for (InjectPlan plan : plans) {
            if (plan.amount <= 0) continue;
            int maxPer = plan.stack.m_41741_();
            // 1) 物品栏拿取：至多一组
            int take = Math.min(plan.amount, maxPer);
            int got = 0;
            while (got < take && invIdx < invSlotIds.size()) {
                int n = Math.min(take - got, maxPer);
                if (!sendGhost(plan.stack, n, invSlotIds.get(invIdx++))) {
                    MessageHolder.sendMessageUnchecked("[BII] ghost 写入失败（协议不可用），停止注入");
                    return;
                }
                got += n;
            }
            // 2) 背包注入：剩余缺口（物品栏实际拿到多少就从缺口扣多少）
            int toStore = plan.amount - got;
            int put = 0;
            while (put < toStore && bpIdx < emptySlotIds.size()) {
                int n = Math.min(toStore - put, maxPer);
                if (!sendGhost(plan.stack, n, emptySlotIds.get(bpIdx++))) {
                    MessageHolder.sendMessageUnchecked("[BII] ghost 写入失败（协议不可用），停止注入");
                    return;
                }
                put += n;
            }
            if (got > 0) takenKinds++;
            totalTaken += got;
            totalStored += put;
            if (debug) {
                MessageHolder.sendMessageUnchecked("[BII] " + plan.stack.m_41786_().getString()
                        + " 拿取" + got + " 存背包" + put);
            }
        }

        MessageHolder.sendMessageUnchecked("[BII] 完成：物品栏拿取 " + totalTaken + " 个（"
                + takenKinds + " 种，每种至多一组），背包注入 " + totalStored + " 个");
        mc.m_91152_(null);
    }

    private static boolean sendGhost(ItemStack proto, int count, int slotId) {
        try {
            ItemStack ghost = proto.m_41777_();
            ghost.m_41764_(count);
            return (Boolean) mSendSetGhostSlotMessage.invoke(null, ghost, slotId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 每 tick 调用：处理自动打开背包后的操作。
     */
    public static void onClientTick(Minecraft mc) {
        // 跟踪背包 menu 已稳定存在多少 tick（用于槽位数据同步等待）
        Object currentMenu = getBackpackMenu(mc);
        if (currentMenu != null) {
            menuReadyDelayTicks++;
        } else {
            menuReadyDelayTicks = 0;
        }

        // 阶段1：等待背包 GUI 打开且槽位同步，然后显示 BII 界面
        if (waitingToShowGui) {
            pendingShowGuiTicks++;
            if (pendingShowGuiTicks > 60) {
                waitingToShowGui = false;
                pendingPlacement = null;
                MessageHolder.sendMessageUnchecked("[BII] 打开背包超时");
                return;
            }
            if (currentMenu != null && menuReadyDelayTicks >= MENU_SYNC_DELAY) {
                waitingToShowGui = false;
                showInjectGui(mc, pendingPlacement, currentMenu);
                pendingPlacement = null;
            }
            return;
        }

        // 阶段2：注入流程（点击确认后）
        if (!autoOpenBackpack) return;
        autoOpenTicks++;

        if (autoOpenTicks > 60) {
            autoOpenBackpack = false;
            pendingPlans = null;
            MessageHolder.sendMessageUnchecked("[BII] 打开背包超时");
            return;
        }

        if (currentMenu != null && menuReadyDelayTicks >= MENU_SYNC_DELAY) {
            autoOpenBackpack = false;
            MessageHolder.sendMessageUnchecked("[BII] 背包已同步，开始注入...");
            doInject(pendingPlans, mc, currentMenu);
            pendingPlans = null;
            mc.m_91152_(null);
        }
    }

    private static int findBackpackSlot(Inventory inv) {
        for (int i = 0; i < inv.m_6643_(); i++) {
            ItemStack stack = inv.m_8020_(i);
            if (stack.m_41619_()) continue;
            String className = stack.m_41720_().getClass().getName();
            if (className.contains("sophisticatedbackpacks.backpack.BackpackItem")) {
                return i;
            }
        }
        return -1;
    }

    private static void sendBackpackOpenPacket(Minecraft mc, int slotIndex) {
        try {
            Class<?> msgClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackOpenMessage");
            Object msg = msgClass.getConstructor(int.class).newInstance(slotIndex);
            Class<?> handlerClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler");
            Object handler = handlerClass.getField("INSTANCE").get(null);
            java.lang.reflect.Method sendMethod = handlerClass.getMethod("sendToServer", Object.class);
            sendMethod.invoke(handler, msg);
        } catch (Exception e) {
            MessageHolder.sendMessageUnchecked("[BII] 无法发送背包打开请求: " + e.getMessage());
            autoOpenBackpack = false;
            pendingPlans = null;
        }
    }

    public static void resetBatchState() {
    }

    private static boolean isTriggerJustPressed() {
        List<Integer> keys = LitematicaMixinMod.BII_INJECT_HOTKEY.getKeybind().getKeys();
        boolean pressed = false;
        if (keys != null) {
            Minecraft mc = Minecraft.m_91087_();
            long handle = mc.m_91268_().m_85439_();
            for (Integer keyCode : keys) {
                if (keyCode == null) continue;
                boolean down = keyCode < 0
                        ? GLFW.glfwGetMouseButton(handle, keyCode + 100) == 1
                        : keyCode > 0 && GLFW.glfwGetKey(handle, keyCode) == 1;
                if (down) {
                    pressed = true;
                    break;
                }
            }
        }
        boolean justPressed = pressed && !wasTriggerPressed;
        wasTriggerPressed = pressed;
        return justPressed;
    }

    private static Object getBackpackMenu(Minecraft mc) {
        try {
            return mGetStorageMenu.invoke(null, mc);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<net.minecraft.world.inventory.Slot> getRealSlots(Object menu) {
        try {
            return (List<net.minecraft.world.inventory.Slot>) mGetRealSlots.invoke(null, menu);
        } catch (Exception e) {
            return null;
        }
    }

    private static int countInBackpack(Object menu, ItemStack stack) {
        try {
            return (Integer) mCountItemInBackpack.invoke(null, menu, stack);
        } catch (Exception e) {
            return 0;
        }
    }

    private static int countInInventory(Inventory inv, Item targetItem) {
        int count = 0;
        for (int i = 0; i < inv.m_6643_(); i++) {
            ItemStack s = inv.m_8020_(i);
            if (!s.m_41619_() && s.m_41720_() == targetItem) {
                count += s.m_41613_();
            }
        }
        return count;
    }

    /**
     * 不依赖背包 UI 打开，直接通过 Capability 读取玩家背包中所有 Sophisticated Backpacks 的内容。
     */
    private static int countInPlayerBackpacksByCapability(Inventory inv, Item targetItem) {
        int count = 0;
        for (int i = 0; i < inv.m_6643_(); i++) {
            ItemStack stack = inv.m_8020_(i);
            if (stack.m_41619_()) continue;
            String className = stack.m_41720_().getClass().getName();
            if (!className.contains("sophisticatedbackpacks.backpack.BackpackItem")) continue;
            count += countInBackpackStack(stack, targetItem);
        }
        return count;
    }

    private static int countInBackpackStack(ItemStack backpackStack, Item targetItem) {
        try {
            net.minecraftforge.common.capabilities.Capability<net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper> cap =
                    net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper.getCapabilityInstance();
            net.minecraftforge.common.util.LazyOptional<net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper> opt =
                    backpackStack.getCapability(cap);
            if (!opt.isPresent()) return 0;
            net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper wrapper = opt.orElse(null);
            if (wrapper == null) return 0;
            net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler handler = wrapper.getInventoryHandler();
            if (handler == null) return 0;
            int slots = handler.getSlots();
            int count = 0;
            for (int s = 0; s < slots; s++) {
                ItemStack slotStack = handler.getStackInSlot(s);
                if (!slotStack.m_41619_() && slotStack.m_41720_() == targetItem) {
                    count += slotStack.m_41613_();
                }
            }
            return count;
        } catch (Exception e) {
            if (LitematicaMixinMod.DEBUG_MESSAGE.getBooleanValue()) {
                MessageHolder.sendMessageUnchecked("[BII-DEBUG] cap强类型失败: " + e.getClass().getSimpleName() + " " + e.getMessage());
            }
            return 0;
        }
    }
}
