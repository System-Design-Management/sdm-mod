package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.ModEntities;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.BoyEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.GirlEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.NpcEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.NpcPose;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.StudentEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public final class StoryNpcSpawnService {

    /**
     * 1体分のスポーン情報。
     *
     * @param x    X座標
     * @param y    Y座標
     * @param z    Z座標
     * @param yaw  向き（度）: 0=南, 90=西, 180=北, 270=東
     * @param pose 姿勢（NpcPose）: STANDING / FACE_UP / FACE_DOWN / LYING_RIGHT / LYING_LEFT
     */
    private record SpawnEntry(double x, double y, double z, float yaw, NpcPose pose) {}

    // --- 配置情報をここに追加する ---

    private static final SpawnEntry[] BOY_ENTRIES = {
        new SpawnEntry(-148.0, 28.0, -625.0, 225.0f, NpcPose.FACE_DOWN),
        new SpawnEntry(-147.0, 25.4, -611.0, 30.0f, NpcPose.LYING_LEFT),
    };

    private static final SpawnEntry[] GIRL_ENTRIES = {
        // new SpawnEntry(x, y, z, yaw, NpcPose.STANDING),
        new SpawnEntry(-167.0, 29.0, -627.0, 45.0f, NpcPose.FACE_DOWN),
    };

    private static final SpawnEntry[] STUDENT_ENTRIES = {
        // new SpawnEntry(x, y, z, yaw, NpcPose.STANDING),
        new SpawnEntry(-170.0, 28.0, -623.0, 90.0f, NpcPose.FACE_DOWN),
    };

    // --------------------------------

    private StoryNpcSpawnService() {
    }

    public static void spawnAll(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        clearAll(server);
        spawnGroup(world, ModEntities.BOY, BOY_ENTRIES);
        spawnGroup(world, ModEntities.GIRL, GIRL_ENTRIES);
        spawnGroup(world, ModEntities.STUDENT, STUDENT_ENTRIES);
    }

    private static <T extends NpcEntity> void spawnGroup(
        ServerWorld world,
        EntityType<T> entityType,
        SpawnEntry[] entries
    ) {
        for (SpawnEntry entry : entries) {
            ChunkPos chunk = new ChunkPos(BlockPos.ofFloored(entry.x(), entry.y(), entry.z()));
            world.getChunk(chunk.x, chunk.z);

            T entity = entityType.create(world, SpawnReason.EVENT);
            if (entity == null) {
                throw new IllegalStateException("Failed to create entity: " + entityType);
            }
            entity.refreshPositionAndAngles(entry.x(), entry.y(), entry.z(), entry.yaw(), 0.0f);
            // AI無効エンティティは bodyYaw / headYaw が自動更新されないため明示的に設定する。
            // setupTransforms はレンダラーが bodyYaw を参照するため、これを設定しないと yaw が反映されない。
            entity.setBodyYaw(entry.yaw());
            entity.setHeadYaw(entry.yaw());
            entity.setNpcPose(entry.pose());
            world.spawnEntity(entity);
        }
    }

    private static void clearAll(MinecraftServer server) {
        server.getWorlds().forEach(world -> {
            List<Entity> toRemove = new ArrayList<>();
            world.iterateEntities().forEach(entity -> {
                if (entity instanceof BoyEntity
                    || entity instanceof GirlEntity
                    || entity instanceof StudentEntity) {
                    toRemove.add(entity);
                }
            });
            toRemove.forEach(Entity::discard);
        });
    }
}
