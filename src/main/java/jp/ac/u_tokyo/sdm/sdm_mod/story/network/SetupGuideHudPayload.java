package jp.ac.u_tokyo.sdm.sdm_mod.story.network;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetupGuideHudPayload(boolean visible) implements CustomPayload {
    public static final CustomPayload.Id<SetupGuideHudPayload> ID =
        new CustomPayload.Id<>(Identifier.of(SdmMod.MOD_ID, "setup_guide_hud"));

    public static final PacketCodec<RegistryByteBuf, SetupGuideHudPayload> CODEC =
        PacketCodec.ofStatic(
            (buf, payload) -> buf.writeBoolean(payload.visible()),
            buf -> new SetupGuideHudPayload(buf.readBoolean())
        );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
