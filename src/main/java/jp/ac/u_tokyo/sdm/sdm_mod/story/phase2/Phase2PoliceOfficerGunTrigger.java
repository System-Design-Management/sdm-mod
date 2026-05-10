package jp.ac.u_tokyo.sdm.sdm_mod.story.phase2;

import jp.ac.u_tokyo.sdm.sdm_mod.entity.PoliceOfficerEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.story.StoryModule;
import jp.ac.u_tokyo.sdm.sdm_mod.story.runtime.StoryManager;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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

            if (RECEIVED_PLAYERS.contains(player.getUuid())) {
                return ActionResult.SUCCESS;
            }

            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            RECEIVED_PLAYERS.add(player.getUuid());
            // プレイヤーにリボルバーを渡す
            giveRevolver(serverPlayer);
            // 銃を渡したので警官のメインハンドを空にする。
            ((PoliceOfficerEntity) entity).equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            Phase2TutorialDialogueService.handleGunPickup(serverPlayer);
            LOGGER.info(
                "Player {} received revolver from police officer during phase2.",
                player.getName().getString()
            );
            return ActionResult.SUCCESS;
        });
    }

    private static void giveRevolver(ServerPlayerEntity player) {
        ServerCommandSource source = player.getServer().getCommandSource()
            .withLevel(2)
            .withEntity(player)
            .withPosition(player.getPos())
            .withRotation(player.getRotationClient());
        player.getServer().getCommandManager().executeWithPrefix(source, "function thepa:give/revolver");
        player.getServer().getCommandManager().executeWithPrefix(source, "function thepa:give/bullets");
    }
}
