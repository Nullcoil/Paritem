package net.nullcoil.paritem.entity.minecart;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.nullcoil.paritem.blocks.ModBlocks;
import net.nullcoil.paritem.gui.hoppers.gold.GoldHopperScreenHandler;
import net.nullcoil.paritem.items.ModItems;

import java.util.Optional;

/**
 * Gold Hopper Minecart.
 * Same filter blacklist behaviour as the Gold Hopper block.
 * Transfer rate is vanilla (1 item).
 */
public class GoldHopperMinecart extends ParitemAbstractMinecart {

    public static final int FILTER_SLOTS = 5;

    private final SimpleContainer filterInventory = new SimpleContainer(FILTER_SLOTS) {
        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            // Mark the cart dirty so the filter is saved
            GoldHopperMinecart.this.setChanged();
        }
    };

    public GoldHopperMinecart(EntityType<? extends GoldHopperMinecart> type, Level level) {
        super(type, level);
    }

    // ------------------------------------------------------------------
    // Required abstract methods
    // ------------------------------------------------------------------

    @Override
    public BlockState getDisplayBlockState() {
        return ModBlocks.GOLD_HOPPER.defaultBlockState();
    }

    @Override
    public Item getCartItem() {
        return ModItems.GOLD_HOPPER_MINECART;
    }

    // ------------------------------------------------------------------
    // Filter logic (identical to GoldHopperBlockEntity)
    // ------------------------------------------------------------------

    public Container getFilterInventory() {
        return filterInventory;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) return false;

        for (int i = 0; i < filterInventory.getContainerSize(); i++) {
            ItemStack filterStack = filterInventory.getItem(i);
            if (!filterStack.isEmpty() && ItemStack.isSameItemSameComponents(filterStack, stack)) {
                return false; // blacklisted
            }
        }
        return super.canPlaceItem(slot, stack);
    }

    // ------------------------------------------------------------------
    // Save / load filter
    // ------------------------------------------------------------------

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        ValueOutput filterChild = output.child("filter_inventory");
        NonNullList<ItemStack> filterList = NonNullList.withSize(FILTER_SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < FILTER_SLOTS; i++) {
            filterList.set(i, filterInventory.getItem(i));
        }
        ContainerHelper.saveAllItems(filterChild, filterList);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        Optional<ValueInput> filterChild = input.child("filter_inventory");
        if (filterChild.isPresent()) {
            NonNullList<ItemStack> tempList = NonNullList.withSize(FILTER_SLOTS, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(filterChild.get(), tempList);

            for (int i = 0; i < FILTER_SLOTS; i++) {
                filterInventory.setItem(i, tempList.get(i));
            }
        }
    }

    // ------------------------------------------------------------------
    // Menu
    // ------------------------------------------------------------------

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new GoldHopperScreenHandler(containerId, inventory, this.filterInventory, this);
    }

    // Optional but good practice – some code paths call the 3-arg version
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new GoldHopperScreenHandler(containerId, inventory, this.filterInventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.paritem.gold_hopper_minecart");
    }
}