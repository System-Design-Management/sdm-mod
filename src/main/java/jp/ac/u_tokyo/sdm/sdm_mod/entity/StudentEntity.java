package jp.ac.u_tokyo.sdm.sdm_mod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;

public class StudentEntity extends NpcEntity {
    public StudentEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createStudentAttributes() {
        return NpcEntity.createNpcAttributes();
    }
}
