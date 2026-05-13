package jp.ac.u_tokyo.sdm.sdm_mod;

import jp.ac.u_tokyo.sdm.sdm_mod.entity.BoyEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.GirlEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.PoliceOfficerEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.StudentEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.SdmLogoEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public final class ModEntities {
    public static final EntityType<StudentEntity> STUDENT = register(
        "student",
        EntityType.Builder
            .create(StudentEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .maxTrackingRange(8)
    );

    public static final EntityType<GirlEntity> GIRL = register(
        "girl",
        EntityType.Builder
            .create(GirlEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .maxTrackingRange(8)
    );

    public static final EntityType<BoyEntity> BOY = register(
        "boy",
        EntityType.Builder
            .create(BoyEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .maxTrackingRange(8)
    );

    public static final EntityType<PoliceOfficerEntity> POLICE_OFFICER = register(
        "police_officer",
        EntityType.Builder
            .create(PoliceOfficerEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .maxTrackingRange(8)
    );

    public static final EntityType<SdmLogoEntity> SDM_LOGO = register(
        "sdm_logo",
        EntityType.Builder
            .create(SdmLogoEntity::new, SpawnGroup.MISC)
            .dimensions(2.0f, 2.0f)
            .maxTrackingRange(8)
    );

    private ModEntities() {
    }

    private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        Identifier id = Identifier.of(SdmMod.MOD_ID, name);
        RegistryKey<EntityType<?>> key = RegistryKey.of(Registries.ENTITY_TYPE.getKey(), id);
        return Registry.register(Registries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void initialize() {
        FabricDefaultAttributeRegistry.register(STUDENT, StudentEntity.createStudentAttributes());
        FabricDefaultAttributeRegistry.register(GIRL, GirlEntity.createGirlAttributes());
        FabricDefaultAttributeRegistry.register(BOY, BoyEntity.createBoyAttributes());
        FabricDefaultAttributeRegistry.register(POLICE_OFFICER, PoliceOfficerEntity.createPoliceOfficerAttributes());
    }
}
