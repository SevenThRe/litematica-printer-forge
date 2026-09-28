package xyz.jxmm.litematica_printer_forge.gui;

import fi.dy.masa.litematica.schematic.placement.SchematicPlacement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xyz.jxmm.litematica_printer_forge.utils.BackpackInjector;

import java.util.List;

public class SchematicPickerScreen extends Screen {
    private final List<SchematicPlacement> placements;
    private int scroll = 0;
    private static final int ROW_HEIGHT = 24;
    private static final int VISIBLE_ROWS = 8;
    private static final int LIST_WIDTH = 360;
    private static final int LIST_X = 10;
    private static final int LIST_Y = 40;

    public SchematicPickerScreen(List<SchematicPlacement> placements) {
        super(Component.m_237115_("选择原理图"));
        this.placements = placements;
    }

    @Override
    protected void m_7856_() {
        super.m_7856_();
        int btnY = LIST_Y + VISIBLE_ROWS * ROW_HEIGHT + 10;
        this.m_142416_(Button.m_253074_(Component.m_237115_("取消"), b -> this.m_7379_())
                .m_252987_(LIST_X, btnY, 80, 20).m_253136_());
    }

    @Override
    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        graphics.m_280056_(this.f_96547_, "选择要注入材料的原理图（共 " + placements.size() + " 张）", LIST_X, 10, 0xFFFFFF, false);

        int end = Math.min(placements.size(), scroll + VISIBLE_ROWS);
        for (int i = scroll; i < end; i++) {
            SchematicPlacement p = placements.get(i);
            int y = LIST_Y + (i - scroll) * ROW_HEIGHT;
            boolean hovered = mouseX >= LIST_X && mouseX <= LIST_X + LIST_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT - 2;
            graphics.m_280509_(LIST_X, y, LIST_X + LIST_WIDTH, y + ROW_HEIGHT - 2, hovered ? 0x80FFFFFF : 0x80000000);
            graphics.m_280056_(this.f_96547_, p.getName(), LIST_X + 6, y + 7, hovered ? 0xFFFF55 : 0xFFFFFF, false);
        }

        if (placements.size() > VISIBLE_ROWS) {
            graphics.m_280056_(this.f_96547_, "滚动: " + (scroll + 1) + "-" + end + " / " + placements.size(), LIST_X + LIST_WIDTH + 10, LIST_Y, 0xAAAAAA, false);
        }
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean m_6375_(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int end = Math.min(placements.size(), scroll + VISIBLE_ROWS);
            for (int i = scroll; i < end; i++) {
                int y = LIST_Y + (i - scroll) * ROW_HEIGHT;
                if (mouseX >= LIST_X && mouseX <= LIST_X + LIST_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT - 2) {
                    BackpackInjector.openGuiFor(Minecraft.m_91087_(), placements.get(i));
                    return true;
                }
            }
        }
        return super.m_6375_(mouseX, mouseY, button);
    }

    @Override
    public boolean m_6050_(double mouseX, double mouseY, double scrollDelta) {
        if (mouseX >= LIST_X && mouseX <= LIST_X + LIST_WIDTH && mouseY >= LIST_Y && mouseY < LIST_Y + VISIBLE_ROWS * ROW_HEIGHT) {
            scroll = (int) Math.max(0, Math.min(placements.size() - VISIBLE_ROWS, scroll - scrollDelta));
            return true;
        }
        return super.m_6050_(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean m_6913_() {
        return false;
    }
}
