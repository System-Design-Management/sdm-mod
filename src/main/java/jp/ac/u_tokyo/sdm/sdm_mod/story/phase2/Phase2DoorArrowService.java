package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.DoorArrowPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.TeacherDialogueService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Phase2DoorArrowService {
    private static final String PHASE2_ID = "phase2";
    private static final String PC_USED_TEXT = "本は3階の大閲覧室にあるみたいだな。階段を登っていこう。";
    private static final String THIRD_FLOOR_ARRIVED_TEXT1 = "3階に着いたようだな。右上の矢印の方向に進むと大閲覧室のドアが見えてくるぞ。";
    private static final String THIRD_FLOOR_ARRIVED_TEXT2 = "他のドアは壊れているから開かないかもしれない。いいか、本棚の陰から奴らが飛び出してくるかもしれない。もし襲われたら……躊躇するな、戦え！";
    // 1つ目のセリフ（38文字）の表示 + 自動消去（40tick）に余裕を持たせた遅延
    private static final long SECOND_DIALOGUE_DELAY_TICKS = 85L;
    private static final int THIRD_FLOOR_MIN_Y = 41;
    private static final Set<UUID> LOCATION_SCREEN_VIEWERS = new HashSet<>();
    private static final Set<UUID> THIRD_FLOOR_REACHED_AFTER_MAP = new HashSet<>();
    private static final Map<UUID, Boolean> LAST_ARROW_VISIBILITY = new HashMap<>();
    private static final Map<UUID, Boolean> LAST_THIRD_FLOOR_STATE = new HashMap<>();
    private static final Map<UUID, Long> PENDING_SECOND_DIALOGUE_TICK = new HashMap<>();

    private Phase2DoorArrowService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase2DoorArrowService::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> clearState());
    }

    public static void recordLocationScreenOpened(ServerPlayerEntity player) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE2_ID)) {
            return;
        }

        if (LOCATION_SCREEN_VIEWERS.add(player.getUuid())) {
            TeacherDialogueService.showAsHud(player, PC_USED_TEXT);
        }
    }

    public static void resetProgress() {
        clearState();
    }

    private static void tick(MinecraftServer server) {
        StoryManager sm = StoryModule.getStoryManager();
        long currentTick = server.getOverworld().getTime();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            trackThirdFloorArrival(player, currentTick);
            boolean visible = sm.isActive()
                && sm.isAtChapter(PHASE2_ID)
                && THIRD_FLOOR_REACHED_AFTER_MAP.contains(player.getUuid())
                && isOnThirdFloor(player);
            updateArrowVisibility(player, visible, currentTick);

            Long pendingTick = PENDING_SECOND_DIALOGUE_TICK.get(player.getUuid());
            if (pendingTick != null && currentTick >= pendingTick) {
                PENDING_SECOND_DIALOGUE_TICK.remove(player.getUuid());
                TeacherDialogueService.showAsHud(player, THIRD_FLOOR_ARRIVED_TEXT2);
            }
        }
    }

    private static void trackThirdFloorArrival(ServerPlayerEntity player, long currentTick) {
        UUID playerId = player.getUuid();
        boolean onThirdFloor = isOnThirdFloor(player);
        boolean wasOnThirdFloor = LAST_THIRD_FLOOR_STATE.getOrDefault(playerId, onThirdFloor);
        if (!wasOnThirdFloor && onThirdFloor && LOCATION_SCREEN_VIEWERS.contains(playerId)
                && !THIRD_FLOOR_REACHED_AFTER_MAP.contains(playerId)) {
            THIRD_FLOOR_REACHED_AFTER_MAP.add(playerId);
            TeacherDialogueService.showAsHud(player, THIRD_FLOOR_ARRIVED_TEXT1);
            PENDING_SECOND_DIALOGUE_TICK.put(playerId, currentTick + SECOND_DIALOGUE_DELAY_TICKS);
        }

        LAST_THIRD_FLOOR_STATE.put(playerId, onThirdFloor);
    }

    private static boolean isOnThirdFloor(ServerPlayerEntity player) {
        return player.getY() >= THIRD_FLOOR_MIN_Y;
    }

    private static void updateArrowVisibility(ServerPlayerEntity player, boolean visible, long currentTick) {
        UUID playerId = player.getUuid();
        Boolean lastVisible = LAST_ARROW_VISIBILITY.get(playerId);
        if (lastVisible != null && lastVisible == visible) {
            return;
        }

        LAST_ARROW_VISIBILITY.put(playerId, visible);
        ServerPlayNetworking.send(player, new DoorArrowPayload(visible));
    }

    private static void clearState() {
        LOCATION_SCREEN_VIEWERS.clear();
        THIRD_FLOOR_REACHED_AFTER_MAP.clear();
        LAST_ARROW_VISIBILITY.clear();
        LAST_THIRD_FLOOR_STATE.clear();
        PENDING_SECOND_DIALOGUE_TICK.clear();
    }
}
