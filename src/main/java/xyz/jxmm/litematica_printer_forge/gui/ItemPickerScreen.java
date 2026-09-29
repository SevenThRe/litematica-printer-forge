package xyz.jxmm.litematica_printer_forge.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Creative-style block item picker: search box + scrollable item grid.
 * Calls the callback with the clicked block item, then returns to the parent screen.
 */
public class ItemPickerScreen extends Screen {
    public interface PickerCallback {
        void onPick(Item item);
    }

    private final PickerCallback callback;
    private final Screen parent;
    private final String title;
    private EditBox searchBox;
    private int scroll = 0;
    private static final int COLS = 9;
    private static final int CELL = 24;
    private static final int GRID_X = 10;
    private static final int GRID_Y = 56;
    private static List<Item> allBlockItems = null;
    private String lastFilter = null;
    private List<Item> filteredCache = new ArrayList<>();

    public ItemPickerScreen(String title, Screen parent, PickerCallback callback) {
        super(Component.m_237115_(title));
        this.title = title;
        this.parent = parent;
        this.callback = callback;
    }

    private static List<Item> buildItemList() {
        List<Item> items = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof BlockItem) {
                items.add(item);
            }
        }
        items.sort((a, b) -> String.valueOf(ForgeRegistries.ITEMS.getKey(a))
                .compareTo(String.valueOf(ForgeRegistries.ITEMS.getKey(b))));
        return items;
    }

    private List<Item> filtered() {
        String f = searchBox == null ? "" : searchBox.m_94155_().toLowerCase().trim();
        if (f.equals(lastFilter)) return filteredCache;
        lastFilter = f;
        if (allBlockItems == null) allBlockItems = buildItemList();
        filteredCache = new ArrayList<>();
        if (f.isEmpty()) {
            filteredCache.addAll(allBlockItems);
        } else {
            for (Item item : allBlockItems) {
                String id = String.valueOf(ForgeRegistries.ITEMS.getKey(item));
                String name = new ItemStack(item).m_41786_().getString().toLowerCase();
                if (id.contains(f) || name.contains(f)) {
                    filteredCache.add(item);
                }
            }
        }
        return filteredCache;
    }

    @Override
    protected void m_7856_() {
        super.m_7856_();
        searchBox = new EditBox(this.f_96547_, GRID_X, 30, 240, 16, Component.m_237115_("search"));
        this.m_142416_(searchBox);
    }

    @Override
    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.m_280273_(graphics);
        graphics.m_280056_(this.f_96547_, title, GRID_X, 12, 0xFFFFFF, false);
        graphics.m_280056_(this.f_96547_, "搜索(注册名/物品名):", GRID_X, 21, 0xAAAAAA, false);

        List<Item> list = filtered();
        int gridW = COLS * CELL;
        int visibleRows = Math.max(1, (this.f_96543_ - GRID_Y - 30) / CELL);
        int totalRows = (list.size() + COLS - 1) / COLS;
        if (scroll > totalRows - visibleRows) scroll = Math.max(0, totalRows - visibleRows);

        int hoverNameY = this.f_96543_ - 20;
        for (int i = scroll * COLS; i < list.size() && i < (scroll + visibleRows) * COLS; i++) {
            Item item = list.get(i);
            int row = i / COLS - scroll;
            int col = i % COLS;
            int x = GRID_X + col * CELL;
            int y = GRID_Y + row * CELL;
            boolean hover = mouseX >= x && mouseX < x + CELL - 2 && mouseY >= y && mouseY < y + CELL - 2;
            graphics.m_280509_(x, y, x + CELL - 2, y + CELL - 2, hover ? 0x90FFFFFF : 0x80000000);
            graphics.m_280480_(new ItemStack(item), x + 3, y + 3);
            if (hover) {
                String id = String.valueOf(ForgeRegistries.ITEMS.getKey(item));
                graphics.m_280509_(GRID_X, hoverNameY, GRID_X + gridW, hoverNameY + 18, 0xC0000000);
                graphics.m_280056_(this.f_96547_, new ItemStack(item).m_41786_().getString(), GRID_X + 4, hoverNameY + 2, 0xFFFF55, false);
                graphics.m_280056_(this.f_96547_, id, GRID_X + 4, hoverNameY + 10, 0xAAAAAA, false);
            }
        }
        if (totalRows > visibleRows) {
            graphics.m_280056_(this.f_96547_, "滚动: 行 " + (scroll + 1) + "/" + totalRows, GRID_X + gridW + 8, GRID_Y, 0xAAAAAA, false);
        }
        graphics.m_280056_(this.f_96547_, "共 " + list.size() + " 个方块 | 点击选择 | ESC 取消", GRID_X + gridW + 8, GRID_Y + 12, 0x888888, false);
        super.m_88315_(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean m_6375_(double mouseX, double mouseY, int button) {
        if (button == 0) {
            List<Item> list = filtered();
            int visibleRows = Math.max(1, (this.f_96543_ - GRID_Y - 30) / CELL);
            for (int i = scroll * COLS; i < list.size() && i < (scroll + visibleRows) * COLS; i++) {
                int row = i / COLS - scroll;
                int col = i % COLS;
                int x = GRID_X + col * CELL;
                int y = GRID_Y + row * CELL;
                if (mouseX >= x && mouseX < x + CELL - 2 && mouseY >= y && mouseY < y + CELL - 2) {
                    Minecraft.m_91087_().m_91152_(parent);
                    callback.onPick(list.get(i));
                    return true;
                }
            }
        }
        return super.m_6375_(mouseX, mouseY, button);
    }

    @Override
    public boolean m_6050_(double mouseX, double mouseY, double delta) {
        List<Item> list = filtered();
        int visibleRows = Math.max(1, (this.f_96543_ - GRID_Y - 30) / CELL);
        int totalRows = (list.size() + COLS - 1) / COLS;
        scroll = (int) Math.max(0, Math.min(Math.max(0, totalRows - visibleRows), scroll - delta));
        return true;
    }

    @Override
    public void m_7379_() {
        // ESC: go back to the parent screen without picking
        Minecraft.m_91087_().m_91152_(parent);
    }
}
