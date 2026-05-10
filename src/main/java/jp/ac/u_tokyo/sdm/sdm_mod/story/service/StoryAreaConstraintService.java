package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public final class StoryAreaConstraintService {
    private static final double MIN_X = -211.0D;
    private static final double MAX_X = -104.0D;
    private static final double MIN_Y = 23.0D;
    private static final double MAX_Y = 77.0D;
    private static final double MIN_Z = -791.0D;
    private static final double MAX_Z = -593.0D;
    private static final Vec3d FALLBACK_SAFE_POS = new Vec3d(-160.5D, 25.0D, -599.0D);
    private static final long NOTIFICATION_COOLDOWN_TICKS = 20L;
    private static final Map<UUID, SafePosition> LAST_SAFE_POSITIONS = new HashMap<>();
    private static final Map<UUID, Long> LAST_NOTIFICATION_TICKS = new HashMap<>();

    private StoryAreaConstraintService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(StoryAreaConstraintService::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> clearState());
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive()) {
            clearState();
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            handlePlayer(player, server.getOverworld());
        }
    }

    private static void handlePlayer(ServerPlayerEntity player, ServerWorld fallbackWorld) {
        Vec3d currentPos = player.getPos();
        if (isInside(currentPos)) {
            LAST_SAFE_POSITIONS.put(player.getUuid(), new SafePosition((ServerWorld) player.getWorld(), currentPos));
            return;
        }

        SafePosition safePosition = LAST_SAFE_POSITIONS.getOrDefault(
            player.getUuid(),
            new SafePosition(fallbackWorld, FALLBACK_SAFE_POS)
        );
        player.teleport(
            safePosition.world(),
            safePosition.pos().x,
            safePosition.pos().y,
            safePosition.pos().z,
            Set.<PositionFlag>of(),
            player.getYaw(),
            player.getPitch(),
            false
        );
        notifyRepelled(player);
    }

    private static boolean isInside(Vec3d pos) {
        return pos.x >= MIN_X
            && pos.x <= MAX_X
            && pos.y >= MIN_Y
            && pos.y <= MAX_Y
            && pos.z >= MIN_Z
            && pos.z <= MAX_Z;
    }

    private static void notifyRepelled(ServerPlayerEntity player) {
        long currentTick = player.getWorld().getTime();
        Long lastNotificationTick = LAST_NOTIFICATION_TICKS.get(player.getUuid());
        if (lastNotificationTick != null && currentTick - lastNotificationTick < NOTIFICATION_COOLDOWN_TICKS) {
            return;
        }

        LAST_NOTIFICATION_TICKS.put(player.getUuid(), currentTick);
        player.sendMessage(Text.literal("ストーリーエリアの外には移動できません。"), true);
    }

    private static void clearState() {
        LAST_SAFE_POSITIONS.clear();
        LAST_NOTIFICATION_TICKS.clear();
    }

    private record SafePosition(ServerWorld world, Vec3d pos) {
    }
}
