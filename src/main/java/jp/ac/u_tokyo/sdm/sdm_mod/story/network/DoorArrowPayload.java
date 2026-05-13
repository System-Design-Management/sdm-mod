package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DoorArrowPayload(boolean visible) implements CustomPayload {
    public static final CustomPayload.Id<DoorArrowPayload> ID =
        new CustomPayload.Id<>(Identifier.of("sdm_mod", "door_arrow"));

    public static final PacketCodec<RegistryByteBuf, DoorArrowPayload> CODEC =
        PacketCodec.ofStatic(
            (buf, payload) -> buf.writeBoolean(payload.visible()),
            buf -> new DoorArrowPayload(buf.readBoolean())
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
