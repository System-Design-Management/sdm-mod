package jp.ac.u_tokyo.sdm.sdm_mod.client.screen.warp;

import java.util.List;
import jp.ac.u_tokyo.sdm.sdm_mod.screen.WarpSelectScreenHandler;
import jp.ac.u_tokyo.sdm.sdm_mod.warp.WarpDestination;
import jp.ac.u_tokyo.sdm.sdm_mod.warp.WarpWorldState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

public final class WarpSelectScreen extends HandledScreen<WarpSelectScreenHandler> {
    private static final Text DESCRIPTION_TEXT = Text.translatable("screen.sdm_mod.warp.description");
    private static final Text CLOSE_TEXT = Text.translatable("screen.sdm_mod.warp.close");
    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_HEIGHT = 176;
    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;

    public WarpSelectScreen(
        WarpSelectScreenHandler handler,
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

        List<WarpDestination> destinations = WarpWorldState.getDestinations();
        int buttonX = this.x + (this.backgroundWidth - BUTTON_WIDTH) / 2;
        int currentY = this.y + 36;

        for (int i = 0; i < destinations.size(); i++) {
            WarpDestination destination = destinations.get(i);
            int destinationIndex = i;
            this.addDrawableChild(ButtonWidget.builder(
                Text.translatable(destination.nameTranslationKey()),
                button -> selectDestination(destinationIndex)
            ).dimensions(buttonX, currentY, BUTTON_WIDTH, BUTTON_HEIGHT).build());
            currentY += BUTTON_HEIGHT + BUTTON_GAP;
        }

        this.addDrawableChild(ButtonWidget.builder(CLOSE_TEXT, button -> this.close())
            .dimensions(buttonX, this.y + this.backgroundHeight - 26, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build());
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        context.drawText(this.textRenderer, this.title, titleX, 10, 0xFF1F1F1F, false);
        context.drawWrappedText(
            this.textRenderer,
            DESCRIPTION_TEXT,
            16,
            24,
            this.backgroundWidth - 32,
            0xFF333333,
            false
        );
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.fill(this.x, this.y, this.x + this.backgroundWidth, this.y + this.backgroundHeight, 0xFFE6DFCF);
        context.drawBorder(this.x, this.y, this.backgroundWidth, this.backgroundHeight, 0xFF5E4A36);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x66000000);
    }

    private void selectDestination(int destinationIndex) {
        if (this.client == null || this.client.interactionManager == null) {
            return;
        }

        // ScreenHandler のボタンIDとして destinationIndex をそのままサーバーへ渡す。
        this.client.interactionManager.clickButton(this.handler.syncId, destinationIndex);
        this.close();
    }
}
