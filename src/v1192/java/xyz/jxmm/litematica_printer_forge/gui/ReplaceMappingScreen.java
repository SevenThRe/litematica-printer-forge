package xyz.jxmm.litematica_printer_forge.gui;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.jxmm.litematica_printer_forge.LitematicaMixinMod;
import xyz.jxmm.litematica_printer_forge.utils.BlockReplacer;

import java.util.ArrayList;
import java.util.List;

/**
 * Visual block replacement mapping editor.
 * Rows show from-item -> to-item with item icons; picking uses ItemPickerScreen
 * (two-stage: pick the block to replace, then the block to replace it with).
 * Changes are written back to the blockReplaceMappings config on save/close.
 */
public class ReplaceMappingScreen extends Screen {
    private final List<Item[]> rows = new ArrayList<>();
    private Item pendingFrom = null;
    private int scroll = 0;
    private static final int LIST_X = 10;
    private static final int LIST_Y = 46;
    private static final int ROW_H = 26;
    private static final int VISIBLE_ROWS = 7;
    private static final int LIST_W = 420;

    public ReplaceMappingScreen() {
        super(Component.m_237115_("方块替换映射编辑器"));
        loadFromConfig();
    }

    private void loadFromConfig() {
        rows.clear();
        for (String line : LitematicaMixinMod.CORAL_REPLACE_MAPPINGS.getStrings()) {
            Item[] pair = parseLine(line);
            if (pair != null) rows.add(pair);
        }
    }

    private static Item[] parseLine(String line) {
        if (line == null) return null;
        int eq = line.indexOf('=');
        if (eq <= 0) return null;
        try {
            Item from = ForgeRegistries.ITEMS.getValue(new ResourceLocation(line.substring(0, eq).trim()));
            Item to = ForgeRegistries.ITEMS.getValue(new ResourceLocation(line.substring(eq + 1).trim()));
            if (from == null || to == null) return null;
            return new Item[]{from, to};
        } catch (Exception e) {
            return null;
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Item[] pair : rows) {
            lines.add("minecraft:" + ForgeRegistries.ITEMS.getKey(pair[0]) + "=minecraft:"
                    + ForgeRegistries.ITEMS.getKey(pair[1]));
        }
        LitematicaMixinMod.CORAL_REPLACE_MAPPINGS.setStrings(lines);
    }

    @Override
    protected void m_7856_() {
        super.m_7856_();
        int btnY = LIST_Y + VISIBLE_ROWS * ROW_H + 8;
        this.m_142416_(new Button(LIST_X, btnY, 100, 20, Component.m_237115_("+ 添加映射"), b -> openPicker()));
        this.m_142416_(new Button(LIST_X + 110, btnY, 110, 20, Component.m_237115_("恢复内置默认"), b -> {
            rows.clear();
            for (String line : BlockReplacer.getDefaultMappingLines()) {
                Item[] pair = parseLine(line);
                if (pair != null) rows.add(pair);
            }
            scroll = 0;
        }));
        this.m_142416_(new Button(LIST_X + 230, btnY, 100, 20, Component.m_237115_("关闭并保存"), b -> {
            save();
            this.m_7379_();
        }));
        this.m_142416_(new Button(LIST_X + 340, btnY, 70, 20, Component.m_237115_("取消"), b -> this.m_7379_()));
    }

    private void openPicker() {
        Minecraft mc = Minecraft.m_91087_();
        mc.m_91152_(new ItemPickerScreen(
                pendingFrom == null ? "选择【被替换】方块 (1/2)" : "选择【替换为】方块 (2/2)",
                this,
                item -> {
                    if (pendingFrom == null) {
                        pendingFrom = item;
                        mc.m_91152_(new ItemPickerScreen("选择【替换为】方块 (2/2)", this, item2 -> {
                            this.rows.add(new Item[]{pendingFrom, item2});
                            this.pendingFrom = null;
                        }));
                    } else {
                        this.rows.add(new Item[]{pendingFrom, item});
                        this.pendingFrom = null;
                    }
                }));
    }

    @Override
    public void m_6305_(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.m_7333_(poseStack);
        this.f_96547_.m_92756_(poseStack, "方块替换映射编辑器", LIST_X, 10, 0xFFFFFF, false);
        this.f_96547_.m_92756_(poseStack, "打印/ESP/材料统计会把左侧方块替换为右侧方块", LIST_X, 22, 0xAAAAAA, false);

        int end = Math.min(rows.size(), scroll + VISIBLE_ROWS);
        for (int i = scroll; i < end; i++) {
            Item[] pair = rows.get(i);
            int y = LIST_Y + (i - scroll) * ROW_H;
            GuiComponent.m_93172_(poseStack, LIST_X, y, LIST_X + LIST_W, y + ROW_H - 2, 0x80000000);

            this.f_96542_.m_115123_(new ItemStack(pair[0]), LIST_X + 4, y + 2);
            String fromName = new ItemStack(pair[0]).m_41786_().getString();
            if (fromName.length() > 13) fromName = fromName.substring(0, 12) + "…";
            this.f_96547_.m_92756_(poseStack, fromName, LIST_X + 24, y + 8, 0xFFFFFF, false);

            this.f_96547_.m_92756_(poseStack, "→", LIST_X + 130, y + 8, 0xFFFF55, false);

            this.f_96542_.m_115123_(new ItemStack(pair[1]), LIST_X + 150, y + 2);
            String toName = new ItemStack(pair[1]).m_41786_().getString();
            if (toName.length() > 13) toName = toName.substring(0, 12) + "…";
            this.f_96547_.m_92756_(poseStack, toName, LIST_X + 170, y + 8, 0xFFFFFF, false);

            int delX = LIST_X + LIST_W - 30;
            boolean hoverDel = mouseX >= delX && mouseX < delX + 24 && mouseY >= y + 2 && mouseY < y + 20;
            GuiComponent.m_93172_(poseStack, delX, y + 2, delX + 24, y + 20, hoverDel ? 0xFFAA3333 : 0xFF553333);
            this.f_96547_.m_92756_(poseStack, "删", delX + 8, y + 9, 0xFFFFFF, false);
        }

        if (rows.size() > VISIBLE_ROWS) {
            this.f_96547_.m_92756_(poseStack, "滚动: " + (scroll + 1) + "-" + end + " / " + rows.size(),
                    LIST_X + LIST_W + 8, LIST_Y, 0xAAAAAA, false);
        }
        if (pendingFrom != null) {
            String nm = new ItemStack(pendingFrom).m_41786_().getString();
            this.f_96547_.m_92756_(poseStack, "下一步: 选择替换为 → " + nm, LIST_X + LIST_W + 8, LIST_Y + 14, 0x55FFFF, false);
        }
        super.m_6305_(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean m_6375_(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int end = Math.min(rows.size(), scroll + VISIBLE_ROWS);
            for (int i = scroll; i < end; i++) {
                int y = LIST_Y + (i - scroll) * ROW_H;
                int delX = LIST_X + LIST_W - 30;
                if (mouseX >= delX && mouseX < delX + 24 && mouseY >= y + 2 && mouseY < y + 20) {
                    rows.remove(i);
                    return true;
                }
            }
        }
        return super.m_6375_(mouseX, mouseY, button);
    }

    @Override
    public boolean m_6050_(double mouseX, double mouseY, double delta) {
        if (mouseX >= LIST_X && mouseX <= LIST_X + LIST_W && mouseY >= LIST_Y
                && mouseY < LIST_Y + VISIBLE_ROWS * ROW_H) {
            int maxScroll = Math.max(0, rows.size() - VISIBLE_ROWS);
            scroll = (int) Math.max(0, Math.min(maxScroll, scroll - delta));
            return true;
        }
        return super.m_6050_(mouseX, mouseY, delta);
    }

    @Override
    public void m_7379_() {
        save();
        super.m_7379_();
    }

    /** Package-private entry used by the hotkey handler in LitematicaMixinMod. */
    public static void open(Minecraft mc) {
        mc.m_91152_(new ReplaceMappingScreen());
    }
}
