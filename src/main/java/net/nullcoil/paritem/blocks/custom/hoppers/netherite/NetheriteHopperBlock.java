package net.nullcoil.paritem.blocks.custom.hoppers.netherite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nullcoil.paritem.blocks.ModBlockEntities;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class NetheriteHopperBlock extends HopperBlock {

    private final Function<BlockState, VoxelShape> shapes;

    public NetheriteHopperBlock(Properties properties) {
        super(properties);
        // The hollow interior (same as vanilla) – used only to hollow out the top ring
        VoxelShape inside = Block.column(12.0, 11.0, 16.0);
        this.shapes = this.makeShapes(inside);
    }

    private Function<BlockState, VoxelShape> makeShapes(final VoxelShape inside) {
        // Top ring (unchanged)
        VoxelShape topRingOuter = Block.box(0, 10, 0, 16, 16, 16);
        VoxelShape topRing = Shapes.join(topRingOuter, inside, BooleanOp.ONLY_FIRST);

        // For DOWN: just the top ring + a solid 8x10x8 funnel from y=0-10
        VoxelShape downFunnelOuter = Block.box(4, 0, 4, 12, 10, 12);
        VoxelShape downFunnelInner = Block.box(5, -1, 5, 11, 10, 11);
        VoxelShape downFunnel = Shapes.join(downFunnelOuter, downFunnelInner, BooleanOp.ONLY_FIRST);

        // For horizontal facings: one continuous box from inside to outside\
        VoxelShape northFunnelOuter = Block.box(4, 2, 0, 12, 10, 12);
        VoxelShape northFunnelInner = Block.box(5, 3, 0, 11, 9, 11);
        VoxelShape northFunnel = Shapes.join(northFunnelOuter, northFunnelInner, BooleanOp.ONLY_FIRST);


        VoxelShape southFunnelOuter = Block.box(4, 2, 4, 12, 10, 16);
        VoxelShape westFunnelOuter  = Block.box(0, 2, 4, 12, 10, 12);
        VoxelShape eastFunnelOuter  = Block.box(4, 2, 4, 16, 10, 12);

        Map<Direction, VoxelShape> horizontalSpouts = Shapes.rotateAll(northFunnel);

        Map<Direction, VoxelShape> spouts = new EnumMap<>(Direction.class);
        spouts.putAll(horizontalSpouts);                 // for horizontals, use merged box
        spouts.put(Direction.DOWN, downFunnel);          // for DOWN, use the funnel as spout
        spouts.put(Direction.UP, downFunnel);

        // Base shape is only the top ring (no internal funnel, because funnel is part of the spout for horizontals)
        VoxelShape base = topRing;

        return this.getShapeForEachState(
                state -> {
                    Direction facing = state.getValue(FACING);
                    if (facing == Direction.DOWN) {
                        // DOWN: top ring + funnel (which is the spout)
                        return Shapes.or(base, spouts.get(facing));
                    } else {
                        // Horizontal: top ring + merged spout (which already includes the internal part)
                        return Shapes.or(base, spouts.get(facing));
                    }
                },
                new Property[]{ENABLED}
        );
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes.apply(state);
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        // Selection outline matches collision shape
        return getShape(state, level, pos, CollisionContext.empty());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NetheriteHopperBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, ModBlockEntities.NETHERITE_HOPPER, NetheriteHopperBlockEntity::pushItemsTick);
    }
}