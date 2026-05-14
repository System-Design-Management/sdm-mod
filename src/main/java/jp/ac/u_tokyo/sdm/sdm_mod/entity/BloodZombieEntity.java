package jp.ac.u_tokyo.sdm.sdm_mod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;

public class BloodZombieEntity extends ZombieEntity {
    public BloodZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createBloodZombieAttributes() {
        return ZombieEntity.createZombieAttributes();
    }
}
