package jp.ac.u_tokyo.sdm.sdm_mod.client.video;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

final class VideoSoundSilencer {
    private static final Identifier DEAD_PEOPLE_VOICE_ID = Identifier.of(SdmMod.MOD_ID, "dead_people_voice");

    private VideoSoundSilencer() {
    }

    static void silenceStoryNoise() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.getSoundManager().stopSounds(null, SoundCategory.HOSTILE);
        client.getSoundManager().stopSounds(DEAD_PEOPLE_VOICE_ID, null);
    }
}
