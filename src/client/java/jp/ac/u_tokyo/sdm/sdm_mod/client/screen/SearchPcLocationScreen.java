package jp.ac.u_tokyo.sdm.sdm_mod.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class SearchPcLocationScreen extends Screen {
    private static final Text CLOSE_BUTTON_TEXT = Text.literal("X");
    private static final Text HEADER_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.header_title");
    private static final Text SECTION_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.section_title");
    private static final Text PAGE_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.page_title");
    private static final Text BREADCRUMB_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.breadcrumb");
    private static final Text LOCATION_MESSAGE_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.message");
    private static final Text MAP_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.map_title");
    private static final Text MAP_DIAGRAM_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.map.diagram_title");
    private static final Text INFO_LINE_1_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.info_line_1");
    private static final Text INFO_LINE_2_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.info_line_2");
    private static final Text INFO_LINE_3_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.info_line_3");
    private static final Text FLOOR_LABEL_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.floor_label");
    private static final Text MAP_STAIRS_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.map.stairs");
    private static final Text MAP_READING_ROOM_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.map.center");

    private static final int PANEL_WIDTH = 396;
    private static final int PANEL_HEIGHT = 248;

    private final Screen parent;

    public SearchPcLocationScreen(Screen parent) {
        super(Text.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        this.addDrawableChild(ButtonWidget.builder(CLOSE_BUTTON_TEXT, button -> this.close())
            .dimensions(left + PANEL_WIDTH - 24, top + 6, 16, 16)
            .build());
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        drawPanel(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x7A020A14);
    }

    private void drawPanel(DrawContext context) {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        int right = left + PANEL_WIDTH;
        int bottom = top + PANEL_HEIGHT;
        int sidebarRight = left + 84;

        context.fill(left + 4, top + 4, right + 4, bottom + 4, 0x33000000);
        context.fill(left, top, right, bottom, 0xFFF7FBFF);
        context.drawBorder(left, top, PANEL_WIDTH, PANEL_HEIGHT, 0xFF0D4A9D);

        context.fill(left, top, right, top + 28, 0xFFF4F8FE);
        context.fill(left, top + 28, right, top + 52, 0xFF1B67C2);
        context.drawText(this.textRenderer, HEADER_TITLE_TEXT, left + 38, top + 8, 0xFF1C1C1C, false);
        context.drawText(this.textRenderer, SECTION_TITLE_TEXT, left + 10, top + 34, 0xFFFFFFFF, false);

        drawMiniLogo(context, left + 10, top + 6);

        context.fill(left + 10, top + 58, sidebarRight, bottom - 34, 0xFFF0F6FF);
        context.drawBorder(left + 10, top + 58, 74, bottom - top - 92, 0xFF0D4A9D);
        drawSidebarItem(context, left + 12, top + 60, 70, 22, 0xFF9A4B1A, 0xFF5D2507, Text.translatable("screen.sdm_mod.search_pc.location.sidebar.hours"));
        drawSidebarItem(context, left + 12, top + 86, 70, 18, 0xFF1B67C2, 0xFF0D4A9D, Text.translatable("screen.sdm_mod.search_pc.location.sidebar.guide"));
        drawSidebarItem(context, left + 12, top + 106, 70, 18, 0xFF1B67C2, 0xFF0D4A9D, Text.translatable("screen.sdm_mod.search_pc.location.sidebar.collection"));
        drawSidebarItem(context, left + 12, top + 126, 70, 18, 0xFF1B67C2, 0xFF0D4A9D, Text.translatable("screen.sdm_mod.search_pc.location.sidebar.access"));
        context.drawText(this.textRenderer, Text.translatable("screen.sdm_mod.search_pc.location.sidebar.hours_time"), left + 18, top + 74, 0xFFFFFFFF, false);

        context.drawText(this.textRenderer, PAGE_TITLE_TEXT, left + 100, top + 64, 0xFF222222, false);
        context.drawBorder(left + 98, top + 80, 246, 16, 0xFF7AA0D6);
        context.drawText(this.textRenderer, BREADCRUMB_TEXT, left + 104, top + 85, 0xFF2E5A94, false);

        context.drawText(this.textRenderer, LOCATION_MESSAGE_TEXT, left + 100, top + 106, 0xFF161616, false);
        context.drawText(this.textRenderer, MAP_TITLE_TEXT, left + 100, top + 126, 0xFF1C1C1C, false);

        drawFloorMap(context, left + 118, top + 142);
        drawInfoBox(context, left + 298, top + 144);
    }

    private void drawMiniLogo(DrawContext context, int left, int top) {
        context.fill(left, top + 6, left + 20, top + 20, 0xFFD7A84D);
        context.drawBorder(left, top + 6, 20, 14, 0xFF5A3F14);
        context.fill(left + 7, top + 8, left + 10, top + 18, 0xFF5A3F14);
        context.fill(left + 11, top + 8, left + 14, top + 18, 0xFF5A3F14);
        context.fill(left + 4, top + 12, left + 17, top + 14, 0xFF5A3F14);
    }

    private void drawSidebarItem(DrawContext context, int left, int top, int width, int height, int fillColor, int borderColor, Text label) {
        context.fill(left, top, left + width, top + height, fillColor);
        context.drawBorder(left, top, width, height, borderColor);
        context.drawText(this.textRenderer, label, left + 6, top + 5, 0xFFFFFFFF, false);
    }

    private void drawFloorMap(DrawContext context, int left, int top) {
        int outerWidth = 162;
        int outerHeight = 96;
        int titleWidth = 118;
        int titleLeft = left + (outerWidth - titleWidth) / 2;

        context.fill(left, top, left + outerWidth, top + outerHeight, 0xFFFFFFFF);
        context.drawBorder(left, top, outerWidth, outerHeight, 0xFF4C3418);

        context.fill(titleLeft, top + 4, titleLeft + titleWidth, top + 18, 0xFFFFFFFF);
        context.drawBorder(titleLeft, top + 4, titleWidth, 14, 0xFF4C3418);
        context.drawText(this.textRenderer, MAP_DIAGRAM_TITLE_TEXT, titleLeft + 10, top + 8, 0xFF3B2510, false);

        context.fill(left + 8, top + 22, left + 36, top + 70, 0xFF5B4321);
        context.fill(left + 126, top + 22, left + 154, top + 70, 0xFF5B4321);
        context.drawBorder(left + 8, top + 22, 28, 48, 0xFF3F2B12);
        context.drawBorder(left + 126, top + 22, 28, 48, 0xFF3F2B12);

        context.fill(left + 36, top + 34, left + 54, top + 42, 0xFFF7E6B9);
        context.fill(left + 108, top + 34, left + 126, top + 42, 0xFFF7E6B9);
        context.fill(left + 54, top + 34, left + 108, top + 64, 0xFFF7E6B9);
        context.fill(left + 36, top + 42, left + 126, top + 72, 0xFFF7E6B9);

        context.fill(left + 14, top + 72, left + 146, top + 90, 0xFFD9C095);
        context.drawBorder(left + 14, top + 72, 132, 18, 0xFF8A7047);
        context.drawText(this.textRenderer, MAP_READING_ROOM_TEXT, left + 62, top + 78, 0xFF3B2510, false);

        drawStairsIcon(context, left + 80, top + 50);
        context.drawText(this.textRenderer, MAP_STAIRS_TEXT, left + 74, top + 63, 0xFF3B2510, false);

    }

    private void drawInfoBox(DrawContext context, int left, int top) {
        context.fill(left, top, left + 84, top + 58, 0xFFFFFFFF);
        context.drawBorder(left, top, 84, 58, 0xFF7AA0D6);
        context.drawText(this.textRenderer, INFO_LINE_1_TEXT, left + 8, top + 10, 0xFF1C1C1C, false);
        context.drawText(this.textRenderer, INFO_LINE_2_TEXT, left + 8, top + 24, 0xFF1C1C1C, false);
        context.drawText(this.textRenderer, INFO_LINE_3_TEXT, left + 8, top + 38, 0xFF1C1C1C, false);
        context.drawText(this.textRenderer, FLOOR_LABEL_TEXT, left + 47, top - 12, 0xFF0D4A9D, false);
    }

    private void drawStairsIcon(DrawContext context, int centerX, int top) {
        int left = centerX - 6;
        int right = centerX + 6;

        context.fill(left, top, right, top + 12, 0xFFF7E6B9);
        context.drawBorder(left, top, 12, 12, 0xFF4C3418);
        for (int y = top + 2; y < top + 12; y += 2) {
            context.fill(left + 2, y, right - 2, y + 1, 0xFF4C3418);
        }
        context.fill(left + 2, top + 12, right - 2, top + 14, 0xFFF7E6B9);
        context.drawBorder(left + 2, top + 12, 8, 2, 0xFF4C3418);
    }
}
