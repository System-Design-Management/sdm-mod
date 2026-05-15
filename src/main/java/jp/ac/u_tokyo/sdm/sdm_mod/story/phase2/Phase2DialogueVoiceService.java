package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.TeacherDialogueService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

final class Phase2DialogueVoiceService {
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 1.0F;
    private static final int TEXT_DISMISS_TICKS = 30;
    private static final int MIN_DISPLAY_MARGIN_TICKS = 10;
    private static final Map<UUID, PlayerDialogueState> STATES = new HashMap<>();
    private static boolean initialized;

    private Phase2DialogueVoiceService() {
    }

    static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        ServerTickEvents.END_SERVER_TICK.register(Phase2DialogueVoiceService::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> STATES.clear());
    }

    static void enqueue(ServerPlayerEntity player, String key, String text, SoundEvent voice, int voiceTicks) {
        enqueue(player, key, text, voice, voiceTicks, DeliveryMode.QUEUE);
    }

    static void enqueue(
        ServerPlayerEntity player,
        String key,
        String text,
        SoundEvent voice,
        int voiceTicks,
        DeliveryMode mode
    ) {
        PlayerDialogueState state = STATES.computeIfAbsent(player.getUuid(), ignored -> new PlayerDialogueState());
        DialogueCue cue = new DialogueCue(key, text, voice, displayTicks(text, voiceTicks));

        if (mode == DeliveryMode.COALESCE && containsCue(state, key)) {
            return;
        }

        if (mode == DeliveryMode.REPLACE_PENDING) {
            removePending(state, key);
        }

        if (mode == DeliveryMode.INTERRUPT) {
            stopActiveSound(player, state);
            state.current = null;
            state.queue.clear();
        }

        state.queue.addLast(cue);
        startNextIfIdle(player, state, player.getWorld().getTime());
    }

    static void enqueueText(ServerPlayerEntity player, String key, String text) {
        enqueueText(player, key, text, DeliveryMode.QUEUE);
    }

    static void enqueueText(ServerPlayerEntity player, String key, String text, DeliveryMode mode) {
        PlayerDialogueState state = STATES.computeIfAbsent(player.getUuid(), ignored -> new PlayerDialogueState());
        DialogueCue cue = new DialogueCue(key, text, null, displayTicks(text, 0));

        if (mode == DeliveryMode.COALESCE && containsCue(state, key)) {
            return;
        }

        if (mode == DeliveryMode.REPLACE_PENDING) {
            removePending(state, key);
        }

        if (mode == DeliveryMode.INTERRUPT) {
            stopActiveSound(player, state);
            state.current = null;
            state.queue.clear();
        }

        state.queue.addLast(cue);
        startNextIfIdle(player, state, player.getWorld().getTime());
    }

    private static void tick(MinecraftServer server) {
        long currentTick = server.getOverworld().getTime();
        Iterator<Map.Entry<UUID, PlayerDialogueState>> iterator = STATES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PlayerDialogueState> entry = iterator.next();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }

            PlayerDialogueState state = entry.getValue();
            if (state.current != null && currentTick >= state.current.endTick()) {
                state.current = null;
            }

            startNextIfIdle(player, state, currentTick);
            if (state.current == null && state.queue.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private static void startNextIfIdle(ServerPlayerEntity player, PlayerDialogueState state, long currentTick) {
        if (state.current != null) {
            return;
        }

        DialogueCue cue = state.queue.pollFirst();
        if (cue == null) {
            return;
        }

        TeacherDialogueService.showAsHud(player, cue.text(), cue.displayTicks());
        if (cue.voice() != null) {
            player.playSoundToPlayer(cue.voice(), SoundCategory.VOICE, VOLUME, PITCH);
        }
        state.current = new ActiveDialogueCue(cue, currentTick + cue.displayTicks());
    }

    private static boolean containsCue(PlayerDialogueState state, String key) {
        if (state.current != null && state.current.cue().key().equals(key)) {
            return true;
        }

        for (DialogueCue cue : state.queue) {
            if (cue.key().equals(key)) {
                return true;
            }
        }

        return false;
    }

    private static void removePending(PlayerDialogueState state, String key) {
        state.queue.removeIf(cue -> cue.key().equals(key));
    }

    private static void stopActiveSound(ServerPlayerEntity player, PlayerDialogueState state) {
        if (state.current == null || state.current.cue().voice() == null) {
            return;
        }

        Identifier soundId = Registries.SOUND_EVENT.getId(state.current.cue().voice());
        player.networkHandler.sendPacket(new StopSoundS2CPacket(soundId, SoundCategory.VOICE));
    }

    private static int displayTicks(String text, int voiceTicks) {
        int textTicks = text.length() + TEXT_DISMISS_TICKS + MIN_DISPLAY_MARGIN_TICKS;
        int soundTicks = voiceTicks > 0 ? voiceTicks + MIN_DISPLAY_MARGIN_TICKS : 0;
        return Math.max(textTicks, soundTicks);
    }

    enum DeliveryMode {
        QUEUE,
        INTERRUPT,
        COALESCE,
        REPLACE_PENDING
    }

    private static final class PlayerDialogueState {
        private final Deque<DialogueCue> queue = new ArrayDeque<>();
        private ActiveDialogueCue current;
    }

    private record DialogueCue(String key, String text, SoundEvent voice, int displayTicks) {
    }

    private record ActiveDialogueCue(DialogueCue cue, long endTick) {
    }
}
