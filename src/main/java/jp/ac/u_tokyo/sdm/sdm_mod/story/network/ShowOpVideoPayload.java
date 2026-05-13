package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ShowOpVideoPayload() implements CustomPayload {
    public static final ShowOpVideoPayload INSTANCE = new ShowOpVideoPayload();
    public static final Id<ShowOpVideoPayload> ID = new Id<>(Identifier.of(SdmMod.MOD_ID, "show_op_video"));
    public static final PacketCodec<RegistryByteBuf, ShowOpVideoPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
