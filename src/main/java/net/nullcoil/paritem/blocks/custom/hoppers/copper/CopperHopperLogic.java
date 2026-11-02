package net.nullcoil.paritem.blocks.custom.hoppers.copper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.nullcoil.paritem.Paritem;

import java.util.Collection;
import java.util.Collections;

import static java.util.Objects.requireNonNull;

public class CopperHopperLogic {

    public static Collection<Identifier> NBT_MATCH_ENABLED_IDS = Collections.emptySet();
    private boolean isRedstoneStrengthIgnoresFilterItems = false;

    private static final CopperHopperLogic INSTANCE = new CopperHopperLogic();
    public static CopperHopperLogic get() { return INSTANCE; }

    private Collection<Identifier> nbtMatchEnabledIDS = Collections.emptySet();
    public boolean isRedstoneStrengthIgnoresFilterItems() {
        return this.isRedstoneStrengthIgnoresFilterItems;
    }

    public static boolean shouldVetoPushInto(Container into, ItemStack pushedItem) {
        return !containsAtLeast(into, pushedItem, 1);
    }

    public static boolean shouldVetoPullInto(Container into, ItemStack pulledItem) {
        return isCopperHopper(into) && !containsAtLeast(into, pulledItem, 1);
    }

    public static boolean shouldVetoPullFrom(Container from, ItemStack pulledItem) {
        return isCopperHopper(from) && !containsAtLeast(from, pulledItem, 2);
    }

    public BlockEntityType<CopperHopperBlockEntity> getBlockEntityType() {
        return (BlockEntityType<CopperHopperBlockEntity>)
                requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(
                        Identifier.fromNamespaceAndPath(Paritem.MOD_ID, "copper_hopper_be")
                ));
    }

    public static boolean shouldVetoPushFrom(Container from, ItemStack pushedItem, Level world, BlockPos pos) {
        if (!isCopperHopper(from)) return false;
        if (!containsAtLeast(from, pushedItem, 2)) {
            return true; // never push the last one
        }
        // If we're pointing down, no need to check below
        if (((CopperHopperBlockEntity) from).getBlockState().getValue(HopperBlock.FACING) == Direction.DOWN) {
            return false;
        }
        // Check if the copper hopper below is trying to filter this item —
        // if so, hold onto it so the one below can pull it down instead
        final BlockPos below = pos.mutable().relative(Direction.Axis.Y, -1);
        final BlockEntity blockEntity = world.getBlockEntity(below);
        if (!isCopperHopper(blockEntity)) return false;
        return containsAtLeast((Container) blockEntity, pushedItem, 1);
    }

    private static boolean isCopperHopper(Container target) {
        return target instanceof CopperHopperBlockEntity;
    }

    private static boolean isCopperHopper(BlockEntity blockEntity) {
        return blockEntity instanceof CopperHopperBlockEntity;
    }

    private static boolean containsAtLeast(Container inventory, ItemStack inputItem, int atLeast) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            final ItemStack filterStack = inventory.getItem(i);
            if (isMatch(filterStack, inputItem)) {
                count += filterStack.getCount();
                if (count >= atLeast) return true;
            }
        }
        return false;
    }

    private static boolean isMatch(ItemStack first, ItemStack second) {
        if (second.isEmpty() || first.isEmpty()) return false;
        if (first == second) return true;
        return first.is(second.getItem()) &&
                (!NBT_MATCH_ENABLED_IDS.contains(BuiltInRegistries.ITEM.getKey(first.getItem())) ||
                        areComponentsEqual(first, second));
    }

    private static boolean areComponentsEqual(ItemStack left, ItemStack right) {
        if (left.isEmpty() && right.isEmpty()) return true;
        if (!left.isEmpty() && !right.isEmpty()) {
            DataComponentPatch l = left.getComponentsPatch();
            DataComponentPatch r = right.getComponentsPatch();
            return l != null && l.equals(r);
        }
        return false;
    }
}