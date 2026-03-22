package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.game.GameRulesInitializer;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.state.StoryProgress;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

public final class StoryStartService {
    private StoryStartService() {
    }

    public static StoryProgress start(MinecraftServer server) {
        GameRulesInitializer.applyStoryDefaults(server);
        server.getPlayerManager().getPlayerList().forEach(StoryStartService::resetPlayerState);
        server.getPlayerManager().getPlayerList().forEach(StoryStartService::preparePlayerForStory);
        executeServerCommand(server, "function thepa:give/revolver with @a");
        executeServerCommand(server, "function thepa:give/bullets with @a");

        StoryManager storyManager = StoryModule.getStoryManager();
        storyManager.reset();
        return storyManager.getProgress();
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
        player.changeGameMode(GameMode.ADVENTURE);

        // TODO: ストーリー開始地点の座標が確定したら、ここでテレポートする。
    }

    private static void executeServerCommand(MinecraftServer server, String command) {
        ServerCommandSource commandSource = server.getCommandSource().withLevel(2);
        server.getCommandManager().executeWithPrefix(commandSource, command);
    }
}
