package jp.ac.u_tokyo.sdm.sdm_mod.client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

public final class Phase5GameOverScreen extends Screen {
    private static final Text GAME_OVER_TEXT = Text.literal("GAME OVER");
    private static final int BACKGROUND_COLOR = 0xFF000000;
    private static final int TEXT_COLOR = 0xFFFF5555;
    private static final int RETURN_DELAY_TICKS = 60;

    private int remainingTicks = RETURN_DELAY_TICKS;
    private boolean disconnecting;

    public Phase5GameOverScreen() {
        super(GAME_OVER_TEXT);
    }

    @Override
    public void tick() {
        if (disconnecting) {
            return;
        }

        remainingTicks--;
        if (remainingTicks > 0) {
            return;
        }

        disconnecting = true;
        MinecraftClient client = MinecraftClient.getInstance();
        client.disconnect(new TitleScreen(), false);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, width, height, BACKGROUND_COLOR);
        context.drawCenteredTextWithShadow(textRenderer, GAME_OVER_TEXT, width / 2, height / 2 - 4, TEXT_COLOR);
    }
}
