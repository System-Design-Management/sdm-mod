package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import jp.ac.u_tokyo.sdm.sdm_mod.ModSounds;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class Phase2TutorialDialogueService {
    private static final String PHASE2_ID = "phase2";
    private static final Identifier REVOLVER_MODEL_ID = Identifier.of("minecraft", "guns/revolver");
    private static final String GZ_DATA_KEY = "gz_data";
    private static final String BULLETS_KEY = "bullets";
    private static final long INTRO_LOCK_TICKS = 20L;
    private static final long FOLLOW_UP_DELAY_TICKS = 120L;
    private static final long POST_GUN_DELAY_TICKS = 20L;
    private static final String INTRO_TEXT = "聞こえるか。落ち着いて周囲を見ろ。";
    private static final String GUN_PICKUP_TEXT = "警官が倒れている!? 拳銃を持っているかもしれない。使わせてもらおう。";
    private static final String RELOAD_GUN_TEXT = "受け取ったな。しゃがみながら使って、まずは弾を込めろ。";
    private static final String FIRE_GUN_TEXT = "そうだ。弾は入った。図書館の前にゾンビがいるな。使って倒してみろ。";
    private static final String BLOCKED_BEFORE_GUN_TEXT = "焦るな。まずは警官のところへ行って武器を受け取れ。";
    private static final String BLOCKED_BEFORE_RELOAD_TEXT = "落ち着け。しゃがみながら使って、先に弾を込めるんだ。";
    private static final String BLOCKED_BEFORE_FIRST_SHOT_TEXT = "弾は入ったはずだ。そのまま使ってみろ。";
    private static final String BLOCKED_BEFORE_KILL_TEXT = "まだ先を急ぐな。図書館前のゾンビを片づけてから進め。";
    private static final String KILL_CONFIRMED_TEXT = "よし。その調子だ。学生証を持って中に入るぞ！";
    private static final String EXIT_GUIDE_TEXT = "準備はできた。先へ進め。";
    private static final String GATE_PASSED_TEXT = "まずは目当ての本がどこにあるか調べよう。右側にあるパソコンが使えそうだ。……画面に集中しすぎて、背後を取られないように気をつけろよ！";
    private static final String GATE_PASSED_TEXT2 = "ゾンビと入力して検索すれば、本がヒットするはずだ。場所を確認してくれ。";
    // GATE_PASSED_TEXT（66文字）の表示 + 自動消去（40tick）に余裕を持たせた遅延
    private static final long GATE_PASSED_FOLLOW_UP_DELAY_TICKS = 110L;
    private static final double GATE_PASSED_Z_THRESHOLD = -634.0;
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
        if (progress.stage.ordinal() >= DialogueStage.WAITING_FOR_RELOAD.ordinal()) {
            return;
        }

        progress.phase2Started = true;
        progress.stage = DialogueStage.WAITING_FOR_RELOAD;
        progress.lastKnownBullets = findRevolverBullets(player);
        schedule(
            progress,
            DialogueCue.POST_GUN_INSTRUCTION,
            player.getWorld().getTime() + POST_GUN_DELAY_TICKS
        );
    }

    public static void handleBlockedAdvance(ServerPlayerEntity player) {
        DialogueProgress progress = PROGRESS.get(player.getUuid());
        if (progress == null) {
            Phase2DialogueVoiceService.showAsHud(player, BLOCKED_BEFORE_GUN_TEXT, ModSounds.PHASE2_LINE_02_06);
            return;
        }

        if (progress.stage == DialogueStage.WAITING_FOR_GUN_PICKUP) {
            Phase2DialogueVoiceService.showAsHud(player, BLOCKED_BEFORE_GUN_TEXT, ModSounds.PHASE2_LINE_02_06);
            return;
        }

        if (progress.stage == DialogueStage.WAITING_FOR_RELOAD) {
            Phase2DialogueVoiceService.showAsHud(player, BLOCKED_BEFORE_RELOAD_TEXT, ModSounds.PHASE2_LINE_02_07);
            return;
        }

        if (progress.stage == DialogueStage.WAITING_FOR_FIRST_SHOT) {
            Phase2DialogueVoiceService.showAsHud(player, BLOCKED_BEFORE_FIRST_SHOT_TEXT, ModSounds.PHASE2_LINE_02_08);
            return;
        }

        if (progress.stage == DialogueStage.WAITING_FOR_TUTORIAL_KILL) {
            Phase2DialogueVoiceService.showAsHud(player, BLOCKED_BEFORE_KILL_TEXT, ModSounds.PHASE2_LINE_02_09);
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
            Phase2DialogueVoiceService.showAsHud(player, KILL_CONFIRMED_TEXT, ModSounds.PHASE2_LINE_02_10);
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
                Phase2DialogueVoiceService.showAsHud(player, INTRO_TEXT, ModSounds.PHASE2_LINE_02_01);
                schedule(progress, DialogueCue.GUN_PICKUP_PROMPT, currentTick + FOLLOW_UP_DELAY_TICKS);
            }

            if (!progress.gatePassedDialogueShown && player.getZ() < GATE_PASSED_Z_THRESHOLD) {
                progress.gatePassedDialogueShown = true;
                Phase2DialogueVoiceService.showAsHud(player, GATE_PASSED_TEXT, ModSounds.PHASE2_LINE_02_11);
                progress.gatePassedFollowUpTick = currentTick + GATE_PASSED_FOLLOW_UP_DELAY_TICKS;
            }

            if (progress.gatePassedFollowUpTick > 0 && currentTick >= progress.gatePassedFollowUpTick) {
                progress.gatePassedFollowUpTick = 0;
                Phase2DialogueVoiceService.showAsHud(player, GATE_PASSED_TEXT2, ModSounds.PHASE2_LINE_02_12);
            }

            trackWeaponProgress(player, progress);
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
                Phase2DialogueVoiceService.showAsHud(player, GUN_PICKUP_TEXT, ModSounds.PHASE2_LINE_02_02);
            }
            case POST_GUN_INSTRUCTION -> {
                if (progress.stage != DialogueStage.WAITING_FOR_RELOAD) {
                    return;
                }
                Phase2DialogueVoiceService.showAsHud(player, RELOAD_GUN_TEXT, ModSounds.PHASE2_LINE_02_03);
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

    private static void trackWeaponProgress(ServerPlayerEntity player, DialogueProgress progress) {
        int bullets = findRevolverBullets(player);
        switch (progress.stage) {
            case WAITING_FOR_RELOAD -> {
                if (bullets > 0) {
                    progress.stage = DialogueStage.WAITING_FOR_FIRST_SHOT;
                    progress.lastKnownBullets = bullets;
                    progress.pendingCue = null;
                    progress.pendingTick = Long.MAX_VALUE;
                    Phase2DialogueVoiceService.showAsHud(player, FIRE_GUN_TEXT, ModSounds.PHASE2_LINE_02_05);
                    return;
                }
                progress.lastKnownBullets = bullets;
            }
            case WAITING_FOR_FIRST_SHOT -> {
                if (progress.lastKnownBullets > bullets && bullets >= 0) {
                    progress.stage = DialogueStage.WAITING_FOR_TUTORIAL_KILL;
                }
                progress.lastKnownBullets = bullets;
            }
            case WAITING_FOR_TUTORIAL_KILL -> progress.lastKnownBullets = bullets;
            case WAITING_FOR_GUN_PICKUP, READY_TO_EXIT -> {
            }
        }
    }

    private static int findRevolverBullets(ServerPlayerEntity player) {
        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            int bullets = getRevolverBullets(player.getInventory().getStack(slot));
            if (bullets >= 0) {
                return bullets;
            }
        }

        return -1;
    }

    private static int getRevolverBullets(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }

        Identifier itemModel = stack.get(DataComponentTypes.ITEM_MODEL);
        if (!REVOLVER_MODEL_ID.equals(itemModel)) {
            return -1;
        }

        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) {
            return -1;
        }

        NbtCompound root = customData.copyNbt();
        NbtElement gzDataElement = root.get(GZ_DATA_KEY);
        if (!(gzDataElement instanceof NbtCompound gzData)) {
            return -1;
        }

        NbtElement bulletsElement = gzData.get(BULLETS_KEY);
        if (!(bulletsElement instanceof AbstractNbtNumber number)) {
            return -1;
        }

        return number.intValue();
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
        WAITING_FOR_RELOAD,
        WAITING_FOR_FIRST_SHOT,
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
        private int lastKnownBullets = -1;
        private boolean gatePassedDialogueShown;
        private long gatePassedFollowUpTick = 0;
    }
}
