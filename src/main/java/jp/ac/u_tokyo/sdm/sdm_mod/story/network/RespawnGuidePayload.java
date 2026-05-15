package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record RespawnGuidePayload() implements CustomPayload {
    public static final RespawnGuidePayload INSTANCE = new RespawnGuidePayload();
    public static final Id<RespawnGuidePayload> ID =
        new Id<>(Identifier.of(SdmMod.MOD_ID, "respawn_guide"));
    public static final PacketCodec<RegistryByteBuf, RespawnGuidePayload> CODEC =
        PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
