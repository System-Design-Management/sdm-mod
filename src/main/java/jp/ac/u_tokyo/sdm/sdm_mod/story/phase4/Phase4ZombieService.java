package jp.ac.u_tokyo.sdm.sdm_mod.story.phase4;

import jp.ac.u_tokyo.sdm.sdm_mod.story.service.StoryCombatService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.MinecraftServer;
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
    private static final String PHASE4_ZOMBIE_TAG = "sdm_mod.phase4_zombie";
    private static final int SPAWN_COUNT = 10;
    private static final double MOVE_SPEED = 0.9D;
    private static final Map<UUID, BlockPos> DESTINATIONS = new HashMap<>();

    private Phase4ZombieService() {
    }

    public static void initialize() {
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> DESTINATIONS.remove(entity.getUuid()));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> DESTINATIONS.clear());
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
        startMovingToDestination(zombie, destinationPos);
    }

    private static void startMovingToDestination(ZombieEntity zombie, BlockPos destinationPos) {
        Vec3d destinationCenter = Vec3d.ofBottomCenter(destinationPos);
        zombie.getNavigation().startMovingTo(destinationCenter.x, destinationCenter.y, destinationCenter.z, MOVE_SPEED);
    }

    public static void cleanup(MinecraftServer server) {
        server.getWorlds().forEach(world -> world.iterateEntities().forEach(entity -> {
            if (!isManagedPhaseZombie(entity)) {
                return;
            }

            DESTINATIONS.remove(entity.getUuid());
            entity.discard();
        }));
        Iterator<Map.Entry<UUID, BlockPos>> iterator = DESTINATIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
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
