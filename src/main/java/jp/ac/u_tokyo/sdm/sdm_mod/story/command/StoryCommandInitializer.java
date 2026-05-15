package jp.ac.u_tokyo.sdm.sdm_mod.story.command;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.context.CommandContext;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowOpVideoPayload;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public final class StoryCommandInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoryCommandInitializer.class);

    private StoryCommandInitializer() {
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
            literal("sdm_story")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("start")
                    .executes(StoryCommandInitializer::executeStart))
                .then(literal("setup")
                    .executes(StoryCommandInitializer::executeSetup))
        ));
    }

    private static int executeSetup(CommandContext<ServerCommandSource> context) {
        try {
            context.getSource().getServer().getPlayerManager().getPlayerList()
                .forEach(player -> {
                    player.getInventory().clear();
                    player.changeGameMode(GameMode.ADVENTURE);
                    ServerWorld world = (ServerWorld) player.getWorld();
                    player.teleport(world, -93, 24, -451, Set.<PositionFlag>of(), player.getYaw(), player.getPitch(), false);
                });
            return 1;
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to execute setup.", exception);
            return 0;
        }
    }

    private static int executeStart(CommandContext<ServerCommandSource> context) {
        try {
            // OP 動画をクライアントに表示させる。動画終了後にクライアントが StoryVideoStartPayload を送信し
            // サーバー側で StoryStartService.start() が呼ばれる。
            context.getSource().getServer().getPlayerManager().getPlayerList()
                .forEach(player -> ServerPlayNetworking.send(player, ShowOpVideoPayload.INSTANCE));
            return 1;
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to trigger OP video.", exception);
            return 0;
        }
    }
}
