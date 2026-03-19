package jp.ac.u_tokyo.sdm.extra_items.client.screen;

import jp.ac.u_tokyo.sdm.extra_items.screen.TechnicalBookScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public final class TechnicalBookScreen extends HandledScreen<TechnicalBookScreenHandler> {
    private static final Text SUBTITLE_TEXT = Text.translatable("screen.extra_items.technical_book.subtitle");
    private static final Text FORMULA_TEXT = Text.translatable("screen.extra_items.technical_book.formula");
    private static final Text EXPLANATION_TEXT = Text.translatable("screen.extra_items.technical_book.explanation");
    private static final Text FOOTNOTE_TEXT = Text.translatable("screen.extra_items.technical_book.footnote");
    private static final Text CLOSE_TEXT = Text.translatable("screen.extra_items.technical_book.close");
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

        int buttonWidth = 80;
        int buttonHeight = 20;
        int buttonX = this.x + (this.backgroundWidth - buttonWidth) / 2;
        int buttonY = this.y + this.backgroundHeight - 30;

        this.addDrawableChild(ButtonWidget.builder(CLOSE_TEXT, button -> this.close())
            .dimensions(buttonX, buttonY, buttonWidth, buttonHeight)
            .build());
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // TODO: ページ送りや章構成を追加する場合は、左右ページごとに操作を分ける。
        int titleWidth = this.textRenderer.getWidth(this.title);
        int subtitleWidth = this.textRenderer.getWidth(SUBTITLE_TEXT);

        context.drawText(this.textRenderer, this.title, (this.backgroundWidth - titleWidth) / 2, 14, 0xFF3A2412, false);
        context.drawText(this.textRenderer, SUBTITLE_TEXT, (this.backgroundWidth - subtitleWidth) / 2, 30, 0xFF70543F, false);
        context.drawCenteredTextWithShadow(this.textRenderer, FORMULA_TEXT, this.backgroundWidth / 2, 70, 0xFF2B1D12);

        context.drawWrappedText(
            this.textRenderer,
            EXPLANATION_TEXT,
            24,
            96,
            this.backgroundWidth - 48,
            0xFF4A3828,
            false
        );
        context.drawWrappedText(
            this.textRenderer,
            FOOTNOTE_TEXT,
            24,
            138,
            this.backgroundWidth - 48,
            0xFF6C5845,
            false
        );
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int left = this.x;
        int top = this.y;
        int right = left + this.backgroundWidth;
        int bottom = top + this.backgroundHeight;
        int centerX = left + this.backgroundWidth / 2;

        // 専用テクスチャを用意するまで、見開きの本のような簡易レイアウトを描画する。
        context.fillGradient(left, top, right, bottom, 0xFFECD8B4, 0xFFD7BE95);
        context.fill(centerX - 3, top + 10, centerX + 3, bottom - 10, 0xFF8B6A45);
        context.fill(left + 8, top + 8, centerX - 5, bottom - 8, 0xFFF7E9C9);
        context.fill(centerX + 5, top + 8, right - 8, bottom - 8, 0xFFF7E9C9);
        context.drawBorder(left, top, this.backgroundWidth, this.backgroundHeight, 0xFF6B4A2E);
        context.drawBorder(left + 8, top + 8, centerX - left - 13, this.backgroundHeight - 16, 0xFFD0B183);
        context.drawBorder(centerX + 5, top + 8, right - centerX - 13, this.backgroundHeight - 16, 0xFFD0B183);
        context.fill(left + 16, top + 46, right - 16, top + 47, 0xCCB18A5C);
        context.fill(left + 16, top + 48, right - 16, top + 49, 0x55FFFFFF);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // GUI 全体の下地も明るめにして、中央パネルが描けていない場合でも原因を切り分けやすくする。
        context.fillGradient(0, 0, this.width, this.height, 0xFF5A4332, 0xFF2C2018);
    }
}
