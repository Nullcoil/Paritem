package net.nullcoil.paritem.blocks;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.nullcoil.paritem.Paritem;

public class ModBlockSets {

    public static final BlockSetType NETHERITE = BlockSetType.register(new BlockSetType("netherite",
            false,
            false,
            false,
            BlockSetType.PressurePlateSensitivity.EVERYTHING,
            SoundType.NETHERITE_BLOCK,
            SoundEvents.IRON_DOOR_CLOSE,
            SoundEvents.IRON_DOOR_OPEN,
            SoundEvents.IRON_TRAPDOOR_CLOSE,
            SoundEvents.IRON_TRAPDOOR_OPEN,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
            SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
            SoundEvents.STONE_BUTTON_CLICK_OFF,
            SoundEvents.STONE_BUTTON_CLICK_ON));

    public static void register() {
        Paritem.LOGGER.info("Registering Block Sets for " + Paritem.MOD_ID);
    }
}
