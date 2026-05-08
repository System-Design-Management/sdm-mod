package jp.ac.u_tokyo.sdm.sdm_mod.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class TeacherDialogueHud implements HudElement {
    public static final TeacherDialogueHud INSTANCE = new TeacherDialogueHud();

    private static final Identifier TEACHER_FACE_TEXTURE =
        Identifier.of("sdm_mod", "textures/gui/teacher_face.png");

    private static final int FACE_RENDER_SIZE = 64;
    private static final int FACE_TEXTURE_SIZE = 128;
    private static final int PANEL_HEIGHT = 90;
    private static final int PANEL_BOTTOM_MARGIN = 16;
    private static final int PANEL_LEFT_MARGIN = 16;
    private static final int PANEL_RIGHT_MARGIN = 16;
    private static final int TEXT_PADDING = 12;
    // 1ティックあたりに進む文字数。
    private static final int CHARS_PER_TICK = 1;
    // 全文表示後、自動で消えるまでのティック数（20tick = 1秒）。
    private static final int DISMISS_TICKS = 100;

    private boolean active = false;
    private String fullText = "";
    private int visibleChars = 0;
    private int ticksSinceComplete = 0;

    private TeacherDialogueHud() {
    }

    /** サーバーからパケットを受け取ったときに呼ぶ。メインスレッドから呼ぶこと。 */
    public void show(String text) {
        this.fullText = text;
        this.visibleChars = 0;
        this.ticksSinceComplete = 0;
        this.active = true;
    }

    /**
     * ClientTickEvents で毎ティック呼び出す。
     * Screen ではないのでゲームのティックが止まらず、ここで時間を管理できる。
     */
    public void tick() {
        if (!active) {
            return;
        }

        if (visibleChars < fullText.length()) {
            visibleChars = Math.min(visibleChars + CHARS_PER_TICK, fullText.length());
        } else {
            // 全文表示済み。DISMISS_TICKS 後に自動消去する。
            ticksSinceComplete++;
            if (ticksSinceComplete >= DISMISS_TICKS) {
                active = false;
            }
        }
    }

    // HudElement.render() は毎フレーム呼ばれる。active でなければ何も描画しない。
    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!active) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int panelTop = screenHeight - PANEL_HEIGHT - PANEL_BOTTOM_MARGIN;
        int panelBottom = screenHeight - PANEL_BOTTOM_MARGIN;
        int panelLeft = PANEL_LEFT_MARGIN;
        int panelRight = screenWidth - PANEL_RIGHT_MARGIN;

        // アイコンはパネル内に縦中央揃えで配置する。
        int faceX = panelLeft;
        int faceY = panelTop + (PANEL_HEIGHT - FACE_RENDER_SIZE) / 2;

        // テキストボックスはアイコンの右隣から画面右端まで。
        int boxLeft = faceX + FACE_RENDER_SIZE + 8;
        int boxRight = panelRight;
        int boxWidth = boxRight - boxLeft;

        // 半透明の暗い背景。
        context.fill(boxLeft, panelTop, boxRight, panelBottom, 0xCC111111);
        // 縁取り。
        context.drawBorder(boxLeft, panelTop, boxWidth, PANEL_HEIGHT, 0xAAFFFFFF);

        // 教授アイコンを描画する。
        // 描画サイズ(FACE_RENDER_SIZE)と切り取り領域(FACE_TEXTURE_SIZE)を分けることで
        // テクスチャ全体を縮小して表示できる。
        context.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            TEACHER_FACE_TEXTURE,
            faceX, faceY,
            0.0f, 0.0f,
            FACE_RENDER_SIZE, FACE_RENDER_SIZE,
            FACE_TEXTURE_SIZE, FACE_TEXTURE_SIZE,
            FACE_TEXTURE_SIZE, FACE_TEXTURE_SIZE
        );

        // 現在表示すべき文字数ぶんだけ描画する（ストリーム効果）。
        context.drawWrappedText(
            client.textRenderer,
            Text.literal(fullText.substring(0, visibleChars)),
            boxLeft + TEXT_PADDING,
            panelTop + TEXT_PADDING,
            boxWidth - TEXT_PADDING * 2,
            0xFFFFFFFF,
            true
        );
    }
}
