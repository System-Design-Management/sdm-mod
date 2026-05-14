package jp.ac.u_tokyo.sdm.sdm_mod.story.phase4;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Phase4To5RegionTrigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Phase4To5RegionTrigger.class);
    private static final String PHASE4_ID = "phase4";
    private static final String PHASE5_ID = "phase5";
    private static final double TRIGGER_X = -173.0D;

    private Phase4To5RegionTrigger() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase4To5RegionTrigger::tick);
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE4_ID)) {
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.getX() > TRIGGER_X) {
                continue;
            }

            tryAdvanceToPhase5(
                server,
                "Story advanced from {} to {} after player {} reached x <= {} at ({}, {}, {}).",
                PHASE4_ID,
                PHASE5_ID,
                player.getName().getString(),
                TRIGGER_X,
                player.getX(),
                player.getY(),
                player.getZ()
            );
            return;
        }
    }

    public static boolean tryAdvanceToPhase5(MinecraftServer server, String logMessage, Object... logArguments) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE4_ID)) {
            return false;
        }

        storyManager.advanceToChapter(PHASE5_ID);
        notifyTriggered(server);
        LOGGER.info(logMessage, logArguments);
        return true;
    }

    private static void notifyTriggered(MinecraftServer server) {
        // TODO: Remove this debug notification once phase transitions are verified in playtesting.
        server.getPlayerManager().broadcast(Text.literal("[DEBUG] ストーリーのフェーズが " + PHASE5_ID + " に切り替わりました。"), false);
    }
}
