package jp.ac.u_tokyo.sdm.sdm_mod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class PoliceOfficerEntity extends ZombieEntity {
    public PoliceOfficerEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        setAiDisabled(true);
        setPersistent();
        setCanPickUpLoot(false);
        setBaby(false);
        setSilent(true);
    }

    public static DefaultAttributeContainer.Builder createPoliceOfficerAttributes() {
        return ZombieEntity.createZombieAttributes();
    }

    @Override
    protected void initGoals() {
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        return false;
    }
}
