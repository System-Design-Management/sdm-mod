package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StoryVideoStartPayload() implements CustomPayload {
    public static final StoryVideoStartPayload INSTANCE = new StoryVideoStartPayload();
    public static final Id<StoryVideoStartPayload> ID = new Id<>(Identifier.of(SdmMod.MOD_ID, "story_video_start"));
    public static final PacketCodec<RegistryByteBuf, StoryVideoStartPayload> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
