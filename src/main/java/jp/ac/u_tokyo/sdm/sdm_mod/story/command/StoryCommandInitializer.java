package jp.ac.u_tokyo.sdm.sdm_mod.story.command;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.context.CommandContext;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowOpVideoPayload;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        ));
    }

    private static int executeStart(CommandContext<ServerCommandSource> context) {
        try {
            // OP 動画をクライアントに表示させる。動画終了後にクライアントが StoryVideoStartPayload を送信し
            // サーバー側で StoryStartService.start() が呼ばれる。
            context.getSource().getServer().getPlayerManager().getPlayerList()
                .forEach(player -> ServerPlayNetworking.send(player, ShowOpVideoPayload.INSTANCE));
            context.getSource().sendFeedback(
                () -> Text.literal("OP video triggered. Story will start after playback."),
                true
            );
            return 1;
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to trigger OP video.", exception);
            context.getSource().sendError(Text.literal("Failed to trigger OP video. Check the log for details."));
            return 0;
        }
    }
}
