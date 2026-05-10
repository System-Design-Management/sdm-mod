package jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.BoyEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;

public class BoyEntityRenderer extends NpcEntityRenderer<BoyEntity> {

    private static final Identifier TEXTURE = Identifier.of(SdmMod.MOD_ID, "textures/entity/boy.png");

    public BoyEntityRenderer(EntityRendererFactory.Context context) {
        super(context, true);
    }

    @Override
    public Identifier getTexture(PlayerEntityRenderState state) {
        return TEXTURE;
    }
}
