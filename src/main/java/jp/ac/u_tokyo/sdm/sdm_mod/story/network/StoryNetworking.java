package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2DoorArrowService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4DialogueClosedPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4FireworkService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4ZombieService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.FireworkShakePayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase5.Phase5OnaraClosedPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase5.Phase5OnaraPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryStartService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class StoryNetworking {
    private static final String PHASE3_ID = "phase3";
    private static final String PHASE4_ID = "phase4";
    private static final String PHASE5_ID = "phase5";

    private StoryNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(Phase5GameOverPayload.ID, Phase5GameOverPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowOpVideoPayload.ID, ShowOpVideoPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SetupGuideHudPayload.ID, SetupGuideHudPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowEdVideoPayload.ID, ShowEdVideoPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DoorArrowPayload.ID, DoorArrowPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowBookUiPayload.ID, ShowBookUiPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StoryVideoStartPayload.ID, StoryVideoStartPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SearchPcLocationOpenedPayload.ID, SearchPcLocationOpenedPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(Phase4DialogueClosedPayload.ID, Phase4DialogueClosedPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(Phase5OnaraPayload.ID, Phase5OnaraPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(FireworkShakePayload.ID, FireworkShakePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(Phase5OnaraClosedPayload.ID, Phase5OnaraClosedPayload.CODEC);
        // 動画再生終了後にクライアントからこのパケットが届いたらストーリーを開始する
        ServerPlayNetworking.registerGlobalReceiver(StoryVideoStartPayload.ID, (payload, context) ->
            context.server().execute(() -> StoryStartService.start(context.server()))
        );
        ServerPlayNetworking.registerGlobalReceiver(SearchPcLocationOpenedPayload.ID, (payload, context) ->
            context.server().execute(() -> Phase2DoorArrowService.recordLocationScreenOpened(context.player()))
        );
        // 教授ダイアログを閉じたプレイヤーからパケットが届いたら、花火を打ち上げてからphase4へ進む
        ServerPlayNetworking.registerGlobalReceiver(Phase4DialogueClosedPayload.ID, (payload, context) ->
            context.server().execute(() -> {
                StoryManager storyManager = StoryModule.getStoryManager();
                if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE3_ID)) {
                    return;
                }
                storyManager.advanceToChapter(PHASE4_ID);
                Phase4FireworkService.launchOnce(context.server());
                context.server().getPlayerManager().getPlayerList()
                    .forEach(p -> ServerPlayNetworking.send(p, FireworkShakePayload.INSTANCE));
            })
        );
        // おならダイアログを閉じたプレイヤーからパケットが届いたらゾンビをスポーンさせる
        ServerPlayNetworking.registerGlobalReceiver(Phase5OnaraClosedPayload.ID, (payload, context) ->
            context.server().execute(() -> {
                StoryManager storyManager = StoryModule.getStoryManager();
                if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE5_ID)) {
                    return;
                }
                Phase4ZombieService.spawnPhase4Zombies(context.server().getOverworld());
            })
        );
    }
}
