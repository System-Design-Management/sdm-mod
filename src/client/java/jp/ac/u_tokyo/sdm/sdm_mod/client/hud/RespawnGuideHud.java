package jp.ac.u_tokyo.sdm.sdm_mod.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public final class RespawnGuideHud implements HudElement {
    public static final RespawnGuideHud INSTANCE = new RespawnGuideHud();

    private static final String MESSAGE = "入り口からやり直し";
    private static final int DURATION_TICKS = 60;
    private static final int MARGIN = 16;
    private static final int PADDING_X = 10;
    private static final int PADDING_Y = 8;

    private int remainingTicks = 0;

    private RespawnGuideHud() {
    }

    public void show() {
        remainingTicks = DURATION_TICKS;
    }

    public void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (remainingTicks <= 0) {
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
