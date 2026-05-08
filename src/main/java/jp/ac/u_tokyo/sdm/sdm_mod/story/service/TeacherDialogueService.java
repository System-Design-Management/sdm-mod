package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.network.TeacherDialogueHudPayload;
import jp.ac.u_tokyo.sdm.sdm_mod.network.TeacherDialoguePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TeacherDialogueService {
    private TeacherDialogueService() {
    }

    /**
     * 指定プレイヤーにダイアログ画面（Screen版）を表示する。
     * ゲームが一時停止し、プレイヤーが手動で閉じる必要がある。
     */
    public static void show(ServerPlayerEntity player, String text) {
        ServerPlayNetworking.send(player, new TeacherDialoguePayload(text));
    }

    /**
     * 指定プレイヤーにダイアログをHUDオーバーレイとして表示する。
     * ゲームは止まらず、全文表示後5秒で自動的に消える。
     */
    public static void showAsHud(ServerPlayerEntity player, String text) {
        ServerPlayNetworking.send(player, new TeacherDialogueHudPayload(text));
    }
}
