package net.nullcoil.paritem.entity.minecart;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

public abstract class ParitemAbstractMinecart extends MinecartHopper {
    public ParitemAbstractMinecart(EntityType<? extends ParitemAbstractMinecart> type, Level level) {
        super(type, level);
    }

    public abstract @NonNull BlockState getDisplayBlockState();
    public abstract Item getCartItem();

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return getDisplayBlockState();
    }
    @Override
    protected Item getDropItem() {
        return getCartItem();
    }
    @Override
    public ItemStack getPickResult() {
        return new ItemStack(getCartItem());
    }
    @Override
    public Component getDisplayName() {
        return Component.translatable(getCartItem().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return super.createMenu(containerId, inventory);
    }
}
