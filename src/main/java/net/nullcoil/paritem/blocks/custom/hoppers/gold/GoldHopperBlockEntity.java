package net.nullcoil.paritem.blocks.custom.hoppers.gold;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.nullcoil.paritem.blocks.ModBlockEntities;
import net.nullcoil.paritem.gui.hoppers.gold.GoldHopperScreenHandler;

import java.util.Optional;

public class GoldHopperBlockEntity extends HopperBlockEntity {
    public static final int FILTER_SLOTS = 5;
    private final SimpleContainer filterInventory = new SimpleContainer(FILTER_SLOTS) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }
    };

    public GoldHopperBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.GOLD_HOPPER;
    }

    @Override
    public boolean isValidBlockState(BlockState state) {
        return state.getBlock() instanceof GoldHopperBlock;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.paritem.gold_hopper");
    }

    // Provide access to the filter inventory for the GUI
    public Container getFilterInventory() {
        return filterInventory;
    }

    // Override to prevent automated insertion if item matches any filter slot
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) return false;
        // Check all filter slots
        for (int i = 0; i < filterInventory.getContainerSize(); i++) {
            ItemStack filterStack = filterInventory.getItem(i);
            if (!filterStack.isEmpty() && ItemStack.isSameItemSameComponents(filterStack, stack)) {
                return false; // item type is blacklisted
            }
        }
        return super.canPlaceItem(slot, stack);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        Optional<ValueInput> filterChild = input.child("filter_inventory");
        if (filterChild.isPresent()) {
            NonNullList<ItemStack> tempList = NonNullList.withSize(FILTER_SLOTS, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(filterChild.get(), tempList);

            for (int i = 0; i < FILTER_SLOTS; i++) {
                filterInventory.setItem(i, tempList.get(i));
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput filterChild = output.child("filter_inventory");
        NonNullList<ItemStack> filterList = NonNullList.withSize(FILTER_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < FILTER_SLOTS; i++) {
            filterList.set(i, filterInventory.getItem(i));
        }
        ContainerHelper.saveAllItems(filterChild, filterList);
    }

    // Custom menu provider
    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        return new GoldHopperScreenHandler(id, playerInventory, this.filterInventory, this);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GoldHopperScreenHandler(id, playerInventory, this.filterInventory, this);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state); // drops the main hopper inventory

        // Also drop the filter inventory
        for (int i = 0; i < filterInventory.getContainerSize(); i++) {
            ItemStack stack = filterInventory.getItem(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }
}