package jp.ac.u_tokyo.sdm.sdm_mod.story.runtime;

import jp.ac.u_tokyo.sdm.sdm_mod.story.registry.StoryChapterDefinition;
import jp.ac.u_tokyo.sdm.sdm_mod.story.registry.StoryChapterRegistry;
import jp.ac.u_tokyo.sdm.sdm_mod.story.state.StoryProgress;

public final class StoryManager {
    private StoryProgress progress;

    private StoryManager(StoryProgress progress) {
        this.progress = progress;
    }

    public static StoryManager createDefault() {
        StoryChapterDefinition startingChapter = StoryChapterRegistry.getStartingChapter();
        return new StoryManager(StoryProgress.startingAt(startingChapter.id()));
    }

    public StoryProgress getProgress() {
        return progress;
    }

    public void reset() {
        StoryChapterDefinition startingChapter = StoryChapterRegistry.getStartingChapter();
        progress = StoryProgress.startingAt(startingChapter.id());
    }
}
