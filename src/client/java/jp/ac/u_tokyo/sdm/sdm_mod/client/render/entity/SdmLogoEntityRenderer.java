package jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.model.SdmLogoEntityModel;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.SdmLogoEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class SdmLogoEntityRenderer extends EntityRenderer<SdmLogoEntity, EntityRenderState> {
    private static final Identifier TEXTURE = Identifier.of(SdmMod.MOD_ID, "textures/entity/sdm_logo.png");
    private final SdmLogoEntityModel model;

    public SdmLogoEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.model = new SdmLogoEntityModel(context.getPart(SdmLogoEntityModel.LAYER));
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public void render(EntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(state, matrices, vertexConsumers, light);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0f));
        model.setAngles(state);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(model.getLayer(TEXTURE));
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
    }
}
