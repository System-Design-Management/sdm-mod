package jp.ac.u_tokyo.sdm.sdm_mod.story.registry;

import java.util.LinkedHashMap;
import java.util.Map;

public final class StoryChapterRegistry {
    private static final Map<String, StoryChapterDefinition> CHAPTERS = new LinkedHashMap<>();

    private StoryChapterRegistry() {
    }

    public static void initialize() {
        CHAPTERS.clear();

        // TODO: ストーリー章データの実体を追加したら、ここで登録する。
        register(new StoryChapterDefinition("prologue", "Prologue"));
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

    private static void register(StoryChapterDefinition chapter) {
        CHAPTERS.put(chapter.id(), chapter);
    }
}
