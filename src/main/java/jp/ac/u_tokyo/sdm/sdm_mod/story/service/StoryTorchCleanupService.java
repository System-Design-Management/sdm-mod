package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class StoryTorchCleanupService {
    private static final int BLOCK_UPDATE_FLAGS = 3;
    // TODO: Remove this temporary torch cleanup once the story world lighting is finalized.
    private static final BlockPos REGION_CORNER_A = new BlockPos(-206, 24, -618);
    private static final BlockPos REGION_CORNER_B = new BlockPos(-115, 73, -783);

    private StoryTorchCleanupService() {
    }

    public static void removeTorchesInStoryArea(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        int minX = Math.min(REGION_CORNER_A.getX(), REGION_CORNER_B.getX());
        int maxX = Math.max(REGION_CORNER_A.getX(), REGION_CORNER_B.getX());
        int minY = Math.min(REGION_CORNER_A.getY(), REGION_CORNER_B.getY());
        int maxY = Math.max(REGION_CORNER_A.getY(), REGION_CORNER_B.getY());
        int minZ = Math.min(REGION_CORNER_A.getZ(), REGION_CORNER_B.getZ());
        int maxZ = Math.max(REGION_CORNER_A.getZ(), REGION_CORNER_B.getZ());
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutablePos.set(x, y, z);
                    BlockState state = world.getBlockState(mutablePos);
                    if (state.isOf(Blocks.TORCH) || state.isOf(Blocks.WALL_TORCH)) {
                        world.setBlockState(mutablePos, Blocks.AIR.getDefaultState(), BLOCK_UPDATE_FLAGS);
                    }
                }
            }
        }
    }
}
