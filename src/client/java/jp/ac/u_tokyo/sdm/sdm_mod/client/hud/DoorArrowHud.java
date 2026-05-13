package jp.ac.u_tokyo.sdm.sdm_mod.client.hud;

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

    private DoorArrowHud() {
    }

    /** サーバーから DoorArrowPayload を受け取ったときに呼ぶ。 */
    public void setEnabled(boolean enabled) {
        this.arrowEnabled = enabled;
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!arrowEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) return;

        // Y 座標が MIN_Y 未満のときは表示しない
        if (player.getY() < MIN_Y) return;

        double px = player.getX();
        double pz = player.getZ();

        // 最も近いドアを選択する
        double targetX = DOOR_CENTERS[0][0];
        double targetZ = DOOR_CENTERS[0][1];
        double minDistSq = distSq(px, pz, targetX, targetZ);
        for (double[] door : DOOR_CENTERS) {
            double d = distSq(px, pz, door[0], door[1]);
            if (d < minDistSq) {
                minDistSq = d;
                targetX = door[0];
                targetZ = door[1];
            }
        }

        // プレイヤーの向き（Minecraft yaw: 0=南, 90=西, 180=北, -90=東）を
        // コンパス方位（北=0, 東=90, 南=180, 西=270）に変換する。
        double playerBearing = ((player.getYaw() + 180.0) % 360 + 360) % 360;

        // ドアへのコンパス方位を計算する。
        double dx = targetX - px;
        double dz = targetZ - pz;
        double doorBearing = Math.toDegrees(Math.atan2(dx, -dz));

        // 画面上の矢印回転角（時計回り正）をラジアンに変換する。
        double arrowAngleDeg = doorBearing - playerBearing;
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
