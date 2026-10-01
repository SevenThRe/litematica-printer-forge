package xyz.jxmm.litematica_printer_forge.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import xyz.jxmm.litematica_printer_forge.utils.BackpackInjector;
import xyz.jxmm.litematica_printer_forge.utils.BlueprintEntry;

import java.util.List;

/** Picker for the Create blueprints BII can use as a material source (shown when more than one is carried). */
public class BlueprintPickerScreen extends Screen {
    private final List<BlueprintEntry> blueprints;
    private int scroll = 0;
    private static final int ROW_HEIGHT = 24;
    private static final int VISIBLE_ROWS = 8;
    private static final int LIST_WIDTH = 360;
    private static final int LIST_X = 10;
    private static final int LIST_Y = 40;

    public BlueprintPickerScreen(List<BlueprintEntry> blueprints) {
        super(Component.m_237115_("选择 Create 蓝图"));
        this.blueprints = blueprints;
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
        graphics.m_280056_(this.f_96547_, "选择要注入材料的 Create 蓝图（共 " + blueprints.size() + " 张）", LIST_X, 10, 0xFFFFFF, false);

        int end = Math.min(blueprints.size(), scroll + VISIBLE_ROWS);
        for (int i = scroll; i < end; i++) {
            BlueprintEntry entry = blueprints.get(i);
            int y = LIST_Y + (i - scroll) * ROW_HEIGHT;
            boolean hovered = mouseX >= LIST_X && mouseX <= LIST_X + LIST_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT - 2;
            graphics.m_280509_(LIST_X, y, LIST_X + LIST_WIDTH, y + ROW_HEIGHT - 2, hovered ? 0x80FFFFFF : 0x80000000);
            graphics.m_280056_(this.f_96547_, entry.name, LIST_X + 6, y + 3, hovered ? 0xFFFF55 : 0xFFFFFF, false);
            graphics.m_280056_(this.f_96547_, entry.detail, LIST_X + 6, y + 12, 0xAAAAAA, false);
        }

        if (blueprints.size() > VISIBLE_ROWS) {
            graphics.m_280056_(this.f_96547_, "滚动: " + (scroll + 1) + "-" + end + " / " + blueprints.size(), LIST_X + LIST_WIDTH + 10, LIST_Y, 0xAAAAAA, false);
        }
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean m_6375_(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int end = Math.min(blueprints.size(), scroll + VISIBLE_ROWS);
            for (int i = scroll; i < end; i++) {
                int y = LIST_Y + (i - scroll) * ROW_HEIGHT;
                if (mouseX >= LIST_X && mouseX <= LIST_X + LIST_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT - 2) {
                    BackpackInjector.openGuiForBlueprint(Minecraft.m_91087_(), blueprints.get(i));
                    return true;
                }
            }
        }
        return super.m_6375_(mouseX, mouseY, button);
    }

    @Override
    public boolean m_6050_(double mouseX, double mouseY, double scrollDelta) {
        if (mouseX >= LIST_X && mouseX <= LIST_X + LIST_WIDTH && mouseY >= LIST_Y && mouseY < LIST_Y + VISIBLE_ROWS * ROW_HEIGHT) {
            scroll = (int) Math.max(0, Math.min(blueprints.size() - VISIBLE_ROWS, scroll - scrollDelta));
            return true;
        }
        return super.m_6050_(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean m_6913_() {
        return false;
    }
}
