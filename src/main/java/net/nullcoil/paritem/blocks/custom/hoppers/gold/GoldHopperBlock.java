// net.nullcoil.paritem.blocks.custom.hoppers.gold.GoldHopperBlock
package net.nullcoil.paritem.blocks.custom.hoppers.gold;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.nullcoil.paritem.blocks.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class GoldHopperBlock extends HopperBlock {
    public GoldHopperBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GoldHopperBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof GoldHopperBlockEntity goldHopper) {
            player.openMenu(goldHopper);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Use the vanilla hopper ticker - it works because GoldHopperBlockEntity extends HopperBlockEntity
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.GOLD_HOPPER, GoldHopperBlockEntity::pushItemsTick);
    }
}