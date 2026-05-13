package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public final class Phase2TutorialZombieService {
    private static final String PHASE2_ID = "phase2";
    private static final String PHASE2_TUTORIAL_ZOMBIE_TAG = "sdm_mod.phase2_tutorial_zombie";
    private static final Vec3d SPAWN_POS = new Vec3d(-160.5D, 28.0D, -620.0D);
    private static final float SPAWN_YAW = 0.0F;
    private static final float SPAWN_PITCH = 0.0F;

    private Phase2TutorialZombieService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase2TutorialZombieService::tick);
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            if (isManagedTutorialZombie(entity)) {
                entity.removeCommandTag(PHASE2_TUTORIAL_ZOMBIE_TAG);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(Phase2TutorialZombieService::cleanup);
    }

    public static void spawnPhase2TutorialZombie(ServerWorld world) {
        if (findTaggedZombie(world.getServer()) != null) {
            return;
        }

        ZombieEntity zombie = EntityType.ZOMBIE.create(world, SpawnReason.EVENT);
        if (zombie == null) {
            throw new IllegalStateException("Failed to create phase2 tutorial zombie entity.");
        }

        zombie.refreshPositionAndAngles(SPAWN_POS.x, SPAWN_POS.y, SPAWN_POS.z, SPAWN_YAW, SPAWN_PITCH);
        zombie.setAiDisabled(true);
        zombie.setPersistent();
        zombie.setCanPickUpLoot(false);
        zombie.setBaby(false);
        zombie.addCommandTag(PHASE2_TUTORIAL_ZOMBIE_TAG);
        world.spawnEntity(zombie);
    }

    public static boolean isTutorialZombieAlive(MinecraftServer server) {
        return findTaggedZombie(server) != null;
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE2_ID)) {
            cleanup(server);
            return;
        }

        server.getWorlds().forEach(world -> world.iterateEntities().forEach(entity -> {
            if (!(entity instanceof ZombieEntity zombie) || !isManagedTutorialZombie(zombie)) {
                return;
            }

            zombie.getNavigation().stop();
            zombie.setTarget(null);
            zombie.refreshPositionAndAngles(zombie.getX(), zombie.getY(), zombie.getZ(), SPAWN_YAW, SPAWN_PITCH);
        }));
    }

    public static boolean isManagedTutorialZombie(Entity entity) {
        return entity instanceof ZombieEntity zombie && zombie.getCommandTags().contains(PHASE2_TUTORIAL_ZOMBIE_TAG);
    }

    private static ZombieEntity findTaggedZombie(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof ZombieEntity zombie && isManagedTutorialZombie(zombie)) {
                    return zombie;
                }
            }
        }

        return null;
    }

    private static void cleanup(MinecraftServer server) {
        server.getWorlds().forEach(world -> world.iterateEntities().forEach(entity -> {
            if (entity instanceof ZombieEntity zombie && isManagedTutorialZombie(zombie)) {
                zombie.discard();
            }
        }));
    }
}
