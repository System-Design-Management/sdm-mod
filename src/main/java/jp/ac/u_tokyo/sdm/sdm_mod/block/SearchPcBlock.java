package jp.ac.u_tokyo.sdm.sdm_mod.block;

import jp.ac.u_tokyo.sdm.sdm_mod.screen.SearchPcScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

public class SearchPcBlock extends Block {
    private static final Text TITLE = Text.translatable("screen.sdm_mod.search_pc.title");
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public SearchPcBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                SearchPcBlock::createScreenHandler,
                TITLE
            ));
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public VoxelShape getCullingShape(BlockState state) {
        return VoxelShapes.empty();
    }

    private static SearchPcScreenHandler createScreenHandler(
        int syncId,
        PlayerInventory inventory,
        PlayerEntity player
    ) {
        return new SearchPcScreenHandler(syncId, inventory);
    }
}
