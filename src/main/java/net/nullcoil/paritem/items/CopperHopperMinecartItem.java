package net.nullcoil.paritem.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.nullcoil.paritem.entity.ModEntities;
import net.nullcoil.paritem.entity.minecart.copper.CopperHopperMinecart;

public class CopperHopperMinecartItem extends MinecartItem {

    private final WeatherState weatherState;
    private final boolean waxed;

    public CopperHopperMinecartItem(Properties properties, WeatherState weatherState, boolean waxed) {
        super(ModEntities.COPPER_HOPPER_MINECART, properties);
        this.weatherState = weatherState;
        this.waxed = waxed;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState blockState = level.getBlockState(pos);

        if (!blockState.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        }

        ItemStack itemStack = context.getItemInHand();
        RailShape shape = blockState.getBlock() instanceof BaseRailBlock rail
                ? blockState.getValue(rail.getShapeProperty())
                : RailShape.NORTH_SOUTH;

        double offset = shape.isSlope() ? 0.5 : 0.0;
        Vec3 spawnPos = new Vec3(
                pos.getX() + 0.5,
                pos.getY() + 0.0625 + offset,
                pos.getZ() + 0.5
        );

        AbstractMinecart cart = AbstractMinecart.createMinecart(
                level,
                spawnPos.x, spawnPos.y, spawnPos.z,
                ModEntities.COPPER_HOPPER_MINECART,
                EntitySpawnReason.DISPENSER,
                itemStack,
                context.getPlayer()
        );

        if (cart == null) {
            return InteractionResult.FAIL;
        }

        // Apply the variant this item represents
        if (cart instanceof CopperHopperMinecart copperCart) {
            copperCart.withState(weatherState);
            if (waxed) {
                copperCart.waxed();
            } else {
                copperCart.unwaxed();
            }
        }

        if (AbstractMinecart.useExperimentalMovement(level)) {
            for (Entity entity : level.getEntities(null, cart.getBoundingBox())) {
                if (entity instanceof AbstractMinecart) {
                    return InteractionResult.FAIL;
                }
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(cart);
            serverLevel.gameEvent(
                    GameEvent.ENTITY_PLACE,
                    pos,
                    GameEvent.Context.of(context.getPlayer(), serverLevel.getBlockState(pos.below()))
            );
        }

        itemStack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}