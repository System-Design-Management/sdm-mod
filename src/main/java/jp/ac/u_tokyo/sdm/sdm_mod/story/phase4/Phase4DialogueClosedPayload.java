package jp.ac.u_tokyo.sdm.sdm_mod.story.phase4;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record Phase4DialogueClosedPayload() implements CustomPayload {
    public static final CustomPayload.Id<Phase4DialogueClosedPayload> ID =
        new CustomPayload.Id<>(Identifier.of("sdm_mod", "phase4_dialogue_closed"));
    public static final PacketCodec<RegistryByteBuf, Phase4DialogueClosedPayload> CODEC =
        PacketCodec.ofStatic(
            (buf, payload) -> {},
            buf -> new Phase4DialogueClosedPayload()
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
