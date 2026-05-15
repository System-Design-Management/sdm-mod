package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import jp.ac.u_tokyo.sdm.sdm_mod.entity.PoliceOfficerEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.game.CommandLockState;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Phase2PoliceOfficerGunTrigger {
    private static final Logger LOGGER = LoggerFactory.getLogger(Phase2PoliceOfficerGunTrigger.class);
    private static final String PHASE2_ID = "phase2";
    private static final double GUN_PICKUP_DISTANCE_SQUARED = 9.0D;
    private static final Set<UUID> RECEIVED_PLAYERS = new HashSet<>();

    private Phase2PoliceOfficerGunTrigger() {
    }

    public static void reset() {
        RECEIVED_PLAYERS.clear();
    }

    public static boolean hasReceivedGun(ServerPlayerEntity player) {
        return RECEIVED_PLAYERS.contains(player.getUuid());
    }

    public static void initialize() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            if (!(entity instanceof PoliceOfficerEntity)) {
                return ActionResult.PASS;
            }

            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE2_ID)) {
                return ActionResult.PASS;
            }

            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            return receiveGunIfNeeded(serverPlayer, (PoliceOfficerEntity) entity);
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient() || hand != Hand.MAIN_HAND) {
                return ActionResult.PASS;
            }

            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return ActionResult.PASS;
            }

            StoryManager storyManager = StoryModule.getStoryManager();
            if (!storyManager.isActive() || !storyManager.isAtChapter(PHASE2_ID)) {
                return ActionResult.PASS;
            }

            if (RECEIVED_PLAYERS.contains(player.getUuid())) {
                return ActionResult.PASS;
            }

            PoliceOfficerEntity policeOfficer = findNearbyPoliceOfficer(serverPlayer);
            if (policeOfficer == null) {
                return ActionResult.PASS;
            }

            return receiveGunIfNeeded(serverPlayer, policeOfficer);
        });
    }

    private static ActionResult receiveGunIfNeeded(ServerPlayerEntity player, PoliceOfficerEntity policeOfficer) {
        if (RECEIVED_PLAYERS.contains(player.getUuid())) {
            return ActionResult.SUCCESS;
        }

        RECEIVED_PLAYERS.add(player.getUuid());
        // プレイヤーにリボルバーを渡す
        giveRevolver(player);
        // 銃を渡したので警官のメインハンドを空にする。
        policeOfficer.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        Phase2TutorialDialogueService.handleGunPickup(player);
        LOGGER.info(
            "Player {} received revolver from police officer during phase2.",
            player.getName().getString()
        );
        return ActionResult.SUCCESS;
    }

    private static PoliceOfficerEntity findNearbyPoliceOfficer(ServerPlayerEntity player) {
        for (ServerWorld world : player.getServer().getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (!(entity instanceof PoliceOfficerEntity policeOfficer)) {
                    continue;
                }

                if (policeOfficer.getWorld() != player.getWorld()) {
                    continue;
                }

                if (player.squaredDistanceTo(policeOfficer) <= GUN_PICKUP_DISTANCE_SQUARED) {
                    return policeOfficer;
                }
            }
        }

        return null;
    }

    private static void giveRevolver(ServerPlayerEntity player) {
        CommandLockState.runUnlocked(() -> {
            ServerCommandSource source = player.getServer().getCommandSource()
                .withLevel(2)
                .withEntity(player)
                .withPosition(player.getPos())
                .withRotation(player.getRotationClient());
            player.getServer().getCommandManager().executeWithPrefix(source, "function thepa:give/revolver");
            player.getServer().getCommandManager().executeWithPrefix(source, "function thepa:give/bullets");
        });
    }
}
