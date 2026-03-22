package jp.ac.u_tokyo.sdm.sdm_mod.game;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;

public final class GameRulesInitializer {
    private GameRulesInitializer() {
    }

    public static void applyStoryDefaults(MinecraftServer server) {
        server.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);
    }
}
