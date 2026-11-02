package net.nullcoil.paritem.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.nullcoil.paritem.Paritem;
import net.nullcoil.paritem.blocks.custom.RedstoneCampfireBlockEntity;
import net.nullcoil.paritem.blocks.custom.hoppers.copper.CopperHopperBlockEntity;
import net.nullcoil.paritem.blocks.custom.hoppers.gold.GoldHopperBlockEntity;
import net.nullcoil.paritem.blocks.custom.hoppers.netherite.NetheriteHopperBlockEntity;

public class ModBlockEntities {
    public static BlockEntityType<RedstoneCampfireBlockEntity> REDSTONE_CAMPFIRE;
    public static BlockEntityType<CopperHopperBlockEntity> COPPER_HOPPER;
    public static BlockEntityType<GoldHopperBlockEntity> GOLD_HOPPER;
    public static BlockEntityType<NetheriteHopperBlockEntity> NETHERITE_HOPPER;

    public static void register() {
        // Redstone campfire (unchanged)
        REDSTONE_CAMPFIRE = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Paritem.MOD_ID, "redstone_campfire_be"),
                FabricBlockEntityTypeBuilder.create(
                        (pos, state) -> new RedstoneCampfireBlockEntity(REDSTONE_CAMPFIRE, pos, state),
                        ModBlocks.REDSTONE_CAMPFIRE
                ).build()
        );

        // Copper hopper – pass all 8 variants directly
        COPPER_HOPPER = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Paritem.MOD_ID, "copper_hopper_be"),
                FabricBlockEntityTypeBuilder.create(
                        CopperHopperBlockEntity::new,
                        ModBlocks.COPPER_HOPPER.weathering().unaffected(),
                        ModBlocks.COPPER_HOPPER.weathering().exposed(),
                        ModBlocks.COPPER_HOPPER.weathering().weathered(),
                        ModBlocks.COPPER_HOPPER.weathering().oxidized(),
                        ModBlocks.COPPER_HOPPER.waxed().unaffected(),
                        ModBlocks.COPPER_HOPPER.waxed().exposed(),
                        ModBlocks.COPPER_HOPPER.waxed().weathered(),
                        ModBlocks.COPPER_HOPPER.waxed().oxidized()
                ).build()
        );

        GOLD_HOPPER = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Paritem.MOD_ID, "gold_hopper_be"),
                FabricBlockEntityTypeBuilder.create(
                        GoldHopperBlockEntity::new,
                        ModBlocks.GOLD_HOPPER
                ).build()
        );

        NETHERITE_HOPPER = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Paritem.MOD_ID, "netherite_hopper_be"),
                FabricBlockEntityTypeBuilder.create(
                        NetheriteHopperBlockEntity::new,
                        ModBlocks.NETHERITE_HOPPER
                ).build()
        );

        Paritem.LOGGER.info("Registered Block Entities");
    }
}