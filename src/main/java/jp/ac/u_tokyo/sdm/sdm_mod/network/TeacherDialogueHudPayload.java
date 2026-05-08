package jp.ac.u_tokyo.sdm.sdm_mod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TeacherDialogueHudPayload(String text) implements CustomPayload {
    public static final CustomPayload.Id<TeacherDialogueHudPayload> ID =
        new CustomPayload.Id<>(Identifier.of("sdm_mod", "teacher_dialogue_hud"));

    public static final PacketCodec<RegistryByteBuf, TeacherDialogueHudPayload> CODEC =
        PacketCodec.ofStatic(
            (buf, payload) -> buf.writeString(payload.text()),
            buf -> new TeacherDialogueHudPayload(buf.readString())
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
