package jp.ac.u_tokyo.sdm.sdm_mod.story.phase3;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryCombatService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

public final class Phase3ZombieService {
    private static final String PHASE2_ID = "phase2";
    private static final String PHASE3_ID = "phase3";
    private static final String PHASE4_ID = "phase4";
    private static final String PHASE5_ID = "phase5";
    private static final String PHASE3_ZOMBIE_TAG = "sdm_mod.phase3_zombie";
    private static final String PHASE5_ENRAGED_TAG = "sdm_mod.phase5_enraged";
    private static final List<BlockPos> PHASE2_SPAWN_POSITIONS = List.of(
        new BlockPos(-175, 41, -638),
        new BlockPos(-187, 41, -636),
        new BlockPos(-202, 41, -637),
        new BlockPos(-160, 41, -628),
        new BlockPos(-126, 41, -636)
    );
    private static final double IDLE_HOME_RADIUS = 2.5;
    private static final double IDLE_HOME_RADIUS_SQUARED = IDLE_HOME_RADIUS * IDLE_HOME_RADIUS;
    private static final double RETURN_SPEED = 0.9;
    private static final int IDLE_LOOK_INTERVAL_TICKS = 40;
    private static final Map<UUID, BlockPos> HOME_POSITIONS = new HashMap<>();
    private static final Set<UUID> CHASING_ZOMBIES = new HashSet<>();

    private Phase3ZombieService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase3ZombieService::tick);
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
            HOME_POSITIONS.remove(entity.getUuid());
            CHASING_ZOMBIES.remove(entity.getUuid());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            HOME_POSITIONS.clear();
            CHASING_ZOMBIES.clear();
        });
    }

    public static void spawnPhase2Zombies(ServerWorld world) {
        for (BlockPos spawnPos : PHASE2_SPAWN_POSITIONS) {
            if (findTaggedZombie(world.getServer(), spawnPos) != null) {
                continue;
            }

            spawn(world, Vec3d.ofBottomCenter(spawnPos));
        }
    }

    public static ZombieEntity spawn(ServerWorld world, Vec3d pos) {
        ZombieEntity zombie = EntityType.ZOMBIE.create(world, SpawnReason.EVENT);
        if (zombie == null) {
            throw new IllegalStateException("Failed to create phase3 zombie entity.");
        }

        zombie.refreshPositionAndAngles(pos.x, pos.y, pos.z, zombie.getYaw(), zombie.getPitch());
        register(zombie);
        world.spawnEntity(zombie);
        return zombie;
    }

    public static void register(ZombieEntity zombie) {
        zombie.addCommandTag(PHASE3_ZOMBIE_TAG);
        zombie.setPersistent();
        StoryCombatService.configureStoryZombieCombat(zombie);
        HOME_POSITIONS.put(zombie.getUuid(), zombie.getBlockPos());
    }

    public static boolean isManagedPhaseZombie(Entity entity) {
        return entity instanceof ZombieEntity zombie && zombie.getCommandTags().contains(PHASE3_ZOMBIE_TAG);
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        boolean phase2Active = storyManager.isActive() && storyManager.isAtChapter(PHASE2_ID);
        boolean phase3Active = storyManager.isActive() && storyManager.isAtChapter(PHASE3_ID);
        boolean phase4Active = storyManager.isActive() && storyManager.isAtChapter(PHASE4_ID);
        boolean phase5Active = storyManager.isActive() && storyManager.isAtChapter(PHASE5_ID);

        List<ZombieEntity> toCleanup = new java.util.ArrayList<>();
        server.getWorlds().forEach(world -> world.iterateEntities().forEach(entity -> {
            if (!(entity instanceof ZombieEntity zombie) || !zombie.getCommandTags().contains(PHASE3_ZOMBIE_TAG)) {
                return;
            }

            if (phase2Active) {
                tickPhase2Zombie(zombie);
                return;
            }

            if (phase3Active) {
                tickPhase3Zombie(zombie);
                return;
            }

            if (phase5Active) {
                tickPhase5Zombie(zombie);
                return;
            }

            // phase4以降またはストーリー非アクティブ時は消去
            toCleanup.add(zombie);
        }));

        for (ZombieEntity zombie : toCleanup) {
            cleanup(zombie);
        }

        pruneMissingHomes(server);
    }

    private static void tickPhase2Zombie(ZombieEntity zombie) {
        zombie.getNavigation().stop();
        zombie.setTarget(null);
        CHASING_ZOMBIES.remove(zombie.getUuid());
        zombie.setForwardSpeed(0.0f);
        zombie.setSidewaysSpeed(0.0f);
        zombie.setMovementSpeed(0.0f);
    }

    private static void tickPhase3Zombie(ZombieEntity zombie) {
        BlockPos homePos = HOME_POSITIONS.computeIfAbsent(zombie.getUuid(), uuid -> zombie.getBlockPos());
        if (hasActivePlayerTarget(zombie)) {
            CHASING_ZOMBIES.add(zombie.getUuid());
            return;
        }

        if (CHASING_ZOMBIES.remove(zombie.getUuid())) {
            homePos = zombie.getBlockPos();
            HOME_POSITIONS.put(zombie.getUuid(), homePos);
        }

        Vec3d homeCenter = Vec3d.ofBottomCenter(homePos);
        if (zombie.squaredDistanceTo(homeCenter) > IDLE_HOME_RADIUS_SQUARED) {
            moveTowardHome(zombie, homeCenter);
            return;
        }

        zombie.getNavigation().stop();
        zombie.setForwardSpeed(0.0f);
        zombie.setSidewaysSpeed(0.0f);
        zombie.setMovementSpeed(0.0f);

        if (zombie.age % IDLE_LOOK_INTERVAL_TICKS == 0) {
            double yawRadians = Math.toRadians(zombie.getRandom().nextDouble() * 360.0);
            Vec3d lookTarget = homeCenter.add(Math.cos(yawRadians) * 2.0, 0.0, Math.sin(yawRadians) * 2.0);
            zombie.getLookControl().lookAt(lookTarget.x, zombie.getEyeY(), lookTarget.z);
        }
    }

    private static void tickPhase5Zombie(ZombieEntity zombie) {
        ensurePhase5Combat(zombie);
        ServerPlayerEntity target = findNearestAttackablePlayer(zombie);
        zombie.setTarget(target);
    }

    private static boolean hasActivePlayerTarget(ZombieEntity zombie) {
        LivingEntity target = zombie.getTarget();
        if (!(target instanceof PlayerEntity player)) {
            return false;
        }

        return player.isAlive() && !player.isSpectator() && zombie.getVisibilityCache().canSee(player);
    }

    private static void moveTowardHome(ZombieEntity zombie, Vec3d homeCenter) {
        Path path = zombie.getNavigation().findPathTo(homeCenter.x, homeCenter.y, homeCenter.z, 1);
        if (path == null) {
            zombie.getNavigation().stop();
            return;
        }

        zombie.getNavigation().startMovingAlong(path, RETURN_SPEED);
    }

    private static void ensurePhase5Combat(ZombieEntity zombie) {
        if (zombie.getCommandTags().contains(PHASE5_ENRAGED_TAG)) {
            return;
        }

        StoryCombatService.configurePhase5ZombieCombat(zombie);
        zombie.addCommandTag(PHASE5_ENRAGED_TAG);
    }

    private static ServerPlayerEntity findNearestAttackablePlayer(ZombieEntity zombie) {
        ServerWorld world = (ServerWorld) zombie.getWorld();
        ServerPlayerEntity nearestPlayer = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!player.isAlive() || player.isSpectator()) {
                continue;
            }

            double distanceSquared = zombie.squaredDistanceTo(player);
            if (distanceSquared >= nearestDistanceSquared) {
                continue;
            }

            nearestPlayer = player;
            nearestDistanceSquared = distanceSquared;
        }

        return nearestPlayer;
    }

    private static void cleanup(ZombieEntity zombie) {
        HOME_POSITIONS.remove(zombie.getUuid());
        CHASING_ZOMBIES.remove(zombie.getUuid());
        zombie.discard();
    }

    private static void pruneMissingHomes(MinecraftServer server) {
        Iterator<Map.Entry<UUID, BlockPos>> iterator = HOME_POSITIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, BlockPos> entry = iterator.next();
            if (findTaggedZombie(server, entry.getKey()) != null) {
                continue;
            }

            iterator.remove();
        }
    }

    private static ZombieEntity findTaggedZombie(MinecraftServer server, UUID uuid) {
        for (ServerWorld world : server.getWorlds()) {
            Entity entity = world.getEntity(uuid);
            if (entity instanceof ZombieEntity zombie && zombie.getCommandTags().contains(PHASE3_ZOMBIE_TAG)) {
                return zombie;
            }
        }

        return null;
    }

    private static ZombieEntity findTaggedZombie(MinecraftServer server, BlockPos pos) {
        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (!(entity instanceof ZombieEntity zombie) || !zombie.getCommandTags().contains(PHASE3_ZOMBIE_TAG)) {
                    continue;
                }

                if (zombie.getBlockPos().equals(pos)) {
                    return zombie;
                }
            }
        }

        return null;
    }
}
