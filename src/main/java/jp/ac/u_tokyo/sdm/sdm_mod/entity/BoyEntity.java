package jp.ac.u_tokyo.sdm.sdm_mod.entity;

import jp.ac.u_tokyo.sdm.sdm_mod.ModSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;

public class BoyEntity extends NpcEntity {
    public BoyEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createBoyAttributes() {
        return NpcEntity.createNpcAttributes();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.DEAD_PEOPLE_VOICE;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 200; // 10秒
    }
}
