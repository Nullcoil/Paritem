package net.nullcoil.paritem.gui.hoppers.gold;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nullcoil.paritem.gui.FilterSlot;
import net.nullcoil.paritem.gui.ModMenuTypes;

public class GoldHopperScreenHandler extends AbstractContainerMenu {
    public static final int FILTER_SLOTS = 5;
    public static final int HOPPER_SLOTS = 5;
    public static final int TOTAL_BLOCK_SLOTS = FILTER_SLOTS + HOPPER_SLOTS; // 10

    private final Container filterInventory;
    private final Container hopperInventory;

    // Client constructor (dummy containers)
    public GoldHopperScreenHandler(int id, Inventory playerInv) {
        this(id, playerInv, new SimpleContainer(FILTER_SLOTS), new SimpleContainer(HOPPER_SLOTS));
    }

    // Main constructor
    public GoldHopperScreenHandler(int id, Inventory playerInv,
                                   Container filterInventory,
                                   Container hopperInventory) {
        super(ModMenuTypes.GOLD_HOPPER_MENU, id);
        this.filterInventory = filterInventory;
        this.hopperInventory = hopperInventory;

        // Validate sizes
        checkContainerSize(filterInventory, FILTER_SLOTS);
        checkContainerSize(hopperInventory, HOPPER_SLOTS);

        filterInventory.startOpen(playerInv.player);
        hopperInventory.startOpen(playerInv.player);

        // --- Filter row (top) - slots 0..4 ---
        for (int col = 0; col < FILTER_SLOTS; col++) {
            int x = 44 + col * 18;
            int y = 20;
            final int slotIndex = col;
            this.addSlot(new FilterSlot(filterInventory, col, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    // No empty stack, and must not already exist in ANY filter slot (including this one)
                    if (stack.isEmpty()) return false;
                    return !isItemTypeInFilter(stack, slotIndex);
                }
            });
        }

        // --- Hopper row (bottom) - slots 5..9 ---
        for (int col = 0; col < HOPPER_SLOTS; col++) {
            int x = 44 + col * 18;
            int y = 38;
            this.addSlot(new Slot(hopperInventory, col, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    if (stack.isEmpty()) return false;
                    // Cannot place if any filter slot contains the same item type
                    return !isItemTypeInFilter(stack, -1);
                }
            });
        }

        // --- Player inventory (main + hotbar) ---
        // Main inventory slots: 9..35
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = 9 + row * 9 + col;
                int x = 8 + col * 18;
                int y = 69 + row * 18;
                this.addSlot(new Slot(playerInv, slotIndex, x, y));
            }
        }
        // Hotbar slots: 0..8
        for (int col = 0; col < 9; col++) {
            int x = 8 + col * 18;
            int y = 127;
            this.addSlot(new Slot(playerInv, col, x, y));
        }
    }

    // Helper: checks if an item type already exists in any filter slot (optionally excluding a specific slot)
    private boolean isItemTypeInFilter(ItemStack stack, int excludeSlot) {
        for (int i = 0; i < FILTER_SLOTS; i++) {
            if (i == excludeSlot) continue;
            ItemStack filterStack = filterInventory.getItem(i);
            if (!filterStack.isEmpty() && ItemStack.isSameItemSameComponents(filterStack, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack original = slot.getItem();
        ItemStack stack = original.copy();
        boolean success = false;

        // Moving from our block's inventories (filter or hopper) to player inventory
        if (slotIndex < TOTAL_BLOCK_SLOTS) {
            success = this.moveItemStackTo(stack, TOTAL_BLOCK_SLOTS, this.slots.size(), false);
        }
        // Moving from player inventory to our block
        else {
            // Rule: stack size == 1 -> try filter slots first (only empty ones)
            if (stack.getCount() == 1) {
                // Find first empty filter slot that also passes the duplicate check
                int emptyFilterSlot = -1;
                for (int i = 0; i < FILTER_SLOTS; i++) {
                    ItemStack filterStack = filterInventory.getItem(i);
                    if (filterStack.isEmpty() && !isItemTypeInFilter(stack, i)) {
                        emptyFilterSlot = i;
                        break;
                    }
                }
                if (emptyFilterSlot != -1) {
                    // Move the single item to that specific filter slot
                    Slot targetSlot = this.slots.get(emptyFilterSlot);
                    if (targetSlot.mayPlace(stack)) {
                        targetSlot.set(stack.copy());
                        slot.set(ItemStack.EMPTY);
                        success = true;
                    }
                }
            }

            // If size > 1 (or size == 1 but no empty filter slot available) -> try hopper slots
            if (!success) {
                // Use the standard moveItemStackTo for hopper slots (indices FILTER_SLOTS .. TOTAL_BLOCK_SLOTS-1)
                success = this.moveItemStackTo(stack, FILTER_SLOTS, TOTAL_BLOCK_SLOTS, false);
            }
        }

        if (success) {
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            return original;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.filterInventory.stopOpen(player);
        this.hopperInventory.stopOpen(player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.hopperInventory.stillValid(player);
    }
}