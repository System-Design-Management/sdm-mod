package jp.ac.u_tokyo.sdm.sdm_mod.client.render.entity.state;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.math.Direction;

public class PosterEntityRenderState extends EntityRenderState {
    public String posterId = "";
    public Direction facing = Direction.NORTH;
    public float width = 1.0f;
    public float height = 1.0f;
}
