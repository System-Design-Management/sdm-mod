package jp.ac.u_tokyo.sdm.sdm_mod.screen;

import jp.ac.u_tokyo.sdm.sdm_mod.warp.WarpDestination;
import jp.ac.u_tokyo.sdm.sdm_mod.warp.WarpWorldState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class WarpSelectScreenHandler extends net.minecraft.screen.ScreenHandler {
    public WarpSelectScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ModScreenHandlers.WARP_SELECT, syncId);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return false;
        }

        return WarpWorldState.getDestinationByIndex(id)
            .map(destination -> teleportToDestination(serverPlayer, destination))
            .orElse(false);
    }

    private static boolean teleportToDestination(ServerPlayerEntity player, WarpDestination destination) {
        BlockPos blockPos = destination.blockPos();

        // ブロック中心へ移動させると、到着直後に壁へめり込むリスクを減らせる。
        player.requestTeleport(
            blockPos.getX() + 0.5,
            blockPos.getY(),
            blockPos.getZ() + 0.5
        );
        player.sendMessage(
            Text.translatable(
                "screen.sdm_mod.warp.teleported",
                Text.translatable(destination.nameTranslationKey())
            ),
            true
        );
        player.closeHandledScreen();
        return true;
    }
}
