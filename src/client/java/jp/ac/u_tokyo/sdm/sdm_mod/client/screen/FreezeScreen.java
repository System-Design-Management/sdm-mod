package jp.ac.u_tokyo.sdm.sdm_mod.client.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * 完全に透明で入力を全て遮断するスクリーン。
 * プレイヤーの動きを止めたい間だけ開き、指定ティック後に自動で閉じる。
 */
public final class FreezeScreen extends Screen {
    private final int durationTicks;
    private final Runnable afterClose;
    private int tickCounter = 0;
    private boolean removedFired = false;

    public FreezeScreen(int durationTicks, Runnable afterClose) {
        super(Text.empty());
        this.durationTicks = durationTicks;
        this.afterClose = afterClose;
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
    public void tick() {
        tickCounter++;
        if (tickCounter >= durationTicks) {
            this.close();
        }
    }

    // キー・マウス入力を全て吸収してゲームに渡さない
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 何も描画しない（透明）
    }

    @Override
    public void removed() {
        super.removed();
        if (afterClose != null && !removedFired) {
            removedFired = true;
            afterClose.run();
        }
    }
}
