package jp.ac.u_tokyo.sdm.sdm_mod.story.state;

public record StoryProgress(String currentChapterId, int sceneIndex) {
    public static StoryProgress startingAt(String chapterId) {
        return new StoryProgress(chapterId, 0);
    }
}
