package jp.ac.u_tokyo.sdm.sdm_mod.client;

/** setup 後にチャット/コマンド入力画面を開けないようにするクライアント側フラグ。 */
public final class ClientCommandLockState {
    private static volatile boolean locked = false;

    private ClientCommandLockState() {
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
