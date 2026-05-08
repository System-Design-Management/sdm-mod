package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.network.TeacherDialoguePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TeacherDialogueService {
    private TeacherDialogueService() {
    }

    /**
     * 指定プレイヤーにダイアログ画面を表示する。
     * ストーリーの任意のタイミングから呼び出せる。
     */
    public static void show(ServerPlayerEntity player, String text) {
        ServerPlayNetworking.send(player, new TeacherDialoguePayload(text));
    }
}
