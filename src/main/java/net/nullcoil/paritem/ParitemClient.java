package net.nullcoil.paritem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.nullcoil.paritem.blocks.ModBlockEntities;
import net.nullcoil.paritem.client.RedstoneCampfireRenderer;
import net.nullcoil.paritem.entity.ModEntities;
import net.nullcoil.paritem.gui.ModMenuTypes;
import net.nullcoil.paritem.gui.hoppers.copper.CopperHopperScreen;
import net.nullcoil.paritem.gui.hoppers.gold.GoldHopperScreen;

public class ParitemClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(
                ModEntities.COPPER_HOPPER_MINECART,
                context -> new MinecartRenderer(context, ModelLayers.HOPPER_MINECART));
        EntityRendererRegistry.register(
                ModEntities.GOLD_HOPPER_MINECART,
                context -> new MinecartRenderer(context, ModelLayers.HOPPER_MINECART));
        EntityRendererRegistry.register(
                ModEntities.NETHERITE_HOPPER_MINECART,
                context -> new MinecartRenderer(context, ModelLayers.HOPPER_MINECART));
        BlockEntityRendererRegistry.register(ModBlockEntities.REDSTONE_CAMPFIRE, RedstoneCampfireRenderer::new);
        MenuScreens.register(ModMenuTypes.COPPER_HOPPER_MENU, CopperHopperScreen::new);
        MenuScreens.register(ModMenuTypes.GOLD_HOPPER_MENU, GoldHopperScreen::new);
    }
}
