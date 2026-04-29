package jp.ac.u_tokyo.sdm.sdm_mod.story.runtime;

import jp.ac.u_tokyo.sdm.sdm_mod.story.registry.StoryChapterDefinition;
import jp.ac.u_tokyo.sdm.sdm_mod.story.registry.StoryChapterRegistry;
import jp.ac.u_tokyo.sdm.sdm_mod.story.state.StoryProgress;

public final class StoryManager {
    private StoryProgress progress;
    private boolean active;

    private StoryManager(StoryProgress progress, boolean active) {
        this.progress = progress;
        this.active = active;
    }

    public static StoryManager createDefault() {
        StoryChapterDefinition startingChapter = StoryChapterRegistry.getStartingChapter();
        return new StoryManager(StoryProgress.startingAt(startingChapter.id()), false);
    }

    public StoryProgress getProgress() {
        return progress;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() {
        active = true;
    }

    public boolean isAtChapter(String chapterId) {
        return progress.currentChapterId().equals(chapterId);
    }

    public StoryProgress advanceToChapter(String chapterId) {
        if (!StoryChapterRegistry.contains(chapterId)) {
            throw new IllegalArgumentException("Unknown story chapter: " + chapterId);
        }

        progress = StoryProgress.startingAt(chapterId);
        return progress;
    }

    public void reset() {
        StoryChapterDefinition startingChapter = StoryChapterRegistry.getStartingChapter();
        progress = StoryProgress.startingAt(startingChapter.id());
    }
}
