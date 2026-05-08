package jp.ac.u_tokyo.sdm.sdm_mod.client.screen;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class TeacherDialogueScreen extends Screen {
    private static final Identifier TEACHER_FACE_TEXTURE =
        Identifier.of("sdm_mod", "textures/gui/teacher_face.png");

    // アイコンのレンダリングサイズ（px）。テクスチャ実寸とは独立して拡縮できる。
    private static final int FACE_RENDER_SIZE = 80;
    // teacher_face.png の実際のピクセルサイズ。UV計算に使う。
    private static final int FACE_TEXTURE_SIZE = 128;

    private static final int PANEL_HEIGHT = 90;
    private static final int PANEL_BOTTOM_MARGIN = 16;
    private static final int PANEL_LEFT_MARGIN = 16;
    private static final int PANEL_RIGHT_MARGIN = 16;
    private static final int TEXT_PADDING = 12;
    // 1ティック(50ms)あたりに表示を進める文字数。多いほど速く流れる。
    private static final int CHARS_PER_TICK = 2;

    private final String fullText;
    private int visibleChars = 0;

    public TeacherDialogueScreen(String text) {
        super(Text.empty());
        this.fullText = text;
    }

    // false にするとゲームのサーバーティックが止まらない。
    // ストーリー中は世界が動き続けてほしいため false にする。
    @Override
    public boolean shouldPause() {
        return false;
    }

    // 毎ティック呼ばれる。visibleChars を増やすことでストリーム表示を実現する。
    @Override
    public void tick() {
        if (visibleChars < fullText.length()) {
            visibleChars = Math.min(visibleChars + CHARS_PER_TICK, fullText.length());
        }
    }

    // クリックで「全文即表示 → 閉じる」の2段階動作にする。
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (visibleChars < fullText.length()) {
            visibleChars = fullText.length();
        } else {
            this.close();
        }
        return true;
    }

    // Space / Enter / Escape でも同様に操作できるようにする。
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE
            || keyCode == GLFW.GLFW_KEY_ENTER
            || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (visibleChars < fullText.length()) {
                visibleChars = fullText.length();
            } else {
                this.close();
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // パネルの座標を算出する。
        int panelTop = this.height - PANEL_HEIGHT - PANEL_BOTTOM_MARGIN;
        int panelBottom = this.height - PANEL_BOTTOM_MARGIN;
        int panelLeft = PANEL_LEFT_MARGIN;
        int panelRight = this.width - PANEL_RIGHT_MARGIN;

        // 教授アイコンはパネルの上端から FACE_RENDER_SIZE 分、パネルより上に突き出て配置する。
        int faceX = panelLeft;
        int faceY = panelTop - FACE_RENDER_SIZE + PANEL_HEIGHT;

        // テキストボックスはアイコンの右から始まる。
        int boxLeft = faceX + FACE_RENDER_SIZE + 8;
        int boxRight = panelRight;
        int boxWidth = boxRight - boxLeft;

        // 半透明の暗い背景でテキストボックスを描画する。
        context.fill(boxLeft, panelTop, boxRight, panelBottom, 0xCC111111);
        // 縁取りで吹き出しらしさを出す。
        context.drawBorder(boxLeft, panelTop, boxWidth, PANEL_HEIGHT, 0xAAFFFFFF);

        // 教授アイコンを描画する。
        // 引数: pipeline, テクスチャID, 描画先X, 描画先Y, UV-X, UV-Y, 描画幅, 描画高さ, テクスチャ幅, テクスチャ高さ
        context.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            TEACHER_FACE_TEXTURE,
            faceX, faceY,
            0.0f, 0.0f,
            FACE_RENDER_SIZE, FACE_RENDER_SIZE,
            FACE_TEXTURE_SIZE, FACE_TEXTURE_SIZE
        );

        // ストリーム表示中のテキストを描画する。
        String displayText = fullText.substring(0, visibleChars);
        context.drawWrappedText(
            this.textRenderer,
            Text.literal(displayText),
            boxLeft + TEXT_PADDING,
            panelTop + TEXT_PADDING,
            boxWidth - TEXT_PADDING * 2,
            0xFFFFFFFF,
            true
        );

        // 全文表示済みのとき、閉じ方のヒントを右下に表示する。
        if (visibleChars >= fullText.length()) {
            String hint = "[ クリックまたは Space で閉じる ]";
            int hintWidth = this.textRenderer.getWidth(hint);
            context.drawText(
                this.textRenderer,
                hint,
                boxRight - hintWidth - TEXT_PADDING,
                panelBottom - this.textRenderer.fontHeight - 6,
                0xAAFFFFFF,
                false
            );
        }

        super.render(context, mouseX, mouseY, delta);
    }
}
