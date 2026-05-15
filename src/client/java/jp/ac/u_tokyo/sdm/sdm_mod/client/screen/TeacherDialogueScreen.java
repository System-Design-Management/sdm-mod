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
    private static final int FACE_RENDER_SIZE = 128;
    // teacher_face.png の実際のピクセルサイズ。UV計算に使う。
    private static final int FACE_TEXTURE_SIZE = 128;

    private static final int PANEL_HEIGHT = 90;
    private static final int PANEL_BOTTOM_MARGIN = 16;
    private static final int PANEL_LEFT_MARGIN = 16;
    private static final int PANEL_RIGHT_MARGIN = 16;
    private static final int TEXT_PADDING = 12;
    // 1文字進めるのに必要なティック数。大きいほど遅くなる（1=20字/秒、2=10字/秒、4=5字/秒）。
    private static final int TICKS_PER_CHAR = 1;

    private final String fullText;
    private final Runnable afterClose;
    // 0 = 手動で閉じる。正数 = 指定ティック後に自動で閉じる。
    private final int autoCloseTicks;
    private int visibleChars = 0;
    private int tickCounter = 0;
    private int autoCloseCounter = 0;
    private boolean removedFired = false;

    public TeacherDialogueScreen(String text) {
        this(text, null, 0);
    }

    public TeacherDialogueScreen(String text, Runnable afterClose) {
        this(text, afterClose, 0);
    }

    public TeacherDialogueScreen(String text, Runnable afterClose, int autoCloseTicks) {
        super(Text.empty());
        this.fullText = text;
        this.afterClose = afterClose;
        this.autoCloseTicks = autoCloseTicks;
        // 自動消去モードでは最初から全文を表示する
        if (autoCloseTicks > 0) {
            this.visibleChars = text.length();
        }
    }

    // false にするとゲームのサーバーティックが止まらない。
    // ストーリー中は世界が動き続けてほしいため false にする。
    @Override
    public boolean shouldPause() {
        return false;
    }

    // 毎ティック呼ばれる。TICKS_PER_CHAR ティックごとに1文字進める。自動消去モードではカウントダウンする。
    @Override
    public void tick() {
        if (autoCloseTicks > 0) {
            autoCloseCounter++;
            if (autoCloseCounter >= autoCloseTicks) {
                this.close();
            }
            return;
        }
        if (visibleChars < fullText.length()) {
            tickCounter++;
            if (tickCounter >= TICKS_PER_CHAR) {
                tickCounter = 0;
                visibleChars++;
            }
        }
    }

    // クリックで「全文即表示 → 閉じる」の2段階動作にする。自動消去モードでは操作を受け付けない。
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (autoCloseTicks > 0) {
            return false;
        }
        if (visibleChars < fullText.length()) {
            visibleChars = fullText.length();
        } else {
            this.close();
        }
        return true;
    }

    // Space / Enter / Escape でも同様に操作できるようにする。自動消去モードでは操作を受け付けない。
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (autoCloseTicks > 0) {
            return false;
        }
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
        // 引数: pipeline, テクスチャID, 描画先X/Y, UV開始, 描画サイズ, 切り取り領域サイズ, テクスチャ実寸
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
            String hint = autoCloseTicks > 0 ? "" : "[ クリックまたは Space で閉じる ]";
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

    @Override
    public void removed() {
        super.removed();
        if (afterClose != null && !removedFired) {
            removedFired = true;
            afterClose.run();
        }
    }
}
