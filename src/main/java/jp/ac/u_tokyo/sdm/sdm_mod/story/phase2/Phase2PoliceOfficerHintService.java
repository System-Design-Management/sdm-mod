package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryPoliceOfficerService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class Phase2PoliceOfficerHintService {
    private static final String PHASE2_ID = "phase2";
    private static final double HINT_DISTANCE_SQUARED = 9.0D;
    private static final long HINT_COOLDOWN_TICKS = 40L;
    private static final Map<UUID, Long> LAST_HINT_TICKS = new HashMap<>();

    private Phase2PoliceOfficerHintService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase2PoliceOfficerHintService::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> LAST_HINT_TICKS.clear());
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE2_ID)) {
            LAST_HINT_TICKS.clear();
            return;
        }

        Entity policeOfficer = findManagedPoliceOfficer(server);
        if (policeOfficer == null) {
            return;
        }

        long currentTick = server.getOverworld().getTime();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (Phase2PoliceOfficerGunTrigger.hasReceivedGun(player)) {
                continue;
            }

            if (player.squaredDistanceTo(policeOfficer) > HINT_DISTANCE_SQUARED) {
                continue;
            }

            Long lastHintTick = LAST_HINT_TICKS.get(player.getUuid());
            if (lastHintTick != null && currentTick - lastHintTick < HINT_COOLDOWN_TICKS) {
                continue;
            }

            LAST_HINT_TICKS.put(player.getUuid(), currentTick);
            player.sendMessage(Text.literal("使用キーで警官から武器を受け取れ。"), true);
        }
    }

    private static Entity findManagedPoliceOfficer(MinecraftServer server) {
        for (net.minecraft.server.world.ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (StoryPoliceOfficerService.isManagedPoliceOfficer(entity)) {
                    return entity;
                }
            }
        }

        return null;
    }
}
