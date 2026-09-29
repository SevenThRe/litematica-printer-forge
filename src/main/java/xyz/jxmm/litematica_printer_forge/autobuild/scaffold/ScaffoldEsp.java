package xyz.jxmm.litematica_printer_forge.autobuild.scaffold;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;

/**
 * ESP overlay for auto-build scaffolding (T14): every marked scaffold cell is
 * boxed in the configurable autoBuildScaffoldColor so the player can see what
 * will be dismantled during CLEANUP. Rendered through walls, same technique as
 * MissingBlockEsp (AFTER_TRANSLUCENT_BLOCKS, depth-test disabled).
 */
public final class ScaffoldEsp {
    private ScaffoldEsp() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (!LitematicaMixinMod.AUTO_BUILD_SCAFFOLD_ESP.getBooleanValue()) {
            return;
        }
        if (!ScaffoldManager.isJobActive()
                && !ScaffoldManager.isTeardownActive()
                && ScaffoldManager.getMarkedCount() == 0) {
            return;
        }
        Minecraft mc = Minecraft.m_91087_();
        if (mc.f_91074_ == null || mc.f_91073_ == null) {
            return;
        }
        List<BlockPos> positions = ScaffoldManager.getMarkedPositions();
        if (positions.isEmpty()) {
            return;
        }
        int color = LitematicaMixinMod.AUTO_BUILD_SCAFFOLD_COLOR.getIntegerValue();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >>> 24) == 0 ? 90 : color >>> 24;
        PoseStack poseStack = event.getPoseStack();
        Vec3 cam = mc.f_91063_.m_109153_().m_90583_();
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
        for (BlockPos pos : positions) {
            addBoxQuads(buffer, matrix, pos, r, g, b, a);
        }
        tesselator.m_85914_();
        RenderSystem.lineWidth(1.5F);
        buffer.m_166779_(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.f_85815_);
        for (BlockPos pos : positions) {
            addBoxEdges(buffer, matrix, pos, r, g, b, 255);
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
        float x0 = (float) pos.m_123341_() - 0.001F;
        float y0 = (float) pos.m_123342_() - 0.001F;
        float z0 = (float) pos.m_123343_() - 0.001F;
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
        float x0 = (float) pos.m_123341_() - 0.001F;
        float y0 = (float) pos.m_123342_() - 0.001F;
        float z0 = (float) pos.m_123343_() - 0.001F;
        float x1 = x0 + 1.002F;
        float y1 = y0 + 1.002F;
        float z1 = z0 + 1.002F;
        // 12 edges
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
