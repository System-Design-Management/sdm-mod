package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record Phase5GameOverPayload(boolean allowSkip) implements CustomPayload {
    public static final Phase5GameOverPayload INSTANCE = new Phase5GameOverPayload(false);
    public static final Id<Phase5GameOverPayload> ID = new Id<>(Identifier.of(SdmMod.MOD_ID, "phase5_game_over"));
    public static final PacketCodec<RegistryByteBuf, Phase5GameOverPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOLEAN,
        Phase5GameOverPayload::allowSkip,
        Phase5GameOverPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
