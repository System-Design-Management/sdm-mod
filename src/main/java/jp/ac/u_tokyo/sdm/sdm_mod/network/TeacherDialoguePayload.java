package jp.ac.u_tokyo.sdm.sdm_mod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TeacherDialoguePayload(String text) implements CustomPayload {
    public static final CustomPayload.Id<TeacherDialoguePayload> ID =
        new CustomPayload.Id<>(Identifier.of("sdm_mod", "teacher_dialogue"));

    // text フィールドをバイト列に書き込む/読み込む方法を定義する。
    // Fabric のパケット登録に CODEC が必須。
    public static final PacketCodec<RegistryByteBuf, TeacherDialoguePayload> CODEC =
        PacketCodec.ofStatic(
            (buf, payload) -> buf.writeString(payload.text()),
            buf -> new TeacherDialoguePayload(buf.readString())
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
