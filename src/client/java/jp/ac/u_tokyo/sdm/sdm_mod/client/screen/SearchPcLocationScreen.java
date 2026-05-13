package jp.ac.u_tokyo.sdm.sdm_mod.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class SearchPcLocationScreen extends Screen {
    private static final Text HEADER_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.header_title");
    private static final Text SECTION_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.section_title");
    private static final Text PAGE_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.page_title");
    private static final Text BREADCRUMB_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.breadcrumb");
    private static final Text LOCATION_MESSAGE_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.message");
    private static final Text MAP_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.map_title");
    private static final Text INFO_LINE_1_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.info_line_1");
    private static final Text INFO_LINE_2_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.info_line_2");
    private static final Text INFO_LINE_3_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.info_line_3");
    private static final Text FLOOR_BADGE_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.floor_badge");
    private static final Text FLOOR_LABEL_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.floor_label");
    private static final Text MAP_LEFT_TOP_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.map.left_top");
    private static final Text MAP_LEFT_BOTTOM_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.map.left_bottom");
    private static final Text MAP_CENTER_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.map.center");
    private static final Text MAP_RIGHT_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.map.right");
    private static final Text HIGHLIGHT_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.highlight");
    private static final Text BACK_TEXT = Text.translatable("screen.sdm_mod.search_pc.location.back");

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

        this.addDrawableChild(ButtonWidget.builder(BACK_TEXT, button -> this.close())
            .dimensions(this.width / 2 - 54, this.height / 2 + 102, 108, 20)
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
        context.fill(left, top, left + 150, top + 80, 0xFFF7E3B6);
        context.drawBorder(left, top, 150, 80, 0xFF9A6A21);
        context.fill(left + 16, top + 8, left + 42, top + 70, 0xFF5A3A15);
        context.fill(left + 42, top + 44, left + 74, top + 58, 0xFF5A3A15);
        context.fill(left + 108, top, left + 132, top + 80, 0xFF5A3A15);
        context.fill(left + 12, top + 12, left + 39, top + 34, 0xFF6A4A1F);
        context.fill(left + 16, top + 38, left + 39, top + 66, 0xFF6A4A1F);
        context.fill(left + 112, top + 50, left + 128, top + 74, 0xFF1260BD);
        context.drawBorder(left + 112, top + 50, 16, 24, 0xFF0B4284);
        context.drawText(this.textRenderer, FLOOR_BADGE_TEXT, left + 116, top + 57, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, MAP_LEFT_TOP_TEXT, left + 18, top + 16, 0xFFF5E9D4, false);
        context.drawText(this.textRenderer, MAP_LEFT_BOTTOM_TEXT, left + 18, top + 46, 0xFFF5E9D4, false);
        context.drawText(this.textRenderer, MAP_CENTER_TEXT, left + 56, top + 54, 0xFF5A3A15, false);
        context.drawText(this.textRenderer, MAP_RIGHT_TEXT, left + 113, top + 34, 0xFFF5E9D4, false);
        context.drawText(this.textRenderer, HIGHLIGHT_TEXT, left + 80, top + 8, 0xFF0D4A9D, false);
    }

    private void drawInfoBox(DrawContext context, int left, int top) {
        context.fill(left, top, left + 84, top + 58, 0xFFFFFFFF);
        context.drawBorder(left, top, 84, 58, 0xFF7AA0D6);
        context.drawText(this.textRenderer, INFO_LINE_1_TEXT, left + 8, top + 10, 0xFF1C1C1C, false);
        context.drawText(this.textRenderer, INFO_LINE_2_TEXT, left + 8, top + 24, 0xFF1C1C1C, false);
        context.drawText(this.textRenderer, INFO_LINE_3_TEXT, left + 8, top + 38, 0xFF1C1C1C, false);
        context.drawText(this.textRenderer, FLOOR_LABEL_TEXT, left + 47, top - 12, 0xFF0D4A9D, false);
    }
}
