package jp.ac.u_tokyo.sdm.sdm_mod.game;

/** setup 実行後にすべてのプレイヤーコマンドをブロックするためのフラグ。 */
public final class CommandLockState {
    private static volatile boolean locked = false;
    /** サーバー内部コマンド実行中はロックを一時的に無視するためのスレッドローカルフラグ。 */
    private static final ThreadLocal<Boolean> suppressed = ThreadLocal.withInitial(() -> false);

    private CommandLockState() {
    }

    public static void lock() {
        locked = true;
    }

    public static void unlock() {
        locked = false;
    }

    public static boolean isLocked() {
        return locked && !suppressed.get();
    }

    /** サーバー内部コマンド（give 等）をロック中でも実行するためのラッパー。 */
    public static void runUnlocked(Runnable action) {
        suppressed.set(true);
        try {
            action.run();
        } finally {
            suppressed.set(false);
        }
    }
}
