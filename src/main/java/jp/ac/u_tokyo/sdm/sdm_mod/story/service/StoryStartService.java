package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.game.GameRulesInitializer;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.state.StoryProgress;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameMode;

import java.util.Set;

public final class StoryStartService {
    private static final String STORY_START_CHAPTER_ID = "phase2";
    private static final double STORY_START_X = -160.0;
    private static final double STORY_START_Y = 27.0;
    private static final double STORY_START_Z = -614.0;

    private StoryStartService() {
    }

    public static StoryProgress start(MinecraftServer server) {
        GameRulesInitializer.applyStoryDefaults(server);
        server.getPlayerManager().getPlayerList().forEach(player -> stopBackgroundMusic(server, player));
        // Remove existing hostile/passive mobs before players are reset into the story state.
        StoryEntityControlService.clearNonPlayerLivingEntities(server);
        server.getPlayerManager().getPlayerList().forEach(StoryStartService::resetPlayerState);
        server.getPlayerManager().getPlayerList().forEach(StoryStartService::preparePlayerForStory);
        server.getPlayerManager().getPlayerList().forEach(player -> {
            executePlayerCommand(server, player, "function thepa:give/revolver");
            executePlayerCommand(server, player, "function thepa:give/bullets");
            executePlayerCommand(server, player, "function thepa:give/pump_shotgun");
            executePlayerCommand(server, player, "function thepa:give/shells");
            executePlayerCommand(server, player, "function thepa:give/m1_garand");
            executePlayerCommand(server, player, "function thepa:give/medium_round");
            executePlayerCommand(server, player, "give @s sdm_mod:student_id 1");
        });
        StoryFlashlightLightService.enable(server);

        StoryManager storyManager = StoryModule.getStoryManager();
        storyManager.reset();
        storyManager.advanceToChapter(STORY_START_CHAPTER_ID);
        notifyPhaseChange(server, STORY_START_CHAPTER_ID);
        // Mark the story as active last so entity-load hooks do not run during setup.
        storyManager.activate();
        return storyManager.getProgress();
    }

    private static void stopBackgroundMusic(MinecraftServer server, ServerPlayerEntity player) {
        executePlayerCommand(server, player, "stopsound @s music");
    }

    private static void resetPlayerState(ServerPlayerEntity player) {
        player.getInventory().clear();
        player.getEnderChestInventory().clear();
        player.clearStatusEffects();
        player.extinguish();
        player.setOnGround(true);
        player.setHealth(player.getMaxHealth());
        player.getHungerManager().setFoodLevel(20);
        player.getHungerManager().setSaturationLevel(5.0f);
        player.setAir(player.getMaxAir());
        player.setFireTicks(0);
        player.fallDistance = 0.0f;
        player.setFrozenTicks(0);
        player.experienceLevel = 0;
        player.totalExperience = 0;
        player.experienceProgress = 0.0f;
    }

    private static void preparePlayerForStory(ServerPlayerEntity player) {
        // TODO: 開始地点の高さを再確認し、安全が確認できたら ADVENTURE に戻す。
        ServerWorld world = (ServerWorld) player.getWorld();
        ChunkPos destinationChunk = new ChunkPos(BlockPos.ofFloored(STORY_START_X, STORY_START_Y, STORY_START_Z));

        // Load the destination chunk before teleporting so the first command execution is not racing chunk load.
        world.getChunk(destinationChunk.x, destinationChunk.z);
        player.changeGameMode(GameMode.CREATIVE);
        player.teleport(
            world,
            STORY_START_X,
            STORY_START_Y,
            STORY_START_Z,
            Set.<PositionFlag>of(),
            player.getYaw(),
            player.getPitch(),
            false
        );
    }

    private static void executePlayerCommand(MinecraftServer server, ServerPlayerEntity player, String command) {
        ServerCommandSource commandSource = server.getCommandSource()
            .withLevel(2)
            .withEntity(player)
            .withPosition(player.getPos())
            .withRotation(player.getRotationClient());
        server.getCommandManager().executeWithPrefix(commandSource, command);
    }

    private static void notifyPhaseChange(MinecraftServer server, String chapterId) {
        // TODO: Remove this debug notification once phase transitions are verified in playtesting.
        server.getPlayerManager().broadcast(Text.literal("[DEBUG] ストーリーのフェーズが " + chapterId + " に切り替わりました。"), false);
    }
}
