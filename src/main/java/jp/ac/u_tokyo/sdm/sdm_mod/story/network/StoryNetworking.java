package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class StoryNetworking {
    private StoryNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(Phase5GameOverPayload.ID, Phase5GameOverPayload.CODEC);
    }
}
