package jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity;

import jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.state.PosterEntityRenderState;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.PosterEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.poster.PosterDefinition;
import jp.ac.u_tokyo.sdm.sdm_mod.poster.PosterRegistry;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

public class PosterEntityRenderer extends EntityRenderer<PosterEntity, PosterEntityRenderState> {
    public PosterEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public PosterEntityRenderState createRenderState() {
        return new PosterEntityRenderState();
    }

    @Override
    public void updateRenderState(PosterEntity entity, PosterEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.posterId = entity.getPosterId();
        state.facing = entity.getPosterFacing();

        PosterDefinition def = PosterRegistry.get(entity.getPosterId());
        if (def != null) {
            state.width = def.width();
            state.height = def.height();
        }
    }

    @Override
    public void render(PosterEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        PosterDefinition def = PosterRegistry.get(state.posterId);
        if (def == null) return;

        float w = state.width / 2.0f;
        float h = state.height / 2.0f;

        Identifier texture = def.texture();
        RenderLayer layer = RenderLayer.getEntityCutoutNoCull(texture);
        VertexConsumer consumer = vertexConsumers.getBuffer(layer);

        matrices.push();
        // Rotate so the quad faces the stored direction.
        // Default quad (no rotation) faces SOUTH (+Z). Applying NEGATIVE_Y rotation
        // by getPositiveHorizontalDegrees() maps each direction correctly:
        // SOUTH=0°, WEST=90°, NORTH=180°, EAST=270°
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(
            Direction.getHorizontalDegreesOrThrow(state.facing)
        ));

        MatrixStack.Entry entry = matrices.peek();

        // Draw a quad in the X-Y plane centered at origin.
        // Winding: top-left → bottom-left → bottom-right → top-right (CCW from front)
        consumer.vertex(entry, -w,  h, 0).color(255, 255, 255, 255).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0, 0, 1);
        consumer.vertex(entry, -w, -h, 0).color(255, 255, 255, 255).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0, 0, 1);
        consumer.vertex(entry,  w, -h, 0).color(255, 255, 255, 255).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0, 0, 1);
        consumer.vertex(entry,  w,  h, 0).color(255, 255, 255, 255).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(entry, 0, 0, 1);

        matrices.pop();
    }

}
