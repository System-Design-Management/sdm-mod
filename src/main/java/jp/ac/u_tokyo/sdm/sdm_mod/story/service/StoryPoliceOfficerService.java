package jp.ac.u_tokyo.sdm.sdm_mod.story.service;

import jp.ac.u_tokyo.sdm.sdm_mod.ModEntities;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.NpcPose;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.PoliceOfficerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;
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
        policeOfficer.setBodyYaw(0.0f);
        policeOfficer.setHeadYaw(0.0f);
        // 警官は倒れている演出のため FACE_DOWN 姿勢を設定する。
        policeOfficer.setNpcPose(NpcPose.FACE_DOWN);
        world.spawnEntity(policeOfficer);

        ItemStack revolver = new ItemStack(Items.CARROT_ON_A_STICK);
        NbtCompound gzData = new NbtCompound();
        gzData.putByte("capacity", (byte) 6);
        gzData.putByte("bullets", (byte) 0);
        gzData.putByte("reload_time", (byte) 3);
        gzData.putByte("id", (byte) 0);
        gzData.putFloat("debuff", -0.5f);
        NbtCompound customData = new NbtCompound();
        customData.put("gz_data", gzData);
        revolver.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));
        revolver.set(DataComponentTypes.ITEM_MODEL, Identifier.of("minecraft", "guns/revolver"));
        policeOfficer.equipStack(EquipmentSlot.MAINHAND, revolver);
    }

    public static boolean isManagedPoliceOfficer(Entity entity) {
        return entity instanceof PoliceOfficerEntity;
    }

    private static void clearManagedPoliceOfficers(MinecraftServer server) {
        server.getWorlds().forEach(world -> {
            List<Entity> toRemove = new ArrayList<>();
            world.iterateEntities().forEach(entity -> {
                if (isManagedPoliceOfficer(entity)) {
                    toRemove.add(entity);
                }
            });
            toRemove.forEach(Entity::discard);
        });
    }
}
