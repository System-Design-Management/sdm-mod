package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SearchPcLocationClosedPayload() implements CustomPayload {
    public static final SearchPcLocationClosedPayload INSTANCE = new SearchPcLocationClosedPayload();
    public static final Id<SearchPcLocationClosedPayload> ID =
        new Id<>(Identifier.of(SdmMod.MOD_ID, "search_pc_location_closed"));
    public static final PacketCodec<RegistryByteBuf, SearchPcLocationClosedPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
