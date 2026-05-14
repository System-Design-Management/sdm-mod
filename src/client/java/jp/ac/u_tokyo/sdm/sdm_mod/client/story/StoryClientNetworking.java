package jp.ac.u_tokyo.sdm.sdm_mod.client.story;

import jp.ac.u_tokyo.sdm.sdm_mod.client.hud.DoorArrowHud;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.BookInteractionScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.Phase5GameOverScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.TeacherDialogueScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.video.EdVideoScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.video.OpVideoScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.DoorArrowPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowBookUiPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.Phase5GameOverPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowEdVideoPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowOpVideoPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4DialogueClosedPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4ProfessorDialoguePayload;
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
        ClientPlayNetworking.registerGlobalReceiver(ShowBookUiPayload.ID, (payload, context) ->
            context.client().execute(() ->
                context.client().setScreen(new BookInteractionScreen(payload.title(), payload.isKeyBook())))
        );
        // phase4 開始時に教授のセリフ画面を表示し、閉じたらサーバーに通知する
        ClientPlayNetworking.registerGlobalReceiver(Phase4ProfessorDialoguePayload.ID, (payload, context) ->
            context.client().execute(() ->
                context.client().setScreen(new TeacherDialogueScreen(
                    "私が花火を打ち上げてやつらを部屋の隅におびきよせる。その間に部屋から出て、図書館の外に逃げろ！",
                    () -> ClientPlayNetworking.send(new Phase4DialogueClosedPayload())
                ))
            )
        );
    }
}
