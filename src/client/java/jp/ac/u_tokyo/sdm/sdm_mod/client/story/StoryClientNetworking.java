package jp.ac.u_tokyo.sdm.sdm_mod.client.story;

import jp.ac.u_tokyo.sdm.sdm_mod.client.hud.DoorArrowHud;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.Phase5GameOverScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.video.EdVideoScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.video.OpVideoScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.DoorArrowPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.Phase5GameOverPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowEdVideoPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowOpVideoPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class StoryClientNetworking {
    private StoryClientNetworking() {
    }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(Phase5GameOverPayload.ID, (payload, context) -> {
            context.client().setScreen(new Phase5GameOverScreen());
        });
        ClientPlayNetworking.registerGlobalReceiver(ShowOpVideoPayload.ID, (payload, context) ->
            context.client().execute(() -> context.client().setScreen(new OpVideoScreen()))
        );
        ClientPlayNetworking.registerGlobalReceiver(ShowEdVideoPayload.ID, (payload, context) ->
            context.client().execute(() -> context.client().setScreen(new EdVideoScreen()))
        );
        ClientPlayNetworking.registerGlobalReceiver(DoorArrowPayload.ID, (payload, context) ->
            context.client().execute(() -> DoorArrowHud.INSTANCE.setEnabled(payload.visible()))
        );
    }
}
