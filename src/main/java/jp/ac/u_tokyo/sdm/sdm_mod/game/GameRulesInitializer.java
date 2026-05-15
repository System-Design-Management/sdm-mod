package jp.ac.u_tokyo.sdm.sdm_mod.game;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;

import java.util.ArrayList;
import java.util.List;

public final class GameRulesInitializer {
    private static final long STORY_NIGHT_TIME = 18000L;
    private static final long SETUP_DAY_TIME = 6000L;

    private GameRulesInitializer() {
    }

    public static void applySetupDefaults(MinecraftServer server) {
        server.setDifficulty(Difficulty.PEACEFUL, true);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        for (ServerWorld world : server.getWorlds()) {
            world.setTimeOfDay(SETUP_DAY_TIME);
            clearHostileMobs(world);
        }
    }

    private static void clearHostileMobs(ServerWorld world) {
        List<Entity> toRemove = new ArrayList<>();
        world.iterateEntities().forEach(entity -> {
            if (entity instanceof HostileEntity) {
                toRemove.add(entity);
            }
        });
        toRemove.forEach(Entity::discard);
    }

    public static void applyStoryDefaults(MinecraftServer server) {
        server.setDifficulty(Difficulty.HARD, true);
        server.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);
        server.getGameRules().get(GameRules.LOG_ADMIN_COMMANDS).set(false, server);
        server.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        // Natural mob spawning must stay off once the story starts.
        server.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
        server.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, server);
        server.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK).set(false, server);

        for (ServerWorld world : server.getWorlds()) {
            world.setTimeOfDay(STORY_NIGHT_TIME);
            world.setWeather(Integer.MAX_VALUE, 0, false, false);
        }
    }
}
