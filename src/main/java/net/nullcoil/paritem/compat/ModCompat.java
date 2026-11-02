package net.nullcoil.paritem.compat;

import net.fabricmc.loader.api.FabricLoader;

public class ModCompat {
    public static final boolean COPPER_FIRE = FabricLoader.getInstance().isModLoaded("copperfire");

    private ModCompat() {}
}
