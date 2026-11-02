package net.nullcoil.paritem.gui;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.nullcoil.paritem.Paritem;
import net.nullcoil.paritem.gui.hoppers.copper.CopperHopperScreenHandler;
import net.nullcoil.paritem.gui.hoppers.gold.GoldHopperScreenHandler;

public class ModMenuTypes {
    public static MenuType<CopperHopperScreenHandler> COPPER_HOPPER_MENU;
    public static MenuType<GoldHopperScreenHandler>   GOLD_HOPPER_MENU;

    public static void register() {
        COPPER_HOPPER_MENU = Registry.register(
                BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(Paritem.MOD_ID, "copper_hopper_menu"),
                new MenuType<>(CopperHopperScreenHandler::new, FeatureFlags.VANILLA_SET));
        GOLD_HOPPER_MENU = Registry.register(
                BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(Paritem.MOD_ID, "gold_hopper_menu"),
                new MenuType<>(GoldHopperScreenHandler::new, FeatureFlags.VANILLA_SET));

        Paritem.LOGGER.info("Registered Menu Types");
    }
}