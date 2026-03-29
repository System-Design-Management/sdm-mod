package jp.ac.u_tokyo.sdm.sdm_mod.game;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;

public final class GameRulesInitializer {
    private static final long STORY_NIGHT_TIME = 18000L;

    private GameRulesInitializer() {
    }

    public static void applyStoryDefaults(MinecraftServer server) {
        server.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        // Natural mob spawning must stay off once the story starts.
        server.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
        server.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, server);

        for (ServerWorld world : server.getWorlds()) {
            world.setTimeOfDay(STORY_NIGHT_TIME);
            world.setWeather(Integer.MAX_VALUE, 0, false, false);
        }
    }
}
