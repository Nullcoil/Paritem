package net.nullcoil.paritem.entity.minecart.copper;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.nullcoil.paritem.blocks.custom.hoppers.copper.CopperHopperLogic;
import net.nullcoil.paritem.entity.minecart.ParitemAbstractMinecart;
import net.nullcoil.paritem.gui.hoppers.copper.CopperHopperScreenHandler;

public class CopperHopperMinecart extends ParitemAbstractMinecart {

    // fields
    private static final long WAXED_SENTINEL = -2L;
    private static final long UNSET = -1L;
    private long nextWeatheringTick = UNSET;
    private static final int minWeatheringTick = 504000;
    private static final int maxWeatheringTick = 552000;

    // in tick()
    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide() && !this.isWaxed()) {
            this.updateWeathering();
        }
    }

    private void updateWeathering() {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        if (this.isWaxed()) return; // safety

        long gameTime = serverLevel.getGameTime();
        WeatherState state = this.getWeatherState();

        if (state == WeatherState.OXIDIZED) {
            return; // already fully oxidized
        }

        if (this.nextWeatheringTick == UNSET) {
            // schedule first oxidation
            this.nextWeatheringTick = gameTime + serverLevel.getRandom().nextIntBetweenInclusive(minWeatheringTick, maxWeatheringTick);
            return;
        }

        if (gameTime >= this.nextWeatheringTick) {
            WeatherState next = state.next();
            this.setWeatherState(next);

            if (next != WeatherState.OXIDIZED) {
                // schedule the next stage
                this.nextWeatheringTick = gameTime + serverLevel.getRandom().nextIntBetweenInclusive(minWeatheringTick, maxWeatheringTick);
            } else {
                this.nextWeatheringTick = 0L;
            }
        }
    }

    // ------------------------------------------------------------------
    // Synched data
    // ------------------------------------------------------------------

    private static final EntityDataAccessor<WeatherState> DATA_WEATHER_STATE =
            SynchedEntityData.defineId(CopperHopperMinecart.class, EntityDataSerializers.WEATHERING_COPPER_STATE);

    private static final EntityDataAccessor<Boolean> DATA_WAXED =
            SynchedEntityData.defineId(CopperHopperMinecart.class, EntityDataSerializers.BOOLEAN);

    public CopperHopperMinecart(EntityType<? extends CopperHopperMinecart> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WEATHER_STATE, WeatherState.UNAFFECTED);
        builder.define(DATA_WAXED, false);
    }

    // ------------------------------------------------------------------
    // State accessors
    // ------------------------------------------------------------------

    public WeatherState getWeatherState() {
        return this.entityData.get(DATA_WEATHER_STATE);
    }

    public void setWeatherState(WeatherState state) {
        this.entityData.set(DATA_WEATHER_STATE, state);
    }

    public boolean isWaxed() {
        return this.entityData.get(DATA_WAXED);
    }

    public void setWaxed(boolean waxed) {
        this.entityData.set(DATA_WAXED, waxed);
    }

    // Fluent-style helpers (useful when spawning from items)
    public CopperHopperMinecart withState(WeatherState state) {
        this.setWeatherState(state);
        return this;
    }

    public CopperHopperMinecart waxed() {
        this.setWaxed(true);
        return this;
    }

    public CopperHopperMinecart unwaxed() {
        this.setWaxed(false);
        return this;
    }

    // ------------------------------------------------------------------
    // Display / drop
    // ------------------------------------------------------------------

    @Override
    public BlockState getDisplayBlockState() {
        return CopperHopperMinecartVariants.getBlock(this.getWeatherState(), this.isWaxed()).defaultBlockState();
    }

    @Override
    public Item getCartItem() {
        return CopperHopperMinecartVariants.getItem(this.getWeatherState(), this.isWaxed());
    }

    // ------------------------------------------------------------------
    // Filter behaviour
    // ------------------------------------------------------------------

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) return false;
        return !CopperHopperLogic.shouldVetoPushInto(this, stack);
    }

    // ------------------------------------------------------------------
    // Interaction (wax / scrape)
    // ------------------------------------------------------------------

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 position) {
        ItemStack stack = player.getItemInHand(hand);

        // Honeycomb → wax
        if (stack.is(Items.HONEYCOMB) && !this.isWaxed()) {
            if (!this.level().isClientSide()) {
                this.setWaxed(true);
                this.level().levelEvent(this, 3003, this.blockPosition(), 0);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.setWaxed(true);
                this.nextWeatheringTick = WAXED_SENTINEL;
            }
            return InteractionResult.SUCCESS;
        }

        // Axe
        if (stack.is(ItemTags.AXES)) {
            if (this.isWaxed()) {
                if (!this.level().isClientSide()) {
                    this.setWaxed(false);
                    this.playSound(SoundEvents.AXE_WAX_OFF, 1.0F, 1.0F);
                    this.level().levelEvent(this, 3004, this.blockPosition(), 0);
                    this.setWaxed(false);
                    this.nextWeatheringTick = UNSET;
                    stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
                }
                return InteractionResult.SUCCESS;
            } else if (this.getWeatherState() != WeatherState.UNAFFECTED) {
                if (!this.level().isClientSide()) {
                    this.setWeatherState(this.getWeatherState().previous());
                    this.playSound(SoundEvents.AXE_SCRAPE, 1.0F, 1.0F);
                    this.level().levelEvent(this, 3005, this.blockPosition(), 0);
                    stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.interact(player, hand, position);
    }

    // ------------------------------------------------------------------
    // Save / load
    // ------------------------------------------------------------------

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("weather_state", WeatherState.CODEC, this.getWeatherState());
        output.putBoolean("waxed", this.isWaxed());
        output.putLong("next_weather_age", this.nextWeatheringTick);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setWeatherState(input.read("weather_state", WeatherState.CODEC).orElse(WeatherState.UNAFFECTED));
        this.setWaxed(input.getBooleanOr("waxed", false));
        this.nextWeatheringTick = input.getLongOr("next_weather_age", UNSET);
    }

    // ------------------------------------------------------------------
    // Menu
    // ------------------------------------------------------------------

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new CopperHopperScreenHandler(containerId, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.paritem.copper_hopper_minecart");
    }
}