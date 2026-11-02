package net.nullcoil.paritem.items;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperCollection;
import net.nullcoil.paritem.Paritem;
import net.nullcoil.paritem.entity.ModEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModItems {

    public static final WeatheringCopperCollection<Item> COPPER_HOPPER_MINECART;
    public static final Item GOLD_HOPPER_MINECART;
    public static final Item NETHERITE_NUGGET;
    public static final Item NETHERITE_HOPPER_MINECART;

    static {
        Item unaffected     = registerCopperMinecart("copper_hopper_minecart", false, WeatheringCopper.WeatherState.UNAFFECTED);
        Item exposed        = registerCopperMinecart("exposed_copper_hopper_minecart", false, WeatheringCopper.WeatherState.EXPOSED);
        Item weathered      = registerCopperMinecart("weathered_copper_hopper_minecart", false, WeatheringCopper.WeatherState.WEATHERED);
        Item oxidized       = registerCopperMinecart("oxidized_copper_hopper_minecart", false, WeatheringCopper.WeatherState.OXIDIZED);

        Item waxedUnaffected = registerCopperMinecart("waxed_copper_hopper_minecart", true, WeatheringCopper.WeatherState.UNAFFECTED);
        Item waxedExposed    = registerCopperMinecart("waxed_exposed_copper_hopper_minecart", true, WeatheringCopper.WeatherState.EXPOSED);
        Item waxedWeathered  = registerCopperMinecart("waxed_weathered_copper_hopper_minecart", true, WeatheringCopper.WeatherState.WEATHERED);
        Item waxedOxidized   = registerCopperMinecart("waxed_oxidized_copper_hopper_minecart", true, WeatheringCopper.WeatherState.OXIDIZED);

        COPPER_HOPPER_MINECART = new WeatheringCopperCollection<>(
                new WeatheringCopperCollection.ByState<>(unaffected, exposed, weathered, oxidized),
                new WeatheringCopperCollection.ByState<>(waxedUnaffected, waxedExposed, waxedWeathered, waxedOxidized)
        );

        // ----- Other items -----
        GOLD_HOPPER_MINECART = registerItem(
                "gold_hopper_minecart",
                props -> new MinecartItem(ModEntities.GOLD_HOPPER_MINECART, props),
                new Item.Properties().stacksTo(1));

        NETHERITE_NUGGET = registerItem("netherite_nugget", Item::new, new Item.Properties());

        NETHERITE_HOPPER_MINECART = registerItem(
                "netherite_hopper_minecart",
                props -> new MinecartItem(ModEntities.NETHERITE_HOPPER_MINECART, props),
                new Item.Properties().stacksTo(1)
        );
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Item registerCopperMinecart(String name, boolean waxed, WeatheringCopper.WeatherState state) {
        return registerItem(
                name,
                props -> new CopperHopperMinecartItem(props, state, waxed),
                new Item.Properties().stacksTo(1)
        );
    }

    private static Item registerItem(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        Identifier id = Identifier.fromNamespaceAndPath(Paritem.MOD_ID, name);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        Item item = factory.apply(properties.setId(itemKey));
        return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }

    private static void addWeatheringSet(FabricCreativeModeTabOutput entries, Item after, WeatheringCopperCollection<Item> set) {
        List<Item> items = new ArrayList<>();
        set.forEach(items::add);
        entries.insertAfter(after, items.toArray(Item[]::new));
    }

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.accept(NETHERITE_NUGGET));

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS)
                .register(entries -> {
                    // Copper set first, then gold, then netherite
                    addWeatheringSet(entries, Items.HOPPER_MINECART, COPPER_HOPPER_MINECART);
                    entries.insertAfter(COPPER_HOPPER_MINECART.waxed().oxidized(), GOLD_HOPPER_MINECART, NETHERITE_HOPPER_MINECART);
                });
    }
}