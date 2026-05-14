package jp.ac.u_tokyo.sdm.sdm_mod.story.phase4;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Phase4FireworkService {
    private static final double LAUNCH_X = -192.0;
    private static final double LAUNCH_Y = 25.0;
    private static final double LAUNCH_Z = -603.0;
    private static final int FIREWORK_COUNT = 10;
    private static final int FLIGHT_DURATION = 3;
    private static final float SOUND_VOLUME = 10.0f;
    private static final float BLAST_SOUND_VOLUME = 100.0f;

    private static final AtomicBoolean launched = new AtomicBoolean(false);

    private Phase4FireworkService() {
    }

    /**
     * phase4 開始時に呼び出して、発射済みフラグをリセットする。
     */
    public static void reset() {
        launched.set(false);
    }

    /**
     * 最初に呼ばれたときだけ花火を打ち上げる（複数プレイヤーが同時にUIを閉じた場合の重複発射防止）。
     */
    public static void launchOnce(MinecraftServer server) {
        if (!launched.compareAndSet(false, true)) {
            return;
        }
        launch(server);
    }

    private static void launch(MinecraftServer server) {
        ServerWorld world = server.getOverworld();

        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        rocket.set(DataComponentTypes.FIREWORKS, new FireworksComponent(FLIGHT_DURATION, List.of()));

        for (int i = 0; i < FIREWORK_COUNT; i++) {
            FireworkRocketEntity firework = new FireworkRocketEntity(world, rocket, LAUNCH_X, LAUNCH_Y, LAUNCH_Z, false);
            world.spawnEntity(firework);
        }

        world.playSound(null, LAUNCH_X, LAUNCH_Y, LAUNCH_Z,
            SoundEvents.ENTITY_FIREWORK_ROCKET_SHOOT, SoundCategory.AMBIENT,
            SOUND_VOLUME, 1.0f);
        world.playSound(null, LAUNCH_X, LAUNCH_Y, LAUNCH_Z,
            SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST_FAR, SoundCategory.AMBIENT,
            BLAST_SOUND_VOLUME, 1.0f);
    }
}
