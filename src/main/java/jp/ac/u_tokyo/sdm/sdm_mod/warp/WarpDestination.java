package jp.ac.u_tokyo.sdm.sdm_mod.warp;

import net.minecraft.util.math.BlockPos;

public record WarpDestination(
    String id,
    String nameTranslationKey,
    BlockPos blockPos
) {
}
