package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryStartService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class StoryNetworking {
    private StoryNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(Phase5GameOverPayload.ID, Phase5GameOverPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowOpVideoPayload.ID, ShowOpVideoPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowEdVideoPayload.ID, ShowEdVideoPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DoorArrowPayload.ID, DoorArrowPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowBookUiPayload.ID, ShowBookUiPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(StoryVideoStartPayload.ID, StoryVideoStartPayload.CODEC);
        // 動画再生終了後にクライアントからこのパケットが届いたらストーリーを開始する
        ServerPlayNetworking.registerGlobalReceiver(StoryVideoStartPayload.ID, (payload, context) ->
            context.server().execute(() -> StoryStartService.start(context.server()))
        );
    }
}
