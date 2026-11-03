package net.nullcoil.paritem.mixin;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Lets compat code correct a block state's light level after the block is built.
 * Used by {@link net.nullcoil.paritem.compat.ParitemWallLanterns}.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public interface BlockStateBaseAccessor {
    @Mutable
    @Accessor("lightEmission")
    void paritem$setLightEmission(int lightEmission);
}
