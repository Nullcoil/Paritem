package net.nullcoil.paritem.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.nullcoil.paritem.blocks.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class RedstoneCampfireBlock extends CampfireBlock {
    private final boolean spawnParticles;
    private final int fireDamage;

    public RedstoneCampfireBlock(boolean spawnParticles, int fireDamage, Properties properties) {
        super(spawnParticles, fireDamage, properties);
        this.spawnParticles = spawnParticles;
        this.fireDamage = fireDamage;
    }

    private boolean spawnParticles() { return spawnParticles; }
    private int fireDamage() { return fireDamage; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedstoneCampfireBlockEntity(ModBlockEntities.REDSTONE_CAMPFIRE, pos, state);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
        return direction == Direction.DOWN ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
        return getSignal(state, getter, pos, direction);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return state.getValue(LIT) ? createTickerHelper(type, ModBlockEntities.REDSTONE_CAMPFIRE,
                    RedstoneCampfireBlockEntity::particleTick) : null;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        if (state.getValue(LIT)) {
            RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> quickCheck =
                    RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);
            return createTickerHelper(type, ModBlockEntities.REDSTONE_CAMPFIRE,
                    (l, p, s, e) -> RedstoneCampfireBlockEntity.cookTick(serverLevel, p, s, e, quickCheck));
        } else {
            return createTickerHelper(type, ModBlockEntities.REDSTONE_CAMPFIRE,
                    RedstoneCampfireBlockEntity::cooldownTick);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level,
                                          BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof RedstoneCampfireBlockEntity campfire) {
            ItemStack itemInHand = player.getItemInHand(hand);
            if (level.recipeAccess().propertySet(RecipePropertySet.CAMPFIRE_INPUT).test(itemInHand)) {
                if (level instanceof ServerLevel serverLevel && campfire.placeFood(serverLevel, player, itemInHand)) {
                    player.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
                    return InteractionResult.SUCCESS_SERVER;
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof RedstoneCampfireBlockEntity campfire) {
            for (int slot = 0; slot < campfire.getContainerSize(); slot++) {
                if (!campfire.getItem(slot).isEmpty()) {
                    ItemStack taken = campfire.takeItem(slot);
                    if (!taken.isEmpty()) {
                        // FIX: addItem merges into existing inventory stacks properly.
                        // setItemInHand was replacing the hand slot with exactly 1 item.
                        player.addItem(taken);
                        player.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }
}