package jp.ac.u_tokyo.sdm.sdm_mod.story.phase5;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowEdVideoPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Phase5To6RegionTrigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Phase5To6RegionTrigger.class);
    private static final String PHASE5_ID = "phase5";
    private static final String PHASE6_ID = "phase6";
    private static final double TRIGGER_Z = -619.0D;

    private Phase5To6RegionTrigger() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase5To6RegionTrigger::tick);
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE5_ID)) {
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.getZ() < TRIGGER_Z) {
                continue;
            }

            LOGGER.info(
                "Story advanced from {} to {} after player {} reached z >= {} at ({}, {}, {}).",
                PHASE5_ID, PHASE6_ID,
                player.getName().getString(),
                TRIGGER_Z,
                player.getX(), player.getY(), player.getZ()
            );
            advanceToPhase6(server);
            return;
        }
    }

    private static void advanceToPhase6(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        storyManager.advanceToChapter(PHASE6_ID);
        // TODO: Remove this debug notification once phase transitions are verified in playtesting.
        server.getPlayerManager().broadcast(
            Text.literal("[DEBUG] ストーリーのフェーズが " + PHASE6_ID + " に切り替わりました。"),
            false
        );
        server.getPlayerManager().getPlayerList()
            .forEach(player -> ServerPlayNetworking.send(player, ShowEdVideoPayload.INSTANCE));
    }
}
