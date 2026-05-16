package jp.ac.u_tokyo.sdm.sdm_mod.story.phase5;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import jp.ac.u_tokyo.sdm.sdm_mod.ModSounds;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2DialogueVoiceService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4ZombieService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Phase5OnaraDialogueService {
    private static final String PHASE5_ID = "phase5";
    private static final String ONARA_WARNING_TEXT = "こんなときになんてデカいおならしてるんだ！！匂いに奴らが反応して集まってくるぞ！急いで図書館の外まで逃げるんだ！！";
    private static final int LINE_05_01_TICKS = 160;
    private static final long STAIR_ARROW_START_DELAY_TICKS = LINE_05_01_TICKS + 10L;
    private static final long ZOMBIE_SPAWN_DELAY_TICKS = 180L;
    private static final Map<UUID, Long> PENDING_ZOMBIE_SPAWN_TICKS = new HashMap<>();
    private static boolean zombiesSpawned;

    private Phase5OnaraDialogueService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase5OnaraDialogueService::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> clearState());
    }

    public static void start(ServerPlayerEntity player) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE5_ID) || zombiesSpawned) {
            return;
        }

        UUID playerId = player.getUuid();
        if (PENDING_ZOMBIE_SPAWN_TICKS.containsKey(playerId)) {
            return;
        }

        Phase2DialogueVoiceService.enqueue(
            player,
            "phase5_onara_warning",
            ONARA_WARNING_TEXT,
            ModSounds.PHASE5_LINE_05_01,
            LINE_05_01_TICKS,
            Phase2DialogueVoiceService.DeliveryMode.INTERRUPT
        );
        Phase5StairArrowService.schedule(player, STAIR_ARROW_START_DELAY_TICKS);
        PENDING_ZOMBIE_SPAWN_TICKS.put(playerId, player.getWorld().getTime() + ZOMBIE_SPAWN_DELAY_TICKS);
    }

    private static void tick(MinecraftServer server) {
        if (zombiesSpawned) {
            PENDING_ZOMBIE_SPAWN_TICKS.clear();
            return;
        }

        long currentTick = server.getOverworld().getTime();
        Iterator<Map.Entry<UUID, Long>> iterator = PENDING_ZOMBIE_SPAWN_TICKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (currentTick < entry.getValue()) {
                continue;
            }

            iterator.remove();
            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE5_ID)) {
                continue;
            }

            zombiesSpawned = true;
            Phase4ZombieService.spawnPhase4Zombies(server.getOverworld());
            PENDING_ZOMBIE_SPAWN_TICKS.clear();
            return;
        }
    }

    private static void clearState() {
        PENDING_ZOMBIE_SPAWN_TICKS.clear();
        zombiesSpawned = false;
    }
}
