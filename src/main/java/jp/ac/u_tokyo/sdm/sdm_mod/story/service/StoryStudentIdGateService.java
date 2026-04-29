package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jp.ac.u_tokyo.sdm.sdm_mod.ModItems;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class StoryStudentIdGateService {
    private static final long NOTIFICATION_COOLDOWN_TICKS = 20L;
    private static final GateRegion STUDENT_ID_GATE_REGION = new GateRegion(
        new BlockPos(-161, 29, -634),
        new BlockPos(-160, 32, -632)
    );
    private static final Vec3d FALLBACK_SAFE_POS = new Vec3d(-159.5, 29.0, -633.0);
    private static final Map<UUID, SafePosition> LAST_SAFE_POSITIONS = new HashMap<>();
    private static final Map<UUID, Long> LAST_NOTIFICATION_TICKS = new HashMap<>();

    private StoryStudentIdGateService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(StoryStudentIdGateService::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LAST_SAFE_POSITIONS.clear();
            LAST_NOTIFICATION_TICKS.clear();
        });
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive()) {
            LAST_SAFE_POSITIONS.clear();
            LAST_NOTIFICATION_TICKS.clear();
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            handlePlayer(player);
        }
    }

    private static void handlePlayer(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        BlockPos currentBlockPos = player.getBlockPos();
        if (!STUDENT_ID_GATE_REGION.contains(currentBlockPos)) {
            LAST_SAFE_POSITIONS.put(playerId, SafePosition.fromPlayer(player));
            return;
        }

        if (player.getMainHandStack().isOf(ModItems.STUDENT_ID)) {
            return;
        }

        SafePosition safePosition = LAST_SAFE_POSITIONS.getOrDefault(
            playerId,
            new SafePosition((ServerWorld) player.getWorld(), FALLBACK_SAFE_POS)
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
        notifyMissingStudentId(player);
    }

    private static void notifyMissingStudentId(ServerPlayerEntity player) {
        long currentTick = player.getWorld().getTime();
        long lastNotificationTick = LAST_NOTIFICATION_TICKS.getOrDefault(player.getUuid(), Long.MIN_VALUE);
        if (currentTick - lastNotificationTick < NOTIFICATION_COOLDOWN_TICKS) {
            return;
        }

        LAST_NOTIFICATION_TICKS.put(player.getUuid(), currentTick);
        player.sendMessage(Text.literal("学生証を右手に持ってください。"), false);
    }

    private record GateRegion(BlockPos cornerA, BlockPos cornerB) {
        private boolean contains(BlockPos pos) {
            return pos.getX() >= Math.min(cornerA.getX(), cornerB.getX())
                && pos.getX() <= Math.max(cornerA.getX(), cornerB.getX())
                && pos.getY() >= Math.min(cornerA.getY(), cornerB.getY())
                && pos.getY() <= Math.max(cornerA.getY(), cornerB.getY())
                && pos.getZ() >= Math.min(cornerA.getZ(), cornerB.getZ())
                && pos.getZ() <= Math.max(cornerA.getZ(), cornerB.getZ());
        }
    }

    private record SafePosition(ServerWorld world, Vec3d pos) {
        private static SafePosition fromPlayer(ServerPlayerEntity player) {
            return new SafePosition((ServerWorld) player.getWorld(), player.getPos());
        }
    }
}
