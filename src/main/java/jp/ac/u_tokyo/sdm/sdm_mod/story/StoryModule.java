package jp.ac.u_tokyo.sdm.sdm_mod.story;

import jp.ac.u_tokyo.sdm.sdm_mod.story.phase3.Phase3To4BookTrigger;
import jp.ac.u_tokyo.sdm.sdm_mod.story.registry.StoryChapterRegistry;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryEntityControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StoryModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoryModule.class);
    private static StoryManager storyManager;

    private StoryModule() {
    }

    public static void initialize() {
        StoryChapterRegistry.initialize();
        storyManager = StoryManager.createDefault();
        StoryEntityControlService.initialize();
        Phase3To4BookTrigger.initialize();
        LOGGER.info("Story module initialized with {} chapter(s).", StoryChapterRegistry.size());
    }

    public static StoryManager getStoryManager() {
        if (storyManager == null) {
            throw new IllegalStateException("Story module is not initialized.");
        }

        return storyManager;
    }
}
