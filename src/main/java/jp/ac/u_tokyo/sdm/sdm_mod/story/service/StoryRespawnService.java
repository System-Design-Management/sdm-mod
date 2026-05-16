package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.game.CommandLockState;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.RespawnGuidePayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class StoryRespawnService {
    private static final String PHASE2_ID = "phase2";
    private static final String PHASE3_ID = "phase3";
    private static final BlockPos[] PHASE3_RESPAWN_DOORS = {
        new BlockPos(-175, 41, -640),
        new BlockPos(-176, 41, -640)
    };
    private static final String PHASE4_ID = "phase4";
    private static final float RESPAWN_YAW = 180.0f;
    private static final float RESPAWN_PITCH = 0.0f;
    private static final int DEATH_RESPAWN_SEARCH_RADIUS = 3;
    private static final int[] DEATH_RESPAWN_VERTICAL_OFFSETS = {0, 1, -1, 2, -2, 3, -3};
    private static final double PLAYER_RESPAWN_HALF_WIDTH = 0.3;
    private static final double PLAYER_RESPAWN_HEIGHT = 1.8;
    private static final long RESPAWN_PROTECTION_TICKS = 100L;
    private static final Map<String, RespawnPoint> FALLBACK_RESPAWN_POINTS = Map.of(
        PHASE2_ID, new RespawnPoint(-160.5, 28.0, -625.0, RESPAWN_YAW, RESPAWN_PITCH),
        PHASE3_ID, new RespawnPoint(-175.0, 41.0, -642.0, RESPAWN_YAW, RESPAWN_PITCH),
        PHASE4_ID, new RespawnPoint(-118.0, 41.0, -636.0, RESPAWN_YAW, RESPAWN_PITCH)
    );
    private static final Map<UUID, Long> RESPAWN_PROTECTION_END_TICKS = new HashMap<>();

    private StoryRespawnService() {
    }

    public static void initialize() {
        ServerPlayerEvents.AFTER_RESPAWN.register(StoryRespawnService::handleRespawn);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity player) || amount <= 0.0f) {
                return true;
            }

            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !isProtectedRespawnChapter(storyManager.getProgress().currentChapterId())) {
                RESPAWN_PROTECTION_END_TICKS.remove(player.getUuid());
                return true;
            }

            Long protectionEndTick = RESPAWN_PROTECTION_END_TICKS.get(player.getUuid());
            if (protectionEndTick == null) {
                return true;
            }

            if (player.getWorld().getTime() > protectionEndTick) {
                RESPAWN_PROTECTION_END_TICKS.remove(player.getUuid());
                return true;
            }

            return false;
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> RESPAWN_PROTECTION_END_TICKS.clear());
    }

    private static void handleRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        if (alive) {
            return;
        }

        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive()) {
            return;
        }

        String currentChapterId = storyManager.getProgress().currentChapterId();
        RespawnPoint fallbackRespawnPoint = FALLBACK_RESPAWN_POINTS.get(currentChapterId);
        if (fallbackRespawnPoint == null) {
            return;
        }

        MinecraftServer server = newPlayer.getServer();
        if (server == null) {
            return;
        }

        ServerWorld world = server.getOverworld();
        RespawnPoint respawnPoint = findDeathRespawnPoint(world, oldPlayer).orElse(fallbackRespawnPoint);
        ChunkPos destinationChunk = new ChunkPos(BlockPos.ofFloored(respawnPoint.x(), respawnPoint.y(), respawnPoint.z()));

        // Load the destination chunk before teleporting so command execution runs from a stable world state.
        world.getChunk(destinationChunk.x, destinationChunk.z);

        // phase3 only: preserve the exact inventory the player had before dying.
        // Other phases clear inventory and re-give a standard loadout.
        boolean preserveInventory = PHASE3_ID.equals(storyManager.getProgress().currentChapterId());
        if (!preserveInventory) {
            newPlayer.getInventory().clear();
            newPlayer.playerScreenHandler.sendContentUpdates();
        }

        // Defer teleport to the next tick so Minecraft's own respawn position
        // setup (TeleportTarget) does not overwrite our custom coordinates.
        server.execute(() -> {
            if (preserveInventory) {
                closePhase3RespawnDoors(world, newPlayer);
            }

            newPlayer.teleport(
                world,
                respawnPoint.x(),
                respawnPoint.y(),
                respawnPoint.z(),
                Set.<PositionFlag>of(),
                respawnPoint.yaw(),
                respawnPoint.pitch(),
                false
            );

            if (!preserveInventory) {
                giveRespawnLoadout(server, newPlayer);
            }

            RESPAWN_PROTECTION_END_TICKS.put(
                newPlayer.getUuid(),
                newPlayer.getWorld().getTime() + RESPAWN_PROTECTION_TICKS
            );
            ServerPlayNetworking.send(newPlayer, RespawnGuidePayload.INSTANCE);
        });
    }

    private static boolean isProtectedRespawnChapter(String chapterId) {
        return PHASE2_ID.equals(chapterId) || PHASE3_ID.equals(chapterId) || PHASE4_ID.equals(chapterId);
    }

    private static Optional<RespawnPoint> findDeathRespawnPoint(ServerWorld world, ServerPlayerEntity oldPlayer) {
        if (oldPlayer.getWorld() != world) {
            return Optional.empty();
        }

        BlockPos deathPos = BlockPos.ofFloored(oldPlayer.getX(), oldPlayer.getY(), oldPlayer.getZ());
        for (int yOffset : DEATH_RESPAWN_VERTICAL_OFFSETS) {
            for (int radius = 0; radius <= DEATH_RESPAWN_SEARCH_RADIUS; radius++) {
                for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                    for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                        if (Math.max(Math.abs(xOffset), Math.abs(zOffset)) != radius) {
                            continue;
                        }

                        BlockPos candidate = deathPos.add(xOffset, yOffset, zOffset);
                        if (isSafeRespawnPosition(world, candidate)) {
                            return Optional.of(new RespawnPoint(
                                candidate.getX() + 0.5,
                                candidate.getY(),
                                candidate.getZ() + 0.5,
                                oldPlayer.getYaw(),
                                oldPlayer.getPitch()
                            ));
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static boolean isSafeRespawnPosition(ServerWorld world, BlockPos feetPos) {
        BlockState groundState = world.getBlockState(feetPos.down());
        if (!groundState.isSideSolidFullSquare(world, feetPos.down(), Direction.UP)) {
            return false;
        }

        if (hasFluid(world, feetPos) || hasFluid(world, feetPos.up())) {
            return false;
        }

        Box playerBox = new Box(
            feetPos.getX() + 0.5 - PLAYER_RESPAWN_HALF_WIDTH,
            feetPos.getY(),
            feetPos.getZ() + 0.5 - PLAYER_RESPAWN_HALF_WIDTH,
            feetPos.getX() + 0.5 + PLAYER_RESPAWN_HALF_WIDTH,
            feetPos.getY() + PLAYER_RESPAWN_HEIGHT,
            feetPos.getZ() + 0.5 + PLAYER_RESPAWN_HALF_WIDTH
        );
        return world.isSpaceEmpty(playerBox);
    }

    private static boolean hasFluid(ServerWorld world, BlockPos pos) {
        return !world.getBlockState(pos).getFluidState().isEmpty();
    }

    private static void closePhase3RespawnDoors(ServerWorld world, ServerPlayerEntity player) {
        for (BlockPos pos : PHASE3_RESPAWN_DOORS) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock door) {
                door.setOpen(player, world, state, pos, false);
            }
        }
    }

    private static void giveRespawnLoadout(MinecraftServer server, ServerPlayerEntity player) {
        executePlayerCommand(server, player, "function thepa:give/revolver");
        executePlayerCommand(server, player, "function thepa:give/bullets");
        executePlayerCommand(server, player, "give @s sdm_mod:student_id 1");
    }

    private static void executePlayerCommand(MinecraftServer server, ServerPlayerEntity player, String command) {
        CommandLockState.runUnlocked(() -> {
            ServerCommandSource commandSource = server.getCommandSource()
                .withLevel(2)
                .withEntity(player)
                .withPosition(player.getPos())
                .withRotation(player.getRotationClient());
            server.getCommandManager().executeWithPrefix(commandSource, command);
        });
    }

    private record RespawnPoint(double x, double y, double z, float yaw, float pitch) {
    }
}
