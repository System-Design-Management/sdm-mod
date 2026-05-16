package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.registry.StoryChapterRegistry;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.Phase5GameOverPayload;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Phase5GameOverService {
    private static final String PHASE5_ID = "phase5";

    private Phase5GameOverService() {
    }

    public static void initialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity player)) {
                return;
            }

            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive()) {
                return;
            }

            String currentChapterId = storyManager.getProgress().currentChapterId();
            if (StoryChapterRegistry.getOrder(currentChapterId) < StoryChapterRegistry.getOrder(PHASE5_ID)) {
                return;
            }

            ServerPlayNetworking.send(player, new Phase5GameOverPayload(StoryVideoSkipState.isVideoSkipAllowed()));
        });
    }
}
