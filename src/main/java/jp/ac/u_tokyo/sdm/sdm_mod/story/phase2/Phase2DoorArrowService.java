package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.network.DoorArrowPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class Phase2DoorArrowService {
    private static final String PHASE2_ID = "phase2";
    private static boolean wasInPhase2 = false;

    private Phase2DoorArrowService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase2DoorArrowService::tick);
    }

    private static void tick(MinecraftServer server) {
        StoryManager sm = StoryModule.getStoryManager();
        boolean inPhase2 = sm.isActive() && sm.isAtChapter(PHASE2_ID);

        if (inPhase2 == wasInPhase2) return;
        wasInPhase2 = inPhase2;

        DoorArrowPayload payload = new DoorArrowPayload(inPhase2);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}
// TODO: 「search_pcを使用した」かつ「3階にいる」の条件で矢印を出すようにする。