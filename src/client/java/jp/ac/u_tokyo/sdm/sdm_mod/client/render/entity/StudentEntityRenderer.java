package jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity;

import jp.ac.u_tokyo.sdm.sdm_mod.SdmMod;
import jp.ac.u_tokyo.sdm.sdm_mod.entity.StudentEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;

public class StudentEntityRenderer extends NpcEntityRenderer<StudentEntity> {

    private static final Identifier TEXTURE = Identifier.of(SdmMod.MOD_ID, "textures/entity/student.png");

    public StudentEntityRenderer(EntityRendererFactory.Context context) {
        super(context, false);
    }

    @Override
    public Identifier getTexture(PlayerEntityRenderState state) {
        return TEXTURE;
    }
}
