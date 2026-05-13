package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SearchPcLocationOpenedPayload() implements CustomPayload {
    public static final SearchPcLocationOpenedPayload INSTANCE = new SearchPcLocationOpenedPayload();
    public static final Id<SearchPcLocationOpenedPayload> ID =
        new Id<>(Identifier.of(SdmMod.MOD_ID, "search_pc_location_opened"));
    public static final PacketCodec<RegistryByteBuf, SearchPcLocationOpenedPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
