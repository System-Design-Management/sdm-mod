package jp.ac.u_tokyo.sdm.sdm_mod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;

public class GirlEntity extends NpcEntity {
    public GirlEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createGirlAttributes() {
        return NpcEntity.createNpcAttributes();
    }
}
