package net.nullcoil.paritem.mixin;


import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.nullcoil.paritem.blocks.custom.hoppers.copper.CopperHopperLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {

    /**
     * Somewhat invasive change to prevent the hopper from pushing out its last item.  Basically make the stacks
     * read as empty if they shouldn't be pushed out.
     */
    @Redirect(method = "ejectItems(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/HopperBlockEntity;)Z",
            at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;getItem(I)Lnet/minecraft/world/item/ItemStack;"),
            remap = false)
    private static ItemStack __getStack(HopperBlockEntity pushingInventory, int slot,
                                        // params from enclosing scope:
                                        Level level, BlockPos blockPos, HopperBlockEntity ignored) {
        final ItemStack original = pushingInventory.getItem(slot);
        if (!original.isEmpty() && CopperHopperLogic.get().shouldVetoPushFrom(pushingInventory, original, level, blockPos)) {
            return ItemStack.EMPTY;
        }
        return original;
    }

    /**
     * Apply filtering behavior to free floating entities above the hopper.
     */
    @Inject(method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private static void __extract_itemEntity(Container pullingInventory, ItemEntity pulledEntity, CallbackInfoReturnable<Boolean> returnable) {
        if (CopperHopperLogic.get().shouldVetoPullInto(pullingInventory, pulledEntity.getItem())) {
            returnable.setReturnValue(false);
        }
    }
}
