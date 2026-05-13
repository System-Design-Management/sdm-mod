package jp.ac.u_tokyo.sdm.sdm_mod.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jp.ac.u_tokyo.sdm.sdm_mod.screen.SearchPcScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class SearchPcScreen extends HandledScreen<SearchPcScreenHandler> {
    private static final Text TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.title");
    private static final Text SUBTITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.subtitle");
    private static final Text LOGIN_TEXT = Text.translatable("screen.sdm_mod.search_pc.login");
    private static final Text HELP_LINK_TEXT = Text.translatable("screen.sdm_mod.search_pc.help");
    private static final Text INPUT_ASSIST_TEXT = Text.translatable("screen.sdm_mod.search_pc.input_assist");
    private static final Text ENGLISH_TEXT = Text.translatable("screen.sdm_mod.search_pc.english");
    private static final Text BANNER_TEXT = Text.translatable("screen.sdm_mod.search_pc.banner");
    private static final Text SIMPLE_MODE_TEXT = Text.translatable("screen.sdm_mod.search_pc.mode.simple");
    private static final Text ADVANCED_MODE_TEXT = Text.translatable("screen.sdm_mod.search_pc.mode.advanced");
    private static final Text SEARCH_TEXT = Text.translatable("screen.sdm_mod.search_pc.search");
    private static final Text CLEAR_TEXT = Text.translatable("screen.sdm_mod.search_pc.clear");
    private static final Text RESULTS_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.results_title");
    private static final Text HELP_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.help_title");
    private static final Text EMPTY_QUERY_TEXT = Text.translatable("screen.sdm_mod.search_pc.prompt");
    private static final Text NO_RESULTS_TEXT = Text.translatable("screen.sdm_mod.search_pc.no_results");
    private static final Text HINT_TEXT = Text.translatable("screen.sdm_mod.search_pc.hint_enter");
    private static final Text AUTHOR_LABEL_TEXT = Text.translatable("screen.sdm_mod.search_pc.label.author");
    private static final Text LOCATION_LABEL_TEXT = Text.translatable("screen.sdm_mod.search_pc.label.location");
    private static final Text STATUS_LABEL_TEXT = Text.translatable("screen.sdm_mod.search_pc.label.status");
    private static final Text FOOTER_TEXT = Text.translatable("screen.sdm_mod.search_pc.footer");

    private static final int PANEL_WIDTH = 404;
    private static final int PANEL_HEIGHT = 254;
    private static final int HEADER_HEIGHT = 34;
    private static final int NAV_HEIGHT = 16;
    private static final int QUERY_FIELD_HEIGHT = 22;
    private static final int BODY_HEIGHT = 78;
    private static final int SEARCH_BUTTON_WIDTH = 54;
    private static final int CLEAR_BUTTON_WIDTH = 46;

    private static final List<Text> NAV_LABELS = List.of(
        Text.translatable("screen.sdm_mod.search_pc.nav.home"),
        Text.translatable("screen.sdm_mod.search_pc.nav.ask"),
        Text.translatable("screen.sdm_mod.search_pc.nav.service"),
        Text.translatable("screen.sdm_mod.search_pc.nav.new_arrivals"),
        Text.translatable("screen.sdm_mod.search_pc.nav.databases"),
        Text.translatable("screen.sdm_mod.search_pc.nav.repository")
    );

    private static final List<DatabaseTab> DATABASE_TABS = List.of(
        new DatabaseTab(Text.translatable("screen.sdm_mod.search_pc.database.utokyo"), "utokyo"),
        new DatabaseTab(Text.translatable("screen.sdm_mod.search_pc.database.cinii_books"), "cinii_books"),
        new DatabaseTab(Text.translatable("screen.sdm_mod.search_pc.database.cinii_research"), "cinii_research"),
        new DatabaseTab(Text.translatable("screen.sdm_mod.search_pc.database.irdb"), "irdb"),
        new DatabaseTab(Text.translatable("screen.sdm_mod.search_pc.database.ndl"), "ndl"),
        new DatabaseTab(Text.translatable("screen.sdm_mod.search_pc.database.worldcat"), "worldcat")
    );

    private static final List<Text> HELP_LINES = List.of(
        Text.translatable("screen.sdm_mod.search_pc.help_line_1"),
        Text.translatable("screen.sdm_mod.search_pc.help_line_2"),
        Text.translatable("screen.sdm_mod.search_pc.help_line_3"),
        Text.translatable("screen.sdm_mod.search_pc.help_line_4")
    );

    private static final List<CatalogEntry> CATALOG = List.of(
        new CatalogEntry(
            "utokyo",
            "学術情報リテラシー入門",
            "東京大学附属図書館",
            "総合図書館 3F 開架",
            "利用可",
            "検索キーワードの組み立て方と OPAC の使い方を解説。"
        ),
        new CatalogEntry(
            "utokyo",
            "東京大学百年史 部局史",
            "東京大学百年史編集委員会",
            "総合図書館 B1 書庫",
            "館内利用",
            "東京大学の沿革をまとめた学内所蔵資料。"
        ),
        new CatalogEntry(
            "cinii_books",
            "情報探索演習",
            "山田 太郎",
            "学外共同利用資料 / CiNii Books",
            "取り寄せ可",
            "日本語資料の横断検索を想定した演習書。"
        ),
        new CatalogEntry(
            "cinii_research",
            "大学図書館における検索行動の分析",
            "佐藤 花子",
            "CiNii Research",
            "オンライン",
            "検索語と行動ログの関係を扱う論文。"
        ),
        new CatalogEntry(
            "irdb",
            "学術成果公開のワークフロー設計",
            "UTokyo Repository Team",
            "IRDB",
            "オンライン",
            "機関リポジトリ連携の整理。"
        ),
        new CatalogEntry(
            "ndl",
            "近代日本の大学図書館",
            "国立国会図書館デジタルコレクション",
            "国立国会図書館サーチ",
            "オンライン",
            "国立国会図書館経由で参照できる関連資料。"
        ),
        new CatalogEntry(
            "worldcat",
            "Discovery Systems in Academic Libraries",
            "Emily Carter",
            "WorldCat",
            "ILL 候補",
            "海外図書館の蔵書検索 UI 事例集。"
        ),
        new CatalogEntry(
            "utokyo",
            "データサイエンス概論",
            "松本 恒一",
            "総合図書館 2F 開架",
            "貸出中",
            "データサイエンス基礎の教科書。"
        )
    );

    private TextFieldWidget queryField;
    private boolean advancedMode;
    private int selectedDatabaseIndex;
    private boolean searched;
    private List<CatalogEntry> searchResults = List.of();
    private String lastQuery = "";

    public SearchPcScreen(
        SearchPcScreenHandler handler,
        PlayerInventory inventory,
        Text title
    ) {
        super(handler, inventory, title);
        this.backgroundWidth = PANEL_WIDTH;
        this.backgroundHeight = PANEL_HEIGHT;
        this.playerInventoryTitleX = -1000;
        this.playerInventoryTitleY = -1000;
    }

    @Override
    protected void init() {
        super.init();

        this.queryField = new TextFieldWidget(
            this.textRenderer,
            this.x + 12,
            this.y + 130,
            238,
            QUERY_FIELD_HEIGHT,
            Text.empty()
        );
        this.queryField.setMaxLength(64);
        this.queryField.setDrawsBackground(false);
        this.queryField.setFocused(true);
        this.addDrawableChild(this.queryField);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, TITLE_TEXT, 48, 8, 0xFF121212, false);
        context.drawText(this.textRenderer, SUBTITLE_TEXT, 48, 20, 0xFF2B2B2B, false);
        context.drawText(this.textRenderer, "UTokyo", this.backgroundWidth - 54, 9, 0xFF0F5AB4, false);
        context.drawText(this.textRenderer, LOGIN_TEXT, this.backgroundWidth - 126, 12, 0xFFFFFFFF, false);

        int navX = 10;
        for (Text navLabel : NAV_LABELS) {
            context.drawText(this.textRenderer, navLabel, navX, 40, 0xFFFFFFFF, false);
            navX += this.textRenderer.getWidth(navLabel) + 12;
        }

        drawDatabaseTabs(context);

        context.drawText(this.textRenderer, TITLE_TEXT, 46, 82, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, BANNER_TEXT, 144, 83, 0xFFF0F6FF, false);

        drawModeTabs(context);

        if (this.queryField.getText().isEmpty()) {
            context.drawText(
                this.textRenderer,
                Text.translatable("screen.sdm_mod.search_pc.query_placeholder"),
                16,
                137,
                0xFF6E7E97,
                false
            );
        }
        context.drawText(this.textRenderer, SEARCH_TEXT, 270, 137, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, CLEAR_TEXT, 331, 137, 0xFF0F4B9D, false);
        context.drawText(this.textRenderer, HINT_TEXT, 12, 156, 0xFF50627B, false);

        if (this.searched) {
            drawSearchResults(context);
        } else {
            drawHelpPanel(context);
        }

        context.drawText(this.textRenderer, HELP_LINK_TEXT, this.backgroundWidth - 128, 22, 0xFF0F5AB4, false);
        context.drawText(this.textRenderer, INPUT_ASSIST_TEXT, this.backgroundWidth - 78, 22, 0xFF0F5AB4, false);
        context.drawText(this.textRenderer, ENGLISH_TEXT, this.backgroundWidth - 32, 22, 0xFF0F5AB4, false);
        context.drawText(this.textRenderer, FOOTER_TEXT, 136, this.backgroundHeight - 14, 0xFF3F5A7A, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // 背景パネルは render() で先に描画する。
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        this.drawPanel(context);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x66000000);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (clickModeTab(mouseX, mouseY) || clickDatabaseTab(mouseX, mouseY)) {
                return true;
            }
            if (getSearchButtonRect().contains(mouseX, mouseY)) {
                performSearch();
                return true;
            }
            if (getClearButtonRect().contains(mouseX, mouseY)) {
                this.queryField.setText("");
                this.searched = false;
                this.searchResults = List.of();
                this.lastQuery = "";
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            performSearch();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawPanel(DrawContext context) {
        int left = this.x;
        int top = this.y;
        int right = left + this.backgroundWidth;
        int bottom = top + this.backgroundHeight;

        context.fill(left + 4, top + 4, right + 4, bottom + 4, 0x33000000);
        context.fillGradient(left, top, right, top + HEADER_HEIGHT, 0xFFFDFEFE, 0xFFF2F7FF);
        context.fill(left, top + HEADER_HEIGHT, right, top + HEADER_HEIGHT + NAV_HEIGHT, 0xFF0F58B0);
        context.fill(left + 8, top + 52, right - 8, top + 70, 0xFFF5F8FE);
        context.fillGradient(left + 8, top + 74, right - 8, top + 98, 0xFF1B69C3, 0xFF0D56A7);
        context.fill(left + 8, top + 102, right - 8, top + 150, 0xFFEAF2FD);
        context.fill(left + 8, top + 164, right - 8, top + 164 + BODY_HEIGHT, 0xFFFDFEFF);
        context.fill(left + 12, top + 130, left + 250, top + 130 + QUERY_FIELD_HEIGHT, 0xFFFFFFFF);
        context.fillGradient(left + 256, top + 130, left + 256 + SEARCH_BUTTON_WIDTH, top + 130 + QUERY_FIELD_HEIGHT, 0xFF1E74D7, 0xFF0D5CB7);
        context.fill(left + 316, top + 130, left + 316 + CLEAR_BUTTON_WIDTH, top + 130 + QUERY_FIELD_HEIGHT, 0xFFF9FCFF);
        context.fill(left + 8, top + this.backgroundHeight - 18, right - 8, bottom - 8, 0xFFF0F6FF);

        context.drawBorder(left, top, this.backgroundWidth, this.backgroundHeight, 0xFF0D4A9D);
        context.drawBorder(left + 8, top + 52, this.backgroundWidth - 16, 18, 0xFF0D4A9D);
        context.drawBorder(left + 8, top + 74, this.backgroundWidth - 16, 24, 0xFF0D4A9D);
        context.drawBorder(left + 8, top + 102, this.backgroundWidth - 16, 48, 0xFF0D4A9D);
        context.drawBorder(left + 8, top + 164, this.backgroundWidth - 16, BODY_HEIGHT, 0xFF0D4A9D);
        context.drawBorder(left + 12, top + 130, 238, QUERY_FIELD_HEIGHT, 0xFF0D4A9D);
        context.drawBorder(left + 256, top + 130, SEARCH_BUTTON_WIDTH, QUERY_FIELD_HEIGHT, 0xFF0D4A9D);
        context.drawBorder(left + 316, top + 130, CLEAR_BUTTON_WIDTH, QUERY_FIELD_HEIGHT, 0xFF0D4A9D);

        context.fill(left + 14, top + 10, left + 38, top + 30, 0xFFD7A84D);
        context.drawBorder(left + 14, top + 10, 24, 20, 0xFF533A14);
        context.fill(left + 20, top + 14, left + 24, top + 26, 0xFF533A14);
        context.fill(left + 27, top + 14, left + 31, top + 26, 0xFF533A14);
        context.fill(left + 18, top + 18, left + 33, top + 20, 0xFF533A14);
        context.fill(left + 260, top + 8, left + 348, top + 24, 0xFF1C6FD0);
        context.drawBorder(left + 260, top + 8, 88, 16, 0xFF0D4A9D);
    }

    private void drawDatabaseTabs(DrawContext context) {
        for (int i = 0; i < DATABASE_TABS.size(); i++) {
            ClickRect rect = getDatabaseTabRect(i);
            boolean selected = i == this.selectedDatabaseIndex;
            int fillColor = selected ? 0xFF1D6FD0 : 0xFFF5F8FE;
            int textColor = selected ? 0xFFFFFFFF : 0xFF0D4A9D;

            context.fill(rect.left() - this.x, rect.top() - this.y, rect.right() - this.x, rect.bottom() - this.y, fillColor);
            context.drawBorder(rect.left() - this.x, rect.top() - this.y, rect.width(), rect.height(), 0xFF0D4A9D);

            Text label = DATABASE_TABS.get(i).label();
            int labelX = rect.left() - this.x + (rect.width() - this.textRenderer.getWidth(label)) / 2;
            int labelY = rect.top() - this.y + 5;
            context.drawText(this.textRenderer, label, labelX, labelY, textColor, false);
        }
    }

    private void drawModeTabs(DrawContext context) {
        ClickRect simpleRect = getSimpleModeRect();
        ClickRect advancedRect = getAdvancedModeRect();

        drawModeTab(context, simpleRect, !this.advancedMode, SIMPLE_MODE_TEXT);
        drawModeTab(context, advancedRect, this.advancedMode, ADVANCED_MODE_TEXT);
    }

    private void drawModeTab(DrawContext context, ClickRect rect, boolean selected, Text label) {
        int fillColor = selected ? 0xFF1D6FD0 : 0xFFFFFFFF;
        int textColor = selected ? 0xFFFFFFFF : 0xFF0D4A9D;

        context.fill(rect.left() - this.x, rect.top() - this.y, rect.right() - this.x, rect.bottom() - this.y, fillColor);
        context.drawBorder(rect.left() - this.x, rect.top() - this.y, rect.width(), rect.height(), 0xFF0D4A9D);

        int labelX = rect.left() - this.x + (rect.width() - this.textRenderer.getWidth(label)) / 2;
        int labelY = rect.top() - this.y + 5;
        context.drawText(this.textRenderer, label, labelX, labelY, textColor, false);
    }

    private void drawHelpPanel(DrawContext context) {
        context.fill(8, 164, this.backgroundWidth - 8, 182, 0xFF135DB4);
        context.drawText(this.textRenderer, HELP_TITLE_TEXT, 16, 170, 0xFFFFFFFF, false);

        int currentY = 188;
        for (Text helpLine : HELP_LINES) {
            context.drawWrappedText(
                this.textRenderer,
                helpLine,
                16,
                currentY,
                this.backgroundWidth - 32,
                0xFF1F2B3A,
                false
            );
            currentY += 12;
        }
    }

    private void drawSearchResults(DrawContext context) {
        context.fill(8, 164, this.backgroundWidth - 8, 182, 0xFF135DB4);
        context.drawText(this.textRenderer, RESULTS_TITLE_TEXT, 16, 170, 0xFFFFFFFF, false);

        if (this.lastQuery.isEmpty()) {
            context.drawWrappedText(
                this.textRenderer,
                EMPTY_QUERY_TEXT,
                16,
                188,
                this.backgroundWidth - 32,
                0xFF1F2B3A,
                false
            );
            return;
        }

        Text summaryText = Text.translatable(
            "screen.sdm_mod.search_pc.summary",
            this.lastQuery,
            this.searchResults.size()
        );
        context.drawText(this.textRenderer, summaryText, 16, 186, 0xFF1F2B3A, false);

        if (this.searchResults.isEmpty()) {
            context.drawWrappedText(
                this.textRenderer,
                NO_RESULTS_TEXT,
                16,
                202,
                this.backgroundWidth - 32,
                0xFF1F2B3A,
                false
            );
            return;
        }

        int currentY = 198;
        int visibleCount = Math.min(2, this.searchResults.size());
        for (int i = 0; i < visibleCount; i++) {
            CatalogEntry entry = this.searchResults.get(i);
            context.drawText(this.textRenderer, Text.literal(entry.title()), 16, currentY, 0xFF0D4A9D, false);

            String metadata = AUTHOR_LABEL_TEXT.getString() + ": " + entry.author()
                + " / " + entry.status();
            context.drawText(this.textRenderer, metadata, 22, currentY + 10, 0xFF33465E, false);
            currentY += 22;
        }

        if (this.searchResults.size() > visibleCount) {
            Text moreText = Text.translatable(
                "screen.sdm_mod.search_pc.more_results",
                this.searchResults.size() - visibleCount
            );
            context.drawText(this.textRenderer, moreText, 16, 234, 0xFF0F5AB4, false);
        }
    }

    private void performSearch() {
        String query = this.queryField.getText().trim();
        this.lastQuery = query;
        this.searched = true;

        if (query.isEmpty()) {
            this.searchResults = List.of();
            return;
        }

        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        DatabaseTab selectedDatabase = DATABASE_TABS.get(this.selectedDatabaseIndex);
        List<CatalogEntry> matches = new ArrayList<>();
        for (CatalogEntry entry : CATALOG) {
            if (!entry.databaseId().equals(selectedDatabase.id())) {
                continue;
            }
            if (matchesEntry(entry, normalizedQuery)) {
                matches.add(entry);
            }
        }

        this.searchResults = matches;
    }

    private boolean matchesEntry(CatalogEntry entry, String normalizedQuery) {
        String searchableText = (
            entry.title() + " "
                + entry.author() + " "
                + entry.location() + " "
                + entry.summary()
        ).toLowerCase(Locale.ROOT);

        if (!this.advancedMode) {
            return searchableText.contains(normalizedQuery);
        }

        for (String token : normalizedQuery.split("\\s+")) {
            if (!token.isBlank() && !searchableText.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private boolean clickModeTab(double mouseX, double mouseY) {
        if (getSimpleModeRect().contains(mouseX, mouseY)) {
            this.advancedMode = false;
            return true;
        }
        if (getAdvancedModeRect().contains(mouseX, mouseY)) {
            this.advancedMode = true;
            return true;
        }
        return false;
    }

    private boolean clickDatabaseTab(double mouseX, double mouseY) {
        for (int i = 0; i < DATABASE_TABS.size(); i++) {
            if (getDatabaseTabRect(i).contains(mouseX, mouseY)) {
                this.selectedDatabaseIndex = i;
                return true;
            }
        }
        return false;
    }

    private ClickRect getSimpleModeRect() {
        return new ClickRect(this.x + 14, this.y + 106, 84, 20);
    }

    private ClickRect getAdvancedModeRect() {
        return new ClickRect(this.x + 100, this.y + 106, 84, 20);
    }

    private ClickRect getDatabaseTabRect(int index) {
        int currentX = this.x + 10;
        for (int i = 0; i < index; i++) {
            currentX += getDatabaseTabWidth(i) + 4;
        }
        return new ClickRect(currentX, this.y + 52, getDatabaseTabWidth(index), 18);
    }

    private ClickRect getSearchButtonRect() {
        return new ClickRect(this.x + 256, this.y + 130, SEARCH_BUTTON_WIDTH, QUERY_FIELD_HEIGHT);
    }

    private ClickRect getClearButtonRect() {
        return new ClickRect(this.x + 316, this.y + 130, CLEAR_BUTTON_WIDTH, QUERY_FIELD_HEIGHT);
    }

    private int getDatabaseTabWidth(int index) {
        Text label = DATABASE_TABS.get(index).label();
        return Math.max(36, this.textRenderer.getWidth(label) + 12);
    }

    private record DatabaseTab(Text label, String id) {
    }

    private record CatalogEntry(
        String databaseId,
        String title,
        String author,
        String location,
        String status,
        String summary
    ) {
    }

    private record ClickRect(int left, int top, int width, int height) {
        private int right() {
            return this.left + this.width;
        }

        private int bottom() {
            return this.top + this.height;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= this.left && mouseX < right() && mouseY >= this.top && mouseY < bottom();
        }
    }
}
