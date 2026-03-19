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
        int titleWidth = this.textRenderer.getWidth(this.title);
        int subtitleWidth = this.textRenderer.getWidth(SUBTITLE_TEXT);
        int pageWidth = (this.backgroundWidth - 40) / 2;
        int leftPageX = 20;
        int rightPageX = this.backgroundWidth / 2 + 4;

        context.drawText(this.textRenderer, this.title, (this.backgroundWidth - titleWidth) / 2, 18, 0xFF4A3221, false);
        context.drawText(this.textRenderer, SUBTITLE_TEXT, (this.backgroundWidth - subtitleWidth) / 2, 34, 0xFF8C6F58, false);
        context.drawText(this.textRenderer, Text.literal("01"), leftPageX, 54, 0xFFAE9A84, false);
        context.drawText(this.textRenderer, Text.literal("02"), rightPageX + pageWidth - 12, 54, 0xFFAE9A84, false);

        context.drawWrappedText(
            this.textRenderer,
            LEFT_PAGE_TEXT,
            leftPageX,
            68,
            pageWidth,
            0xFF5B4737,
            false
        );
        context.drawCenteredTextWithShadow(this.textRenderer, FORMULA_TEXT, this.backgroundWidth / 2, 90, 0xFF2F2015);
        context.drawWrappedText(
            this.textRenderer,
            EXPLANATION_TEXT,
            rightPageX,
            68,
            pageWidth,
            0xFF5B4737,
            false
        );
        context.drawWrappedText(
            this.textRenderer,
            RIGHT_PAGE_TEXT,
            rightPageX,
            126,
            pageWidth,
            0xFF7A6654,
            false
        );
        context.drawWrappedText(
            this.textRenderer,
            FOOTNOTE_TEXT,
            leftPageX,
            126,
            pageWidth,
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
        this.renderInGameBackground(context);
        context.fill(0, 0, this.width, this.height, 0x66000000);
    }

    private void drawPanel(DrawContext context) {
        int left = this.x;
        int top = this.y;
        int right = left + this.backgroundWidth;
        int bottom = top + this.backgroundHeight;
        int paperLeft = left + 12;
        int paperTop = top + 28;
        int paperRight = right - 12;
        int paperBottom = bottom - 12;
        int pageGapLeft = left + this.backgroundWidth / 2 - 3;
        int pageGapRight = left + this.backgroundWidth / 2 + 3;

        context.fill(left + 3, top + 4, right + 3, bottom + 4, 0x44100000);
        context.fillGradient(left, top, right, bottom, 0xFFC8B79E, 0xFFB29C80);
        context.drawBorder(left, top, this.backgroundWidth, this.backgroundHeight, 0xFF5E4A36);

        context.fill(left + 1, top + 1, right - 1, top + 22, 0xFFB49C7D);
        context.fill(left + 1, top + 23, right - 1, top + 24, 0x88FFF7EA);
        context.fill(paperLeft, paperTop, paperRight, paperBottom, 0xFFFFFCF4);
        context.drawBorder(paperLeft, paperTop, paperRight - paperLeft, paperBottom - paperTop, 0xFFDCCFB9);
        context.fill(pageGapLeft, paperTop + 6, pageGapRight, paperBottom - 6, 0xFFD5C5AB);
        context.fill(pageGapLeft + 1, paperTop + 6, pageGapRight - 1, paperBottom - 6, 0x55FFF8E8);

        context.fill(paperLeft + 16, paperTop + 58, paperRight - 16, paperTop + 59, 0x33A28C6A);
        context.fill(paperLeft + 16, paperTop + 60, paperRight - 16, paperTop + 61, 0x22FFFFFF);
        context.fill(paperLeft + 10, paperTop + 22, pageGapLeft - 10, paperTop + 23, 0x22A28C6A);
        context.fill(pageGapRight + 10, paperTop + 22, paperRight - 10, paperTop + 23, 0x22A28C6A);
    }
}
