package jp.ac.u_tokyo.sdm.sdm_mod.game;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class CommandPermissionInitializer {
    private CommandPermissionInitializer() {
    }

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(CommandPermissionInitializer::grantCommandAccessToOnlinePlayers);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            grantCommandAccess(server, handler.getPlayer())
        );
    }

    private static void grantCommandAccessToOnlinePlayers(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            grantCommandAccess(server, player);
        }
    }

    private static void grantCommandAccess(MinecraftServer server, ServerPlayerEntity player) {
        GameProfile profile = player.getGameProfile();
        if (server.getPlayerManager().isOperator(profile)) {
            return;
        }

        // TODO: 管理者とそれ以外のプレイヤーでコマンド権限の付与内容を切り替える。
        server.getPlayerManager().addToOperators(profile);
    }
}
