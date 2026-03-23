package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class StoryAmbientLightService {
    private static final int STORY_LIGHT_LEVEL = 8;
    private static final int LIGHT_UPDATE_FLAGS = 3;
    private static final BlockPos[] LIGHT_OFFSETS = {
        new BlockPos(0, 1, 0),
        BlockPos.ORIGIN,
        new BlockPos(0, 2, 0),
        new BlockPos(1, 1, 0),
        new BlockPos(-1, 1, 0),
        new BlockPos(0, 1, 1),
        new BlockPos(0, 1, -1)
    };

    private static final Map<UUID, LightPlacement> ACTIVE_LIGHTS = new HashMap<>();
    private static boolean enabled;

    private StoryAmbientLightService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(StoryAmbientLightService::tick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (enabled) {
                updatePlayerLight(handler.getPlayer());
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            disable();
            ACTIVE_LIGHTS.clear();
        });
    }

    public static void enable(MinecraftServer server) {
        disable();
        enabled = true;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            updatePlayerLight(player);
        }
    }

    public static void disable() {
        enabled = false;
        ACTIVE_LIGHTS.values().forEach(StoryAmbientLightService::removeLightIfPresent);
        ACTIVE_LIGHTS.clear();
    }

    private static void tick(MinecraftServer server) {
        if (!enabled) {
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            updatePlayerLight(player);
        }
    }

    private static void updatePlayerLight(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        LightPlacement currentPlacement = ACTIVE_LIGHTS.get(playerId);
        LightPlacement nextPlacement = findPlacement(player);

        if (currentPlacement != null && currentPlacement.equals(nextPlacement)) {
            return;
        }

        if (nextPlacement != null) {
            placeLight(nextPlacement);
            ACTIVE_LIGHTS.put(playerId, nextPlacement);
        } else {
            ACTIVE_LIGHTS.remove(playerId);
        }

        if (currentPlacement != null) {
            removeLightIfPresent(currentPlacement);
        }
    }

    private static LightPlacement findPlacement(ServerPlayerEntity player) {
        ServerWorld world = player.getWorld();
        BlockPos origin = player.getBlockPos();

        for (BlockPos offset : LIGHT_OFFSETS) {
            BlockPos targetPos = origin.add(offset);
            if (canPlaceLight(world, targetPos)) {
                return new LightPlacement(world, targetPos);
            }
        }

        return null;
    }

    private static boolean canPlaceLight(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isAir() || isStoryLight(state);
    }

    private static void placeLight(LightPlacement placement) {
        placement.world().setBlockState(
            placement.pos(),
            Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, STORY_LIGHT_LEVEL),
            LIGHT_UPDATE_FLAGS
        );
    }

    private static void removeLightIfPresent(LightPlacement placement) {
        BlockState state = placement.world().getBlockState(placement.pos());
        if (isStoryLight(state)) {
            placement.world().setBlockState(placement.pos(), Blocks.AIR.getDefaultState(), LIGHT_UPDATE_FLAGS);
        }
    }

    private static boolean isStoryLight(BlockState state) {
        return state.isOf(Blocks.LIGHT)
            && state.contains(LightBlock.LEVEL_15)
            && state.get(LightBlock.LEVEL_15) == STORY_LIGHT_LEVEL;
    }

    private record LightPlacement(ServerWorld world, BlockPos pos) {
    }
}
