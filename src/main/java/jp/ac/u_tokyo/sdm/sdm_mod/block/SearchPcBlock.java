package jp.ac.u_tokyo.sdm.sdm_mod.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class SearchPcBlock extends Block {
    public SearchPcBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getCullingShape(BlockState state) {
        return VoxelShapes.empty();
    }
}
