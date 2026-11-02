package net.nullcoil.paritem.blocks.custom.hoppers.netherite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nullcoil.paritem.blocks.ModBlockEntities;

public class NetheriteHopperBlockEntity extends HopperBlockEntity {
    private static final int TRANSFER_AMOUNT = 5;

    public NetheriteHopperBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.NETHERITE_HOPPER;
    }

    @Override
    public boolean isValidBlockState(BlockState state) { return state.getBlock() instanceof NetheriteHopperBlock; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.paritem.netherite_hopper");
    }

    public static void pushItemsTick(Level level, BlockPos pos, BlockState state,
                                     NetheriteHopperBlockEntity entity) {
        --entity.cooldownTime;
        if (!entity.isOnCooldown()) {
            entity.setCooldown(0);
            boolean anyMoved = tryMoveItemsMultiple(level, pos, state, entity);
            if (anyMoved) {
                entity.setCooldown(8); // vanilla hopper uses 8 ticks
                setChanged(level, pos, state);
            }
        }
    }

    private static boolean tryMoveItemsMultiple(Level level, BlockPos pos, BlockState state,
                                             NetheriteHopperBlockEntity entity) {
        if (level.isClientSide()) return false;
        if (!state.getValue(NetheriteHopperBlock.ENABLED)) return false;

        boolean anyChanged = false;
        for (int i = 0; i < TRANSFER_AMOUNT; i++) {
            boolean changed = false;

            if (!entity.isEmpty()) {
                changed = ejectOneItem(level, pos, entity);
            }

            if (!entity.inventoryFull()) {
                changed |= HopperBlockEntity.suckInItems(level, entity);
            }

            if (!changed) break;

            anyChanged = true;
        }

        return anyChanged;
    }

    private static boolean ejectOneItem(Level level, BlockPos pos, NetheriteHopperBlockEntity self) {
        BlockPos target = pos.relative(self.getFacing());
        Container container = HopperBlockEntity.getContainerAt(level, target);
        if (container == null) return false;

        Direction insertFace = self.getFacing().getOpposite();
        for (int slot = 0; slot < self.getContainerSize(); slot++) {
            ItemStack stack = self.getItem(slot);
            if (stack.isEmpty()) continue;

            int originalCount = stack.getCount();
            ItemStack remainder = HopperBlockEntity.addItem(
                    self, container, self.removeItem(slot, 1), insertFace);

            if (remainder.isEmpty()) {
                container.setChanged();
                return true;
            }

            stack.setCount(originalCount);
            if (originalCount == 1) {
                self.setItem(slot, stack);
            }
        }
        return false;
    }

    private Direction getFacing() {
        BlockState state = this.getBlockState();
        return state.getValue(NetheriteHopperBlock.FACING);
    }
}
