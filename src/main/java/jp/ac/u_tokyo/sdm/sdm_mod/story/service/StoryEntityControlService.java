package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.entity.NpcEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase2.Phase2TutorialZombieService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase3.Phase3ZombieService;
import jp.ac.u_tokyo.sdm.sdm_mod.story.phase4.Phase4ZombieService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public final class StoryEntityControlService {
    private StoryEntityControlService() {
    }

    public static void initialize() {
        // Once the story is active, immediately discard newly loaded non-player living entities.
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!StoryModule.getStoryManager().isActive()) {
                return;
            }

            if (shouldRemove(entity)) {
                entity.discard();
            }
        });
    }

    public static void clearNonPlayerLivingEntities(MinecraftServer server) {
        // Sweep every loaded world once at story start so pre-existing mobs are also removed.
        // Collect to list first to avoid ConcurrentModificationException during iteration.
        server.getWorlds().forEach(world -> {
            List<Entity> toRemove = new ArrayList<>();
            world.iterateEntities().forEach(entity -> {
                if (shouldRemove(entity)) {
                    toRemove.add(entity);
                }
            });
            toRemove.forEach(Entity::discard);
        });
    }

    private static boolean shouldRemove(Entity entity) {
        // NpcEntity（PoliceOfficer / Boy / Girl / Student など）は演出用 NPC なので保持する。
        if (entity instanceof NpcEntity) {
            return false;
        }

        if (Phase2TutorialZombieService.isManagedTutorialZombie(entity)) {
            return false;
        }

        if (Phase3ZombieService.isManagedPhaseZombie(entity)) {
            return false;
        }

        if (Phase4ZombieService.isManagedPhaseZombie(entity)) {
            return false;
        }

        // Preserve multiplayer participants while removing mobs and other living entities.
        return entity instanceof LivingEntity && !(entity instanceof ServerPlayerEntity);
    }
}
