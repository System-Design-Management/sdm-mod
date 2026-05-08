package jp.ac.u_tokyo.sdm.sdm_mod.client;

import jp.ac.u_tokyo.sdm.sdm_mod.ModEntities;
import jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.PoliceOfficerEntityRenderer;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.TeacherDialogueScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.TechnicalBookScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.screen.warp.WarpSelectScreen;
import jp.ac.u_tokyo.sdm.sdm_mod.client.story.StoryClientNetworking;
import jp.ac.u_tokyo.sdm.sdm_mod.network.TeacherDialoguePayload;
import jp.ac.u_tokyo.sdm.sdm_mod.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class SdmModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.POLICE_OFFICER, PoliceOfficerEntityRenderer::new);
        HandledScreens.register(ModScreenHandlers.TECHNICAL_BOOK, TechnicalBookScreen::new);
        HandledScreens.register(ModScreenHandlers.WARP_SELECT, WarpSelectScreen::new);
        StoryClientNetworking.initialize();
        // サーバーから TeacherDialoguePayload が届いたらダイアログ画面を開く。
        // execute() でメインスレッドに乗せてから setScreen を呼ぶ（スレッドセーフ対策）。
        ClientPlayNetworking.registerGlobalReceiver(TeacherDialoguePayload.ID, (payload, context) ->
            context.client().execute(() ->
                context.client().setScreen(new TeacherDialogueScreen(payload.text()))
            )
        );
    }
}
