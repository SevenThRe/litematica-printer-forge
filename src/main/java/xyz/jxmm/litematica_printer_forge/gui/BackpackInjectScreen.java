package xyz.jxmm.litematica_printer_forge.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import xyz.jxmm.litematica_printer_forge.utils.BackpackInjector;

import java.util.List;

public class BackpackInjectScreen extends Screen {
    private final List<BackpackInjector.InjectPlan> plans;
    private int sortMode = BackpackInjector.lastSortMode;
    private int scroll = 0;
    private static final int ROW_HEIGHT = 24;
    private static final int VISIBLE_ROWS = 8;
    private static final int LIST_WIDTH = 360;
    private static final int LIST_X = 10;
    private static final int LIST_Y = 40;
    private final String schematicName;

    private Button modeBtn;
    private Button confirmBtn;
    private Button cancelBtn;

    public BackpackInjectScreen(List<BackpackInjector.InjectPlan> plans) {
        this(plans, null);
    }

    public BackpackInjectScreen(List<BackpackInjector.InjectPlan> plans, String schematicName) {
        super(Component.m_237115_(schematicName == null ? "背包材料注入" : "背包材料注入 - " + schematicName));
        this.plans = plans;
        this.schematicName = schematicName;
    }

    @Override
    protected void m_7856_() {
        super.m_7856_();
        int btnY = LIST_Y + VISIBLE_ROWS * ROW_HEIGHT + 10;

        modeBtn = Button.m_253074_(Component.m_237115_(BackpackInjector.sortModeName(sortMode)), b -> {
            sortMode = (sortMode + 1) % 3;
            BackpackInjector.lastSortMode = sortMode;
            BackpackInjector.applySort(plans, sortMode);
            scroll = 0;
            b.m_93666_(Component.m_237115_(BackpackInjector.sortModeName(sortMode)));
        }).m_252987_(LIST_X, btnY, 150, 20).m_253136_();
        this.m_142416_(modeBtn);

        confirmBtn = Button.m_253074_(Component.m_237115_("确认注入"), b -> {
            BackpackInjector.inject(plans, sortMode, Minecraft.m_91087_());
            this.m_7379_();
        }).m_252987_(LIST_X + 160, btnY, 80, 20).m_253136_();
        this.m_142416_(confirmBtn);

        cancelBtn = Button.m_253074_(Component.m_237115_("取消"), b -> this.m_7379_())
                .m_252987_(LIST_X + 250, btnY, 60, 20).m_253136_();
        this.m_142416_(cancelBtn);
    }

    @Override
    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        graphics.m_280056_(this.f_96547_, schematicName == null ? "背包材料注入" : "背包材料注入 - " + schematicName, LIST_X, 10, 0xFFFFFF, false);
        graphics.m_280056_(this.f_96547_, "缺失材料（已扣除快捷栏/背包）", LIST_X, LIST_Y - 12, 0xAAAAAA, false);

        int end = Math.min(plans.size(), scroll + VISIBLE_ROWS);
        for (int i = scroll; i < end; i++) {
            BackpackInjector.InjectPlan plan = plans.get(i);
            ItemStack stack = plan.stack;
            int y = LIST_Y + (i - scroll) * ROW_HEIGHT;

            // 背景
            graphics.m_280509_(LIST_X, y, LIST_X + LIST_WIDTH, y + ROW_HEIGHT - 2, 0x80000000);

            // 物品图标
            graphics.m_280480_(stack, LIST_X + 4, y + 2);

            // 名称与数量
            String name = stack.m_41786_().getString();
            if (name.length() > 15) name = name.substring(0, 14) + "…";
            graphics.m_280056_(this.f_96547_, name, LIST_X + 24, y + 6, 0xFFFFFF, false);
            graphics.m_280056_(this.f_96547_, "Y:" + (plan.lowestY == Integer.MAX_VALUE ? "?" : plan.lowestY), LIST_X + 160, y + 6, 0x55FF55, false);
            graphics.m_280056_(this.f_96547_, "计划: " + plan.amount, LIST_X + 200, y + 6, 0x55FFFF, false);

            // 手绘 +/- 按钮（不用 widget，避免重复添加）
            int minusX = LIST_X + 260;
            int plusX = LIST_X + 284;
            int btnY = y + 2;
            boolean hoverMinus = mouseX >= minusX && mouseX < minusX + 20 && mouseY >= btnY && mouseY < btnY + 18;
            boolean hoverPlus = mouseX >= plusX && mouseX < plusX + 20 && mouseY >= btnY && mouseY < btnY + 18;
            graphics.m_280509_(minusX, btnY, minusX + 20, btnY + 18, hoverMinus ? 0xFF666666 : 0xFF444444);
            graphics.m_280509_(plusX, btnY, plusX + 20, btnY + 18, hoverPlus ? 0xFF666666 : 0xFF444444);
            graphics.m_280056_(this.f_96547_, "-", minusX + 7, btnY + 5, 0xFFFFFF, false);
            graphics.m_280056_(this.f_96547_, "+", plusX + 7, btnY + 5, 0xFFFFFF, false);
        }

        // 滚动条提示
        if (plans.size() > VISIBLE_ROWS) {
            graphics.m_280056_(this.f_96547_, "滚动: " + (scroll + 1) + "-" + end + " / " + plans.size(), LIST_X + LIST_WIDTH + 10, LIST_Y, 0xAAAAAA, false);
        }

        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean m_6375_(double mouseX, double mouseY, int button) {
        // 手绘 +/- 按钮的点击检测
        int end = Math.min(plans.size(), scroll + VISIBLE_ROWS);
        for (int i = scroll; i < end; i++) {
            BackpackInjector.InjectPlan plan = plans.get(i);
            int y = LIST_Y + (i - scroll) * ROW_HEIGHT;
            int btnY = y + 2;
            int minusX = LIST_X + 260;
            int plusX = LIST_X + 284;
            if (mouseX >= minusX && mouseX < minusX + 20 && mouseY >= btnY && mouseY < btnY + 18) {
                plan.amount = Math.max(0, plan.amount - 1);
                return true;
            }
            if (mouseX >= plusX && mouseX < plusX + 20 && mouseY >= btnY && mouseY < btnY + 18) {
                plan.amount += 1;
                return true;
            }
        }
        return super.m_6375_(mouseX, mouseY, button);
    }

    @Override
    public boolean m_6050_(double mouseX, double mouseY, double scrollDelta) {
        if (mouseX >= LIST_X && mouseX <= LIST_X + LIST_WIDTH && mouseY >= LIST_Y && mouseY < LIST_Y + VISIBLE_ROWS * ROW_HEIGHT) {
            scroll = (int) Math.max(0, Math.min(plans.size() - VISIBLE_ROWS, scroll - scrollDelta));
            return true;
        }
        return super.m_6050_(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean m_6913_() {
        return false;
    }
}
