package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Phase2To3RegionTrigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Phase2To3RegionTrigger.class);
    private static final String PHASE2_ID = "phase2";
    private static final String PHASE3_ID = "phase3";
    // Temporary trigger region for phase2 -> phase3. Update these corners if the area changes.
    private static final BlockPos REGION_CORNER_A = new BlockPos(-176, 40, -641);
    private static final BlockPos REGION_CORNER_B = new BlockPos(-175, 43, -640);

    private Phase2To3RegionTrigger() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase2To3RegionTrigger::tick);
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE2_ID)) {
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!isInsideTriggerRegion(player.getBlockPos())) {
                continue;
            }

            storyManager.advanceToChapter(PHASE3_ID);
            notifyTriggered(server);
            LOGGER.info(
                "Story advanced from {} to {} after player {} entered region {} -> {}.",
                PHASE2_ID,
                PHASE3_ID,
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
        server.getPlayerManager().broadcast(Text.literal("[DEBUG] Story phase changed to " + PHASE3_ID + "."), false);
    }
}
