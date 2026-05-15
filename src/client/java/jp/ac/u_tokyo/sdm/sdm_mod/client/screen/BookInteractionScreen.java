package jp.ac.u_tokyo.sdm.sdm_mod.client.screen;

import jp.ac.u_tokyo.sdm.sdm_mod.client.hud.TeacherDialogueHud;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4DialogueClosedPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class BookInteractionScreen extends Screen {
    private static final int PANEL_W = 280;
    private static final int PANEL_H = 180;
    private static final float TITLE_SCALE = 2.0f;
    private static final int HEADER_H = 24;
    private static final int PANEL_PAD = 12;
    private static final int BTN_W = 110;
    private static final int BTN_H = 20;
    private static final int BTN_GAP = 12;

    private final String bookTitle;
    private final boolean isKeyBook;

    public BookInteractionScreen(String bookTitle, boolean isKeyBook) {
        super(Text.literal("書棚"));
        this.bookTitle = bookTitle;
        this.isKeyBook = isKeyBook;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        int panelBottom = (height - PANEL_H) / 2 + PANEL_H;
        int btnY = panelBottom + 8;

        if (isKeyBook) {
            int btnX = (width - BTN_W) / 2;
            addDrawableChild(ButtonWidget.builder(Text.literal("持っていく"), btn ->
                openKeyBookDialogue()
            ).dimensions(btnX, btnY, BTN_W, BTN_H).build());
            return;
        }

        int totalBtnW = BTN_W * 2 + BTN_GAP;
        int btn1X = (width - totalBtnW) / 2;
        int btn2X = btn1X + BTN_W + BTN_GAP;

        addDrawableChild(ButtonWidget.builder(Text.literal("持っていく"), btn -> {
            MinecraftClient.getInstance().setScreen(null);
            TeacherDialogueHud.INSTANCE.show("おいおい、それじゃないぞ", 60);
        }).dimensions(btn1X, btnY, BTN_W, BTN_H).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("いらない"), btn ->
            this.close()
        ).dimensions(btn2X, btnY, BTN_W, BTN_H).build());
    }

    private void openKeyBookDialogue() {
        MinecraftClient.getInstance().setScreen(null);
        ClientPlayNetworking.send(new Phase4DialogueClosedPayload());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x66000000);

        int px = (width - PANEL_W) / 2;
        int py = (height - PANEL_H) / 2;

        // shadow
        context.fill(px + 3, py + 4, px + PANEL_W + 3, py + PANEL_H + 4, 0x44100000);
        // parchment background
        context.fillGradient(px, py, px + PANEL_W, py + PANEL_H, 0xFFC8B79E, 0xFFB29C80);
        context.drawBorder(px, py, PANEL_W, PANEL_H, 0xFF5E4A36);
        // header
        context.fill(px + 1, py + 1, px + PANEL_W - 1, py + HEADER_H, 0xFFB49C7D);
        context.fill(px + 1, py + HEADER_H, px + PANEL_W - 1, py + HEADER_H + 1, 0x88FFF7EA);

        // paper area
        int paperL = px + PANEL_PAD;
        int paperT = py + HEADER_H + 4;
        int paperR = px + PANEL_W - PANEL_PAD;
        int paperB = py + PANEL_H - PANEL_PAD;
        context.fill(paperL, paperT, paperR, paperB, 0xFFFFFCF4);
        context.drawBorder(paperL, paperT, paperR - paperL, paperB - paperT, 0xFFDCCFB9);

        // header title
        String header = "書棚から取り出した本";
        int headerW = textRenderer.getWidth(header);
        context.drawText(textRenderer, header, px + (PANEL_W - headerW) / 2, py + 7, 0xFF111111, false);

        // book title（太文字・中央揃え・拡大）
        Text boldTitle = Text.literal(bookTitle).formatted(Formatting.BOLD);
        int titleW = (int)(textRenderer.getWidth(boldTitle) * TITLE_SCALE);
        int titleX = px + (PANEL_W - titleW) / 2;
        int titleY = paperT + 16;
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(titleX, titleY);
        matrices.scale(TITLE_SCALE, TITLE_SCALE);
        context.drawText(textRenderer, boldTitle, 0, 0, 0xFF111111, false);
        matrices.popMatrix();

        super.render(context, mouseX, mouseY, delta);
    }
}
