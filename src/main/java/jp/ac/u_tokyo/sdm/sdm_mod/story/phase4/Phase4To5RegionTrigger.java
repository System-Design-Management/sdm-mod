package jp.ac.u_tokyo.sdm.sdm_mod.story.phase4;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Phase4To5RegionTrigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Phase4To5RegionTrigger.class);
    private static final String PHASE4_ID = "phase4";
    private static final String PHASE5_ID = "phase5";
    // Temporary trigger region for phase4 -> phase5. Update these corners if the area changes.
    private static final BlockPos REGION_CORNER_A = new BlockPos(-149, 40, -644);
    private static final BlockPos REGION_CORNER_B = new BlockPos(-149, 43, -641);

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
            if (!isInsideTriggerRegion(player.getBlockPos())) {
                continue;
            }

            storyManager.advanceToChapter(PHASE5_ID);
            notifyTriggered(server);
            LOGGER.info(
                "Story advanced from {} to {} after player {} entered region {} -> {}.",
                PHASE4_ID,
                PHASE5_ID,
                player.getName().getString(),
                REGION_CORNER_A,
                REGION_CORNER_B
            );
            return;
        }
    }

    private static boolean isInsideTriggerRegion(BlockPos pos) {
        return pos.getX() >= Math.min(REGION_CORNER_A.getX(), REGION_CORNER_B.getX())
            && pos.getX() <= Math.max(REGION_CORNER_A.getX(), REGION_CORNER_B.getX())
            && pos.getY() >= Math.min(REGION_CORNER_A.getY(), REGION_CORNER_B.getY())
            && pos.getY() <= Math.max(REGION_CORNER_A.getY(), REGION_CORNER_B.getY())
            && pos.getZ() >= Math.min(REGION_CORNER_A.getZ(), REGION_CORNER_B.getZ())
            && pos.getZ() <= Math.max(REGION_CORNER_A.getZ(), REGION_CORNER_B.getZ());
    }

    private static void notifyTriggered(MinecraftServer server) {
        // TODO: Remove this debug notification once phase transitions are verified in playtesting.
        server.getPlayerManager().broadcast(Text.literal("[DEBUG] Story phase changed to " + PHASE5_ID + "."), false);
    }
}
