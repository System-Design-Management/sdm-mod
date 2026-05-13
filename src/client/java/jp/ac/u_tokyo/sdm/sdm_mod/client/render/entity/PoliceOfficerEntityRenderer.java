package jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.PoliceOfficerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;

public class PoliceOfficerEntityRenderer extends NpcEntityRenderer<PoliceOfficerEntity> {

    private static final Identifier TEXTURE = Identifier.of(SdmMod.MOD_ID, "textures/entity/police_officer_01.png");

    public PoliceOfficerEntityRenderer(EntityRendererFactory.Context context) {
        super(context, true); // true = スリム体型
    }

    @Override
    public Identifier getTexture(PlayerEntityRenderState state) {
        return TEXTURE;
    }
}
