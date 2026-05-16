package jp.ac.u_tokyo.sdm.sdm_mod.client.hud;

import jp.ac.u_tokyo.sdm.sdm_mod.story.network.DoorArrowPayload;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix3x2fStack;

public final class DoorArrowHud implements HudElement {
    public static final DoorArrowHud INSTANCE = new DoorArrowHud();

    // ドアの中心座標（XZ）。BlockPos(-175,41,-640) と BlockPos(-176,41,-640) の中心。
    private static final double[][] DOOR_CENTERS = {
        { -174.5, -639.5 },
        { -175.5, -639.5 }
    };

    private static final int MIN_Y = 41;
    private static final int BG_SIZE = 56;
    private static final int MARGIN = 16;

    private boolean arrowEnabled = false;
    private boolean customTarget = false;
    private double targetX = 0.0;
    private double targetZ = 0.0;
    private double minVisibleY = MIN_Y;

    private DoorArrowHud() {
    }

    /** サーバーから DoorArrowPayload を受け取ったときに呼ぶ。 */
    public void apply(DoorArrowPayload payload) {
        this.arrowEnabled = payload.visible();
        this.customTarget = payload.customTarget();
        this.targetX = payload.targetX();
        this.targetZ = payload.targetZ();
        this.minVisibleY = payload.minVisibleY();
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!arrowEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) return;

        // 指定された Y 座標より下では表示しない
        if (player.getY() < minVisibleY) return;

        double px = player.getX();
        double pz = player.getZ();

        double currentTargetX = targetX;
        double currentTargetZ = targetZ;
        if (!customTarget) {
            // 最も近いドアを選択する
            currentTargetX = DOOR_CENTERS[0][0];
            currentTargetZ = DOOR_CENTERS[0][1];
            double minDistSq = distSq(px, pz, currentTargetX, currentTargetZ);
            for (double[] door : DOOR_CENTERS) {
                double d = distSq(px, pz, door[0], door[1]);
                if (d < minDistSq) {
                    minDistSq = d;
                    currentTargetX = door[0];
                    currentTargetZ = door[1];
                }
            }
        }

        // プレイヤーの向き（Minecraft yaw: 0=南, 90=西, 180=北, -90=東）を
        // コンパス方位（北=0, 東=90, 南=180, 西=270）に変換する。
        double playerBearing = ((player.getYaw() + 180.0) % 360 + 360) % 360;

        // 目的地へのコンパス方位を計算する。
        double dx = currentTargetX - px;
        double dz = currentTargetZ - pz;
        double targetBearing = Math.toDegrees(Math.atan2(dx, -dz));

        // 画面上の矢印回転角（時計回り正）をラジアンに変換する。
        double arrowAngleDeg = targetBearing - playerBearing;
        float arrowAngleRad = (float) Math.toRadians(arrowAngleDeg);

        int screenWidth = client.getWindow().getScaledWidth();

        // 背景（右上）
        int bgX = screenWidth - MARGIN - BG_SIZE;
        int bgY = MARGIN;
        context.fill(bgX, bgY, bgX + BG_SIZE, bgY + BG_SIZE, 0xCC000000);
        context.drawBorder(bgX, bgY, BG_SIZE, BG_SIZE, 0x88FFFFFF);

        // 矢印をBG中央で描画する
        float cx = bgX + BG_SIZE / 2.0f;
        float cy = bgY + BG_SIZE / 2.0f;

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(cx, cy);
        // JOML rotate: +angle = 標準数学の反時計回り = 画面上で時計回り（Y下向き座標系）
        matrices.rotate(arrowAngleRad);

        // "↑" を3倍スケールで中央揃えに描画する
        float scale = 3.0f;
        matrices.scale(scale, scale);
        String arrow = "↑";
        int textWidth = client.textRenderer.getWidth(arrow);
        int textHeight = client.textRenderer.fontHeight;
        context.drawText(client.textRenderer, arrow,
            -textWidth / 2, -textHeight / 2, 0xFFFFFF00, true);

        matrices.popMatrix();
    }

    private static double distSq(double x1, double z1, double x2, double z2) {
        return (x2 - x1) * (x2 - x1) + (z2 - z1) * (z2 - z1);
    }
}
