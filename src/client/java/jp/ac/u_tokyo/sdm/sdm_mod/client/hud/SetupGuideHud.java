package jp.ac.u_tokyo.sdm.sdm_mod.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public final class SetupGuideHud implements HudElement {
    public static final SetupGuideHud INSTANCE = new SetupGuideHud();

    private static final String MESSAGE = "安田講堂の中に入ってゲームスタート！";
    private static final int MARGIN = 16;
    private static final int PADDING_X = 10;
    private static final int PADDING_Y = 8;

    private boolean visible = false;

    private SetupGuideHud() {
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!visible) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int textWidth = client.textRenderer.getWidth(MESSAGE);
        int textHeight = client.textRenderer.fontHeight;

        int boxLeft = MARGIN;
        int boxTop = MARGIN;
        int boxRight = boxLeft + PADDING_X * 2 + textWidth;
        int boxBottom = boxTop + PADDING_Y * 2 + textHeight;

        context.fill(boxLeft, boxTop, boxRight, boxBottom, 0xCC111111);
        context.drawBorder(boxLeft, boxTop, boxRight - boxLeft, boxBottom - boxTop, 0xAAFFFFFF);
        context.drawText(
            client.textRenderer,
            MESSAGE,
            boxLeft + PADDING_X,
            boxTop + PADDING_Y,
            0xFFFFFFFF,
            true
        );
    }
}
