package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.ModBlocks;
import jp.ac.u_tokyo.sdm.sdm_mod.ModEntities;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.PosterEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.block.SearchPcBlock;
import jp.ac.u_tokyo.sdm.sdm_mod.game.GameRulesInitializer;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.SetupGuideHudPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2DoorArrowService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2PoliceOfficerGunTrigger;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2TutorialDialogueService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2TutorialZombieService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase3.Phase3ZombieService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.state.StoryProgress;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
        server.getPlayerManager().getPlayerList()
            .forEach(player -> ServerPlayNetworking.send(player, new SetupGuideHudPayload(false)));
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
        // Mark the story as active last so entity-load hooks do not run during setup.
        storyManager.activate();
        Phase2TutorialZombieService.spawnPhase2TutorialZombie(server.getOverworld());
        Phase2TutorialDialogueService.start(server);
        Phase3ZombieService.spawnPhase2Zombies(server.getOverworld());
        spawnPosters(server.getOverworld());
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

    /**
     * ストーリー開始時にポスターエンティティを指定座標へ自動配置する。
     *
     * 追加方法:
     *   spawnPoster(world, "poster_id", x, y, z, Direction.NORTH);
     *
     * poster_id は PosterModule.registerPosters() で登録されている名前。
     *   例: "classification0" 〜 "classification19"
     *
     * Direction（向き）は壁の「法線方向」＝ポスターがどちらを向いているか。
     *   NORTH  = 南の壁に貼る（Z が減る方向を向く）
     *   SOUTH  = 北の壁に貼る（Z が増える方向を向く）
     *   WEST   = 東の壁に貼る（X が減る方向を向く）
     *   EAST   = 西の壁に貼る（X が増える方向を向く）
     *
     * 座標はポスターエンティティ自体の中心位置。
     * 壁ブロックの表面から少し浮かせた位置（約0.5ブロック前）を指定すると壁にぴったり貼れる。
     */
    private static void spawnPosters(ServerWorld world) {
        // 各分類ポスターをX=-200〜-120の本棚に対応させて配置する。
        // X座標は本棚ブロックのX + 0.5（ブロック中心）。y=43.5, z は固定。
        // BOOKSHELF_CATEGORY_MAP の順（Phase3BookshelfService 参照）:
        //   0:X=-200, 1:X=-199, 2:X=-193, 3:X=-192, 4:X=-185, 5:X=-184,
        //   6:X=-177, 7:X=-176, 8:X=-169, 9:X=-168, 10:X=-153, 11:X=-152,
        //   12:X=-145, 13:X=-144, 14:X=-137, 15:X=-136, 16:X=-129, 17:X=-128,
        //   18:X=-121, 19:X=-120（正解本棚）
        spawnPoster(world, "classification0",  -199.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification0",  -199.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification1",  -198.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification1",  -198.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification2",  -192.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification2",  -192.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification3",  -191.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification3",  -191.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification4",  -184.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification4",  -184.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification5",  -183.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification5",  -183.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification6",  -176.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification6",  -176.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification7",  -175.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification7",  -175.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification8",  -168.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification8",  -168.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification9",  -167.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification9",  -167.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification10", -152.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification10", -152.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification11", -151.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification11", -151.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification12", -144.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification12", -144.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification13", -143.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification13", -143.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification14", -136.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification14", -136.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification15", -135.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification15", -135.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification16", -128.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification16", -128.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification17", -127.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification17", -127.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification18", -120.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification18", -120.5, 43.5, -638.001, Direction.NORTH);
        spawnPoster(world, "classification19", -119.5, 43.5, -633.991, Direction.SOUTH);
        spawnPoster(world, "classification19", -119.5, 43.5, -638.001, Direction.NORTH);
    }

    private static void spawnPoster(ServerWorld world, String posterId, double x, double y, double z, Direction facing) {
        PosterEntity entity = new PosterEntity(ModEntities.POSTER, world);
        entity.setPosition(x, y, z);
        entity.setPosterId(posterId);
        entity.setPosterFacing(facing);
        world.spawnEntity(entity);
    }
}
