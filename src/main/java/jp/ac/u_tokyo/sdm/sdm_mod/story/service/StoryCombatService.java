package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;

public final class StoryCombatService {
    private static final double STORY_ZOMBIE_MAX_HEALTH = 4.0;
    private static final double STORY_ZOMBIE_ATTACK_DAMAGE = 4.0;

    private StoryCombatService() {
    }

    public static void initialize() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            if (!StoryModule.getStoryManager().isActive()) {
                return ActionResult.PASS;
            }

            if (!isBareHanded(player.getStackInHand(hand))) {
                return ActionResult.PASS;
            }

            return ActionResult.FAIL;
        });
    }

    public static void configureStoryZombieCombat(ZombieEntity zombie) {
        setAttributeBaseValue(zombie, EntityAttributes.MAX_HEALTH, STORY_ZOMBIE_MAX_HEALTH);
        setAttributeBaseValue(zombie, EntityAttributes.ATTACK_DAMAGE, STORY_ZOMBIE_ATTACK_DAMAGE);
        zombie.setHealth((float) STORY_ZOMBIE_MAX_HEALTH);
    }

    private static boolean isBareHanded(ItemStack stack) {
        return stack.isEmpty();
    }

    private static void setAttributeBaseValue(ZombieEntity zombie, RegistryEntry<EntityAttribute> attribute, double value) {
        EntityAttributeInstance instance = zombie.getAttributeInstance(attribute);
        if (instance == null) {
            throw new IllegalStateException("Missing zombie attribute: " + attribute);
        }

        instance.setBaseValue(value);
    }
}
