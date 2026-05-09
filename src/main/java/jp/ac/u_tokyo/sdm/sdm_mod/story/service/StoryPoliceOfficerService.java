package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.ModEntities;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.PoliceOfficerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public final class StoryPoliceOfficerService {
    private static final double PHASE2_POLICE_OFFICER_X = -161.0;
    private static final double PHASE2_POLICE_OFFICER_Y = 25.0;
    private static final double PHASE2_POLICE_OFFICER_Z = -609.0;

    private StoryPoliceOfficerService() {
    }

    public static void spawnPhase2PoliceOfficer(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        ChunkPos destinationChunk = new ChunkPos(BlockPos.ofFloored(
            PHASE2_POLICE_OFFICER_X,
            PHASE2_POLICE_OFFICER_Y,
            PHASE2_POLICE_OFFICER_Z
        ));

        world.getChunk(destinationChunk.x, destinationChunk.z);
        clearManagedPoliceOfficers(server);

        PoliceOfficerEntity policeOfficer = ModEntities.POLICE_OFFICER.create(world, SpawnReason.EVENT);
        if (policeOfficer == null) {
            throw new IllegalStateException("Failed to create police officer entity.");
        }

        policeOfficer.refreshPositionAndAngles(
            PHASE2_POLICE_OFFICER_X,
            PHASE2_POLICE_OFFICER_Y,
            PHASE2_POLICE_OFFICER_Z,
            0.0f,
            0.0f
        );
        world.spawnEntity(policeOfficer);
    }

    public static boolean isManagedPoliceOfficer(Entity entity) {
        return entity instanceof PoliceOfficerEntity;
    }

    private static void clearManagedPoliceOfficers(MinecraftServer server) {
        server.getWorlds().forEach(world -> world.iterateEntities().forEach(entity -> {
            if (isManagedPoliceOfficer(entity)) {
                entity.discard();
            }
        }));
    }
}
