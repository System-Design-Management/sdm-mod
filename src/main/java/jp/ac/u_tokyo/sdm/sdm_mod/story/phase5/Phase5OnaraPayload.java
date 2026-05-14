package jp.ac.u_tokyo.sdm.sdm_mod.story.phase5;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record Phase5OnaraPayload() implements CustomPayload {
    public static final CustomPayload.Id<Phase5OnaraPayload> ID =
        new CustomPayload.Id<>(Identifier.of("sdm_mod", "phase5_onara"));
    public static final PacketCodec<RegistryByteBuf, Phase5OnaraPayload> CODEC =
        PacketCodec.ofStatic((buf, payload) -> {}, buf -> new Phase5OnaraPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
