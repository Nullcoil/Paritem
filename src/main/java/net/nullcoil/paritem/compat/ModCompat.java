package net.nullcoil.paritem.compat;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Soft-dependency checks. Evaluated once, before any Paritem blocks are registered.
 */
public final class ModCompat {
    /**
     * Copper Fire (Crystal Nest) adds its own Prometheus-powered {@code minecraft:copper_campfire}.
     * When it's present, Paritem steps aside and does not register its copper campfire or recipe.
     */
    public static final boolean COPPER_FIRE = FabricLoader.getInstance().isModLoaded("copper_fire");

    private ModCompat() {}
}
