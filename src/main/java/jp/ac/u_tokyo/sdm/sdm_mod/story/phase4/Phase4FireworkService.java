package jp.ac.u_tokyo.sdm.sdm_mod.story.phase4;

import jp.ac.u_tokyo.sdm.sdm_mod.ModSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FireworksComponent;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Phase4FireworkService {
    private static final double LAUNCH_X = -192.0;
    private static final double LAUNCH_Y = 25.0;
    private static final double LAUNCH_Z = -603.0;
    private static final int FIREWORK_COUNT = 10;
    private static final int FLIGHT_DURATION = 3;
    private static final float SOUND_VOLUME = 10.0f;

    // サウンドを4回、1秒（20tick）おきに再生する
    private static final int SOUND_TOTAL_PLAYS = 4;
    private static final int SOUND_INTERVAL_TICKS = 20;

    private static final AtomicBoolean launched = new AtomicBoolean(false);

    // 残り再生回数（0 = 再生なし）
    private static volatile int soundPlaysRemaining = 0;
    // 次の再生までの残りティック
    private static volatile int soundTicksUntilNext = 0;
    // サウンドを再生するサーバーインスタンス
    private static volatile MinecraftServer pendingServer = null;

    private Phase4FireworkService() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (soundPlaysRemaining <= 0) {
                return;
            }
            soundTicksUntilNext--;
            if (soundTicksUntilNext <= 0) {
                playFireworkSound(server.getOverworld());
                soundPlaysRemaining--;
                soundTicksUntilNext = SOUND_INTERVAL_TICKS;
                if (soundPlaysRemaining <= 0) {
                    Phase4ZombieService.cleanup(server);
                }
            }
        });
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

        // 1回目を即座に再生し、残り3回を1秒おきに予約する
        playFireworkSound(world);
        soundPlaysRemaining = SOUND_TOTAL_PLAYS - 1;
        soundTicksUntilNext = SOUND_INTERVAL_TICKS;
    }

    private static void playFireworkSound(ServerWorld world) {
        world.playSound(null, LAUNCH_X, LAUNCH_Y, LAUNCH_Z,
            ModSounds.FIREWORK, SoundCategory.AMBIENT,
            SOUND_VOLUME, 1.0f);
    }
}
