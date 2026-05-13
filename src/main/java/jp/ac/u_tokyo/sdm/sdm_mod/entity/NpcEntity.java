package jp.ac.u_tokyo.sdm.sdm_mod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;

/**
 * Boy / Girl / Student / PoliceOfficer など、ストーリー演出用の静的 NPC の基底クラス。
 * AI は無効化されており、姿勢（NpcPose）をデータトラッカーで保持する。
 * 姿勢はサーバー→クライアントへ同期され、レンダラー側で描画に反映される。
 */
public abstract class NpcEntity extends ZombieEntity {

    // 姿勢を ordinal (int) で保持するトラッカー。
    // サブクラス全員が同じキーを共有するため NpcEntity.class で登録する。
    private static final TrackedData<Integer> NPC_POSE_INDEX =
        DataTracker.registerData(NpcEntity.class, TrackedDataHandlerRegistry.INTEGER);

    protected NpcEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        setAiDisabled(true);
        setPersistent();
        setCanPickUpLoot(false);
        setBaby(false);
        setSilent(true);
    }

    public static DefaultAttributeContainer.Builder createNpcAttributes() {
        return ZombieEntity.createZombieAttributes();
    }

    // DataTracker に初期値（STANDING）を登録する。
    // super.initDataTracker() を呼ばないと親クラスのトラッカー登録が抜けるため必ず呼ぶ。
    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(NPC_POSE_INDEX, NpcPose.STANDING.ordinal());
    }

    public NpcPose getNpcPose() {
        int idx = this.dataTracker.get(NPC_POSE_INDEX);
        NpcPose[] poses = NpcPose.values();
        return (idx >= 0 && idx < poses.length) ? poses[idx] : NpcPose.STANDING;
    }

    public void setNpcPose(NpcPose pose) {
        this.dataTracker.set(NPC_POSE_INDEX, pose.ordinal());
    }

    // カスタムデータの保存・復元。ワールド再ロード後も姿勢が維持される。
    // 1.21.6 では writeNbt/readNbt が writeCustomData/readCustomData に変わった。
    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("NpcPose", this.dataTracker.get(NPC_POSE_INDEX));
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        int idx = view.getInt("NpcPose", -1);
        if (idx >= 0) {
            NpcPose[] poses = NpcPose.values();
            if (idx < poses.length) {
                setNpcPose(poses[idx]);
            }
        }
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    protected void initGoals() {
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        return false;
    }
}
