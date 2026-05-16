package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StoryAutoStartService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoryAutoStartService.class);
    private static final String START_COMMAND = "sdm_story start";
    private static final BlockPos[] START_COMMAND_BLOCK_POSITIONS = {
        new BlockPos(-56, 22, -451),
        new BlockPos(-56, 22, -450)
    };
    private static final Box START_PRESSURE_PLATE_REGION = new Box(-57, 23, -452, -55, 26, -449);

    private static boolean enabled = false;
    private static boolean debugMode = false;

    private StoryAutoStartService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(StoryAutoStartService::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            enabled = false;
            debugMode = false;
        });
    }

    public static void enable(MinecraftServer server, boolean debug) {
        enabled = true;
        debugMode = debug;
        prepareStartPressurePlateCommands(server.getOverworld(), getStartCommand());
    }

    private static void tick(MinecraftServer server) {
        if (!enabled) {
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (START_PRESSURE_PLATE_REGION.contains(player.getX(), player.getY(), player.getZ())) {
                enabled = false;
                server.getCommandManager().executeWithPrefix(server.getCommandSource(), getStartCommand());
                return;
            }
        }
    }

    private static String getStartCommand() {
        return debugMode ? START_COMMAND + " debug" : START_COMMAND;
    }

    private static void prepareStartPressurePlateCommands(ServerWorld world, String startCommand) {
        for (BlockPos pos : START_COMMAND_BLOCK_POSITIONS) {
            world.getChunk(pos);
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!(blockEntity instanceof CommandBlockBlockEntity commandBlock)) {
                LOGGER.warn("Expected start command block at {}, but found {}.", pos, blockEntity);
                continue;
            }

            commandBlock.getCommandExecutor().setCommand(startCommand);
            commandBlock.markDirty();
            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, 3);
            LOGGER.info("Configured start command block at {} to run '{}'.", pos, startCommand);
        }
    }
}
