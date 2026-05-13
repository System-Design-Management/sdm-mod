package jp.ac.u_tokyo.sdm.sdm_mod.item;

import jp.ac.u_tokyo.sdm.sdm_mod.ModEntities;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.PosterEntity;
import jp.ac.u_tokyo.sdm.sdm_mod.poster.PosterDefinition;
import jp.ac.u_tokyo.sdm.sdm_mod.poster.PosterRegistry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PosterItem extends Item {
    public PosterItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;

        Direction side = context.getSide();
        if (side == Direction.UP || side == Direction.DOWN) {
            return ActionResult.FAIL;
        }

        NbtComponent customData = context.getStack().get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null || !customData.contains("poster_id")) {
            return ActionResult.FAIL;
        }
        String posterId = customData.copyNbt().getString("poster_id").orElse("");
        PosterDefinition def = PosterRegistry.get(posterId);
        if (def == null) {
            return ActionResult.FAIL;
        }

        // Place slightly in front of the wall face to avoid z-fighting
        Vec3d spawnPos = context.getBlockPos().toCenterPos().add(
            side.getOffsetX() * 0.501,
            0,
            side.getOffsetZ() * 0.501
        );

        PosterEntity entity = new PosterEntity(ModEntities.POSTER, world);
        entity.setPosition(spawnPos);
        entity.setPosterId(posterId);
        entity.setPosterFacing(side);

        world.spawnEntity(entity);
        context.getStack().decrement(1);
        return ActionResult.SUCCESS;
    }
}
