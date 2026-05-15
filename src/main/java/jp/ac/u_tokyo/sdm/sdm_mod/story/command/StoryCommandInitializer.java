package jp.ac.u_tokyo.sdm.sdm_mod.story.command;

import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.context.CommandContext;
import jp.ac.u_tokyo.sdm.sdm_mod.ModEntities;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.SdmLogoEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.SetupGuideHudPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowOpVideoPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryAutoStartService;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public final class StoryCommandInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoryCommandInitializer.class);
    private static final double SETUP_X = -93.0;
    private static final double SETUP_Y = 24.0;
    private static final double SETUP_Z = -451.0;
    private static final LogoPlacement[] SDM_LOGO_PLACEMENTS = {
        new LogoPlacement(-89.0, 27.0, -456.0, 45.0f),
        new LogoPlacement(-88.0, 27.0, -444.0, 135.0f)
    };

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
                    player.teleport(world, SETUP_X, SETUP_Y, SETUP_Z, Set.<PositionFlag>of(), player.getYaw(), player.getPitch(), false);
                });
            spawnSdmLogo(context.getSource().getServer().getOverworld());
            StoryAutoStartService.enable();
            context.getSource().getServer().getPlayerManager().getPlayerList()
                .forEach(player -> ServerPlayNetworking.send(player, new SetupGuideHudPayload(true)));
            context.getSource().sendFeedback(
                () -> Text.literal("Setup complete: inventory cleared, adventure mode, teleported to (-93, 24, -451)."),
                true
            );
            return 1;
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to execute setup.", exception);
            return 0;
        }
    }

    private static void spawnSdmLogo(ServerWorld world) {
        world.iterateEntities().forEach(entity -> {
            if (entity instanceof SdmLogoEntity) {
                entity.discard();
            }
        });

        for (LogoPlacement placement : SDM_LOGO_PLACEMENTS) {
            ChunkPos logoChunk = new ChunkPos(BlockPos.ofFloored(placement.x(), placement.y(), placement.z()));
            world.getChunk(logoChunk.x, logoChunk.z);

            SdmLogoEntity logo = new SdmLogoEntity(ModEntities.SDM_LOGO, world);
            logo.setPosition(placement.x(), placement.y(), placement.z());
            logo.setYaw(placement.yaw());
            world.spawnEntity(logo);
        }
    }

    private record LogoPlacement(double x, double y, double z, float yaw) {
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
