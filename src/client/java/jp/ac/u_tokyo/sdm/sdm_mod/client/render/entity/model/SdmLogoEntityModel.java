package jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.model;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.Identifier;

public class SdmLogoEntityModel extends EntityModel<EntityRenderState> {
    public static final EntityModelLayer LAYER = new EntityModelLayer(
        Identifier.of(SdmMod.MOD_ID, "sdm_logo"), "main"
    );

    private final ModelPart s2;

    public SdmLogoEntityModel(ModelPart root) {
        super(root, RenderLayer::getEntityCutoutNoCull);
        this.s2 = root.getChild("s2");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        ModelPartData s2 = modelPartData.addChild("s2", ModelPartBuilder.create(), ModelTransform.origin(-14.1974F, 13.7101F, 0.0F));

        ModelPartData s = s2.addChild("s", ModelPartBuilder.create().uv(24, 12).cuboid(1.1974F, -7.2819F, -1.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F))
            .uv(24, 16).cuboid(1.1974F, 0.504F, -1.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F))
            .uv(24, 8).cuboid(1.1974F, 8.2899F, -1.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.origin(0.0F, 0.0F, 0.0F));

        s.addChild("cube_r1", ModelPartBuilder.create().uv(26, 32).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
        s.addChild("cube_r2", ModelPartBuilder.create().uv(36, 8).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(1.9045F, 9.5828F, 0.0F, 0.0F, 0.0F, -0.7854F));
        s.addChild("cube_r3", ModelPartBuilder.create().uv(0, 34).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(4.4903F, 9.5828F, 0.0F, 0.0F, 0.0F, 0.7854F));
        s.addChild("cube_r4", ModelPartBuilder.create().uv(34, 33).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(6.3949F, 7.7859F, 0.0F, 0.0F, 0.0F, 0.3927F));
        s.addChild("cube_r5", ModelPartBuilder.create().uv(8, 35).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(7.5429F, 5.7796F, 0.0F, 0.0F, 0.0F, -0.3927F));
        s.addChild("cube_r6", ModelPartBuilder.create().uv(36, 18).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(6.6117F, 3.3324F, 0.0F, 0.0F, 0.0F, -0.7854F));
        s.addChild("cube_r7", ModelPartBuilder.create().uv(34, 28).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(1.9045F, 1.7969F, 0.0F, 0.0F, 0.0F, -0.7854F));
        s.addChild("cube_r8", ModelPartBuilder.create().uv(10, 30).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-1.1481F, -2.0063F, 0.0F, 0.0F, 0.0F, 0.3927F));
        s.addChild("cube_r9", ModelPartBuilder.create().uv(18, 32).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-0.2168F, -4.4535F, 0.0F, 0.0F, 0.0F, 0.7854F));
        s.addChild("cube_r10", ModelPartBuilder.create().uv(36, 13).cuboid(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(6.6117F, -4.4535F, 0.0F, 0.0F, 0.0F, -0.7854F));

        ModelPartData d = s2.addChild("d", ModelPartBuilder.create().uv(0, 8).cuboid(0.7071F, -17.0071F, -1.0F, 2.0F, 16.0F, 2.0F, new Dilation(0.0F)), ModelTransform.origin(9.1974F, 10.2899F, 0.0F));

        d.addChild("cube_r11", ModelPartBuilder.create().uv(24, 28).cuboid(0.0F, -2.0F, -1.0F, 3.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(9.1241F, -10.3286F, 0.0F, 0.0F, 0.0F, 1.5708F));

        ModelPartData bone = d.addChild("bone", ModelPartBuilder.create(), ModelTransform.origin(8.8668F, -13.0357F, 0.0F));
        bone.addChild("cube_r12", ModelPartBuilder.create().uv(0, 26).cuboid(-1.0F, -2.0F, -1.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-0.6481F, 0.5721F, 0.0F, 0.0F, 0.0F, 1.2217F));
        bone.addChild("cube_r13", ModelPartBuilder.create().uv(24, 24).cuboid(-1.0F, -2.0F, -1.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-2.6944F, -1.4479F, 0.0F, 0.0F, 0.0F, 0.6981F));
        bone.addChild("cube_r14", ModelPartBuilder.create().uv(24, 20).cuboid(-1.0F, -2.0F, -1.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-5.3378F, -1.8918F, 0.0F, 0.0F, 0.0F, 0.0873F));

        ModelPartData bone2 = d.addChild("bone2", ModelPartBuilder.create(), ModelTransform.of(8.8668F, -13.0357F, 0.0F, 3.1416F, 0.0F, 0.0F));
        bone2.addChild("cube_r15", ModelPartBuilder.create().uv(28, 4).cuboid(-1.0F, -2.0F, -1.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-0.6481F, -7.4851F, 0.0F, 0.0F, 0.0F, 1.2217F));
        bone2.addChild("cube_r16", ModelPartBuilder.create().uv(28, 0).cuboid(-1.0F, -2.0F, -1.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-2.6944F, -9.5051F, 0.0F, 0.0F, 0.0F, 0.6981F));
        bone2.addChild("cube_r17", ModelPartBuilder.create().uv(12, 26).cuboid(-1.0F, -2.0F, -1.0F, 4.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(-5.3378F, -9.9491F, 0.0F, 0.0F, 0.0F, 0.0873F));

        ModelPartData m = s2.addChild("m", ModelPartBuilder.create().uv(8, 8).cuboid(12.7071F, -17.0071F, -1.0F, 2.0F, 16.0F, 2.0F, new Dilation(0.0F))
            .uv(16, 8).cuboid(23.243F, -17.0071F, -1.0F, 2.0F, 16.0F, 2.0F, new Dilation(0.0F))
            .uv(0, 30).cuboid(17.4515F, -7.1552F, -1.0F, 3.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.origin(9.1974F, 10.2899F, 0.0F));

        m.addChild("cube_r18", ModelPartBuilder.create().uv(0, 0).cuboid(-1.0F, -2.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(20.8812F, -6.0791F, 0.0F, 0.0F, 0.0F, -1.1781F));
        m.addChild("cube_r19", ModelPartBuilder.create().uv(0, 4).cuboid(-1.0F, -2.0F, -1.0F, 12.0F, 2.0F, 2.0F, new Dilation(0.0F)), ModelTransform.of(13.242F, -15.3179F, 0.0F, 0.0F, 0.0F, 1.1781F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(EntityRenderState state) {}
}
