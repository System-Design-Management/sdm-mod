package jp.ac.u_tokyo.sdm.sdm_mod.story.phase5;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.DoorArrowPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Phase5StairArrowService {
    private static final String PHASE5_ID = "phase5";
    private static final double STAIR_X = -160.5;
    private static final double STAIR_Z = -663.0;
    private static final double GUIDE_MIN_VISIBLE_Y = 40.0;
    private static final Map<UUID, Long> PENDING_START_TICKS = new HashMap<>();
    private static final Set<UUID> ACTIVE_GUIDES = new HashSet<>();

    private Phase5StairArrowService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase5StairArrowService::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> clearState());
    }

    public static void schedule(ServerPlayerEntity player, long delayTicks) {
        PENDING_START_TICKS.put(player.getUuid(), player.getWorld().getTime() + delayTicks);
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        boolean phase5Active = storyManager.isActive() && storyManager.isAtChapter(PHASE5_ID);

        if (!phase5Active) {
            hideAll(server);
            clearState();
            return;
        }

        long currentTick = server.getOverworld().getTime();
        Iterator<Map.Entry<UUID, Long>> pendingIterator = PENDING_START_TICKS.entrySet().iterator();
        while (pendingIterator.hasNext()) {
            Map.Entry<UUID, Long> entry = pendingIterator.next();
            if (currentTick < entry.getValue()) {
                continue;
            }

            pendingIterator.remove();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player == null || player.getY() < GUIDE_MIN_VISIBLE_Y) {
                continue;
            }

            ACTIVE_GUIDES.add(entry.getKey());
            show(player);
        }

        Iterator<UUID> activeIterator = ACTIVE_GUIDES.iterator();
        while (activeIterator.hasNext()) {
            UUID playerId = activeIterator.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player == null) {
                activeIterator.remove();
                continue;
            }

            if (player.getY() < GUIDE_MIN_VISIBLE_Y) {
                hide(player);
                activeIterator.remove();
            }
        }
    }

    private static void show(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, DoorArrowPayload.forTarget(true, STAIR_X, STAIR_Z, GUIDE_MIN_VISIBLE_Y));
    }

    private static void hide(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, DoorArrowPayload.forTarget(false, STAIR_X, STAIR_Z, GUIDE_MIN_VISIBLE_Y));
    }

    private static void hideAll(MinecraftServer server) {
        for (UUID playerId : ACTIVE_GUIDES) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player != null) {
                hide(player);
            }
        }
    }

    private static void clearState() {
        PENDING_START_TICKS.clear();
        ACTIVE_GUIDES.clear();
    }
}
