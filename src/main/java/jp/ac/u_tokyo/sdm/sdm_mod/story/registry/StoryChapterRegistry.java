package jp.ac.u_tokyo.sdm.sdm_mod.story.registry;

import java.util.LinkedHashMap;
import java.util.Map;

public final class StoryChapterRegistry {
    private static final Map<String, StoryChapterDefinition> CHAPTERS = new LinkedHashMap<>();

    private StoryChapterRegistry() {
    }

    public static void initialize() {
        CHAPTERS.clear();

        register(new StoryChapterDefinition("phase1", "Phase 1"));
        register(new StoryChapterDefinition("phase2", "Phase 2"));
        register(new StoryChapterDefinition("phase3", "Phase 3"));
        register(new StoryChapterDefinition("phase4", "Phase 4"));
        register(new StoryChapterDefinition("phase5", "Phase 5"));
        register(new StoryChapterDefinition("phase6", "Phase 6"));
    }

    public static StoryChapterDefinition getStartingChapter() {
        if (CHAPTERS.isEmpty()) {
            throw new IllegalStateException("No story chapters are registered.");
        }

        return CHAPTERS.values().iterator().next();
    }

    public static int size() {
        return CHAPTERS.size();
    }

    public static boolean contains(String chapterId) {
        return CHAPTERS.containsKey(chapterId);
    }

    private static void register(StoryChapterDefinition chapter) {
        CHAPTERS.put(chapter.id(), chapter);
    }
}
