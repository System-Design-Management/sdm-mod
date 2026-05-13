package jp.ac.u_tokyo.sdm.sdm_mod.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PosterEntity extends Entity {
    private static final TrackedData<String> POSTER_ID = DataTracker.registerData(
        PosterEntity.class, TrackedDataHandlerRegistry.STRING
    );
    private static final TrackedData<Byte> FACING = DataTracker.registerData(
        PosterEntity.class, TrackedDataHandlerRegistry.BYTE
    );

    public PosterEntity(EntityType<? extends PosterEntity> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(POSTER_ID, "");
        builder.add(FACING, (byte) Direction.NORTH.getIndex());
    }

    public void setPosterId(String id) {
        dataTracker.set(POSTER_ID, id);
    }

    public String getPosterId() {
        return dataTracker.get(POSTER_ID);
    }

    public void setPosterFacing(Direction direction) {
        dataTracker.set(FACING, (byte) direction.getIndex());
    }

    public Direction getPosterFacing() {
        return Direction.byIndex(dataTracker.get(FACING));
    }

    @Override
    protected void readCustomData(ReadView view) {
        setPosterId(view.getString("PosterId", ""));
        setPosterFacing(Direction.byIndex(view.getByte("Facing", (byte) Direction.NORTH.getIndex())));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putString("PosterId", getPosterId());
        view.putByte("Facing", (byte) getPosterFacing().getIndex());
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean isCollidable(Entity other) {
        return false;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        this.discard();
        return true;
    }
}
