package jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.BloodZombieEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.util.Identifier;

public class BloodZombieEntityRenderer extends ZombieBaseEntityRenderer<BloodZombieEntity, ZombieEntityRenderState, ZombieEntityModel<ZombieEntityRenderState>> {
    private static final Identifier TEXTURE = Identifier.of(SdmMod.MOD_ID, "textures/entity/blood_zombe.png");

    public BloodZombieEntityRenderer(EntityRendererFactory.Context context) {
        super(context,
            new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)),
            new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE_BABY)),
            new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE_INNER_ARMOR)),
            new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE_OUTER_ARMOR)),
            new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE_BABY_INNER_ARMOR)),
            new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE_BABY_OUTER_ARMOR))
        );
    }

    @Override
    public ZombieEntityRenderState createRenderState() {
        return new ZombieEntityRenderState();
    }

    @Override
    public Identifier getTexture(ZombieEntityRenderState state) {
        return TEXTURE;
    }
}
