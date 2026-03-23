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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public final class StoryFlashlightLightService {
    private static final int FLASHLIGHT_LIGHT_LEVEL = 8;
    private static final int LIGHT_UPDATE_FLAGS = 3;
    private static final double FLASHLIGHT_REACH = 10.0;

    private static final Map<UUID, LightPlacement> ACTIVE_LIGHTS = new HashMap<>();
    private static boolean enabled;

    private StoryFlashlightLightService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(StoryFlashlightLightService::tick);
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
        ACTIVE_LIGHTS.values().forEach(StoryFlashlightLightService::removeLightIfPresent);
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
        HitResult hitResult = player.raycast(FLASHLIGHT_REACH, 1.0f, false);
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        ServerWorld world = player.getWorld();
        BlockPos targetPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
        if (!canPlaceLight(world, targetPos)) {
            // TODO: 視線先に直接 light を置けない場合、周辺候補へフォールバックする。
            return null;
        }

        return new LightPlacement(world, targetPos);
    }

    private static boolean canPlaceLight(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isAir() || isFlashlightLight(state);
    }

    private static void placeLight(LightPlacement placement) {
        placement.world().setBlockState(
            placement.pos(),
            Blocks.LIGHT.getDefaultState().with(LightBlock.LEVEL_15, FLASHLIGHT_LIGHT_LEVEL),
            LIGHT_UPDATE_FLAGS
        );
    }

    private static void removeLightIfPresent(LightPlacement placement) {
        BlockState state = placement.world().getBlockState(placement.pos());
        if (isFlashlightLight(state)) {
            placement.world().setBlockState(placement.pos(), Blocks.AIR.getDefaultState(), LIGHT_UPDATE_FLAGS);
        }
    }

    private static boolean isFlashlightLight(BlockState state) {
        return state.isOf(Blocks.LIGHT)
            && state.contains(LightBlock.LEVEL_15)
            && state.get(LightBlock.LEVEL_15) == FLASHLIGHT_LIGHT_LEVEL;
    }

    private record LightPlacement(ServerWorld world, BlockPos pos) {
    }
}
