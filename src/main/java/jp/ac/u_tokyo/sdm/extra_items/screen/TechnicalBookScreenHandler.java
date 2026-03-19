package jp.ac.u_tokyo.sdm.extra_items.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public final class TechnicalBookScreenHandler extends ScreenHandler {
    public TechnicalBookScreenHandler(int syncId, PlayerInventory playerInventory) {
        // 現状は表示専用のため、インベントリスロットはまだ持たない。
        super(ModScreenHandlers.TECHNICAL_BOOK, syncId);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        // TODO: 将来、所持中の専門書のみ開けるなどの利用条件を追加する。
        return true;
    }
}
