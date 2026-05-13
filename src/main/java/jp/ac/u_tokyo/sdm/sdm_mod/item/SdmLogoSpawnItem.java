package jp.ac.u_tokyo.sdm.sdm_mod.item;

import jp.ac.u_tokyo.sdm.sdm_mod.ModEntities;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.SdmLogoEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
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
        Vec3d spawnPos = context.getBlockPos().up(3).toCenterPos().add(0.0f, 0.5f, 0.0f);
        entity.setPosition(spawnPos);

        PlayerEntity player = context.getPlayer();
        if (player != null) {
            double dx = player.getX() - spawnPos.x;
            double dz = player.getZ() - spawnPos.z;       
            float yaw = (float) (Math.atan2(-dx, dz) * (180.0 / Math.PI));
            entity.setYaw(yaw);
        }

        world.spawnEntity(entity);

        context.getStack().decrement(1);
        return ActionResult.SUCCESS;
    }
}
