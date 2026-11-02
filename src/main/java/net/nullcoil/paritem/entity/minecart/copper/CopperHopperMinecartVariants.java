package net.nullcoil.paritem.entity.minecart.copper;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.nullcoil.paritem.blocks.ModBlocks;
import net.nullcoil.paritem.items.ModItems;

public final class CopperHopperMinecartVariants {

    public static Block getBlock(WeatherState state, boolean waxed) {
        // COPPER_HOPPER is a WeatheringCopperCollection<Block>
        var collection = ModBlocks.COPPER_HOPPER;

        return switch (state) {
            case UNAFFECTED -> waxed ? collection.waxed().unaffected()
                                     : collection.weathering().unaffected();
            case EXPOSED    -> waxed ? collection.waxed().exposed()
                                     : collection.weathering().exposed();
            case WEATHERED  -> waxed ? collection.waxed().weathered()
                                     : collection.weathering().weathered();
            case OXIDIZED   -> waxed ? collection.waxed().oxidized()
                                     : collection.weathering().oxidized();
        };
    }

    public static Item getItem(WeatherState state, boolean waxed) {
        // Once you have the minecart items registered the same way
        // (or as individual fields), mirror the same pattern here.
        // For now this assumes individual item fields; adjust if you
        // also put the minecart items into a WeatheringCopperCollection.

        return switch (state) {
            case UNAFFECTED -> waxed ? ModItems.COPPER_HOPPER_MINECART.waxed().unaffected()
                                     : ModItems.COPPER_HOPPER_MINECART.weathering().unaffected();
            case EXPOSED    -> waxed ? ModItems.COPPER_HOPPER_MINECART.waxed().exposed()
                                     : ModItems.COPPER_HOPPER_MINECART.weathering().exposed();
            case WEATHERED  -> waxed ? ModItems.COPPER_HOPPER_MINECART.waxed().weathered()
                                     : ModItems.COPPER_HOPPER_MINECART.weathering().weathered();
            case OXIDIZED   -> waxed ? ModItems.COPPER_HOPPER_MINECART.waxed().oxidized()
                                     : ModItems.COPPER_HOPPER_MINECART.weathering().oxidized();
        };
    }

    private CopperHopperMinecartVariants() {}
}