/*
 * Portions derived from copper-hopper by pcal.net, used under the MIT License.
 * Modifications Copyright (c) 2024 nullcoil
 */

package net.nullcoil.paritem.blocks.custom.hoppers.copper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nullcoil.paritem.blocks.ModBlockEntities;
import net.nullcoil.paritem.gui.hoppers.copper.CopperHopperScreenHandler;

public class CopperHopperBlockEntity extends HopperBlockEntity implements WorldlyContainer {

    private static final int[] SLOTS = {0, 1, 2, 3, 4};

    public CopperHopperBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntities.COPPER_HOPPER; // adjust to your registration
    }

    /**
     * Prevents a vanilla state-validation crash when extending HopperBlockEntity
     * with a non-vanilla block. Any CopperHopperBlock variant (unaffected, exposed,
     * weathered, oxidized, waxed) is valid here.
     */
    @Override
    public boolean isValidBlockState(BlockState state) {
        return state.getBlock() instanceof CopperHopperBlock;
    }

    @Override
    protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return new CopperHopperScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.paritem.copper_hopper");
    }

    // WorldlyContainer — controls what can enter/leave through automation

    @Override
    public int[] getSlotsForFace(Direction ignored) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return !CopperHopperLogic.shouldVetoPushInto(this, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return !CopperHopperLogic.shouldVetoPullFrom(this, stack);
    }


}