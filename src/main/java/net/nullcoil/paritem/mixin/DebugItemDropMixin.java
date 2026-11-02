package net.nullcoil.paritem.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class DebugItemDropMixin {
    @Inject(method = "addFreshEntity", at = @At("HEAD"))
    private void onAddFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ItemEntity itemEntity) {
            System.out.println("=== ITEM DROP: " + itemEntity.getItem() + " ===");
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                System.out.println("  at " + element);
            }
        }
    }
}
