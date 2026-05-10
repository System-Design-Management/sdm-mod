package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.TeacherDialogueService;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class Phase2TutorialDialogueService {
    private static final String PHASE2_ID = "phase2";
    private static final long INTRO_LOCK_TICKS = 20L;
    private static final long FOLLOW_UP_DELAY_TICKS = 120L;
    private static final long POST_GUN_DELAY_TICKS = 20L;
    private static final String INTRO_TEXT = "聞こえるか。落ち着いて周囲を見ろ。";
    private static final String GUN_PICKUP_TEXT = "そのままでは危険だ。警官から武器を受け取れ。";
    private static final String USE_GUN_TEXT = "受け取ったな。前方の敵で試せ。";
    private static final String BLOCKED_BEFORE_GUN_TEXT = "焦るな。まずは警官のところへ行って武器を受け取れ。";
    private static final String BLOCKED_BEFORE_KILL_TEXT = "まだ先を急ぐな。前方の敵を片づけてから進め。";
    private static final String KILL_CONFIRMED_TEXT = "よし。その調子だ。";
    private static final String EXIT_GUIDE_TEXT = "準備はできた。先へ進め。";
    private static final Map<UUID, DialogueProgress> PROGRESS = new HashMap<>();

    private Phase2TutorialDialogueService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase2TutorialDialogueService::tick);
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!Phase2TutorialZombieService.isManagedTutorialZombie(entity)) {
                return;
            }

            MinecraftServer server = entity.getServer();
            if (server == null) {
                return;
            }

            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE2_ID)) {
                return;
            }

            handleTutorialZombieKilled(server);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> clearState());
    }

    public static void start(MinecraftServer server) {
        long currentTick = server.getOverworld().getTime();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            DialogueProgress progress = PROGRESS.computeIfAbsent(player.getUuid(), ignored -> new DialogueProgress());
            if (progress.phase2Started) {
                continue;
            }

            progress.phase2Started = true;
            progress.stage = DialogueStage.WAITING_FOR_GUN_PICKUP;
            progress.lockedPos = player.getPos();
            progress.unlockTick = currentTick + INTRO_LOCK_TICKS;
        }
    }

    public static void handleGunPickup(ServerPlayerEntity player) {
        DialogueProgress progress = PROGRESS.computeIfAbsent(player.getUuid(), ignored -> new DialogueProgress());
        if (progress.stage.ordinal() >= DialogueStage.WAITING_FOR_TUTORIAL_KILL.ordinal()) {
            return;
        }

        progress.phase2Started = true;
        progress.stage = DialogueStage.WAITING_FOR_TUTORIAL_KILL;
        schedule(
            progress,
            DialogueCue.POST_GUN_INSTRUCTION,
            player.getWorld().getTime() + POST_GUN_DELAY_TICKS
        );
    }

    public static void handleBlockedAdvance(ServerPlayerEntity player) {
        DialogueProgress progress = PROGRESS.get(player.getUuid());
        if (progress == null) {
            TeacherDialogueService.showAsHud(player, BLOCKED_BEFORE_GUN_TEXT);
            return;
        }

        if (progress.stage == DialogueStage.WAITING_FOR_GUN_PICKUP) {
            TeacherDialogueService.showAsHud(player, BLOCKED_BEFORE_GUN_TEXT);
            return;
        }

        if (progress.stage == DialogueStage.WAITING_FOR_TUTORIAL_KILL) {
            TeacherDialogueService.showAsHud(player, BLOCKED_BEFORE_KILL_TEXT);
        }
    }

    private static void handleTutorialZombieKilled(MinecraftServer server) {
        long currentTick = server.getOverworld().getTime();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            DialogueProgress progress = PROGRESS.computeIfAbsent(player.getUuid(), ignored -> new DialogueProgress());
            if (progress.stage == DialogueStage.READY_TO_EXIT) {
                continue;
            }

            progress.phase2Started = true;
            progress.stage = DialogueStage.READY_TO_EXIT;
            TeacherDialogueService.showAsHud(player, KILL_CONFIRMED_TEXT);
            schedule(progress, DialogueCue.EXIT_GUIDE, currentTick + FOLLOW_UP_DELAY_TICKS);
        }
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE2_ID)) {
            clearState();
            return;
        }

        long currentTick = server.getOverworld().getTime();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            DialogueProgress progress = PROGRESS.get(player.getUuid());
            if (progress == null) {
                continue;
            }

            if (progress.lockedPos != null) {
                if (currentTick < progress.unlockTick) {
                    holdPlayerInPlace(player, progress.lockedPos);
                    continue;
                }

                progress.lockedPos = null;
                progress.unlockTick = Long.MAX_VALUE;
                TeacherDialogueService.showAsHud(player, INTRO_TEXT);
                schedule(progress, DialogueCue.GUN_PICKUP_PROMPT, currentTick + FOLLOW_UP_DELAY_TICKS);
            }

            if (progress.pendingCue == null || progress.pendingTick > currentTick) {
                continue;
            }

            DialogueCue cue = progress.pendingCue;
            progress.pendingCue = null;
            progress.pendingTick = Long.MAX_VALUE;
            deliverCue(player, progress, cue);
        }
    }

    private static void deliverCue(ServerPlayerEntity player, DialogueProgress progress, DialogueCue cue) {
        switch (cue) {
            case GUN_PICKUP_PROMPT -> {
                if (progress.stage != DialogueStage.WAITING_FOR_GUN_PICKUP) {
                    return;
                }
                TeacherDialogueService.showAsHud(player, GUN_PICKUP_TEXT);
            }
            case POST_GUN_INSTRUCTION -> {
                if (progress.stage != DialogueStage.WAITING_FOR_TUTORIAL_KILL) {
                    return;
                }
                TeacherDialogueService.showAsHud(player, USE_GUN_TEXT);
            }
            case EXIT_GUIDE -> {
                if (progress.stage != DialogueStage.READY_TO_EXIT) {
                    return;
                }
                TeacherDialogueService.showAsHud(player, EXIT_GUIDE_TEXT);
            }
        }
    }

    private static void schedule(DialogueProgress progress, DialogueCue cue, long tick) {
        progress.pendingCue = cue;
        progress.pendingTick = tick;
    }

    private static void holdPlayerInPlace(ServerPlayerEntity player, Vec3d lockedPos) {
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;
        if (player.squaredDistanceTo(lockedPos) > 0.0001D) {
            player.teleport(
                player.getWorld(),
                lockedPos.x,
                lockedPos.y,
                lockedPos.z,
                Set.<PositionFlag>of(),
                player.getYaw(),
                player.getPitch(),
                false
            );
        }
    }

    private static void clearState() {
        PROGRESS.clear();
    }

    private enum DialogueStage {
        WAITING_FOR_GUN_PICKUP,
        WAITING_FOR_TUTORIAL_KILL,
        READY_TO_EXIT
    }

    private enum DialogueCue {
        GUN_PICKUP_PROMPT,
        POST_GUN_INSTRUCTION,
        EXIT_GUIDE
    }

    private static final class DialogueProgress {
        private boolean phase2Started;
        private DialogueStage stage = DialogueStage.WAITING_FOR_GUN_PICKUP;
        private DialogueCue pendingCue;
        private long pendingTick = Long.MAX_VALUE;
        private Vec3d lockedPos;
        private long unlockTick = Long.MAX_VALUE;
    }
}
