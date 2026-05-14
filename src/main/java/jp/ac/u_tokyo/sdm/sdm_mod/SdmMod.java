package jp.ac.u_tokyo.sdm.sdm_mod;

import jp.ac.u_tokyo.sdm.sdm_mod.game.CommandPermissionInitializer;
import jp.ac.u_tokyo.sdm.sdm_mod.network.TeacherDialogueHudPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.network.TeacherDialoguePayload;
import jp.ac.u_tokyo.sdm.sdm_mod.screen.ModScreenHandlers;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.poster.PosterModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.command.StoryCommandInitializer;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.StoryNetworking;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4FireworkService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase5.Phase5OnaraTrigger;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryFlashlightLightService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class SdmMod implements ModInitializer {
    public static final String MOD_ID = "sdm_mod";

    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModEntities.initialize();
        ModScreenHandlers.initialize();
        ModItems.initialize();
        ModSounds.initialize();
        StoryNetworking.initialize();
        // S2C パケット型を登録。これがないとクライアントがパケットを受け取れない。
        PayloadTypeRegistry.playS2C().register(TeacherDialoguePayload.ID, TeacherDialoguePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TeacherDialogueHudPayload.ID, TeacherDialogueHudPayload.CODEC);
        CommandPermissionInitializer.initialize();
        StoryModule.initialize();
        Phase4FireworkService.initialize();
        Phase5OnaraTrigger.initialize();
        StoryFlashlightLightService.initialize();
        StoryCommandInitializer.initialize();
        PosterModule.initialize();
    }
}
