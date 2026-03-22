package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.game.GameRulesInitializer;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.state.StoryProgress;
import net.minecraft.server.MinecraftServer;

public final class StoryStartService {
    private StoryStartService() {
    }

    public static StoryProgress start(MinecraftServer server) {
        GameRulesInitializer.applyStoryDefaults(server);

        StoryManager storyManager = StoryModule.getStoryManager();
        storyManager.reset();
        return storyManager.getProgress();
    }
}
