package jp.ac.u_tokyo.sdm.extra_items.client.screen;

import jp.ac.u_tokyo.sdm.extra_items.screen.TechnicalBookScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public final class TechnicalBookScreen extends HandledScreen<TechnicalBookScreenHandler> {
    private static final Text SUBTITLE_TEXT = Text.translatable("screen.extra_items.technical_book.subtitle");
    private static final Text FORMULA_TEXT = Text.translatable("screen.extra_items.technical_book.formula");
    private static final Text EXPLANATION_TEXT = Text.translatable("screen.extra_items.technical_book.explanation");
    private static final Text FOOTNOTE_TEXT = Text.translatable("screen.extra_items.technical_book.footnote");
    private static final Text LEFT_PAGE_TEXT = Text.translatable("screen.extra_items.technical_book.left_page");
    private static final Text RIGHT_PAGE_TEXT = Text.translatable("screen.extra_items.technical_book.right_page");
    private static final int PANEL_WIDTH = 292;
    private static final int PANEL_HEIGHT = 180;
    private static final int PANEL_PADDING = 12;
    private static final int HEADER_HEIGHT = 24;
    private static final int PAGE_PADDING = 14;
    private static final int GUTTER_WIDTH = 8;
    private static final int FORMULA_SECTION_HEIGHT = 30;

    public TechnicalBookScreen(
        TechnicalBookScreenHandler handler,
        PlayerInventory inventory,
        Text title
    ) {
        super(handler, inventory, title);
        this.backgroundWidth = PANEL_WIDTH;
        this.backgroundHeight = PANEL_HEIGHT;
        this.playerInventoryTitleX = -1000;
        this.playerInventoryTitleY = -1000;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // TODO: ページ送りや章構成を追加する場合は、左右ページごとに操作を分ける。
        PanelMetrics panel = this.getPanelMetrics();
        int titleWidth = this.textRenderer.getWidth(this.title);
        int subtitleWidth = this.textRenderer.getWidth(SUBTITLE_TEXT);
        int leftPageNumberWidth = this.textRenderer.getWidth("01");
        int rightPageNumberWidth = this.textRenderer.getWidth("02");
        int formulaWidth = this.textRenderer.getWidth(FORMULA_TEXT);
        int lineHeight = this.textRenderer.fontHeight;

        int titleX = panel.left + (panel.width - titleWidth) / 2 - this.x;
        int titleY = panel.top + 7 - this.y;
        int subtitleX = panel.paperLeft + (panel.paperWidth - subtitleWidth) / 2 - this.x;
        int subtitleY = panel.paperTop + 8 - this.y;
        int formulaX = panel.paperLeft + (panel.paperWidth - formulaWidth) / 2 - this.x;
        int formulaY = panel.formulaTop + (FORMULA_SECTION_HEIGHT - lineHeight) / 2 - this.y;

        int leftPageX = panel.leftPageLeft + PAGE_PADDING - this.x;
        int leftPageY = panel.pageBodyTop - this.y;
        int rightPageX = panel.rightPageLeft + PAGE_PADDING - this.x;
        int rightPageY = panel.pageBodyTop - this.y;
        int pageTextWidth = panel.pageWidth - PAGE_PADDING * 2;

        context.drawText(this.textRenderer, this.title, titleX, titleY, 0xFF4A3221, false);
        context.drawText(this.textRenderer, SUBTITLE_TEXT, subtitleX, subtitleY, 0xFF8C6F58, false);
        context.drawText(this.textRenderer, "01", leftPageX, panel.pageNumberY - this.y, 0xFFAE9A84, false);
        context.drawText(
            this.textRenderer,
            "02",
            panel.rightPageRight - PAGE_PADDING - rightPageNumberWidth - this.x,
            panel.pageNumberY - this.y,
            0xFFAE9A84,
            false
        );
        context.drawText(this.textRenderer, FORMULA_TEXT, formulaX, formulaY, 0xFF2F2015, false);

        context.drawWrappedText(
            this.textRenderer,
            LEFT_PAGE_TEXT,
            leftPageX,
            leftPageY,
            pageTextWidth,
            0xFF5B4737,
            false
        );
        context.drawWrappedText(
            this.textRenderer,
            EXPLANATION_TEXT,
            rightPageX,
            rightPageY,
            pageTextWidth,
            0xFF5B4737,
            false
        );
        context.drawWrappedText(
            this.textRenderer,
            RIGHT_PAGE_TEXT,
            rightPageX,
            panel.rightPageNoteY - this.y,
            pageTextWidth,
            0xFF7A6654,
            false
        );
        context.drawWrappedText(
            this.textRenderer,
            FOOTNOTE_TEXT,
            leftPageX,
            panel.leftPageNoteY - this.y,
            pageTextWidth,
            0xFF7A6654,
            false
        );
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // 背景パネルは render 内で明示的に描画する。
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        this.drawPanel(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x66000000);
    }

    private void drawPanel(DrawContext context) {
        PanelMetrics panel = this.getPanelMetrics();

        context.fill(panel.left + 3, panel.top + 4, panel.right + 3, panel.bottom + 4, 0x44100000);
        context.fillGradient(panel.left, panel.top, panel.right, panel.bottom, 0xFFC8B79E, 0xFFB29C80);
        context.drawBorder(panel.left, panel.top, panel.width, panel.height, 0xFF5E4A36);

        context.fill(panel.left + 1, panel.top + 1, panel.right - 1, panel.top + HEADER_HEIGHT, 0xFFB49C7D);
        context.fill(panel.left + 1, panel.top + HEADER_HEIGHT, panel.right - 1, panel.top + HEADER_HEIGHT + 1, 0x88FFF7EA);

        context.fill(panel.paperLeft, panel.paperTop, panel.paperRight, panel.paperBottom, 0xFFFFFCF4);
        context.drawBorder(panel.paperLeft, panel.paperTop, panel.paperWidth, panel.paperHeight, 0xFFDCCFB9);

        context.fill(panel.gutterLeft, panel.paperTop + 6, panel.gutterRight, panel.paperBottom - 6, 0xFFD5C5AB);
        context.fill(panel.gutterLeft + 1, panel.paperTop + 6, panel.gutterRight - 1, panel.paperBottom - 6, 0x55FFF8E8);

        context.fill(panel.paperLeft + 12, panel.formulaTop - 6, panel.paperRight - 12, panel.formulaTop - 5, 0x22A28C6A);
        context.fill(panel.paperLeft + 12, panel.formulaBottom + 5, panel.paperRight - 12, panel.formulaBottom + 6, 0x22A28C6A);
        context.fill(panel.paperLeft + 18, panel.leftPageNoteY - 8, panel.gutterLeft - 12, panel.leftPageNoteY - 7, 0x22A28C6A);
        context.fill(panel.gutterRight + 12, panel.rightPageNoteY - 8, panel.paperRight - 18, panel.rightPageNoteY - 7, 0x22A28C6A);
    }

    private PanelMetrics getPanelMetrics() {
        int left = this.x;
        int top = this.y;
        int width = this.backgroundWidth;
        int height = this.backgroundHeight;
        int right = left + width;
        int bottom = top + height;

        int paperLeft = left + PANEL_PADDING;
        int paperTop = top + HEADER_HEIGHT + 4;
        int paperRight = right - PANEL_PADDING;
        int paperBottom = bottom - PANEL_PADDING;
        int paperWidth = paperRight - paperLeft;
        int paperHeight = paperBottom - paperTop;

        int gutterLeft = left + width / 2 - GUTTER_WIDTH / 2;
        int gutterRight = gutterLeft + GUTTER_WIDTH;
        int pageWidth = (paperWidth - GUTTER_WIDTH) / 2;
        int leftPageLeft = paperLeft;
        int leftPageRight = leftPageLeft + pageWidth;
        int rightPageLeft = gutterRight;
        int rightPageRight = paperRight;

        int formulaTop = paperTop + 26;
        int formulaBottom = formulaTop + FORMULA_SECTION_HEIGHT;
        int pageNumberY = formulaBottom + 8;
        int pageBodyTop = pageNumberY + this.textRenderer.fontHeight + 8;
        int leftPageNoteY = paperBottom - 58;
        int rightPageNoteY = paperBottom - 58;

        return new PanelMetrics(
            left, top, right, bottom, width, height,
            paperLeft, paperTop, paperRight, paperBottom, paperWidth, paperHeight,
            gutterLeft, gutterRight,
            leftPageLeft, leftPageRight, rightPageLeft, rightPageRight, pageWidth,
            formulaTop, formulaBottom, pageNumberY, pageBodyTop, leftPageNoteY, rightPageNoteY
        );
    }

    private record PanelMetrics(
        int left,
        int top,
        int right,
        int bottom,
        int width,
        int height,
        int paperLeft,
        int paperTop,
        int paperRight,
        int paperBottom,
        int paperWidth,
        int paperHeight,
        int gutterLeft,
        int gutterRight,
        int leftPageLeft,
        int leftPageRight,
        int rightPageLeft,
        int rightPageRight,
        int pageWidth,
        int formulaTop,
        int formulaBottom,
        int pageNumberY,
        int pageBodyTop,
        int leftPageNoteY,
        int rightPageNoteY
    ) {
    }
}
