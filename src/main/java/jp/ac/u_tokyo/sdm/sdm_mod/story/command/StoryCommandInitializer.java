package jp.ac.u_tokyo.sdm.sdm_mod.story.command;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.context.CommandContext;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryStartService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.state.StoryProgress;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
            StoryProgress progress = StoryStartService.start(context.getSource().getServer());
            context.getSource().sendFeedback(
                () -> Text.literal("Story started at chapter '" + progress.currentChapterId() + "'."),
                true
            );
            return 1;
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to start story.", exception);
            context.getSource().sendError(Text.literal("Failed to start story. Check the log for details."));
            return 0;
        }
    }
}
