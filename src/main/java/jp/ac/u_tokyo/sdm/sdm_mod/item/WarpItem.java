package jp.ac.u_tokyo.sdm.sdm_mod.item;

import jp.ac.u_tokyo.sdm.sdm_mod.screen.WarpSelectScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public final class WarpItem extends Item {
    private static final Text TITLE = Text.translatable("item.sdm_mod.warp_tablet");

    public WarpItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            user.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                WarpItem::createScreenHandler,
                TITLE
            ));
        }

        return ActionResult.SUCCESS;
    }

    private static WarpSelectScreenHandler createScreenHandler(
        int syncId,
        PlayerInventory inventory,
        PlayerEntity player
    ) {
        return new WarpSelectScreenHandler(syncId, inventory);
    }
}
