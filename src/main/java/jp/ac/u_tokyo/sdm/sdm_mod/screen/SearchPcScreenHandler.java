package jp.ac.u_tokyo.sdm.sdm_mod.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public final class SearchPcScreenHandler extends ScreenHandler {
    public SearchPcScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ModScreenHandlers.SEARCH_PC, syncId);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
