package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import java.util.HashMap;
import java.util.List;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class Phase2TutorialGateService {
    private static final String PHASE2_ID = "phase2";
    private static final long NOTIFICATION_COOLDOWN_TICKS = 20L;
    private static final Vec3d FALLBACK_SAFE_POS = new Vec3d(-160.5D, 28.0D, -625.0D);
    private static final List<GateRegion> BLOCKED_REGIONS = List.of(
        GateRegion.create("gate-1", -149, -148, 28, 29, -621, -621),
        GateRegion.create("gate-2", -155, -154, 28, 29, -621, -621),
        GateRegion.create("gate-3", -161, -160, 28, 29, -621, -621),
        GateRegion.create("gate-4", -167, -166, 28, 29, -621, -621),
        GateRegion.create("gate-5", -173, -172, 28, 29, -621, -621)
    );
    private static final Map<UUID, SafePosition> LAST_SAFE_POSITIONS = new HashMap<>();
    private static final Map<UUID, Long> LAST_NOTIFICATION_TICKS = new HashMap<>();

    private Phase2TutorialGateService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase2TutorialGateService::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> clearState());
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE2_ID) || !Phase2TutorialZombieService.isTutorialZombieAlive(server)) {
            clearState();
            return;
        }

        ServerWorld fallbackWorld = server.getOverworld();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            handlePlayer(player, fallbackWorld);
        }
    }

    private static void handlePlayer(ServerPlayerEntity player, ServerWorld fallbackWorld) {
        BlockPos currentBlockPos = player.getBlockPos();
        GateRegion region = findBlockedRegion(currentBlockPos);
        if (region == null) {
            LAST_SAFE_POSITIONS.put(player.getUuid(), SafePosition.fromPlayer(player));
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
        notifyBlocked(player, region);
    }

    private static GateRegion findBlockedRegion(BlockPos pos) {
        for (GateRegion region : BLOCKED_REGIONS) {
            if (region.contains(pos)) {
                return region;
            }
        }

        return null;
    }

    private static void notifyBlocked(ServerPlayerEntity player, GateRegion region) {
        long currentTick = player.getWorld().getTime();
        Long lastNotificationTick = LAST_NOTIFICATION_TICKS.get(player.getUuid());
        if (lastNotificationTick != null && currentTick - lastNotificationTick < NOTIFICATION_COOLDOWN_TICKS) {
            return;
        }

        LAST_NOTIFICATION_TICKS.put(player.getUuid(), currentTick);
        Phase2TutorialDialogueService.handleBlockedAdvance(player);
    }

    private static void clearState() {
        LAST_SAFE_POSITIONS.clear();
        LAST_NOTIFICATION_TICKS.clear();
    }

    private record SafePosition(ServerWorld world, Vec3d pos) {
        private static SafePosition fromPlayer(ServerPlayerEntity player) {
            return new SafePosition((ServerWorld) player.getWorld(), player.getPos());
        }
    }

    private record GateRegion(String id, BlockPos cornerA, BlockPos cornerB) {
        private static GateRegion create(String id, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
            return new GateRegion(id, new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
        }

        private boolean contains(BlockPos pos) {
            return pos.getX() >= Math.min(cornerA.getX(), cornerB.getX())
                && pos.getX() <= Math.max(cornerA.getX(), cornerB.getX())
                && pos.getY() >= Math.min(cornerA.getY(), cornerB.getY())
                && pos.getY() <= Math.max(cornerA.getY(), cornerB.getY())
                && pos.getZ() >= Math.min(cornerA.getZ(), cornerB.getZ())
                && pos.getZ() <= Math.max(cornerA.getZ(), cornerB.getZ());
        }
    }
}
