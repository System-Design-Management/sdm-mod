package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ShowEdVideoPayload() implements CustomPayload {
    public static final ShowEdVideoPayload INSTANCE = new ShowEdVideoPayload();
    public static final Id<ShowEdVideoPayload> ID = new Id<>(Identifier.of(SdmMod.MOD_ID, "show_ed_video"));
    public static final PacketCodec<RegistryByteBuf, ShowEdVideoPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
