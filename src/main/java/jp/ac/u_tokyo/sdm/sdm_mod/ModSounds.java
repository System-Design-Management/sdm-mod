package jp.ac.u_tokyo.sdm.sdm_mod;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {
    public static final SoundEvent STUDENT_ID_GATE_ACCEPT = register("student_id_gate_accept");
    public static final SoundEvent STUDENT_ID_GATE_REJECT = register("student_id_gate_reject");
    public static final SoundEvent FIREWORK = register("firework");
    public static final SoundEvent ONARA = register("onara");
    public static final SoundEvent DEAD_PEOPLE_VOICE = register("dead_people_voice");
    public static final SoundEvent SCREAM = register("scream");
    public static final SoundEvent PHASE2_LINE_02_01 = register("phase2_line_02_01");
    public static final SoundEvent PHASE2_LINE_02_02 = register("phase2_line_02_02");
    public static final SoundEvent PHASE2_LINE_02_03 = register("phase2_line_02_03");
    public static final SoundEvent PHASE2_LINE_02_04 = register("phase2_line_02_04");
    public static final SoundEvent PHASE2_LINE_02_05 = register("phase2_line_02_05");
    public static final SoundEvent PHASE2_LINE_02_06 = register("phase2_line_02_06");
    public static final SoundEvent PHASE2_LINE_02_07 = register("phase2_line_02_07");
    public static final SoundEvent PHASE2_LINE_02_08 = register("phase2_line_02_08");
    public static final SoundEvent PHASE2_LINE_02_09 = register("phase2_line_02_09");
    public static final SoundEvent PHASE2_LINE_02_10 = register("phase2_line_02_10");
    public static final SoundEvent PHASE2_LINE_02_11 = register("phase2_line_02_11");
    public static final SoundEvent PHASE2_LINE_02_12 = register("phase2_line_02_12");
    public static final SoundEvent PHASE2_LINE_02_13 = register("phase2_line_02_13");
    public static final SoundEvent PHASE2_LINE_02_14 = register("phase2_line_02_14");
    public static final SoundEvent PHASE2_LINE_02_15 = register("phase2_line_02_15");
    public static final SoundEvent PHASE3_LINE_03_01 = register("phase3_line_03_01");
    public static final SoundEvent PHASE3_LINE_03_02 = register("phase3_line_03_02");
    public static final SoundEvent PHASE3_LINE_03_03 = register("phase3_line_03_03");
    public static final SoundEvent PHASE3_LINE_03_04 = register("phase3_line_03_04");

    private ModSounds() {
    }

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of(SdmMod.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {
    }
}
