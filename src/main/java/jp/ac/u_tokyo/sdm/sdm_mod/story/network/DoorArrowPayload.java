package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record DoorArrowPayload(
    boolean visible,
    boolean customTarget,
    double targetX,
    double targetZ,
    double minVisibleY
) implements CustomPayload {
    public static final CustomPayload.Id<DoorArrowPayload> ID =
        new CustomPayload.Id<>(Identifier.of("sdm_mod", "door_arrow"));

    public DoorArrowPayload(boolean visible) {
        this(visible, false, 0.0, 0.0, 41.0);
    }

    public static DoorArrowPayload forTarget(boolean visible, double targetX, double targetZ, double minVisibleY) {
        return new DoorArrowPayload(visible, true, targetX, targetZ, minVisibleY);
    }

    public static final PacketCodec<RegistryByteBuf, DoorArrowPayload> CODEC =
        PacketCodec.ofStatic(
            (buf, payload) -> {
                buf.writeBoolean(payload.visible());
                buf.writeBoolean(payload.customTarget());
                buf.writeDouble(payload.targetX());
                buf.writeDouble(payload.targetZ());
                buf.writeDouble(payload.minVisibleY());
            },
            buf -> new DoorArrowPayload(
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
            )
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
