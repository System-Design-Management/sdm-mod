package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class StoryVideoSkipState {
    private static boolean videoSkipAllowed;

    private StoryVideoSkipState() {
    }

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> videoSkipAllowed = false);
    }

    public static boolean isVideoSkipAllowed() {
        return videoSkipAllowed;
    }

    public static void setVideoSkipAllowed(boolean allowed) {
        videoSkipAllowed = allowed;
    }
}
