package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import java.util.Set;
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
import net.minecraft.world.World;

public final class StoryDoorLockService {
    private static final String PHASE2_ID = "phase2";
    private static final String PHASE3_ID = "phase3";
    private static final Set<BlockPos> LOCKED_DOOR_BASE_POSITIONS = Set.of(
        new BlockPos(-161, 41, -640),
        new BlockPos(-160, 41, -640),
        new BlockPos(-146, 41, -640),
        new BlockPos(-145, 41, -640)
    );

    private StoryDoorLockService() {
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            StoryManager storyManager = StoryModule.getStoryManager();
            if (!shouldLockDoors(storyManager)) {
                return ActionResult.PASS;
            }

            BlockPos clickedPos = hitResult.getBlockPos();
            BlockState clickedState = world.getBlockState(clickedPos);
            if (!(clickedState.getBlock() instanceof DoorBlock)) {
                return ActionResult.PASS;
            }

            BlockPos doorBasePos = normalizeDoorBasePos(clickedPos, clickedState);
            if (!LOCKED_DOOR_BASE_POSITIONS.contains(doorBasePos)) {
                return ActionResult.PASS;
            }

            notifyDoorLocked((ServerPlayerEntity) player);
            return ActionResult.FAIL;
        });
    }

    private static boolean shouldLockDoors(StoryManager storyManager) {
        return storyManager.isActive()
            && (storyManager.isAtChapter(PHASE2_ID) || storyManager.isAtChapter(PHASE3_ID));
    }

    private static BlockPos normalizeDoorBasePos(BlockPos clickedPos, BlockState clickedState) {
        if (clickedState.contains(DoorBlock.HALF) && clickedState.get(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
            return clickedPos.down();
        }

        return clickedPos;
    }

    private static void notifyDoorLocked(ServerPlayerEntity player) {
        player.sendMessage(Text.literal("The door will not open right now."), false);
    }
}
