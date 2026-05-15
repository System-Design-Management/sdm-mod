package jp.ac.u_tokyo.sdm.sdm_mod.client.story;

import jp.ac.u_tokyo.sdm.sdm_mod.ModSounds;
import jp.ac.u_tokyo.sdm.sdm_mod.client.ScreenScheduler;
import jp.ac.u_tokyo.sdm.sdm_mod.client.hud.DoorArrowHud;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.BookInteractionScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.FreezeScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.video.BadEdVideoScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.TeacherDialogueScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.video.EdVideoScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.video.OpVideoScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.DoorArrowPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowBookUiPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.Phase5GameOverPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowEdVideoPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowOpVideoPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4DialogueClosedPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase5.Phase5OnaraClosedPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase5.Phase5OnaraPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.sound.PositionedSoundInstance;

public final class StoryClientNetworking {
    private StoryClientNetworking() {
    }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(Phase5GameOverPayload.ID, (payload, context) ->
            context.client().execute(() -> context.client().setScreen(new BadEdVideoScreen()))
        );
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
        // phase5のおなら演出: 音を再生 → 動き停止 → 音終了後に教授UI → 閉じたらゾンビスポーン
        ClientPlayNetworking.registerGlobalReceiver(Phase5OnaraPayload.ID, (payload, context) ->
            context.client().execute(() -> {
                // おならの音を再生する（約30tick = 1.5秒）
                context.client().getSoundManager().play(
                    PositionedSoundInstance.master(ModSounds.ONARA, 1.0f, 1.0f)
                );
                // FreezeScreen で動きを止め、音が終わったら教授UIを開く
                context.client().setScreen(new FreezeScreen(35,
                    () -> ScreenScheduler.schedule(new TeacherDialogueScreen(
                        "こんなときになんてデカいおならしてるんだ！！匂いに奴らが反応して集まってくるぞ！急いで図書館の外まで逃げるんだ！！",
                        () -> ScreenScheduler.scheduleAction(
                            () -> ClientPlayNetworking.send(new Phase5OnaraClosedPayload())
                        )
                    ))
                ));
            })
        );
    }
}
