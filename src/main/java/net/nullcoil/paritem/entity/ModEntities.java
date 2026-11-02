package net.nullcoil.paritem.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.nullcoil.paritem.Paritem;
import net.nullcoil.paritem.entity.minecart.GoldHopperMinecart;
import net.nullcoil.paritem.entity.minecart.NetheriteHopperMinecart;
import net.nullcoil.paritem.entity.minecart.copper.CopperHopperMinecart;

public class ModEntities {

    public static final EntityType<CopperHopperMinecart> COPPER_HOPPER_MINECART;
    public static final EntityType<GoldHopperMinecart> GOLD_HOPPER_MINECART;
    public static final EntityType<NetheriteHopperMinecart> NETHERITE_HOPPER_MINECART;

    static {
        COPPER_HOPPER_MINECART =
                register(
                        "copper_hopper_minecart",
                        EntityType.Builder.<CopperHopperMinecart>of(CopperHopperMinecart::new, MobCategory.MISC)
                                .sized(0.98f, 0.7f)
                                .clientTrackingRange(3)
                                .updateInterval(3));
        GOLD_HOPPER_MINECART =
                register(
                        "gold_hopper_minecart",
                        EntityType.Builder.<GoldHopperMinecart>of(GoldHopperMinecart::new, MobCategory.MISC)
                                .sized(0.98f, 0.7f)
                                .clientTrackingRange(8)
                                .updateInterval(3));
        NETHERITE_HOPPER_MINECART =
                register(
                        "netherite_hopper_minecart",
                        EntityType.Builder.<NetheriteHopperMinecart>of(NetheriteHopperMinecart::new, MobCategory.MISC)
                                .sized(0.98F, 0.7F)
                                .clientTrackingRange(8)
                                .updateInterval(3));
    }

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String name,
            EntityType.Builder<T> builder
    ) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Paritem.MOD_ID, name)
        );

        return Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                key,
                builder.build(key)
        );
    }

    public static void register() {
        Paritem.LOGGER.info("Registered Paritem entities");
    }
}