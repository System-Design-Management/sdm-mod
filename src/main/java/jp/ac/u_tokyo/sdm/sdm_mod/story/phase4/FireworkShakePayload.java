package jp.ac.u_tokyo.sdm.sdm_mod.story.phase4;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record FireworkShakePayload() implements CustomPayload {
    public static final FireworkShakePayload INSTANCE = new FireworkShakePayload();
    public static final CustomPayload.Id<FireworkShakePayload> ID =
        new CustomPayload.Id<>(Identifier.of("sdm_mod", "firework_shake"));
    public static final PacketCodec<RegistryByteBuf, FireworkShakePayload> CODEC =
        PacketCodec.unit(INSTANCE);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
