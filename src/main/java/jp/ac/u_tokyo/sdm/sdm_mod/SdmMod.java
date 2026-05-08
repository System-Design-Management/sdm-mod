package jp.ac.u_tokyo.sdm.sdm_mod;

import jp.ac.u_tokyo.sdm.sdm_mod.game.CommandPermissionInitializer;
import jp.ac.u_tokyo.sdm.sdm_mod.screen.ModScreenHandlers;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.command.StoryCommandInitializer;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.StoryNetworking;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryFlashlightLightService;
import net.fabricmc.api.ModInitializer;

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
        CommandPermissionInitializer.initialize();
        StoryModule.initialize();
        StoryFlashlightLightService.initialize();
        StoryCommandInitializer.initialize();
    }
}
