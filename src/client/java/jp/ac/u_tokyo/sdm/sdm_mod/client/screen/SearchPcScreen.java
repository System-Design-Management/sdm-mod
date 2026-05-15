package jp.ac.u_tokyo.sdm.sdm_mod.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jp.ac.u_tokyo.sdm.sdm_mod.screen.SearchPcScreenHandler;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.SearchPcLocationClosedPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.SearchPcLocationOpenedPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class SearchPcScreen extends HandledScreen<SearchPcScreenHandler> {
    private static final Text CLOSE_BUTTON_TEXT = Text.literal("X");
    private static final Text TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.title");
    private static final Text SUBTITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.subtitle");
    private static final Text BANNER_TEXT = Text.translatable("screen.sdm_mod.search_pc.banner");
    private static final Text SIMPLE_MODE_TEXT = Text.translatable("screen.sdm_mod.search_pc.mode.simple");
    private static final Text ADVANCED_MODE_TEXT = Text.translatable("screen.sdm_mod.search_pc.mode.advanced");
    private static final Text SEARCH_TEXT = Text.translatable("screen.sdm_mod.search_pc.search");
    private static final Text CLEAR_TEXT = Text.translatable("screen.sdm_mod.search_pc.clear");
    private static final Text RESULTS_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.results_title");
    private static final Text NO_RESULTS_TEXT = Text.translatable("screen.sdm_mod.search_pc.no_results");
    private static final Text AUTHOR_LABEL_TEXT = Text.translatable("screen.sdm_mod.search_pc.label.author");
    private static final Text LOCATION_LABEL_TEXT = Text.translatable("screen.sdm_mod.search_pc.label.location");
    private static final Text STATUS_LABEL_TEXT = Text.translatable("screen.sdm_mod.search_pc.label.status");
    private static final Text KEYBOARD_TITLE_TEXT = Text.translatable("screen.sdm_mod.search_pc.keyboard_title");
    private static final Text RESULT_ACTION_TEXT = Text.translatable("screen.sdm_mod.search_pc.result_action");
    private static final Text ZOMBIE_QUERY_TEXT = Text.literal("ゾンビ");

    private static final int PANEL_WIDTH = 404;
    private static final int PANEL_HEIGHT = 238;
    private static final int HEADER_HEIGHT = 28;
    private static final int NAV_HEIGHT = 15;
    private static final int QUERY_MAX_LENGTH = 64;
    private static final int QUERY_FIELD_HEIGHT = 20;
    private static final int RESULT_HEIGHT = 52;
    private static final int KEYBOARD_HEIGHT = 40;
    private static final int SEARCH_BUTTON_WIDTH = 58;
    private static final int CLEAR_BUTTON_WIDTH = 52;
    private static final int KEY_BUTTON_WIDTH = 110;
    private static final int KEY_BUTTON_HEIGHT = 20;
    private static final int TITLE_Y = 6;
    private static final int SUBTITLE_Y = 17;
    private static final int LIBRARY_LABEL_Y = 20;
    private static final int NAV_LABEL_Y = 33;
    private static final int DATABASE_TABS_Y = 44;
    private static final int BANNER_Y = 66;
    private static final int SEARCH_AREA_Y = 92;
    private static final int MODE_TAB_Y = 96;
    private static final int QUERY_FIELD_Y = 114;
    private static final int QUERY_TEXT_X = 16;
    private static final int QUERY_TEXT_Y = 120;
    private static final int QUERY_TEXT_WIDTH = 230;
    private static final int QUERY_BUTTON_Y = 113;
    private static final int RESULT_TOP = 138;
    private static final int KEYBOARD_TOP = 194;
    private static final int KEYBOARD_BUTTON_Y = 213;
    private static final CatalogEntry ZOMBIE_BOOK = new CatalogEntry(
        "utokyo",
        "ゾンビ病理学",
        "舞倉 存美",
        "総合図書館・3F開架",
        "利用可",
        "ゾンビ化症例の病理と治療仮説をまとめた資料。"
    );

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
    private String draftQuery = "";
    private boolean resultSelected;

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
            this.x + QUERY_TEXT_X,
            this.y + QUERY_TEXT_Y,
            QUERY_TEXT_WIDTH,
            QUERY_FIELD_HEIGHT,
            Text.empty()
        );
        this.queryField.setMaxLength(QUERY_MAX_LENGTH);
        this.queryField.setDrawsBackground(false);
        this.queryField.setEditableColor(0xFF111111);
        this.queryField.setTextShadow(false);
        this.queryField.setText(this.draftQuery);
        this.queryField.setFocused(!this.resultSelected);
        this.addDrawableChild(this.queryField);

        this.addDrawableChild(ButtonWidget.builder(SEARCH_TEXT, button -> performSearch())
            .dimensions(this.x + 254, this.y + QUERY_BUTTON_Y, SEARCH_BUTTON_WIDTH, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(CLEAR_TEXT, button -> clearQuery())
            .dimensions(this.x + 318, this.y + QUERY_BUTTON_Y, CLEAR_BUTTON_WIDTH, 20)
            .build());
        this.addDrawableChild(ButtonWidget.builder(CLOSE_BUTTON_TEXT, button -> this.close())
            .dimensions(this.x + this.backgroundWidth - 24, this.y + 6, 16, 16)
            .build());

        addKeyboardButton("ぞ", 0);
        addKeyboardButton("ん", 1);
        addKeyboardButton("び", 2);
    }

    @Override
    public void close() {
        ClientPlayNetworking.send(SearchPcLocationClosedPayload.INSTANCE);
        super.close();
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        Text libraryLabel = Text.translatable("screen.sdm_mod.search_pc.library_label");
        int headerRight = this.backgroundWidth - 32;
        int utokyoX = headerRight - this.textRenderer.getWidth("UTokyo");
        int libraryLabelX = headerRight - this.textRenderer.getWidth(libraryLabel);

        context.drawText(this.textRenderer, TITLE_TEXT, 48, TITLE_Y, 0xFF121212, false);
        context.drawText(this.textRenderer, SUBTITLE_TEXT, 48, SUBTITLE_Y, 0xFF2B2B2B, false);
        context.drawText(this.textRenderer, "UTokyo", utokyoX, 11, 0xFF0F5AB4, false);
        context.drawText(
            this.textRenderer,
            libraryLabel,
            libraryLabelX,
            LIBRARY_LABEL_Y,
            0xFF5F7490,
            false
        );

        int navX = 10;
        for (Text navLabel : NAV_LABELS) {
            context.drawText(this.textRenderer, navLabel, navX, NAV_LABEL_Y, 0xFFFFFFFF, false);
            navX += this.textRenderer.getWidth(navLabel) + 12;
        }

        drawDatabaseTabs(context);

        context.drawText(this.textRenderer, TITLE_TEXT, 46, 72, 0xFFFFFFFF, false);
        context.drawText(this.textRenderer, BANNER_TEXT, 144, 73, 0xFFF0F6FF, false);

        drawModeTabs(context);

        if (this.queryField.getText().isEmpty()) {
            context.drawText(
                this.textRenderer,
                Text.translatable("screen.sdm_mod.search_pc.query_placeholder"),
                QUERY_TEXT_X,
                QUERY_TEXT_Y,
                0xFF6E7E97,
                false
            );
        }
        drawSearchResults(context);

        context.drawText(this.textRenderer, KEYBOARD_TITLE_TEXT, 16, 200, 0xFFFFFFFF, false);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // 背景パネルは render() で先に描画する。
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.queryField != null) {
            this.draftQuery = this.queryField.getText();
        }
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
            if (getSelectableResultRect().contains(mouseX, mouseY) && hasSelectableResult()) {
                openLocationScreen();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (hasSelectableResult()) {
            if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_TAB) {
                this.resultSelected = true;
                this.queryField.setFocused(false);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_UP && this.resultSelected) {
                this.resultSelected = false;
                this.queryField.setFocused(true);
                return true;
            }
            if (this.resultSelected && (keyCode == GLFW.GLFW_KEY_ENTER
                || keyCode == GLFW.GLFW_KEY_KP_ENTER
                || keyCode == GLFW.GLFW_KEY_SPACE)) {
                openLocationScreen();
                return true;
            }
        }

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
        int resultTop = top + RESULT_TOP;
        int keyboardTop = top + KEYBOARD_TOP;
        int bottom = top + this.backgroundHeight;

        context.fill(left + 4, top + 4, right + 4, bottom + 4, 0x33000000);
        context.fillGradient(left, top, right, top + HEADER_HEIGHT, 0xFFFDFEFE, 0xFFF2F7FF);
        context.fill(left, top + HEADER_HEIGHT, right, top + HEADER_HEIGHT + NAV_HEIGHT, 0xFF0F58B0);
        context.fill(left + 8, top + DATABASE_TABS_Y, right - 8, top + DATABASE_TABS_Y + 18, 0xFFF5F8FE);
        context.fillGradient(left + 8, top + BANNER_Y, right - 8, top + BANNER_Y + 22, 0xFF1B69C3, 0xFF0D56A7);
        context.fill(left + 8, top + SEARCH_AREA_Y, right - 8, top + SEARCH_AREA_Y + 42, 0xFFEAF2FD);
        context.fill(left + 12, top + QUERY_FIELD_Y, left + 250, top + QUERY_FIELD_Y + QUERY_FIELD_HEIGHT, 0xFFFFFFFF);
        context.fill(left + 8, resultTop, right - 8, resultTop + RESULT_HEIGHT, 0xFFFDFEFF);
        context.fill(left + 8, keyboardTop, right - 8, keyboardTop + KEYBOARD_HEIGHT, 0xFFF4F8FE);

        context.drawBorder(left, top, this.backgroundWidth, this.backgroundHeight, 0xFF0D4A9D);
        context.drawBorder(left + 8, top + DATABASE_TABS_Y, this.backgroundWidth - 16, 18, 0xFF0D4A9D);
        context.drawBorder(left + 8, top + BANNER_Y, this.backgroundWidth - 16, 22, 0xFF0D4A9D);
        context.drawBorder(left + 8, top + SEARCH_AREA_Y, this.backgroundWidth - 16, 42, 0xFF0D4A9D);
        context.drawBorder(left + 8, resultTop, this.backgroundWidth - 16, RESULT_HEIGHT, 0xFF0D4A9D);
        context.drawBorder(left + 8, keyboardTop, this.backgroundWidth - 16, KEYBOARD_HEIGHT, 0xFF0D4A9D);
        context.drawBorder(left + 12, top + QUERY_FIELD_Y, 238, QUERY_FIELD_HEIGHT, 0xFF0D4A9D);

        context.fill(left + 14, top + 10, left + 38, top + 30, 0xFFD7A84D);
        context.drawBorder(left + 14, top + 10, 24, 20, 0xFF533A14);
        context.fill(left + 20, top + 14, left + 24, top + 26, 0xFF533A14);
        context.fill(left + 27, top + 14, left + 31, top + 26, 0xFF533A14);
        context.fill(left + 18, top + 18, left + 33, top + 20, 0xFF533A14);
        context.fill(left + 250, top + 7, left + 351, top + 29, 0x11A7C8F4);
        context.fill(left + 250, top + 29, left + 351, top + 30, 0x334C85C6);

        context.fill(left + 8, resultTop, right - 8, resultTop + 18, 0xFF135DB4);
        context.fill(left + 8, keyboardTop, right - 8, keyboardTop + 18, 0xFF135DB4);
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

    private void drawSearchResults(DrawContext context) {
        context.drawText(this.textRenderer, RESULTS_TITLE_TEXT, 16, 144, 0xFFFFFFFF, false);

        if (!this.searched) {
            return;
        }

        if (this.searchResults.isEmpty()) {
            context.drawWrappedText(
                this.textRenderer,
                NO_RESULTS_TEXT,
                16,
                158,
                this.backgroundWidth - 32,
                0xFF1F2B3A,
                false
            );
            return;
        }

        int currentY = 158;
        int visibleCount = 1;
        for (int i = 0; i < visibleCount; i++) {
            CatalogEntry entry = this.searchResults.get(i);
            if (hasSelectableResult() && this.resultSelected) {
                context.fill(12, currentY - 2, this.backgroundWidth - 12, currentY + 29, 0x1A1B67C2);
                context.drawBorder(12, currentY - 2, this.backgroundWidth - 24, 31, 0xFF1B67C2);
            }
            context.drawText(this.textRenderer, Text.literal(entry.title()), 16, currentY, 0xFF0D4A9D, false);

            String authorAndStatus = AUTHOR_LABEL_TEXT.getString() + ": " + entry.author()
                + " / " + STATUS_LABEL_TEXT.getString() + ": " + entry.status();
            context.drawText(this.textRenderer, authorAndStatus, 22, currentY + 10, 0xFF33465E, false);
            context.drawText(
                this.textRenderer,
                LOCATION_LABEL_TEXT.getString() + ": " + entry.location(),
                22,
                currentY + 19,
                0xFF4C607A,
                false
            );
            if (hasSelectableResult()) {
                context.drawText(this.textRenderer, RESULT_ACTION_TEXT, 300, currentY + 10, 0xFF0D4A9D, false);
            }
        }

        if (this.searchResults.size() > 1) {
            Text moreText = Text.translatable(
                "screen.sdm_mod.search_pc.more_results",
                this.searchResults.size() - 1
            );
            context.drawText(this.textRenderer, moreText, 16, 184, 0xFF0F5AB4, false);
        }
    }

    private void performSearch() {
        String query = this.queryField.getText().trim();
        this.lastQuery = query;
        this.searched = true;
        this.resultSelected = false;
        this.queryField.setFocused(true);

        if (query.isEmpty()) {
            this.searchResults = List.of();
            return;
        }

        String normalizedQuery = normalizeKana(query).toLowerCase(Locale.ROOT);
        if (normalizedQuery.equals(normalizeKana(ZOMBIE_QUERY_TEXT.getString()).toLowerCase(Locale.ROOT))) {
            this.searchResults = List.of(ZOMBIE_BOOK);
            return;
        }

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
        String searchableText = normalizeKana(
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
        return new ClickRect(this.x + 14, this.y + MODE_TAB_Y, 84, 18);
    }

    private ClickRect getAdvancedModeRect() {
        return new ClickRect(this.x + 100, this.y + MODE_TAB_Y, 84, 18);
    }

    private ClickRect getDatabaseTabRect(int index) {
        int currentX = this.x + 10;
        for (int i = 0; i < index; i++) {
            currentX += getDatabaseTabWidth(i) + 4;
        }
        return new ClickRect(currentX, this.y + DATABASE_TABS_Y, getDatabaseTabWidth(index), 18);
    }

    private void addKeyboardButton(String key, int column) {
        addKeyboardActionButton(Text.literal(key), column, () -> appendToQuery(key));
    }

    private void addKeyboardActionButton(Text label, int column, Runnable action) {
        this.addDrawableChild(ButtonWidget.builder(label, button -> action.run())
            .dimensions(
                this.x + 22 + column * 120,
                this.y + KEYBOARD_BUTTON_Y,
                KEY_BUTTON_WIDTH,
                KEY_BUTTON_HEIGHT
            )
            .build());
    }

    private void appendToQuery(String value) {
        String current = this.queryField.getText();
        if (current.length() + value.length() > QUERY_MAX_LENGTH) {
            return;
        }
        this.queryField.setText(current + value);
        this.queryField.setFocused(true);
    }

    private void clearQuery() {
        this.queryField.setText("");
        this.searched = false;
        this.searchResults = List.of();
        this.lastQuery = "";
        this.draftQuery = "";
        this.resultSelected = false;
        this.queryField.setFocused(true);
    }

    private boolean hasSelectableResult() {
        return !this.searchResults.isEmpty() && this.searchResults.getFirst().title().equals(ZOMBIE_BOOK.title());
    }

    private ClickRect getSelectableResultRect() {
        return new ClickRect(this.x + 12, this.y + 154, 372, 28);
    }

    private void openLocationScreen() {
        if (this.client == null) {
            return;
        }
        this.draftQuery = this.queryField.getText();
        ClientPlayNetworking.send(SearchPcLocationOpenedPayload.INSTANCE);
        this.client.setScreen(new SearchPcLocationScreen(this));
    }

    private String normalizeKana(String value) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character >= 'ぁ' && character <= 'ゖ') {
                builder.append((char) (character + 0x60));
            } else {
                builder.append(character);
            }
        }
        return builder.toString();
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
