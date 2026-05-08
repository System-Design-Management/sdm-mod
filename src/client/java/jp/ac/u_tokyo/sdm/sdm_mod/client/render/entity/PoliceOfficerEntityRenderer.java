package jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class PoliceOfficerEntityRenderer extends ZombieEntityRenderer {
    private static final Identifier TEXTURE = Identifier.of(SdmMod.MOD_ID, "textures/entity/police_officer.png");

    public PoliceOfficerEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public Identifier getTexture(ZombieEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void setupTransforms(ZombieEntityRenderState state, MatrixStack matrices, float bodyYaw, float baseHeight) {
        super.setupTransforms(state, matrices, bodyYaw, baseHeight);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
    }
}
