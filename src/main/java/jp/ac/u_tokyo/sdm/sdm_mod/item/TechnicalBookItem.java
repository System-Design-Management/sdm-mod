package jp.ac.u_tokyo.sdm.sdm_mod.item;

import jp.ac.u_tokyo.sdm.sdm_mod.screen.TechnicalBookScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public final class TechnicalBookItem extends BlockItem {
    private static final Text TITLE = Text.translatable("item.sdm_mod.technical_book");

    public TechnicalBookItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            // 空中で使ったときだけ UI を開き、ブロックを狙ったときの配置は BlockItem 側に任せる。
            user.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                TechnicalBookItem::createScreenHandler,
                TITLE
            ));
        }

        return ActionResult.SUCCESS;
    }

    private static TechnicalBookScreenHandler createScreenHandler(
        int syncId,
        PlayerInventory inventory,
        PlayerEntity player
    ) {
        // TODO: 進行状況やページ情報が必要になったら、ここで ScreenHandler に渡す。
        return new TechnicalBookScreenHandler(syncId, inventory);
    }
}
