package jp.ac.u_tokyo.sdm.sdm_mod.story.phase4;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import jp.ac.u_tokyo.sdm.sdm_mod.ModSounds;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2DialogueVoiceService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Phase4IntroDialogueService {
    private static final String PHASE3_ID = "phase3";
    private static final String PHASE4_ID = "phase4";
    private static final String FOUND_TEXT = "それだ！！よく見つけた！！";
    private static final String FIREWORK_TEXT = "私が花火を打ち上げてやつらを部屋の隅におびきよせる。その間に部屋から出て、図書館の外に逃げろ！";
    private static final int LINE_04_01_TICKS = 60;
    private static final int LINE_04_02_TICKS = 140;
    private static final long INTRO_TOTAL_TICKS = 220L;
    private static final Map<UUID, Long> PENDING_ADVANCE_TICKS = new HashMap<>();

    private Phase4IntroDialogueService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase4IntroDialogueService::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> PENDING_ADVANCE_TICKS.clear());
    }

    public static void start(ServerPlayerEntity player) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE3_ID)) {
            return;
        }

        UUID playerId = player.getUuid();
        if (PENDING_ADVANCE_TICKS.containsKey(playerId)) {
            return;
        }

        Phase2DialogueVoiceService.enqueue(
            player,
            "phase4_intro_found",
            FOUND_TEXT,
            ModSounds.PHASE4_LINE_04_01,
            LINE_04_01_TICKS,
            Phase2DialogueVoiceService.DeliveryMode.INTERRUPT
        );
        Phase2DialogueVoiceService.enqueue(
            player,
            "phase4_intro_firework",
            FIREWORK_TEXT,
            ModSounds.PHASE4_LINE_04_02,
            LINE_04_02_TICKS
        );
        PENDING_ADVANCE_TICKS.put(playerId, player.getWorld().getTime() + INTRO_TOTAL_TICKS);
    }

    private static void tick(MinecraftServer server) {
        long currentTick = server.getOverworld().getTime();
        Iterator<Map.Entry<UUID, Long>> iterator = PENDING_ADVANCE_TICKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Long> entry = iterator.next();
            if (currentTick < entry.getValue()) {
                continue;
            }

            iterator.remove();
            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE3_ID)) {
                continue;
            }

            storyManager.advanceToChapter(PHASE4_ID);
            Phase4FireworkService.launchOnce(server);
            server.getPlayerManager().getPlayerList()
                .forEach(player -> ServerPlayNetworking.send(player, FireworkShakePayload.INSTANCE));
        }
    }
}
