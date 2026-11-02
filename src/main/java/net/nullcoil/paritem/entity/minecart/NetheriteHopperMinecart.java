package net.nullcoil.paritem.entity.minecart;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.nullcoil.paritem.blocks.ModBlocks;
import net.nullcoil.paritem.items.ModItems;

/**
 * Netherite Hopper Minecart.
 * Attempts to transfer up to 5 items in *and* 5 items out per successful cycle,
 * matching the behaviour of the Netherite Hopper block.
 */
public class NetheriteHopperMinecart extends ParitemAbstractMinecart {

    private static final int TRANSFER_AMOUNT = 5;

    public NetheriteHopperMinecart(EntityType<? extends NetheriteHopperMinecart> type, Level level) {
        super(type, level);
    }

    // ------------------------------------------------------------------
    // Required abstract methods
    // ------------------------------------------------------------------

    @Override
    public BlockState getDisplayBlockState() {
        return ModBlocks.NETHERITE_HOPPER.defaultBlockState();
    }

    @Override
    public Item getCartItem() {
        return ModItems.NETHERITE_HOPPER_MINECART;
    }

    // ------------------------------------------------------------------
    // Multi-item transfer (pull + push)
    // ------------------------------------------------------------------

    /**
     * Called by the parent MinecartHopper every tick / track step.
     * We completely replace the normal single-item suck with a multi transfer loop.
     */
    @Override
    public boolean suckInItems() {
        if (this.level().isClientSide() || !this.isEnabled()) {
            return false;
        }

        boolean anyMoved = false;

        for (int i = 0; i < TRANSFER_AMOUNT; i++) {
            boolean moved = false;

            // --- PUSH (eject into container below / in facing direction) ---
            if (!this.isEmpty()) {
                moved |= tryEjectOneItem();
            }

            // --- PULL (from container above or item entities) ---
            if (!this.isFull()) {          // small helper, see below
                // First try container above
                if (HopperBlockEntity.suckInItems(this.level(), this)) {
                    moved = true;
                } else {
                    // Then try nearby item entities
                    AABB box = this.getBoundingBox().inflate(0.25, 0.0, 0.25);
                    for (ItemEntity itemEntity : this.level().getEntitiesOfClass(
                            ItemEntity.class, box, e -> e.isAlive() && !e.hasPickUpDelay())) {

                        if (HopperBlockEntity.addItem(this, itemEntity)) {
                            moved = true;
                            break;
                        }
                    }
                }
            }

            if (!moved) {
                break; // nothing more we can do this cycle
            }

            anyMoved = true;
        }

        return anyMoved;
    }

    /**
     * Tries to push one item into a container at the position the hopper is facing.
     * For a minecart this is almost always downward (into a chest/hopper etc. it is sitting on).
     */
    private boolean tryEjectOneItem() {
        // Hopper minecarts face downward for the purpose of the Hopper interface
        BlockPos targetPos = this.blockPosition().below();
        Container target = HopperBlockEntity.getContainerAt(this.level(), targetPos);

        if (target == null) {
            return false;
        }

        Direction insertSide = Direction.UP; // we are inserting from above the target

        for (int slot = 0; slot < this.getContainerSize(); slot++) {
            ItemStack stack = this.getItem(slot);
            if (stack.isEmpty()) continue;

            int originalCount = stack.getCount();
            ItemStack remainder = HopperBlockEntity.addItem(
                    this, target, this.removeItem(slot, 1), insertSide);

            if (remainder.isEmpty()) {
                target.setChanged();
                return true;
            }

            // put the item back if it couldn't be inserted
            stack.setCount(originalCount);
            if (originalCount == 1) {
                this.setItem(slot, stack);
            }
        }

        return false;
    }

    /** Small helper – true if every slot is occupied by a full stack. */
    private boolean isFull() {
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack stack = this.getItem(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new HopperMenu(containerId, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.paritem.netherite_hopper_minecart");
    }
}