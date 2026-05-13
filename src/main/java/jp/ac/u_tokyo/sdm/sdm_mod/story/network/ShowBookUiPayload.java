package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ShowBookUiPayload(String title, boolean isKeyBook) implements CustomPayload {
    public static final CustomPayload.Id<ShowBookUiPayload> ID =
        new CustomPayload.Id<>(Identifier.of("sdm_mod", "show_book_ui"));

    public static final PacketCodec<RegistryByteBuf, ShowBookUiPayload> CODEC =
        PacketCodec.ofStatic(
            (buf, payload) -> {
                buf.writeString(payload.title());
                buf.writeBoolean(payload.isKeyBook());
            },
            buf -> new ShowBookUiPayload(buf.readString(), buf.readBoolean())
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
