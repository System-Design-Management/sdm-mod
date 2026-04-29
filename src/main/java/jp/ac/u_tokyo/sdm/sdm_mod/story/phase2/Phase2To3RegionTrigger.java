package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public final class Phase2To3RegionTrigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Phase2To3RegionTrigger.class);
    private static final String PHASE2_ID = "phase2";
    private static final String PHASE3_ID = "phase3";
    private static final Set<BlockPos> TRIGGER_DOOR_BASE_POSITIONS = Set.of(
        new BlockPos(-175, 41, -640),
        new BlockPos(-176, 41, -640)
    );

    private Phase2To3RegionTrigger() {
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE2_ID)) {
                return ActionResult.PASS;
            }

            BlockPos clickedPos = hitResult.getBlockPos();
            BlockState clickedState = world.getBlockState(clickedPos);
            if (!(clickedState.getBlock() instanceof DoorBlock)) {
                return ActionResult.PASS;
            }

            BlockPos doorBasePos = normalizeDoorBasePos(clickedPos, clickedState);
            if (!TRIGGER_DOOR_BASE_POSITIONS.contains(doorBasePos)) {
                return ActionResult.PASS;
            }

            storyManager.advanceToChapter(PHASE3_ID);
            notifyTriggered((ServerPlayerEntity) player);
            LOGGER.info(
                "Story advanced from {} to {} after player {} used door at {}.",
                PHASE2_ID,
                PHASE3_ID,
                player.getName().getString(),
                doorBasePos
            );
            return ActionResult.PASS;
        });
    }

    private static BlockPos normalizeDoorBasePos(BlockPos clickedPos, BlockState clickedState) {
        if (clickedState.contains(DoorBlock.HALF) && clickedState.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            return clickedPos.down();
        }

        return clickedPos;
    }

    private static void notifyTriggered(ServerPlayerEntity player) {
        // TODO: Remove this debug notification once phase transitions are verified in playtesting.
        player.getServer().getPlayerManager().broadcast(Text.literal("[DEBUG] ストーリーのフェーズが " + PHASE3_ID + " に切り替わりました。"), false);
    }
}
