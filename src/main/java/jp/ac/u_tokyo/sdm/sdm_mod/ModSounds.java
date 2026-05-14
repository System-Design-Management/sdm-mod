package jp.ac.u_tokyo.sdm.sdm_mod;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {
    public static final SoundEvent STUDENT_ID_GATE_ACCEPT = register("student_id_gate_accept");
    public static final SoundEvent STUDENT_ID_GATE_REJECT = register("student_id_gate_reject");
    public static final SoundEvent FIREWORK = register("firework");

    private ModSounds() {
    }

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of(SdmMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {
    }
}
