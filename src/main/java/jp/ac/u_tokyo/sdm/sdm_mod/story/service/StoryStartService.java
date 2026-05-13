package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.ModBlocks;
import jp.ac.u_tokyo.sdm.sdm_mod.block.SearchPcBlock;
import jp.ac.u_tokyo.sdm.sdm_mod.game.GameRulesInitializer;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2DoorArrowService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2PoliceOfficerGunTrigger;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2TutorialDialogueService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2TutorialZombieService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase3.Phase3ZombieService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.state.StoryProgress;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

import java.util.Set;

public final class StoryStartService {
    private static final String STORY_START_CHAPTER_ID = "phase2";
    private static final int BLOCK_UPDATE_FLAGS = 3;
    private static final double STORY_START_X = -160.5;
    private static final double STORY_START_Y = 25.0;
    private static final double STORY_START_Z = -599.0;
    private static final float STORY_START_YAW = 180.0f;
    private static final float STORY_START_PITCH = 0.0f;
    private static final int LIBRARY_PC_FLOOR_Y = 30;
    private static final int LIBRARY_PC_Z = -644;
    private static final int LIBRARY_PC_FLOOR_START_X = -149;
    private static final int LIBRARY_PC_FLOOR_END_X = -145;
    private static final BlockPos LEFT_LIBRARY_PC_POS = new BlockPos(-149, 31, LIBRARY_PC_Z);
    private static final BlockPos RIGHT_LIBRARY_PC_POS = new BlockPos(-147, 31, LIBRARY_PC_Z);
    private static final Direction LIBRARY_PC_FACING = Direction.SOUTH;

    private StoryStartService() {
    }

    public static StoryProgress start(MinecraftServer server) {
        GameRulesInitializer.applyStoryDefaults(server);
        server.getPlayerManager().getPlayerList().forEach(player -> stopBackgroundMusic(server, player));
        // Remove existing hostile/passive mobs before players are reset into the story state.
        StoryEntityControlService.clearNonPlayerLivingEntities(server);
        StoryPoliceOfficerService.spawnPhase2PoliceOfficer(server);
        StoryNpcSpawnService.spawnAll(server);
        StoryTorchCleanupService.removeTorchesInStoryArea(server);
        Phase2DoorArrowService.resetProgress();
        placeLibrarySearchPcs(server.getOverworld());
        server.getPlayerManager().getPlayerList().forEach(StoryStartService::resetPlayerState);
        server.getPlayerManager().getPlayerList().forEach(StoryStartService::preparePlayerForStory);
        server.getPlayerManager().getPlayerList().forEach(player -> {
            executePlayerCommand(server, player, "give @s sdm_mod:student_id 1");
        });
        StoryFlashlightLightService.enable(server);

        Phase2PoliceOfficerGunTrigger.reset();
        StoryManager storyManager = StoryModule.getStoryManager();
        storyManager.reset();
        storyManager.advanceToChapter(STORY_START_CHAPTER_ID);
        notifyPhaseChange(server, STORY_START_CHAPTER_ID);
        // Mark the story as active last so entity-load hooks do not run during setup.
        storyManager.activate();
        Phase2TutorialZombieService.spawnPhase2TutorialZombie(server.getOverworld());
        Phase2TutorialDialogueService.start(server);
        Phase3ZombieService.spawnPhase2Zombies(server.getOverworld());
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
        ServerWorld world = (ServerWorld) player.getWorld();
        ChunkPos destinationChunk = new ChunkPos(BlockPos.ofFloored(STORY_START_X, STORY_START_Y, STORY_START_Z));

        // Load the destination chunk before teleporting so the first command execution is not racing chunk load.
        world.getChunk(destinationChunk.x, destinationChunk.z);
        player.changeGameMode(GameMode.ADVENTURE);
        player.teleport(
            world,
            STORY_START_X,
            STORY_START_Y,
            STORY_START_Z,
            Set.<PositionFlag>of(),
            STORY_START_YAW,
            STORY_START_PITCH,
            false
        );
    }

    private static void placeLibrarySearchPcs(ServerWorld world) {
        for (int x = LIBRARY_PC_FLOOR_START_X; x <= LIBRARY_PC_FLOOR_END_X; x++) {
            world.setBlockState(
                new BlockPos(x, LIBRARY_PC_FLOOR_Y, LIBRARY_PC_Z),
                Blocks.IRON_BLOCK.getDefaultState(),
                BLOCK_UPDATE_FLAGS
            );
        }

        world.setBlockState(
            LEFT_LIBRARY_PC_POS,
            ModBlocks.SEARCH_PC.getDefaultState().with(SearchPcBlock.FACING, LIBRARY_PC_FACING),
            BLOCK_UPDATE_FLAGS
        );
        world.setBlockState(
            RIGHT_LIBRARY_PC_POS,
            ModBlocks.SEARCH_PC.getDefaultState().with(SearchPcBlock.FACING, LIBRARY_PC_FACING),
            BLOCK_UPDATE_FLAGS
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
