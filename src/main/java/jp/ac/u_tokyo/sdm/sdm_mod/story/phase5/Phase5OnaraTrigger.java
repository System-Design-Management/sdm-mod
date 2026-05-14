package jp.ac.u_tokyo.sdm.sdm_mod.story.phase5;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class Phase5OnaraTrigger {
    private static final String PHASE5_ID = "phase5";
    private static final double TRIGGER_Z_MIN = -641.0;
    private static final double TRIGGER_Z_MAX = -640.0;
    private static boolean triggered = false;

    private Phase5OnaraTrigger() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase5OnaraTrigger::tick);
    }

    public static void reset() {
        triggered = false;
    }

    private static void tick(MinecraftServer server) {
        if (triggered) {
            return;
        }

        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE5_ID)) {
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            double z = player.getZ();
            if (z >= TRIGGER_Z_MIN && z <= TRIGGER_Z_MAX) {
                triggered = true;
                ServerPlayNetworking.send(player, new Phase5OnaraPayload());
                return;
            }
        }
    }
}
