package jp.ac.u_tokyo.sdm.sdm_mod.story.phase4;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record Phase4ProfessorDialoguePayload() implements CustomPayload {
    public static final CustomPayload.Id<Phase4ProfessorDialoguePayload> ID =
        new CustomPayload.Id<>(Identifier.of("sdm_mod", "phase4_professor_dialogue"));
    public static final PacketCodec<RegistryByteBuf, Phase4ProfessorDialoguePayload> CODEC =
        PacketCodec.ofStatic(
            (buf, payload) -> {},
            buf -> new Phase4ProfessorDialoguePayload()
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
