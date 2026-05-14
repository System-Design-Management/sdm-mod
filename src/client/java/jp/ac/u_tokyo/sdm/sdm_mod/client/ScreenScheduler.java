package jp.ac.u_tokyo.sdm.sdm_mod.client;

import net.minecraft.client.gui.screen.Screen;

/**
 * removed() の中で setScreen() を呼ぶと外側の setScreen(null) に上書きされる問題、
 * および removed() の中で ClientPlayNetworking.send() が正しく動作しない問題を回避するため、
 * 次の END_CLIENT_TICK まで処理を遅延させるユーティリティ。
 */
public final class ScreenScheduler {
    private static Screen pending = null;
    private static Runnable pendingAction = null;

    private ScreenScheduler() {
    }

    /** 次ティック終了時に開く画面をセットする。 */
    public static void schedule(Screen screen) {
        pending = screen;
    }

    /** セット済みの画面を取り出してクリアする（なければ null）。 */
    public static Screen poll() {
        Screen screen = pending;
        pending = null;
        return screen;
    }

    /** 次ティック終了時に実行するアクションをセットする。 */
    public static void scheduleAction(Runnable action) {
        pendingAction = action;
    }

    /** セット済みのアクションを取り出して実行する（なければ何もしない）。 */
    public static void runPendingAction() {
        Runnable action = pendingAction;
        pendingAction = null;
        if (action != null) {
            action.run();
        }
    }
}
