package jp.ac.u_tokyo.sdm.sdm_mod.item;

import jp.ac.u_tokyo.sdm.sdm_mod.ModEntities;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.SdmLogoEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public class SdmLogoSpawnItem extends Item {
    public SdmLogoSpawnItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;

        SdmLogoEntity entity = new SdmLogoEntity(ModEntities.SDM_LOGO, world);
        entity.setPosition(context.getBlockPos().up(2).toCenterPos());
        world.spawnEntity(entity);

        context.getStack().decrement(1);
        return ActionResult.SUCCESS;
    }
}
