package jp.ac.u_tokyo.sdm.sdm_mod.story.phase3;

import jp.ac.u_tokyo.sdm.sdm_mod.game.CommandLockState;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowBookUiPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4FireworkService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4ZombieService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Random;

public final class Phase3BookshelfService {
    private static final Logger LOGGER = LoggerFactory.getLogger(Phase3BookshelfService.class);
    private static final String PHASE3_ID = "phase3";
    private static final String PHASE4_ID = "phase4";

    private static final BlockPos CORRECT_BOOKSHELF_POS = new BlockPos(-120, 42, -636);
    private static final String KEY_BOOK_TITLE = "ゾンビ病理学";

    // X座標 → CATEGORIESのインデックス。座標が小さい順にインデックス0から割り当て、
    // X=-120（正解本棚）のみインデックス19（医学・薬学）に対応させる。
    private static final Map<Integer, Integer> BOOKSHELF_CATEGORY_MAP = Map.ofEntries(
        Map.entry(-200, 0),
        Map.entry(-199, 1),
        Map.entry(-193, 2),
        Map.entry(-192, 3),
        Map.entry(-185, 4),
        Map.entry(-184, 5),
        Map.entry(-177, 6),
        Map.entry(-176, 7),
        Map.entry(-169, 8),
        Map.entry(-168, 9),
        Map.entry(-153, 10),
        Map.entry(-152, 11),
        Map.entry(-145, 12),
        Map.entry(-144, 13),
        Map.entry(-137, 14),
        Map.entry(-136, 15),
        Map.entry(-129, 16),
        Map.entry(-128, 17),
        Map.entry(-121, 18),
        Map.entry(-120, 19)
    );

    // 20分類 × 10タイトル。インデックス19が医学・薬学（正解本棚対応）。
    private static final List<List<String>> CATEGORIES = List.of(
        // 0: 総記・情報学 (X=-200)
        List.of(
            "稀覯本修復の化学的基礎", "検索アルゴリズムの系譜", "デジタル・アーカイブ構築論",
            "偽情報の数理モデル", "百科全書の比較文化史", "暗号化通信の倫理学",
            "知識表現の形式言語", "統計データの視覚化技法", "メディア・エコロジー序説",
            "集合知のネットワーク解析"
        ),
        // 1: 哲学・倫理学 (X=-199)
        List.of(
            "現象学と身体知の交差", "科学哲学のフロンティア", "非古典論理の基礎理論",
            "徳倫理学の現代的再構成", "分析哲学における言語論", "生命倫理と法学的閾",
            "スピノザにおける情動論", "存在論の現代的展開", "美学の政治的機能",
            "意識のハードプロブレム"
        ),
        // 2: 心理学・認知科学 (X=-193)
        List.of(
            "視覚情報の短期記憶機構", "群衆心理の動態力学", "発達障害の認知神経学",
            "意思決定の認知バイアス", "色彩心理の文化比較研究", "言語獲得の計算モデル",
            "情動の生理的指標分析", "睡眠の行動科学的考察", "パーソナリティの統計相関",
            "身体化された認知の理論"
        ),
        // 3: 宗教学・神話 (X=-192)
        List.of(
            "密教儀礼の構造分析", "宗教改革の経済史的側面", "比較神話学の諸相",
            "世俗化の社会理論", "聖像破壊の歴史学的研究", "シャマニズムの変容過程",
            "イスラーム法と現代社会", "巡礼の空間人類学", "葬送儀礼の民俗誌",
            "グノーシス主義の思想構造"
        ),
        // 4: 歴史学・考古学 (X=-185)
        List.of(
            "古代都市の灌漑システム", "中世ギルドの法構造", "貨幣流通の広域経済分析",
            "石器製作の実験考古学", "口述史の記録と保存手法", "地中海交易の興亡史",
            "植民地主義と境界線", "古文書の科学的鑑定法", "産業革命の技術伝播論",
            "王権の象徴と宮廷儀礼"
        ),
        // 5: 地理・地誌 (X=-184)
        List.of(
            "都市形成の空間経済学", "限界集落の変遷過程", "地図表現の政治学",
            "気候変動と居住動態", "山岳信仰の地理的分布", "海域アジアの比較地誌",
            "観光開発と景観保存論", "土壌の微細構造分析", "リモートセンシング応用論",
            "境界地域のアイデンティティ"
        ),
        // 6: 政治・軍事 (X=-177)
        List.of(
            "国際協力のゲーム理論", "選挙制度の比較政治学", "官僚制の組織力学",
            "地政学のリスク管理", "平和構築のフィールド研究", "情報戦の理論と実践",
            "憲法改正の比較法学的研究", "核抑止理論の再検討", "地方自治の財政構造分析",
            "ポピュリズムの比較史"
        ),
        // 7: 経済・経営 (X=-176)
        List.of(
            "行動経済学の実証実験", "サプライチェーンの最適化", "コーポレートガバナンス論",
            "暗号資産の金融経済学", "労働市場の構造変化", "イノベーションの普及学",
            "発展途上国の経済援助論", "知的財産の戦略的管理", "消費行動の統計解析",
            "多国籍企業の課税問題"
        ),
        // 8: 社会学・民俗学 (X=-169)
        List.of(
            "階層化社会の変動理論", "ジェンダーと労働社会学", "境界例としての民話研究",
            "多文化共生の社会理論", "贈与の比較社会史", "怪異現象の民俗調査",
            "アーバン・トライバル論", "親族構造の数理モデル", "祝祭の政治経済学",
            "生活空間の環境社会学"
        ),
        // 9: 教育学 (X=-168)
        List.of(
            "学習科学のデザイン理論", "遠隔教育の効果測定法", "教科書検定の歴史的変遷",
            "障害児教育の制度史", "比較教育学の理論的視座", "成人学習者の発達心理",
            "教育格差の再生産構造", "綴り方教育の思想史", "高等教育のガバナンス",
            "非認知スキルの評価論"
        ),
        // 10: 数学・論理学 (X=-153)
        List.of(
            "素数分布のゼータ関数", "トポロジーの基礎概念", "有限群の表現論序説",
            "確率過程論の工学的応用", "圏論による数理構造分析", "非線形力学系の解析手法",
            "グラフ理論とネットワーク", "代数幾何学の現代的進展", "集合論の公理系研究",
            "数値計算の誤差評価論"
        ),
        // 11: 物理学・天文学 (X=-152)
        List.of(
            "超弦理論の幾何学的構成", "量子もつれと情報理論", "恒星進化のシミュレーション",
            "極低温下の物性物理学", "暗黒物質の検出理論", "素粒子標準模型の検証",
            "一般相対性理論の観測証拠", "重力波天文学の最前線", "宇宙の大規模構造形成",
            "非平衡統計力学の基礎"
        ),
        // 12: 化学・物質科学 (X=-145)
        List.of(
            "超分子の自己組織化理論", "触媒反応の電子論的解析", "有機合成の遷移金属触媒",
            "高分子材料の劣化機構", "電気化学の界面理論", "結晶構造のX線解析学",
            "分子軌道法の計算化学", "希少金属の分離精製技術", "光化学反応の精密制御",
            "生体材料の組織適合性"
        ),
        // 13: 地球科学・生物学 (X=-144)
        List.of(
            "プレートテクトニクスと地震", "海洋深層水の循環モデル", "古気候の復元解析技術",
            "遺伝子発現の転写制御", "微生物の極限環境適応", "個体群生態学の数理",
            "進化人類学の新知見", "植物ホルモンの生理作用", "生物多様性の保全戦略",
            "構造生物学の解析手法"
        ),
        // 14: 文学研究 (X=-137)
        List.of(
            "中世物語の異本研究", "叙事詩の比較文学的考察", "テクストクリティークの技法",
            "文学における都市表象論", "作家論の精神分析的手法", "翻訳理論の歴史的展開",
            "韻律論の通時的変遷", "風刺文学の政治的背景", "私小説の構造分析",
            "児童文学のナラトロジー"
        ),
        // 15: 工学・技術 (X=-136)
        List.of(
            "流体機械の設計理論", "ロボットの運動制御工学", "半導体デバイスの物理",
            "橋梁構造の動的解析", "原子力安全工学の再構築", "音響信号のデジタル処理",
            "建築振動の制振理論", "通信プロトコルの最適化", "ナノマシンの設計指針",
            "材料疲労の非破壊検査"
        ),
        // 16: 農林水産業 (X=-129)
        List.of(
            "土壌肥料の農学的基盤", "森林生態の持続的管理", "養殖魚の病理学的研究",
            "農業経済の制度分析", "品種改良の遺伝統計学", "害虫駆除の生物的制御",
            "果樹園のスマート農法", "木材工学の基礎理論", "沿岸漁業の資源管理論",
            "種子保存の低温科学"
        ),
        // 17: 芸術・美術史 (X=-128)
        List.of(
            "ルネサンスの色彩理論", "舞台演出の空間構成論", "現代音楽の作曲技法",
            "映画学の言説分析", "伝統工芸の技術伝承論", "イスラーム建築の幾何学",
            "写真表現の思想的変遷", "文化財の保存科学入門", "アヴァンギャルドの系譜",
            "身体表現の解剖学的視点"
        ),
        // 18: 言語学 (X=-121)
        List.of(
            "生成文法の論理構造", "音韻論の通時的分析", "語用論の社会学的側面",
            "認知言語学の概念拡張", "消滅危機言語の記録手法", "辞書編纂の計量言語学",
            "言語類型論の比較研究", "意味論の形式的記述", "手話言語の構造解析",
            "借用語の形態論的変容"
        ),
        // 19: 医学・薬学 (X=-120) ← 正解本棚対応
        List.of(
            "免疫応答の分子機序", "神経再生の細胞工学", "創薬ターゲットの探索論",
            "公衆衛生の疫学調査法", "ゲノム編集の医療応用", "感染症の数理疫学モデル",
            "漢方医学の科学的検証", "代謝疾患の病態生理", "医療AIの画像診断技術",
            "緩和ケアの臨床倫理"
        )
    );

    private Phase3BookshelfService() {
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            var blockState = world.getBlockState(pos);
            if (!blockState.isOf(Blocks.BOOKSHELF) && !blockState.isOf(Blocks.CHISELED_BOOKSHELF)) {
                return ActionResult.PASS;
            }

            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE3_ID)) {
                LOGGER.debug("Bookshelf clicked at {} but story is not in phase3. active={}, isPhase3={}",
                    pos, storyManager.isActive(), storyManager.isAtChapter(PHASE3_ID));
                return ActionResult.PASS;
            }

            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            if (pos.equals(CORRECT_BOOKSHELF_POS)) {
                Phase4FireworkService.reset();
                giveKeyBook(serverPlayer);
                ServerPlayNetworking.send(serverPlayer, new ShowBookUiPayload(KEY_BOOK_TITLE, true));
                LOGGER.info("Correct bookshelf clicked at {}. Awaiting player dialogue to advance to {}.", pos, PHASE4_ID);
            } else {
                Integer categoryIndex = BOOKSHELF_CATEGORY_MAP.get(pos.getX());
                if (categoryIndex == null) {
                    return ActionResult.PASS;
                }
                List<String> category = CATEGORIES.get(categoryIndex);
                String title = category.get(new Random().nextInt(category.size()));
                ServerPlayNetworking.send(serverPlayer, new ShowBookUiPayload(title, false));
            }

            return ActionResult.SUCCESS;
        });
    }

    private static void giveKeyBook(ServerPlayerEntity player) {
        CommandLockState.runUnlocked(() -> {
            ServerCommandSource source = player.getServer().getCommandSource()
                .withLevel(2)
                .withEntity(player)
                .withPosition(player.getPos());
            player.getServer().getCommandManager().executeWithPrefix(source, "give " + player.getNameForScoreboard() + " sdm_mod:key_book 1");
        });
    }
}
