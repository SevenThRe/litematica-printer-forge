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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
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
    private static boolean autoOpenBackpack = false;
    private static int autoOpenTicks = 0;
    private static List<InjectPlan> pendingPlans = null;

    /** Inject sort modes: 0 = Y ascending (bottom→top, default), 1 = Y descending (top→bottom), 2 = scan order */
    public static int lastSortMode = 0;

    public static void applySort(List<InjectPlan> plans, int sortMode) {
        if (sortMode == 0) {
            plans.sort(java.util.Comparator.comparingInt(p -> p.lowestY));
        } else if (sortMode == 1) {
            plans.sort((a, b) -> Integer.compare(b.lowestY, a.lowestY));
        }
    }

    public static String sortModeName(int sortMode) {
        switch (sortMode) {
            case 0: return "排序: Y升序 (底部→顶部)";
            case 1: return "排序: Y倒序 (顶部→底部)";
            default: return "排序: 扫描顺序";
        }
    }

    // NOTE: InjectPlan and InjectResultListener were moved to top-level classes in the
    // same package (InjectPlan.java / InjectResultListener.java) so Forge's
    // ModuleClassLoader resolves them reliably. They are referenced unqualified below.

    private static void resolve(Minecraft mc) {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> backpackUtil = Class.forName("com.backpackinjector.backpack.BackpackUtil");
            mIsSophisticatedLoaded = backpackUtil.getMethod("isSophisticatedLoaded");
            mGetStorageMenu = backpackUtil.getMethod("getStorageMenu", Minecraft.class);
            // getRealInventorySlots is private static, must use getDeclaredMethod
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
        // Create blueprints carried by the player take priority as the material source; when the
        // player owns none, the litematica placements are used exactly as before.
        if (LitematicaMixinMod.BII_CREATE_BLUEPRINT.getBooleanValue() && mc != null && mc.f_91074_ != null) {
            List<BlueprintEntry> blueprints = CreateBlueprintFix.collectBlueprints(mc);
            if (blueprints.size() == 1) {
                openGuiForBlueprint(mc, blueprints.get(0));
                return;
            }
            if (blueprints.size() > 1) {
                MessageHolder.sendMessageUnchecked("[BII] 找到 " + blueprints.size() + " 张 Create 蓝图");
                mc.m_91152_(new xyz.jxmm.litematica_printer_forge.gui.BlueprintPickerScreen(blueprints));
                return;
            }
        }

        // Prefer the placement currently selected in litematica, so BII always matches
        // what the user has selected in the schematic browser.
        SchematicPlacement selected = DataManager.getSchematicPlacementManager().getSelectedSchematicPlacement();
        if (selected != null && selected.isEnabled()) {
            openGuiFor(mc, selected);
            return;
        }
        List<SchematicPlacement> allPlacements = DataManager.getSchematicPlacementManager().getAllSchematicsPlacements();
        if (allPlacements == null || allPlacements.isEmpty()) {
            MessageHolder.sendMessageUnchecked("[BII] 未选择原理图");
            return;
        }
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
        if (enabled.size() == 1) {
            openGuiFor(mc, enabled.get(0));
        } else {
            mc.m_91152_(new xyz.jxmm.litematica_printer_forge.gui.SchematicPickerScreen(enabled));
        }
    }

    /** Pending "open the backpack, then show this GUI with the synced menu" action. */
    private static java.util.function.Consumer<Object> pendingGuiAction = null;
    private static int pendingShowGuiTicks = 0;
    private static boolean waitingToShowGui = false;
    // After the menu is detected, wait for slot data sync (OpenMenu and ContainerSetContent packets do not arrive at the same time)
    private static int menuReadyDelayTicks = 0;
    private static final int MENU_SYNC_DELAY = 10;

    public static void openGuiFor(Minecraft mc, SchematicPlacement placement) {
        openGuiWhenBackpackReady(mc, menu -> showInjectGui(mc, placement, menu));
    }

    /** Create blueprint variant of {@link #openGuiFor}: same backpack dance, different material source. */
    public static void openGuiForBlueprint(Minecraft mc, BlueprintEntry entry) {
        openGuiWhenBackpackReady(mc, menu -> showInjectGuiForBlueprint(mc, entry, menu));
    }

    private static void openGuiWhenBackpackReady(Minecraft mc, java.util.function.Consumer<Object> showGui) {
        Object menu = getBackpackMenu(mc);
        if (menu != null && menuReadyDelayTicks >= MENU_SYNC_DELAY) {
            showGui.accept(menu);
            return;
        }

        Inventory inv = mc.f_91074_.m_150109_();
        int backpackSlot = findBackpackSlot(inv);
        if (backpackSlot < 0) {
            MessageHolder.sendMessageUnchecked("[BII] 背包中没有找到背包物品");
            return;
        }
        mc.m_91152_(null);
        pendingGuiAction = showGui;
        pendingShowGuiTicks = 0;
        waitingToShowGui = true;
        sendBackpackOpenPacket(mc, backpackSlot);
        MessageHolder.sendMessageUnchecked("[BII] 正在打开背包...");
    }

    private static void showInjectGui(Minecraft mc, SchematicPlacement placement, Object menu) {
        Inventory inv = mc.f_91074_.m_150109_();

        // Reads directly from the schematic container (independent of world load range).
        // The coordinate formula must exactly match LitematicaSchematic.placeBlocksToWorld:
        // getTransformedPlacementPosition (placement rotation/mirror) + transformed sub-region offset + placement origin.
        // Omitting the last two steps scans the world origin, making the missing-material report inconsistent with the verifier.
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
                        // Unloaded chunks count as missing (stocking semantics: those blocks will be placed eventually);
                        // loaded chunks are judged with the same criteria as the verifier (MISSING + WRONG_BLOCK)
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
                        addMaterialFor(missingMap, minYMap, schemState, worldPos.m_123342_());
                    }
                }
            }
        }
        if (missingMap.isEmpty()) {
            MessageHolder.sendMessageUnchecked("[BII] 「" + placement.getName() + "」没有缺失材料");
            return;
        }
        openInjectScreen(mc, menu, missingMap, minYMap, placement.getName());
    }

    /** Create blueprint counterpart of {@link #showInjectGui(SchematicPlacement...)}. */
    private static void showInjectGuiForBlueprint(Minecraft mc, BlueprintEntry entry, Object menu) {
        Map<Item, Integer> missingMap = new LinkedHashMap<>();
        Map<Item, Integer> minYMap = new HashMap<>();
        if (!CreateBlueprintFix.buildMaterials(mc, entry, missingMap, minYMap)) {
            MessageHolder.sendMessageUnchecked("[BII] 无法读取蓝图「" + entry.name
                    + "」：缺少结构文件 schematics/*.nbt（在游戏内预览一次蓝图可让客户端下载）");
            return;
        }
        if (missingMap.isEmpty()) {
            MessageHolder.sendMessageUnchecked("[BII] 蓝图「" + entry.name + "」没有缺失材料");
            return;
        }
        MessageHolder.sendMessageUnchecked("[BII] 蓝图「" + entry.name + "」共 " + missingMap.size() + " 种材料待注入");
        openInjectScreen(mc, menu, missingMap, minYMap, "蓝图:" + entry.name);
    }

    /**
     * Turns a "needed materials" map into {@link InjectPlan}s (subtracting what the player
     * inventory and the open backpack already hold) and opens the injection GUI.
     */
    private static void openInjectScreen(Minecraft mc, Object menu,
                                         Map<Item, Integer> missingMap, Map<Item, Integer> minYMap,
                                         String title) {
        Inventory inv = mc.f_91074_.m_150109_();

        // Count from the open backpack menu. plans covers all short materials (same list as the verifier's missing report);
        // amount defaults to the total shortage (needed - inInv) so the user sees how much is still missing per item;
        // doInject truncates by actual backpack stock at inject time (out-of-stock items are skipped automatically).
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
            MessageHolder.sendMessageUnchecked("[BII] 「" + title + "」材料已足够");
            return;
        }

        applySort(plans, lastSortMode);

        mc.m_91152_(new BackpackInjectScreen(plans, title));
    }

    /**
     * Programmatic entry for the auto build director: same open/inject path as
     * {@link #inject}, but without any GUI, and the verified deltas are reported
     * back through the listener once the server syncs (or empty maps if the menu
     * closed before verification).
     */
    private static InjectResultListener pendingListener = null;

    public static void ensureMaterials(List<InjectPlan> plans, Minecraft mc, InjectResultListener listener) {
        pendingListener = listener;
        inject(plans, lastSortMode, mc);
    }

    /** True while a programmatic/manual injection cycle is in flight. */
    public static boolean isBusy() {
        return pendingSnapshot != null || waitingToShowGui || autoOpenBackpack;
    }

    /** Drops a pending result listener without touching the injection flow. */
    public static void clearProgrammaticState() {
        pendingListener = null;
    }

    /**
     * The plant held by a potted plant block, as a storable item; null for an empty pot
     * or when the content block has no item form.
     */
    public static Item pottedFlowerItem(BlockState state) {
        if (!(state.m_60734_() instanceof FlowerPotBlock)) {
            return null;
        }
        Block content = ((FlowerPotBlock) state.m_60734_()).m_53560_();
        if (content == null) {
            return null;
        }
        Item item = content.m_5456_();
        return item == Items.f_41852_ ? null : item;
    }

    /**
     * Adds the storable items needed for one schematic block into the material maps.
     *
     * Potted plants have no item form of their own (asItem() == AIR), so they expand into the
     * empty flower pot plus the plant they hold; blocks without an item (air, structure void,
     * ...) are skipped. Shared by the litematica scan and the Create blueprint reader.
     */
    public static void addMaterialFor(Map<Item, Integer> needed, Map<Item, Integer> minY,
                                      BlockState schemState, int worldY) {
        if (schemState == null || schemState.m_60795_()) return;
        if (schemState.m_60734_() instanceof FlowerPotBlock) {
            needed.merge(Items.f_42618_, 1, Integer::sum);
            minY.merge(Items.f_42618_, worldY, Math::min);
            Item flower = pottedFlowerItem(schemState);
            if (flower != null) {
                needed.merge(flower, 1, Integer::sum);
                minY.merge(flower, worldY, Math::min);
            }
            return;
        }
        Item item = schemState.m_60734_().m_5456_();
        if (item == Items.f_41852_) return;
        item = BlockReplacer.resolve(item);
        if (item == Items.f_41852_) return;
        needed.merge(item, 1, Integer::sum);
        minY.merge(item, worldY, Math::min);
    }

    public static boolean isBackpackStack(ItemStack stack) {
        return stack != null && !stack.m_41619_()
                && stack.m_41720_().getClass().getName().contains("sophisticatedbackpacks.backpack.BackpackItem");
    }

    /**
     * Visits every stack stored inside a Sophisticated Backpack item through its capability,
     * so backpack contents can be inspected without opening the backpack GUI. Fails silently
     * when Sophisticated Backpacks is not installed.
     */
    public static void forEachBackpackStack(ItemStack backpackStack,
                                            java.util.function.BiConsumer<Integer, ItemStack> visitor) {
        try {
            net.minecraftforge.common.capabilities.Capability<net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper> cap =
                    net.p3pp3rf1y.sophisticatedbackpacks.api.CapabilityBackpackWrapper.getCapabilityInstance();
            net.minecraftforge.common.util.LazyOptional<net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper> opt =
                    backpackStack.getCapability(cap);
            if (!opt.isPresent()) return;
            net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper wrapper = opt.orElse(null);
            if (wrapper == null) return;
            net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler handler = wrapper.getInventoryHandler();
            if (handler == null) return;
            int slots = handler.getSlots();
            for (int s = 0; s < slots; s++) {
                visitor.accept(s, handler.getStackInSlot(s));
            }
        } catch (Throwable t) {
            if (LitematicaMixinMod.DEBUG_MESSAGE.getBooleanValue()) {
                MessageHolder.sendMessageUnchecked("[BII-DEBUG] 读取背包内容失败: " + t);
            }
        }
    }

    public static void inject(List<InjectPlan> plans, int sortMode, Minecraft mc) {
        if (!isAvailable(mc)) {
            MessageHolder.sendMessageUnchecked("[BII] 背包模组不可用");
            return;
        }
        lastSortMode = sortMode;
        applySort(plans, sortMode);

        // Fast path for an already-open, stable backpack GUI (the BII screen usually causes the backpack to close)
        Object menu = getBackpackMenu(mc);
        if (menu != null && menuReadyDelayTicks >= MENU_SYNC_DELAY) {
            doInject(plans, mc, menu);
            return;
        }

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
        // "Inject" semantics = conjure missing materials out of thin air, all via SetGhostSlotMessage (direct server-side Slot.set write):
        // 1) Up to one stack (maxStackSize) per material, ghost-written into empty player inventory slots
        // 2) Remaining shortage ghost-injected into empty backpack storage slots (stock the backpack for later)
        // Total created = shortage. The QUICK_MOVE-click approach was abandoned (server silently rejects it, cause unknown; ghost is proven reliable)
        //
        // Do not log "done" per send count — ghost is a one-way packet with no receipt and the server may reject it.
        // Snapshot all menu slots before sending; 15 ticks later (once server state syncs back) log actual results from slot deltas.
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

        Map<Integer, ItemStack> snapshot = snapshotMenu(absMenu);
        Map<Item, Integer> reqTaken = new LinkedHashMap<>();
        Map<Item, Integer> reqStored = new LinkedHashMap<>();

        List<Integer> emptySlotIds = new ArrayList<>();
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

        int totalTaken = 0;
        int totalStored = 0;
        int sentRequests = 0;
        int invIdx = 0;
        int bpIdx = 0;

        for (InjectPlan plan : plans) {
            if (plan.amount <= 0) continue;
            int maxPer = plan.stack.m_41741_();
            int take = Math.min(plan.amount, maxPer);
            int got = 0;
            while (got < take && invIdx < invSlotIds.size()) {
                int n = Math.min(take - got, maxPer);
                if (!sendGhost(plan.stack, n, invSlotIds.get(invIdx++))) {
                    MessageHolder.sendMessageUnchecked("[BII] ghost 写入失败（协议不可用），停止注入");
                    return;
                }
                got += n;
                sentRequests++;
            }
            int toStore = plan.amount - got;
            int put = 0;
            while (put < toStore && bpIdx < emptySlotIds.size()) {
                int n = Math.min(toStore - put, maxPer);
                if (!sendGhost(plan.stack, n, emptySlotIds.get(bpIdx++))) {
                    MessageHolder.sendMessageUnchecked("[BII] ghost 写入失败（协议不可用），停止注入");
                    return;
                }
                put += n;
                sentRequests++;
            }
            if (got > 0) reqTaken.merge(plan.stack.m_41720_(), got, Integer::sum);
            if (put > 0) reqStored.merge(plan.stack.m_41720_(), put, Integer::sum);
            totalTaken += got;
            totalStored += put;
        }

        if (sentRequests == 0) {
            MessageHolder.sendMessageUnchecked("[BII] 没有可注入的请求（材料已足够或槽位不足）");
            return;
        }
        pendingSnapshot = snapshot;
        pendingReqTaken = reqTaken;
        pendingReqStored = reqStored;
        verifyTicksLeft = VERIFY_DELAY_TICKS;
        MessageHolder.sendMessageUnchecked("[BII] 已发送 " + sentRequests + " 条注入请求（物品栏 "
                + totalTaken + " / 背包 " + totalStored + "），等待服务端确认...");
    }

    // ===== Injection result verification (logs from slot deltas synced back from the server, not send counts) =====
    private static Map<Integer, ItemStack> pendingSnapshot = null;
    private static Map<Item, Integer> pendingReqTaken = null;
    private static Map<Item, Integer> pendingReqStored = null;
    private static int verifyTicksLeft = 0;
    private static final int VERIFY_DELAY_TICKS = 15;

    private static Map<Integer, ItemStack> snapshotMenu(net.minecraft.world.inventory.AbstractContainerMenu absMenu) {
        Map<Integer, ItemStack> snap = new HashMap<>();
        for (net.minecraft.world.inventory.Slot s : absMenu.f_38839_) {
            ItemStack st = s.m_7993_();
            snap.put(s.f_40219_, st.m_41619_() ? ItemStack.f_41583_ : st.m_41777_());
        }
        return snap;
    }

    private static void finishVerify(Minecraft mc) {
        Map<Integer, ItemStack> before = pendingSnapshot;
        Map<Item, Integer> reqTaken = pendingReqTaken;
        Map<Item, Integer> reqStored = pendingReqStored;
        InjectResultListener listener = pendingListener;
        pendingListener = null;
        pendingSnapshot = null;
        pendingReqTaken = null;
        pendingReqStored = null;
        boolean debug = LitematicaMixinMod.DEBUG_MESSAGE.getBooleanValue();
        Object menuObj = getBackpackMenu(mc);
        if (!(menuObj instanceof net.minecraft.world.inventory.AbstractContainerMenu absMenu)) {
            MessageHolder.sendMessageUnchecked("[BII] 注入请求已发送，但背包界面已关闭，无法验证实际结果");
            if (listener != null) {
                listener.onResult(java.util.Collections.emptyMap(), java.util.Collections.emptyMap());
            }
            return;
        }
        net.minecraft.world.Container playerInv = mc.f_91074_.m_150109_();
        Map<Item, Integer> gotInv = new LinkedHashMap<>();
        Map<Item, Integer> gotStored = new LinkedHashMap<>();
        for (net.minecraft.world.inventory.Slot s : absMenu.f_38839_) {
            ItemStack was = before.getOrDefault(s.f_40219_, ItemStack.f_41583_);
            ItemStack now = s.m_7993_();
            int delta = 0;
            if (!now.m_41619_() && now.m_41720_() == was.m_41720_()) {
                delta = now.m_41613_() - was.m_41613_();
            } else if (!now.m_41619_() && was.m_41619_()) {
                delta = now.m_41613_();
            }
            if (delta > 0) {
                (s.f_40218_ == playerInv ? gotInv : gotStored).merge(now.m_41720_(), delta, Integer::sum);
            }
        }
        int totalReq = 0;
        int totalGot = 0;
        int confirmedKinds = 0;
        java.util.Set<Item> allReq = new java.util.LinkedHashSet<>();
        allReq.addAll(reqTaken.keySet());
        allReq.addAll(reqStored.keySet());
        List<String> partial = new ArrayList<>();
        for (Item it : allReq) {
            int req = reqTaken.getOrDefault(it, 0) + reqStored.getOrDefault(it, 0);
            int got = gotInv.getOrDefault(it, 0) + gotStored.getOrDefault(it, 0);
            totalReq += req;
            totalGot += Math.min(got, req);
            if (got >= req) {
                confirmedKinds++;
            } else {
                partial.add(new ItemStack(it).m_41786_().getString() + "(" + got + "/" + req + ")");
            }
            if (debug) {
                MessageHolder.sendMessageUnchecked("[BII] 验证: " + new ItemStack(it).m_41786_().getString()
                        + " 物品栏+" + gotInv.getOrDefault(it, 0) + " 背包+" + gotStored.getOrDefault(it, 0)
                        + "（请求 " + req + "）");
            }
        }
        if (totalReq <= 0) {
            MessageHolder.sendMessageUnchecked("[BII] 无待验证请求");
        } else if (!partial.isEmpty()) {
            MessageHolder.sendMessageUnchecked("[BII] 部分注入：实际 " + totalGot + " / 请求 " + totalReq
                    + "；未生效: " + String.join("、", partial));
        } else {
            MessageHolder.sendMessageUnchecked("[BII] 注入确认：" + confirmedKinds + " 种共 " + totalGot + " 个已实际写入");
        }
        if (listener != null) {
            listener.onResult(gotInv, gotStored);
        }
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

    public static void onClientTick(Minecraft mc) {
        Object currentMenu = getBackpackMenu(mc);
        if (currentMenu != null) {
            menuReadyDelayTicks++;
        } else {
            menuReadyDelayTicks = 0;
        }

        if (pendingSnapshot != null) {
            verifyTicksLeft--;
            if (verifyTicksLeft <= 0) {
                finishVerify(mc);
            }
            return;
        }

        if (waitingToShowGui) {
            pendingShowGuiTicks++;
            if (pendingShowGuiTicks > 60) {
                waitingToShowGui = false;
                pendingGuiAction = null;
                MessageHolder.sendMessageUnchecked("[BII] 打开背包超时");
                return;
            }
            if (currentMenu != null && menuReadyDelayTicks >= MENU_SYNC_DELAY) {
                waitingToShowGui = false;
                java.util.function.Consumer<Object> action = pendingGuiAction;
                pendingGuiAction = null;
                if (action != null) action.accept(currentMenu);
            }
            return;
        }

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
        }
    }

    public static int findBackpackSlot(Inventory inv) {
        for (int i = 0; i < inv.m_6643_(); i++) {
            if (isBackpackStack(inv.m_8020_(i))) {
                return i;
            }
        }
        return -1;
    }

    public static void sendBackpackOpenPacket(Minecraft mc, int slotIndex) {
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

    public static Object getBackpackMenu(Minecraft mc) {
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
     * Reads the contents of all Sophisticated Backpacks in the player inventory via Capability,
     * without opening the backpack UI.
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
