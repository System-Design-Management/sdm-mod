package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import jp.ac.u_tokyo.sdm.sdm_mod.story.service.TeacherDialogueService;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

final class Phase2DialogueVoiceService {
    private static final float VOLUME = 1.0F;
    private static final float PITCH = 1.0F;

    private Phase2DialogueVoiceService() {
    }

    static void showAsHud(ServerPlayerEntity player, String text, SoundEvent voice) {
        TeacherDialogueService.showAsHud(player, text);
        player.playSoundToPlayer(voice, SoundCategory.VOICE, VOLUME, PITCH);
    }
}
