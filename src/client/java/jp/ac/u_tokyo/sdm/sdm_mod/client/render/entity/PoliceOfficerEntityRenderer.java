package jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.PoliceOfficerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class PoliceOfficerEntityRenderer
    extends LivingEntityRenderer<PoliceOfficerEntity, PlayerEntityRenderState, PlayerEntityModel> {

    private static final Identifier TEXTURE = Identifier.of(SdmMod.MOD_ID, "textures/entity/police_officer_01.png");

    public PoliceOfficerEntityRenderer(EntityRendererFactory.Context context) {
        // EntityModelLayers.PLAYER でプレイヤーモデルのパーツを取得する。
        // 第2引数 true はスリム体型。
        super(context, new PlayerEntityModel(context.getPart(EntityModelLayers.PLAYER), true), 0.5f);
        // HeldItemFeatureRenderer を追加することでアイテムを手に描画できるようになる。
        // PlayerEntityModel は BipedEntityModel を継承し ModelWithArms を実装しているので互換性がある。
        this.addFeature(new HeldItemFeatureRenderer<>(this));
    }

    @Override
    public PlayerEntityRenderState createRenderState() {
        return new PlayerEntityRenderState();
    }

    // エンティティの現在の状態をレンダーステートに転写する。
    // ArmedEntityRenderState.updateRenderState() を呼ばないと、手持ちアイテムの ItemRenderState が
    // 更新されず HeldItemFeatureRenderer が何も描画しない。
    @Override
    public void updateRenderState(PoliceOfficerEntity entity, PlayerEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        // 手持ちアイテムの ItemRenderState（GPU に渡す描画情報）を更新する。
        ArmedEntityRenderState.updateRenderState(entity, state, this.itemModelResolver);
        state.mainArm = Arm.RIGHT;
    }

    @Override
    public Identifier getTexture(PlayerEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void setupTransforms(PlayerEntityRenderState state, MatrixStack matrices, float bodyYaw, float baseHeight) {
        super.setupTransforms(state, matrices, bodyYaw, baseHeight);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
    }
}
