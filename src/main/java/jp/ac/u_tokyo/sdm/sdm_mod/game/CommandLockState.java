package jp.ac.u_tokyo.sdm.sdm_mod.game;

/** setup 実行後にすべてのプレイヤーコマンドをブロックするためのフラグ。 */
public final class CommandLockState {
    private static volatile boolean locked = false;

    private CommandLockState() {
    }

    public static void lock() {
        locked = true;
    }

    public static void unlock() {
        locked = false;
    }

    public static boolean isLocked() {
        return locked;
    }
}
