package net.nullcoil.paritem.blocks;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.nullcoil.paritem.Paritem;
import net.nullcoil.paritem.compat.ModCompat;
import net.nullcoil.paritem.blocks.custom.RedstoneCampfireBlock;
import net.nullcoil.paritem.blocks.custom.RedstoneJackOLantern;
import net.nullcoil.paritem.blocks.custom.lanterns.RedstoneLantern;
import net.nullcoil.paritem.blocks.custom.WeatheringPressurePlate;
import net.nullcoil.paritem.blocks.custom.hoppers.copper.CopperHopperBlock;
import net.nullcoil.paritem.blocks.custom.hoppers.copper.WeatheringHopperBlock;
import net.nullcoil.paritem.blocks.custom.hoppers.gold.GoldHopperBlock;
import net.nullcoil.paritem.blocks.custom.hoppers.netherite.NetheriteHopperBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ModBlocks {

    // --- Redstone & Light Variants ---
    public static final Block REDSTONE_LANTERN;       // Gold variant of lantern
    public static final Block REDSTONE_UNLIT_LANTERN; // Unlit variant of Gold Lantern
    public static final Block UNLIT_LANTERN;          // Unlit variant of iron lanterns (regular and soul)
    public static final WeatheringCopperCollection<Block> COPPER_UNLIT_LANTERN; // Unlit variant of copper lanterns
    public static final Block COPPER_CAMPFIRE;        // null when Copper Fire (Crystal Nest) is loaded; it provides its own
    public static final Block REDSTONE_CAMPFIRE;      // Redstone variant of campfire
    public static final Block SOUL_JACKOLANTERN;      // Soul variant of Jack O'Lantern
    public static final Block COPPER_JACKOLANTERN;    // Copper variant of Jack O'Lantern
    public static final Block REDSTONE_JACKOLANTERN;  // Redstone variant of Jack O'Lantern

    // --- Gold Blocks ---
    public static final Block GOLD_CHAIN;
    public static final Block GOLD_BARS;
    public static final Block GOLD_DOOR;
    public static final Block GOLD_TRAPDOOR;

    // --- Netherite / Lava Blocks ---
    public static final Block LAVA_LANTERN;           // Netherite variant of lantern
    public static final Block UNLIT_LAVA_LANTERN;     // Unlit variant of Lava Lantern
    public static final Block NETHERITE_CHAIN;
    public static final Block NETHERITE_BARS;
    public static final Block NETHERITE_DOOR;
    public static final Block NETHERITE_TRAPDOOR;

    // --- Hoppers ---
    public static final WeatheringCopperCollection<Block> COPPER_HOPPER;
    public static final Block GOLD_HOPPER;
    public static final Block NETHERITE_HOPPER;

    // --- Pressure Plates ---
    public static final WeatheringCopperCollection<Block> WEIGHTED_PRESSURE_PLATE; // Copper variant of Weighted Pressure plates
    public static final Block REINFORCED_WEIGHTED_PRESSURE_PLATE;       // Netherite variant of Weighted Pressure plates

    // --- Block Collections ---
    public static List<Block> COPPER_HOPPERS = new ArrayList<>();
    public static List<Block> COPPER_PRESSURE_PLATES = new ArrayList<>();


    // ==========================================
    // STATIC INITIALIZATION (Block Registries)
    // ==========================================
    static {
        // --- Redstone & Light Variants ---
        REDSTONE_LANTERN = registerBlock(
                "redstone_lantern",
                RedstoneLantern::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).lightLevel(le -> 7));

        REDSTONE_UNLIT_LANTERN = registerBlock(
                "redstone_unlit_lantern",
                LanternBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).lightLevel(le -> 0));

        UNLIT_LANTERN = registerBlock(
                "unlit_lantern",
                LanternBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).lightLevel(le -> 0));

        COPPER_UNLIT_LANTERN = registerWeatheringCopper(
                "copper_unlit_lantern",
                (state, props) -> new LanternBlock(props),
                (state, props) -> new WeatheringLanternBlock(state, props),
                ws -> BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_LANTERN.weathering().unaffected()).lightLevel(x -> 0));

        COPPER_CAMPFIRE = ModCompat.COPPER_FIRE ? null : registerBlock(
                "copper_campfire",
                props -> new CampfireBlock(false, 1, props),
                BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE));

        REDSTONE_CAMPFIRE = registerBlock(
                "redstone_campfire",
                props -> new RedstoneCampfireBlock(false, 1, props),
                BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE).lightLevel(Blocks.litBlockEmission(7)));

        SOUL_JACKOLANTERN = registerBlock(
                "soul_jack_o_lantern",
                CarvedPumpkinBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.JACK_O_LANTERN).lightLevel(le -> 10));

        COPPER_JACKOLANTERN = registerBlock(
                "copper_jack_o_lantern",
                CarvedPumpkinBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.JACK_O_LANTERN));

        REDSTONE_JACKOLANTERN = registerBlock(
                "redstone_jack_o_lantern",
                RedstoneJackOLantern::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.JACK_O_LANTERN).lightLevel(le -> 10));

        // --- Gold Blocks ---
        GOLD_CHAIN = registerBlock(
                "gold_chain",
                ChainBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_CHAIN));

        GOLD_BARS = registerBlock(
                "gold_bars",
                IronBarsBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).sound(SoundType.METAL));

        GOLD_DOOR = registerBlock(
                "gold_door",
                props -> new DoorBlock(BlockSetType.GOLD, props),
                BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().isValidSpawn(Blocks::never));

        GOLD_TRAPDOOR = registerBlock(
                "gold_trapdoor",
                props -> new TrapDoorBlock(BlockSetType.GOLD, props),
                BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().isValidSpawn(Blocks::never));

        // --- Netherite / Lava Blocks ---
        LAVA_LANTERN = registerBlock(
                "lava_lantern",
                LanternBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).sound(SoundType.NETHERITE_BLOCK));

        UNLIT_LAVA_LANTERN = registerBlock(
                "lava_unlit_lantern",
                LanternBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).lightLevel(le -> 0).sound(SoundType.NETHERITE_BLOCK));

        NETHERITE_CHAIN = registerBlock(
                "netherite_chain",
                ChainBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_CHAIN));

        NETHERITE_BARS = registerBlock(
                "netherite_bars",
                IronBarsBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BARS).sound(SoundType.NETHERITE_BLOCK));

        NETHERITE_DOOR = registerBlock(
                "netherite_door",
                props -> new DoorBlock(ModBlockSets.NETHERITE, props),
                BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).noOcclusion().isValidSpawn(Blocks::never));

        NETHERITE_TRAPDOOR = registerBlock(
                "netherite_trapdoor",
                props -> new TrapDoorBlock(ModBlockSets.NETHERITE, props),
                BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).noOcclusion().isValidSpawn(Blocks::never));

        // --- Hoppers ---
        COPPER_HOPPER = registerWeatheringCopper(
                "copper_hopper",
                (state, props) -> new CopperHopperBlock(props),      // waxed factory (creates plain CopperHopperBlock)
                WeatheringHopperBlock::new,  // weathering factory (creates WeatheringHopperBlock)
                ws -> BlockBehaviour.Properties.ofFullCopy(Blocks.HOPPER).sound(SoundType.COPPER));

        GOLD_HOPPER = registerBlock(
                "gold_hopper",
                GoldHopperBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.HOPPER).sound(SoundType.METAL));

        NETHERITE_HOPPER = registerBlock(
                "netherite_hopper",
                NetheriteHopperBlock::new,
                BlockBehaviour.Properties.ofFullCopy(Blocks.HOPPER).sound(SoundType.NETHERITE_BLOCK));

        // --- Pressure Plates ---
        WEIGHTED_PRESSURE_PLATE = registerWeatheringCopper(
                "weighted_pressure_plate",
                (state, props) -> new WeightedPressurePlateBlock(75, BlockSetType.COPPER, props),
                WeatheringPressurePlate::new,
                ws -> BlockBehaviour.Properties.ofFullCopy(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE));

        REINFORCED_WEIGHTED_PRESSURE_PLATE = registerBlock(
                "reinforced_weighted_pressure_plate",
                props -> new WeightedPressurePlateBlock(300, ModBlockSets.NETHERITE, props),
                BlockBehaviour.Properties.ofFullCopy(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE));

        // --- Populate Collections ---
        COPPER_HOPPER.forEach(COPPER_HOPPERS::add);

        WEIGHTED_PRESSURE_PLATE.forEach(COPPER_PRESSURE_PLATES::add);
    }


    // ==========================================
    // LIFECYCLE & REGISTRATION METHODS
    // ==========================================
    /**
     * Registers a full set of weathering + waxed copper blocks.
     *
     * @param name          e.g. "copper_hopper"
     * @param waxedFactory      (state, props) -> new YourWaxedBlock(props)
     * @param weatheringFactory (state, props) -> new YourWeatheringBlock(state, props)
     * @param properties        (state) -> BlockBehaviour.Properties...
     */
    private static <WaxedBlock extends Block, WeatheringBlock extends Block & WeatheringCopper, Id> WeatheringCopperCollection<Block> registerWeatheringCopper(
            final String name,
            final BiFunction<WeatheringCopper.WeatherState, BlockBehaviour.Properties, WaxedBlock> waxedFactory,
            final BiFunction<WeatheringCopper.WeatherState, BlockBehaviour.Properties, WeatheringBlock> weatheringFactory,
            final Function<WeatheringCopper.WeatherState, BlockBehaviour.Properties> properties) {
        WeatheringCopperCollection<String> ids = WeatheringCopperCollection.prefixWithState(WeatheringCopperCollection.create(name));

        return WeatheringCopperCollection.registerBlocks(
                ids,
                ModBlocks::registerBlock,   // ← this matches the TriFunction
                waxedFactory,
                weatheringFactory,
                properties
        );
    }

    public static void register() {
        if (COPPER_CAMPFIRE != null) {
            BlockEntityTypes.CAMPFIRE.addValidBlock(COPPER_CAMPFIRE);
        } else {
            Paritem.LOGGER.info("Copper Fire detected; skipping Paritem's copper campfire.");
        }

        OxidizableBlocksRegistry.registerWeatheringCopperBlocks(COPPER_HOPPER);
        OxidizableBlocksRegistry.registerWeatheringCopperBlocks(COPPER_UNLIT_LANTERN);

        addItemsToCreativeTab();

        Paritem.LOGGER.info("Getting that guy his Blocks");
    }

    private static WeatheringCopperCollection<Item> itemsFromBlocks(WeatheringCopperCollection<Block> blocks) {
        return blocks.map(Block::asItem);
    }

    public static void addItemsToCreativeTab() {
        // ==================== FUNCTIONAL BLOCKS ====================
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> {
                    // Lanterns & Light sources
                    entries.insertAfter(Items.SOUL_LANTERN, REDSTONE_LANTERN);
                    entries.insertAfter(REDSTONE_LANTERN, LAVA_LANTERN);

                    // Unlit variants (grouped near their lit counterparts)
                    entries.insertAfter(Items.SOUL_LANTERN, UNLIT_LANTERN);
                    entries.insertAfter(REDSTONE_LANTERN, REDSTONE_UNLIT_LANTERN);
                    entries.insertAfter(LAVA_LANTERN, UNLIT_LAVA_LANTERN);

                    // Copper lanterns (unlit)
                    addWeatheringSet(entries, Items.COPPER_LANTERN.waxed().oxidized(), COPPER_UNLIT_LANTERN);

                    // Campfires
                    if (COPPER_CAMPFIRE != null) {
                        entries.insertAfter(Items.SOUL_CAMPFIRE, COPPER_CAMPFIRE, REDSTONE_CAMPFIRE);
                    } else {
                        entries.insertAfter(Items.SOUL_CAMPFIRE, REDSTONE_CAMPFIRE);
                    }

                    // Jack o' Lanterns
                    entries.insertAfter(Items.JACK_O_LANTERN, SOUL_JACKOLANTERN, COPPER_JACKOLANTERN, REDSTONE_JACKOLANTERN);

                    // Metal chains & bars
                    entries.insertAfter(Items.IRON_CHAIN, GOLD_CHAIN, NETHERITE_CHAIN);

                    entries.insertAfter(Items.IRON_BARS, GOLD_BARS, NETHERITE_BARS);
                });

        // ==================== REDSTONE BLOCKS ====================
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS)
                .register(entries -> {
                    // Redstone light sources
                    entries.insertAfter(Items.REDSTONE_TORCH, REDSTONE_LANTERN, REDSTONE_CAMPFIRE, REDSTONE_JACKOLANTERN);

                    // Hoppers - Copper set first, then gold, then netherite
                    addWeatheringSet(entries, Items.HOPPER, COPPER_HOPPER);
                    entries.insertAfter(COPPER_HOPPER.waxed().oxidized(), GOLD_HOPPER, NETHERITE_HOPPER);

                    // Weighted Pressure Plates
                    addWeatheringSet(entries, Items.LIGHT_WEIGHTED_PRESSURE_PLATE, WEIGHTED_PRESSURE_PLATE);
                    entries.insertAfter(Items.HEAVY_WEIGHTED_PRESSURE_PLATE, REINFORCED_WEIGHTED_PRESSURE_PLATE);
                });

        // ==================== BUILDING BLOCKS ====================
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(entries -> {
                    // Gold variants
                    entries.insertAfter(Items.GOLD_BLOCK,
                            GOLD_BARS, GOLD_DOOR, GOLD_TRAPDOOR, GOLD_CHAIN);

                    // Netherite variants
                    entries.insertAfter(Items.NETHERITE_BLOCK,
                            NETHERITE_BARS, NETHERITE_DOOR, NETHERITE_TRAPDOOR,
                            NETHERITE_CHAIN, REINFORCED_WEIGHTED_PRESSURE_PLATE);
                });
    }

    private static void addWeatheringSet(FabricCreativeModeTabOutput entries, Item after, WeatheringCopperCollection<Block> set) {
        List<Item> items = new ArrayList<>();
        set.forEach(block -> items.add(block.asItem()));
        entries.insertAfter(after, items.toArray(Item[]::new));
    }

    // ==========================================
    // INTERNAL HELPER METHODS
    // ==========================================
    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        return registerBlock(name, factory, properties, true);
    }

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties, boolean buildItem) {
        Identifier id = Identifier.tryBuild(Paritem.MOD_ID, name);

        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        Block block = factory.apply(properties.setId(blockKey));
        if(buildItem) {
            Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)));
        }

        // Register and return the Block
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }
}