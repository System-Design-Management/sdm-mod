package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.Map;
import java.util.Set;

public final class StoryRespawnService {
    private static final String PHASE2_ID = "phase2";
    private static final String PHASE3_ID = "phase3";
    private static final String PHASE4_ID = "phase4";
    private static final float RESPAWN_YAW = 180.0f;
    private static final float RESPAWN_PITCH = 0.0f;
    private static final Map<String, RespawnPoint> RESPAWN_POINTS = Map.of(
        PHASE2_ID, new RespawnPoint(-160.5, 28.0, -625.0),
        PHASE3_ID, new RespawnPoint(-170.5, 41.0, -643.0),
        PHASE4_ID, new RespawnPoint(-118.0, 41.0, -636.0)
    );

    private StoryRespawnService() {
    }

    public static void initialize() {
        ServerPlayerEvents.AFTER_RESPAWN.register(StoryRespawnService::handleRespawn);
    }

    private static void handleRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        if (alive) {
            return;
        }

        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive()) {
            return;
        }

        RespawnPoint respawnPoint = RESPAWN_POINTS.get(storyManager.getProgress().currentChapterId());
        if (respawnPoint == null) {
            return;
        }

        MinecraftServer server = newPlayer.getServer();
        if (server == null) {
            return;
        }

        ServerWorld world = server.getOverworld();
        ChunkPos destinationChunk = new ChunkPos(BlockPos.ofFloored(respawnPoint.x(), respawnPoint.y(), respawnPoint.z()));

        // Load the destination chunk before teleporting so command execution runs from a stable world state.
        world.getChunk(destinationChunk.x, destinationChunk.z);
        newPlayer.getInventory().clear();
        newPlayer.playerScreenHandler.sendContentUpdates();
        newPlayer.teleport(
            world,
            respawnPoint.x(),
            respawnPoint.y(),
            respawnPoint.z(),
            Set.<PositionFlag>of(),
            RESPAWN_YAW,
            RESPAWN_PITCH,
            false
        );
        giveRespawnLoadout(server, newPlayer);
    }

    private static void giveRespawnLoadout(MinecraftServer server, ServerPlayerEntity player) {
        executePlayerCommand(server, player, "function thepa:give/revolver");
        executePlayerCommand(server, player, "function thepa:give/bullets");
        executePlayerCommand(server, player, "give @s sdm_mod:student_id 1");
    }

    private static void executePlayerCommand(MinecraftServer server, ServerPlayerEntity player, String command) {
        ServerCommandSource commandSource = server.getCommandSource()
            .withLevel(2)
            .withEntity(player)
            .withPosition(player.getPos())
            .withRotation(player.getRotationClient());
        server.getCommandManager().executeWithPrefix(commandSource, command);
    }

    private record RespawnPoint(double x, double y, double z) {
    }
}
