package net.nullcoil.paritem.blocks.custom.lanterns;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneLantern extends LanternBlock {
    public RedstoneLantern(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
        boolean isHanging =  (boolean) state.getValue(HANGING);

        return (boolean) state.getValue(HANGING) ?
                (direction != Direction.UP ? 15 : 0) : (direction != Direction.DOWN ? 15 : 0);
    }
}
