package jp.ac.u_tokyo.sdm.sdm_mod.game;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class CommandPermissionInitializer {
    /** MODが独自に付与したOPのUUIDを追跡する。元々OPだったプレイヤーは含まない。 */
    private static final Set<UUID> MOD_GRANTED_OPS = new HashSet<>();

    private CommandPermissionInitializer() {
    }

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(CommandPermissionInitializer::grantCommandAccessToOnlinePlayers);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            grantCommandAccess(server, handler.getPlayer())
        );
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            MOD_GRANTED_OPS.clear();
            CommandLockState.unlock();
        });
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
        MOD_GRANTED_OPS.add(player.getUuid());
    }

    /** setup 実行後にMODが付与したOP権限を全員から剥奪する。 */
    public static void revokeModGrantedOps(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (MOD_GRANTED_OPS.remove(player.getUuid())) {
                server.getCommandManager().executeWithPrefix(
                    server.getCommandSource(),
                    "deop " + player.getName().getString()
                );
            }
        }
    }
}
