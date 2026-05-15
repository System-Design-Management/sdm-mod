package jp.ac.u_tokyo.sdm.sdm_mod.client.render;

/** 花火演出時のカメラシェイク状態を管理する。 */
public final class CameraShakeState {
    private static final int DURATION_TICKS = 70;
    private static final float INTENSITY_DEGREES = 1.5f;

    private static int remainingTicks = 0;
    private static int shakeTick = 0;

    private CameraShakeState() {
    }

    public static void start() {
        remainingTicks = DURATION_TICKS;
        shakeTick = 0;
    }

    public static void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
            shakeTick++;
        }
    }

    /** フレーム補間込みの現在のシェイク角度（度）を返す。シェイク中でなければ 0。 */
    public static float getAngle(float tickDelta) {
        if (remainingTicks <= 0) return 0f;
        float fade = remainingTicks / (float) DURATION_TICKS;
        return (float) (Math.sin((shakeTick + tickDelta) * 1.5) * INTENSITY_DEGREES * fade);
    }
}
