package xyz.jxmm.litematica_printer_forge.utils;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;

public class MissingBlockEsp {
    public static class MissingEntry {
        public final BlockPos pos;
        public final Item item;
        public final net.minecraft.world.level.block.Block block;
        // Expected block after replacement (coralReplace mapping target), for real-time comparison
        public final net.minecraft.world.level.block.Block expected;

        public MissingEntry(BlockPos pos, Item item, net.minecraft.world.level.block.Block block) {
            this(pos, item, block, BlockReplacer.resolveBlock(block));
        }

        public MissingEntry(BlockPos pos, Item item, net.minecraft.world.level.block.Block block, net.minecraft.world.level.block.Block expected) {
            this.pos = pos;
            this.item = item;
            this.block = block;
            this.expected = expected;
        }
    }

    private static volatile List<MissingEntry> cached = Collections.emptyList();

    // ===== Incremental sharded scan state =====
    // A full schematic can hold tens to hundreds of thousands of blocks; a one-shot scan causes periodic stutters.
    // Instead, process BLOCK_BUDGET blocks per tick, spreading a full scan over multiple ticks.
    private static final int BLOCK_BUDGET = 16384;

    private static final class ScanTask {
        final SchematicPlacement placement;
        final fi.dy.masa.litematica.schematic.placement.SubRegionPlacement sub;
        final fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer container;
        final BlockPos regionTransformed;
        final BlockPos origin;
        ScanTask(SchematicPlacement placement,
                 fi.dy.masa.litematica.schematic.placement.SubRegionPlacement sub,
                 fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer container,
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
    private static String placementSignature = "";

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ == null || mc.f_91073_ == null) {
            if (!cached.isEmpty()) {
                cached = Collections.emptyList();
            }
            resetScan();
            return;
        }
        if (!LitematicaMixinMod.ESP_HIGHLIGHT_MISSING.getBooleanValue()) {
            if (!cached.isEmpty()) {
                cached = Collections.emptyList();
            }
            resetScan();
            return;
        }
        String sig = buildPlacementSignature();
        if (!sig.equals(placementSignature)) {
            placementSignature = sig;
            initScan(mc);
        }
        if (tasks.isEmpty()) {
            return;
        }
        scanStep(mc);
    }

    private static String buildPlacementSignature() {
        List<SchematicPlacement> all = DataManager.getSchematicPlacementManager().getAllSchematicsPlacements();
        StringBuilder sb = new StringBuilder();
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

    private static void resetScan() {
        tasks = Collections.emptyList();
        taskIdx = 0;
        bx = by = bz = 0;
        accum.clear();
        placementSignature = "";
    }

    private static void initScan(Minecraft mc) {
        ArrayList<ScanTask> list = new ArrayList<>();
        List<SchematicPlacement> all = DataManager.getSchematicPlacementManager().getAllSchematicsPlacements();
        for (SchematicPlacement placement : all) {
            if (!placement.isEnabled()) continue;
            fi.dy.masa.litematica.schematic.LitematicaSchematic schematic = placement.getSchematic();
            if (schematic == null) continue;
            for (java.util.Map.Entry<String, fi.dy.masa.litematica.schematic.placement.SubRegionPlacement> entry
                    : placement.getEnabledRelativeSubRegionPlacements().entrySet()) {
                fi.dy.masa.litematica.schematic.placement.SubRegionPlacement sub = entry.getValue();
                fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer container =
                        schematic.getSubRegionContainer(entry.getKey());
                if (container == null) continue;
                BlockPos regionTransformed = fi.dy.masa.litematica.util.PositionUtils
                        .getTransformedBlockPos(sub.getPos(), placement.getMirror(), placement.getRotation());
                list.add(new ScanTask(placement, sub, container, regionTransformed, placement.getOrigin()));
            }
        }
        tasks = list;
        taskIdx = 0;
        bx = by = bz = 0;
        accum = new ArrayList<>();
    }

    private static void scanStep(Minecraft mc) {
        Level world = mc.f_91073_;
        int worldBottomY = world.m_141937_();
        int worldTopY = world.m_151558_();
        int budget = BLOCK_BUDGET;

        while (budget > 0 && taskIdx < tasks.size()) {
            ScanTask t = tasks.get(taskIdx);
            net.minecraft.core.Vec3i size = t.container.getSize();
            int perTask = Math.min(budget, 4096);
            int processed = 0;
            while (processed < perTask) {
                BlockState schemState = t.container.get(bx, by, bz);
                boolean advanced = false;
                if (!schemState.m_60795_()) {
                    BlockPos worldPos = fi.dy.masa.litematica.util.PositionUtils
                            .getTransformedPlacementPosition(new BlockPos(bx, by, bz), t.placement, t.sub)
                            .m_121955_((net.minecraft.core.Vec3i) t.regionTransformed)
                            .m_121955_((net.minecraft.core.Vec3i) t.origin);
                    boolean inY = worldPos.m_123342_() >= worldBottomY && worldPos.m_123342_() <= worldTopY;
                    if (!inY) {
                    } else if (world.m_46805_(worldPos)) {
                        // Loaded chunk: judged with the same criteria as the verifier
                        BlockState worldState = world.m_8055_(worldPos);
                        if (worldState.m_60795_() || worldState.m_247087_()
                                || (worldState.m_60734_() != schemState.m_60734_()
                                    && worldState.m_60734_() != BlockReplacer.resolveBlock(schemState.m_60734_()))) {
                            // A falling block with free space below in the world AND no support block in the
                            // schematic can never be placed in vanilla survival; do not highlight it
                            BlockState belowWorld = world.m_8055_(worldPos.m_7495_());
                            WorldSchematic schemWorld = SchematicWorldHandler.getSchematicWorld();
                            BlockState belowSchem = schemWorld == null ? null : schemWorld.m_8055_(worldPos.m_7495_());
                            boolean neverBuildable = schemState.m_60734_() instanceof FallingBlock
                                    && belowSchem != null && belowSchem.m_60795_()
                                    && (belowWorld.m_60795_() || !belowWorld.m_60819_().m_76178_());
                            if (!neverBuildable) {
                                Item item = schemState.m_60734_().m_5456_();
                                if (item != Items.f_41852_) {
                                    item = BlockReplacer.resolve(item);
                                    if (item != Items.f_41852_) {
                                        accum.add(new MissingEntry(worldPos.m_7949_(), item, schemState.m_60734_()));
                                    }
                                }
                            }
                        }
                    } else {
                        // Unloaded chunk: counted as missing (schematic requires placement but it cannot be verified)
                        Item item = schemState.m_60734_().m_5456_();
                        if (item != Items.f_41852_) {
                            item = BlockReplacer.resolve(item);
                            if (item != Items.f_41852_) {
                                accum.add(new MissingEntry(worldPos.m_7949_(), item, schemState.m_60734_()));
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
            double px = mc.f_91074_.m_20185_();
            double py = mc.f_91074_.m_20186_();
            double pz = mc.f_91074_.m_20189_();
            accum.sort(Comparator.comparingDouble(e -> {
                double dx = (double) e.pos.m_123341_() + 0.5 - px;
                double dy = (double) e.pos.m_123342_() + 0.5 - py;
                double dz = (double) e.pos.m_123343_() + 0.5 - pz;
                return dx * dx + dy * dy + dz * dz;
            }));
            int max = LitematicaMixinMod.ESP_MAX_RENDER.getIntegerValue();
            if (max > 0 && accum.size() > max) {
                accum = new ArrayList<>(accum.subList(0, max));
            }
            cached = Collections.unmodifiableList(accum);
            initScan(mc);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (!LitematicaMixinMod.ESP_HIGHLIGHT_MISSING.getBooleanValue()) {
            return;
        }
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ == null || mc.f_91073_ == null) {
            return;
        }
        List<MissingEntry> entries = cached;
        if (entries.isEmpty()) {
            return;
        }
        boolean verifyMode = LitematicaMixinMod.ESP_VERIFICATION_MODE.getBooleanValue();
        Set<Item> heldFilter = null;
        if (!verifyMode) {
            heldFilter = new HashSet<>();
            if (LitematicaMixinMod.ESP_HIGHLIGHT_BY_INVENTORY.getBooleanValue()) {
                // Highlight by the set of items in the player inventory (the printer auto-swaps the held item, so held-item highlighting flickers; the inventory set is stable)
                net.minecraft.world.entity.player.Inventory inv = mc.f_91074_.m_150109_();
                for (int i = 0; i < inv.m_6643_(); i++) {
                    ItemStack st = inv.m_8020_(i);
                    if (!st.m_41619_()) {
                        heldFilter.add(st.m_41720_());
                    }
                }
            } else {
                ItemStack mainHand = mc.f_91074_.m_21205_();
                if (mainHand.m_41720_() instanceof BlockItem) {
                    heldFilter.add(mainHand.m_41720_());
                }
                ItemStack offHand = mc.f_91074_.m_21206_();
                if (offHand.m_41720_() instanceof BlockItem) {
                    heldFilter.add(offHand.m_41720_());
                }
            }
            if (heldFilter.isEmpty()) {
                return;
            }
        }
        int color = verifyMode ? LitematicaMixinMod.ESP_MISSING_COLOR.getIntegerValue()
                : LitematicaMixinMod.ESP_HELD_COLOR.getIntegerValue();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = color >>> 24;
        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.f_91063_.m_109153_().m_90583_();
        Level world = mc.f_91073_;
        poseStack.m_85836_();
        poseStack.m_85837_(-cam.f_82479_, -cam.f_82480_, -cam.f_82481_);
        Matrix4f matrix = poseStack.m_85850_().m_252922_();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> GameRenderer.m_172811_());
        Tesselator tesselator = Tesselator.m_85913_();
        BufferBuilder buffer = tesselator.m_85915_();
        buffer.m_166779_(VertexFormat.Mode.QUADS, DefaultVertexFormat.f_85815_);
        for (MissingEntry entry : entries) {
            if (heldFilter != null && !heldFilter.contains(entry.item)) continue;
            // Real-time check: if the block is already correct (original or replaced) in a loaded chunk, skip immediately without waiting for the next scan round
            if (world.m_46805_(entry.pos)) {
                Block wb = world.m_8055_(entry.pos).m_60734_();
                if (wb == entry.block || wb == entry.expected) continue;
            }
            addBoxQuads(buffer, matrix, entry.pos, r, g, b, a);
        }
        tesselator.m_85914_();
        RenderSystem.lineWidth(1.5F);
        buffer.m_166779_(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
        for (MissingEntry entry : entries) {
            if (heldFilter != null && !heldFilter.contains(entry.item)) continue;
            if (world.m_46805_(entry.pos)) {
                Block wb = world.m_8055_(entry.pos).m_60734_();
                if (wb == entry.block || wb == entry.expected) continue;
            }
            addBoxEdges(buffer, matrix, entry.pos, r, g, b, 255);
        }
        tesselator.m_85914_();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        poseStack.m_85849_();
    }

    private static void vertex(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, int r, int g, int b, int a) {
        buffer.m_252986_(matrix, x, y, z).m_6122_(r, g, b, a).m_5752_();
    }

    private static void addBoxQuads(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, int r, int g, int b, int a) {
        float x0 = (float)pos.m_123341_() - 0.001F;
        float y0 = (float)pos.m_123342_() - 0.001F;
        float z0 = (float)pos.m_123343_() - 0.001F;
        float x1 = x0 + 1.002F;
        float y1 = y0 + 1.002F;
        float z1 = z0 + 1.002F;
        vertex(buffer, matrix, x0, y0, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y0, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y0, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y0, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y1, z0, r, g, b, a);
        vertex(buffer, matrix, x0, y1, z1, r, g, b, a);
        vertex(buffer, matrix, x1, y1, z1, r, g, b, a);
        vertex(buffer, matrix, x1, y1, z0, r, g, b, a);
        vertex(buffer, matrix, x0, y0, z0, r, g, b, a);
        vertex(buffer, matrix, x0, y1, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y1, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y0, z0, r, g, b, a);
        vertex(buffer, matrix, x0, y0, z1, r, g, b, a);
        vertex(buffer, matrix, x1, y0, z1, r, g, b, a);
        vertex(buffer, matrix, x1, y1, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y1, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y0, z0, r, g, b, a);
        vertex(buffer, matrix, x0, y0, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y1, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y1, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y0, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y1, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y1, z1, r, g, b, a);
        vertex(buffer, matrix, x1, y0, z1, r, g, b, a);
    }

    private static void addBoxEdges(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, int r, int g, int b, int a) {
        float x0 = (float)pos.m_123341_() - 0.001F;
        float y0 = (float)pos.m_123342_() - 0.001F;
        float z0 = (float)pos.m_123343_() - 0.001F;
        float x1 = x0 + 1.002F;
        float y1 = y0 + 1.002F;
        float z1 = z0 + 1.002F;
        vertex(buffer, matrix, x0, y0, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y0, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y0, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y0, z1, r, g, b, a);
        vertex(buffer, matrix, x1, y0, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y0, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y0, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y0, z0, r, g, b, a);
        vertex(buffer, matrix, x0, y1, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y1, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y1, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y1, z1, r, g, b, a);
        vertex(buffer, matrix, x1, y1, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y1, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y1, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y1, z0, r, g, b, a);
        vertex(buffer, matrix, x0, y0, z0, r, g, b, a);
        vertex(buffer, matrix, x0, y1, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y0, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y1, z0, r, g, b, a);
        vertex(buffer, matrix, x1, y0, z1, r, g, b, a);
        vertex(buffer, matrix, x1, y1, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y0, z1, r, g, b, a);
        vertex(buffer, matrix, x0, y1, z1, r, g, b, a);
    }
}
