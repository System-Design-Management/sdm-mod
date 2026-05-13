package jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity;

import jp.ac.u_tokyo.sdm.sdm_mod.entity.NpcEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.NpcPose;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;

/**
 * NPC エンティティ共通の基底レンダラー。
 * 各エンティティのレンダラーはこのクラスを継承し、getTexture() だけ実装すればよい。
 *
 * <p>姿勢（NpcPose）に応じた setupTransforms を自動で適用する。
 * <ul>
 *   <li>STANDING: 追加回転なし（立ち姿勢）</li>
 *   <li>FACE_DOWN: X軸 +90° （うつ伏せ）</li>
 *   <li>FACE_UP: X軸 -90° （仰向け）</li>
 *   <li>LYING_RIGHT: Z軸 -90° （右向きに横たわる）</li>
 *   <li>LYING_LEFT: Z軸 +90° （左向きに横たわる）</li>
 * </ul>
 */
public abstract class NpcEntityRenderer<T extends NpcEntity>
    extends LivingEntityRenderer<T, PlayerEntityRenderState, PlayerEntityModel> {

    // updateRenderState() から setupTransforms() へ姿勢を引き渡すためのフィールド。
    // Minecraft のクライアントレンダリングはシングルスレッドなので instance field で安全。
    // （PlayerEntityRenderState に姿勢フィールドがないため、この方法で橋渡しする）
    private NpcPose currentPose = NpcPose.STANDING;

    /**
     * @param context Fabric が渡すレンダラーファクトリコンテキスト
     * @param slim    true = スリム体型（Alex）、false = 通常体型（Steve）
     */
    protected NpcEntityRenderer(EntityRendererFactory.Context context, boolean slim) {
        super(context, new PlayerEntityModel(context.getPart(EntityModelLayers.PLAYER), slim), 0.5f);
        // アイテムを手に描画するためのフィーチャーを追加する。
        this.addFeature(new HeldItemFeatureRenderer<>(this));
    }

    @Override
    public PlayerEntityRenderState createRenderState() {
        return new PlayerEntityRenderState();
    }

    @Override
    public void updateRenderState(T entity, PlayerEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        // 手持ちアイテムを render state に反映する。
        ArmedEntityRenderState.updateRenderState(entity, state, this.itemModelResolver);
        state.mainArm = Arm.RIGHT;
        // 姿勢を保存して setupTransforms() で使えるようにする。
        currentPose = entity.getNpcPose();
    }

    @Override
    protected void setupTransforms(PlayerEntityRenderState state, MatrixStack matrices, float bodyYaw, float baseHeight) {
        super.setupTransforms(state, matrices, bodyYaw, baseHeight);
        // 姿勢に応じてモデルを回転させる。
        // super の後に適用することで、yaw（向き）が処理された状態に上乗せされる。
        switch (currentPose) {
            case FACE_DOWN   -> matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            case FACE_UP     -> matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
            case LYING_RIGHT -> matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-90.0f));
            case LYING_LEFT  -> matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
            case STANDING    -> { /* 追加回転なし */ }
        }
    }
}
