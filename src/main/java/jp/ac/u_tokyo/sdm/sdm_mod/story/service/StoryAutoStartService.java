package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.story.network.ShowOpVideoPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;

public final class StoryAutoStartService {
    private static final Box TRIGGER_REGION = new Box(-55, 23, -451, -52, 26, -448);

    private static boolean enabled = false;

    private StoryAutoStartService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(StoryAutoStartService::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> enabled = false);
    }

    public static void enable() {
        enabled = true;
    }

    private static void tick(MinecraftServer server) {
        if (!enabled) {
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (TRIGGER_REGION.contains(player.getX(), player.getY(), player.getZ())) {
                enabled = false;
                server.getPlayerManager().getPlayerList()
                    .forEach(p -> ServerPlayNetworking.send(p, ShowOpVideoPayload.INSTANCE));
                return;
            }
        }
    }
}
