package jp.ac.u_tokyo.sdm.sdm_mod.story.phase4;

import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryCombatService;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class Phase4ZombieService {
    private static final String PHASE4_ID = "phase4";
    private static final String PHASE5_ID = "phase5";
    private static final String PHASE4_ZOMBIE_TAG = "sdm_mod.phase4_zombie";
    private static final String PHASE5_ENRAGED_TAG = "sdm_mod.phase5_enraged";
    private static final int SPAWN_COUNT = 10;
    private static final double MOVE_SPEED = 0.9D;
    private static final double DESTINATION_REACHED_DISTANCE_SQUARED = 1.0D;
    private static final int REPATH_INTERVAL_TICKS = 20;
    private static final Map<UUID, BlockPos> DESTINATIONS = new HashMap<>();

    private Phase4ZombieService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(Phase4ZombieService::tick);
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> DESTINATIONS.remove(entity.getUuid()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> DESTINATIONS.clear());
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ZombieEntity zombie) || !isManagedPhaseZombie(zombie) || amount <= 0.0f) {
                return true;
            }

            MinecraftServer server = zombie.getServer();
            if (server == null) {
                return true;
            }

            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE4_ID)) {
                return true;
            }

            Phase4To5RegionTrigger.tryAdvanceToPhase5(
                server,
                "Story advanced from {} to {} after phase4 zombie {} at ({}, {}, {}) took damage.",
                PHASE4_ID,
                PHASE5_ID,
                zombie.getUuid(),
                zombie.getX(),
                zombie.getY(),
                zombie.getZ()
            );
            return true;
        });
    }

    public static boolean isManagedPhaseZombie(Entity entity) {
        return entity instanceof ZombieEntity zombie && zombie.getCommandTags().contains(PHASE4_ZOMBIE_TAG);
    }

    public static void spawnPhase4Zombies(ServerWorld world) {
        cleanup(world.getServer());

        List<BlockPos> spawnPositions = buildAreaPositions(-191, -186, 41, -638, -635);
        List<BlockPos> destinationPositions = buildAreaPositions(-203, -203, 41, -639, -624);
        Random random = new Random(world.getRandom().nextLong());
        Collections.shuffle(spawnPositions, random);
        Collections.shuffle(destinationPositions, random);

        for (int i = 0; i < SPAWN_COUNT; i++) {
            BlockPos spawnPos = spawnPositions.get(i);
            BlockPos destinationPos = destinationPositions.get(i);
            spawn(world, Vec3d.ofBottomCenter(spawnPos), destinationPos);
        }
    }

    private static void tick(MinecraftServer server) {
        StoryManager storyManager = StoryModule.getStoryManager();
        boolean storyActive = storyManager.isActive();
        boolean phase4Active = storyActive && storyManager.isAtChapter(PHASE4_ID);
        boolean phase5Active = storyActive && storyManager.isAtChapter(PHASE5_ID);

        server.getWorlds().forEach(world -> world.iterateEntities().forEach(entity -> {
            if (!(entity instanceof ZombieEntity zombie) || !isManagedPhaseZombie(zombie)) {
                return;
            }

            if (phase4Active) {
                tickPhase4Zombie(zombie);
                return;
            }

            if (phase5Active) {
                tickPhase5Zombie(zombie);
                return;
            }

            cleanup(zombie);
        }));

        pruneMissingDestinations(server);
    }

    private static ZombieEntity spawn(ServerWorld world, Vec3d pos, BlockPos destinationPos) {
        ZombieEntity zombie = EntityType.ZOMBIE.create(world, SpawnReason.EVENT);
        if (zombie == null) {
            throw new IllegalStateException("Failed to create phase4 zombie entity.");
        }

        zombie.refreshPositionAndAngles(pos.x, pos.y, pos.z, zombie.getYaw(), zombie.getPitch());
        zombie.setPersistent();
        register(zombie, destinationPos);
        world.spawnEntity(zombie);
        return zombie;
    }

    private static void register(ZombieEntity zombie, BlockPos destinationPos) {
        zombie.addCommandTag(PHASE4_ZOMBIE_TAG);
        StoryCombatService.configureStoryZombieCombat(zombie);
        DESTINATIONS.put(zombie.getUuid(), destinationPos);
    }

    private static void tickPhase4Zombie(ZombieEntity zombie) {
        BlockPos destinationPos = DESTINATIONS.get(zombie.getUuid());
        if (destinationPos == null) {
            zombie.getNavigation().stop();
            return;
        }

        Vec3d destinationCenter = Vec3d.ofBottomCenter(destinationPos);
        if (zombie.squaredDistanceTo(destinationCenter) <= DESTINATION_REACHED_DISTANCE_SQUARED) {
            zombie.getNavigation().stop();
            return;
        }

        if (zombie.age % REPATH_INTERVAL_TICKS != 0 && zombie.getNavigation().isFollowingPath()) {
            return;
        }

        Path path = zombie.getNavigation().findPathTo(destinationPos, 1);
        if (path == null) {
            zombie.getNavigation().startMovingTo(destinationCenter.x, destinationCenter.y, destinationCenter.z, MOVE_SPEED);
            return;
        }

        zombie.getNavigation().startMovingAlong(path, MOVE_SPEED);
    }

    private static void tickPhase5Zombie(ZombieEntity zombie) {
        ensurePhase5Combat(zombie);
        ServerPlayerEntity target = findNearestAttackablePlayer(zombie);
        zombie.setTarget(target);
    }

    public static void cleanup(MinecraftServer server) {
        server.getWorlds().forEach(world -> world.iterateEntities().forEach(entity -> {
            if (!isManagedPhaseZombie(entity)) {
                return;
            }

            cleanup((ZombieEntity) entity);
        }));
        Iterator<Map.Entry<UUID, BlockPos>> iterator = DESTINATIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }

    private static void cleanup(ZombieEntity zombie) {
        DESTINATIONS.remove(zombie.getUuid());
        zombie.discard();
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

    private static void pruneMissingDestinations(MinecraftServer server) {
        Iterator<Map.Entry<UUID, BlockPos>> iterator = DESTINATIONS.entrySet().iterator();
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
            if (entity instanceof ZombieEntity zombie && zombie.getCommandTags().contains(PHASE4_ZOMBIE_TAG)) {
                return zombie;
            }
        }

        return null;
    }

    private static List<BlockPos> buildAreaPositions(int minX, int maxX, int y, int minZ, int maxZ) {
        List<BlockPos> positions = new ArrayList<>();
        for (int x = Math.min(minX, maxX); x <= Math.max(minX, maxX); x++) {
            for (int z = Math.min(minZ, maxZ); z <= Math.max(minZ, maxZ); z++) {
                positions.add(new BlockPos(x, y, z));
            }
        }
        return positions;
    }
}
