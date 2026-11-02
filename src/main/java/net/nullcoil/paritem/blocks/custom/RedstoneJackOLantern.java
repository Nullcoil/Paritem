package net.nullcoil.paritem.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneJackOLantern extends CarvedPumpkinBlock {
    public RedstoneJackOLantern(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
        return state.getValue(FACING).getOpposite() == direction ? 15 : 0;
    }

    protected int getDirectSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
        return getSignal(state, getter, pos, direction);
    }
}
