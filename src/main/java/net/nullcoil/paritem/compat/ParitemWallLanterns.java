package net.nullcoil.paritem.compat;

import com.toastycarbon.walllanterns.WallLanternOptions;
import com.toastycarbon.walllanterns.fabric.WallLanternsEntrypoint;
import com.toastycarbon.walllanterns.fabric.WallLanternsRegistry;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.nullcoil.paritem.Paritem;
import net.nullcoil.paritem.blocks.ModBlocks;
import net.nullcoil.paritem.mixin.BlockStateBaseAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Compatibility with Lanterns Belong on Walls (mod id {@code walllanterns}).
 * <p>
 * Only loaded by Fabric when that mod is installed: it is declared under the {@code walllanterns}
 * entrypoint in fabric.mod.json, which nothing else ever reads.
 * <p>
 * Without this, Paritem's lanterns fall back to the mod's shared "compatibility" wall lantern, which
 * always emits light level 15. Registering them here gives each one a dedicated
 * {@code walllanterns:wall_<name>} block. Those blocks still copy the vanilla lantern's properties
 * (light 15), so as each one is registered we set its light back to what the source lantern emits.
 */
public class ParitemWallLanterns implements WallLanternsEntrypoint {
    private static final String WALL_NAMESPACE = "walllanterns";
    private static final Identifier IRON_HOLDER = Identifier.fromNamespaceAndPath(WALL_NAMESPACE, "block/wall_lantern_holder");

    /** Wall block id -> the Paritem lantern it stands in for. */
    private static final Map<Identifier, Block> SOURCE_BY_WALL_ID = new HashMap<>();
    /** Wall blocks registered so far, used to link copper oxidation and waxing. */
    private static final Map<Identifier, Block> REGISTERED_WALLS = new HashMap<>();
    private static final Set<String> LINKED = new HashSet<>();

    // Copper unlit wall lanterns, by stage: unaffected, exposed, weathered, oxidized.
    private static final Identifier[] COPPER_WALLS = new Identifier[4];
    private static final Identifier[] WAXED_COPPER_WALLS = new Identifier[4];

    @Override
    public void registerLanterns(WallLanternsRegistry registry) {
        // Runs inside Lanterns Belong on Walls' initializer, right before it creates the wall blocks,
        // so this listener sees every one of them as it is registered.
        RegistryEntryAddedCallback.event(BuiltInRegistries.BLOCK).register(ParitemWallLanterns::onBlockRegistered);

        register(registry, ModBlocks.UNLIT_LANTERN, null, IRON_HOLDER);
        register(registry, ModBlocks.REDSTONE_UNLIT_LANTERN, null, IRON_HOLDER);
        register(registry, ModBlocks.UNLIT_LAVA_LANTERN, null, IRON_HOLDER);

        WeatherState[] stages = WeatherState.values();
        Block[] plain = {
                ModBlocks.COPPER_UNLIT_LANTERN.weathering().unaffected(),
                ModBlocks.COPPER_UNLIT_LANTERN.weathering().exposed(),
                ModBlocks.COPPER_UNLIT_LANTERN.weathering().weathered(),
                ModBlocks.COPPER_UNLIT_LANTERN.weathering().oxidized()
        };
        Block[] waxed = {
                ModBlocks.COPPER_UNLIT_LANTERN.waxed().unaffected(),
                ModBlocks.COPPER_UNLIT_LANTERN.waxed().exposed(),
                ModBlocks.COPPER_UNLIT_LANTERN.waxed().weathered(),
                ModBlocks.COPPER_UNLIT_LANTERN.waxed().oxidized()
        };
        for (int i = 0; i < 4; i++) {
            String prefix = stages[i] == WeatherState.UNAFFECTED ? "" : stages[i].getSerializedName() + "_";
            Identifier holder = Identifier.fromNamespaceAndPath(WALL_NAMESPACE, "block/" + prefix + "copper_wall_lantern_holder");
            // Unwaxed walls oxidize; waxed ones stay put. Both reuse the unwaxed lantern model, like Paritem does.
            COPPER_WALLS[i] = register(registry, plain[i], stages[i], holder);
            WAXED_COPPER_WALLS[i] = register(registry, waxed[i], null, holder, BuiltInRegistries.BLOCK.getKey(plain[i]));
        }
    }

    private static Identifier register(WallLanternsRegistry registry, Block lantern, @Nullable WeatherState stage, Identifier holderModel) {
        return register(registry, lantern, stage, holderModel, BuiltInRegistries.BLOCK.getKey(lantern));
    }

    private static Identifier register(WallLanternsRegistry registry, Block lantern, @Nullable WeatherState stage,
                                       Identifier holderModel, Identifier modelSource) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(lantern);
        WallLanternOptions options = WallLanternOptions.create()
                .cutout()
                .lanternModel(modelSource.withPrefix("block/"))
                .lanternHolderModel(holderModel)
                .lootTableOf(lantern.getLootTable());
        if (stage != null) {
            options.weatheringState(stage);
        }
        if (!(lantern.asItem() instanceof BlockItem blockItem)) {
            Paritem.LOGGER.warn("Lanterns Belong on Walls compat: {} has no block item, skipping its wall variant.", id);
            return null;
        }
        registry.registerLantern(options, id, blockItem);

        // Lanterns Belong on Walls names wall blocks "walllanterns:wall_<path>".
        Identifier wallId = Identifier.fromNamespaceAndPath(WALL_NAMESPACE, "wall_" + id.getPath());
        SOURCE_BY_WALL_ID.put(wallId, lantern);
        return wallId;
    }

    private static void onBlockRegistered(int rawId, Identifier id, Block block) {
        Block source = SOURCE_BY_WALL_ID.get(id);
        if (source == null) {
            return;
        }

        // Light emission is baked into each block state when the block is built, so overwrite it per state.
        int light = source.defaultBlockState().getLightEmission();
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            ((BlockStateBaseAccessor) state).paritem$setLightEmission(light);
        }

        REGISTERED_WALLS.put(id, block);
        linkCopper();
    }

    /** Lanterns Belong on Walls only links vanilla copper lanterns, so link Paritem's ourselves. */
    private static void linkCopper() {
        for (int i = 0; i < 4; i++) {
            Block plain = REGISTERED_WALLS.get(COPPER_WALLS[i]);
            Block waxed = REGISTERED_WALLS.get(WAXED_COPPER_WALLS[i]);
            if (plain != null && waxed != null && LINKED.add("wax" + i)) {
                OxidizableBlocksRegistry.registerWaxable(plain, waxed);
            }
            if (i < 3) {
                Block next = REGISTERED_WALLS.get(COPPER_WALLS[i + 1]);
                if (plain != null && next != null && LINKED.add("age" + i)) {
                    OxidizableBlocksRegistry.registerNextStage(plain, next);
                }
            }
        }
        if (LINKED.size() == 7) {
            Paritem.LOGGER.info("Lanterns Belong on Walls compat: wall variants ready for Paritem's unlit lanterns.");
        }
    }
}
