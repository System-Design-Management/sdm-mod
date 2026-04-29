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
    // Temporary trigger regions for phase4 -> phase5. Update these corners if the route layout changes.
    private static final TriggerRegion[] TRIGGER_REGIONS = {
        new TriggerRegion(
            new BlockPos(-149, 40, -644),
            new BlockPos(-149, 43, -641)
        ),
        new TriggerRegion(
            new BlockPos(-161, 40, -641),
            new BlockPos(-160, 43, -640)
        ),
        new TriggerRegion(
            new BlockPos(-176, 40, -641),
            new BlockPos(-175, 43, -640)
        )
    };

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
            TriggerRegion matchedRegion = findMatchedRegion(player.getBlockPos());
            if (matchedRegion == null) {
                continue;
            }

            storyManager.advanceToChapter(PHASE5_ID);
            notifyTriggered(server);
            LOGGER.info(
                "Story advanced from {} to {} after player {} entered region {} -> {}.",
                PHASE4_ID,
                PHASE5_ID,
                player.getName().getString(),
                matchedRegion.cornerA(),
                matchedRegion.cornerB()
            );
            return;
        }
    }

    private static TriggerRegion findMatchedRegion(BlockPos pos) {
        for (TriggerRegion region : TRIGGER_REGIONS) {
            if (region.contains(pos)) {
                return region;
            }
        }

        return null;
    }

    private static void notifyTriggered(MinecraftServer server) {
        // TODO: Remove this debug notification once phase transitions are verified in playtesting.
        server.getPlayerManager().broadcast(Text.literal("[DEBUG] Story phase changed to " + PHASE5_ID + "."), false);
    }

    private record TriggerRegion(BlockPos cornerA, BlockPos cornerB) {
        private boolean contains(BlockPos pos) {
            return pos.getX() >= Math.min(cornerA.getX(), cornerB.getX())
                && pos.getX() <= Math.max(cornerA.getX(), cornerB.getX())
                && pos.getY() >= Math.min(cornerA.getY(), cornerB.getY())
                && pos.getY() <= Math.max(cornerA.getY(), cornerB.getY())
                && pos.getZ() >= Math.min(cornerA.getZ(), cornerB.getZ())
                && pos.getZ() <= Math.max(cornerA.getZ(), cornerB.getZ());
        }
    }
}
