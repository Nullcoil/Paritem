package net.nullcoil.paritem.blocks.custom;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.nullcoil.paritem.Paritem;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Optional;

public class RedstoneCampfireBlockEntity extends BlockEntity implements Container, Clearable {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int NUM_SLOTS = 4;

    private final NonNullList<ItemStack> items;
    private final int[] cookingProgress;
    private final int[] cookingTime;
    // Explicit per-slot flag: true means cooking is done, item is ready for extraction.
    // This avoids sentinel hacks in the int arrays that cookTick would immediately clobber.
    private final boolean[] cooked;

    public RedstoneCampfireBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.items = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
        this.cookingProgress = new int[NUM_SLOTS];
        this.cookingTime = new int[NUM_SLOTS];
        this.cooked = new boolean[NUM_SLOTS];
        Paritem.LOGGER.info("Redstone Campfire attached successfully");
    }

    // ---------- Cooking logic ----------
    public static void cookTick(ServerLevel level, BlockPos pos, BlockState state,
                                RedstoneCampfireBlockEntity entity,
                                RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipeCache) {
        boolean changed = false;
        for (int slot = 0; slot < entity.items.size(); slot++) {
            ItemStack stack = entity.items.get(slot);
            // Skip empty slots AND slots that already finished cooking
            if (stack.isEmpty() || entity.cooked[slot]) continue;

            changed = true;
            entity.cookingProgress[slot]++;

            if (entity.cookingProgress[slot] >= entity.cookingTime[slot]) {
                SingleRecipeInput input = new SingleRecipeInput(stack);
                ItemStack result = recipeCache.getRecipeFor(input, level)
                        .map(recipe -> recipe.value().assemble(input))
                        .orElse(stack);
                if (!result.isEmpty() && result.isItemEnabled(level.enabledFeatures())) {
                    entity.items.set(slot, result.copy());
                    entity.cookingProgress[slot] = 0;
                    entity.cookingTime[slot] = 0;
                    entity.cooked[slot] = true;  // cookTick will now skip this slot
                    level.sendBlockUpdated(pos, state, state, 3);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                }
            }
        }
        if (changed) setChanged(level, pos, state);
    }

    public static void cooldownTick(Level level, BlockPos pos, BlockState state, RedstoneCampfireBlockEntity entity) {
        boolean changed = false;
        for (int slot = 0; slot < entity.items.size(); slot++) {
            if (!entity.cooked[slot] && entity.cookingProgress[slot] > 0) {
                changed = true;
                entity.cookingProgress[slot] = Mth.clamp(entity.cookingProgress[slot] - 2, 0, entity.cookingTime[slot]);
            }
        }
        if (changed) setChanged(level, pos, state);
    }

    public static void particleTick(Level level, BlockPos pos, BlockState state, RedstoneCampfireBlockEntity entity) {
        RandomSource random = level.getRandom();
        if (random.nextFloat() < 0.11F) {
            for (int i = 0; i < random.nextInt(2) + 2; i++) {
                CampfireBlock.makeParticles(level, pos, state.getValue(CampfireBlock.SIGNAL_FIRE), false);
            }
        }
        int rotation = state.getValue(CampfireBlock.FACING).get2DDataValue();
        for (int slot = 0; slot < entity.items.size(); slot++) {
            if (!entity.items.get(slot).isEmpty() && random.nextFloat() < 0.2F) {
                Direction direction = Direction.from2DDataValue(Math.floorMod(slot + rotation, 4));
                float distance = 0.3125F;
                double x = pos.getX() + 0.5 - direction.getStepX() * distance + direction.getClockWise().getStepX() * distance;
                double y = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5 - direction.getStepZ() * distance + direction.getClockWise().getStepZ() * distance;
                for (int i = 0; i < 4; i++) {
                    level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 5.0E-4, 0.0);
                }
            }
        }
    }

    // ---------- Player placing food ----------
    public boolean placeFood(ServerLevel level, @Nullable LivingEntity source, ItemStack food) {
        for (int slot = 0; slot < this.items.size(); slot++) {
            if (this.items.get(slot).isEmpty()) {
                Optional<RecipeHolder<CampfireCookingRecipe>> recipe = level.recipeAccess()
                        .getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(food), level);
                if (recipe.isEmpty()) return false;
                this.cookingTime[slot] = recipe.get().value().cookingTime();
                this.cookingProgress[slot] = 0;
                this.cooked[slot] = false;
                this.items.set(slot, food.consumeAndReturn(1, source));
                level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(source, this.getBlockState()));
                this.markUpdated();
                return true;
            }
        }
        return false;
    }

    // ---------- Player taking item ----------
    public ItemStack takeItem(int slot) {
        if (slot < 0 || slot >= this.items.size()) return ItemStack.EMPTY;
        ItemStack stack = this.items.get(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        this.items.set(slot, ItemStack.EMPTY);
        this.cookingProgress[slot] = 0;
        this.cookingTime[slot] = 0;
        this.cooked[slot] = false;
        this.markUpdated();
        return stack;
    }

    public boolean isCooked(int slot) {
        return slot >= 0 && slot < NUM_SLOTS && this.cooked[slot];
    }

    private void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    // ---------- Container implementation ----------
    @Override
    public int getContainerSize() { return NUM_SLOTS; }

    @Override
    public boolean isEmpty() { return this.items.stream().allMatch(ItemStack::isEmpty); }

    @Override
    public ItemStack getItem(int slot) { return this.items.get(slot); }

    /**
     * Hopper insertion gate. Hoppers run server-side so level will always be ServerLevel here.
     * Rejects if slot is occupied OR item has no campfire cooking recipe.
     */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (!this.items.get(slot).isEmpty()) return false;
        if (!(this.level instanceof ServerLevel serverLevel)) return false;
        return serverLevel.recipeAccess()
                .getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(stack), serverLevel)
                .isPresent();
    }

    /**
     * Hopper extraction gate. Only cooked items may be pulled.
     */
    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return this.isCooked(slot);
    }

    /**
     * Hopper extraction. canTakeItem is the primary guard; this double-checks.
     */
    @Override
    public ItemStack removeItem(int slot, int count) {
        if (slot < 0 || slot >= this.items.size()) return ItemStack.EMPTY;
        if (!this.cooked[slot]) return ItemStack.EMPTY;
        ItemStack stack = this.items.get(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        this.items.set(slot, ItemStack.EMPTY);
        this.cookingProgress[slot] = 0;
        this.cookingTime[slot] = 0;
        this.cooked[slot] = false;
        this.markUpdated();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.removeItem(slot, 64);
    }

    /**
     * Hopper insertion. canPlaceItem is the primary guard; this is a safety net.
     */
    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= this.items.size()) return;
        if (stack.isEmpty()) {
            this.items.set(slot, ItemStack.EMPTY);
            this.cookingProgress[slot] = 0;
            this.cookingTime[slot] = 0;
            this.cooked[slot] = false;
            this.markUpdated();
            return;
        }
        if (this.level instanceof ServerLevel serverLevel) {
            Optional<RecipeHolder<CampfireCookingRecipe>> recipe = serverLevel.recipeAccess()
                    .getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(stack), serverLevel);
            if (recipe.isPresent()) {
                this.items.set(slot, stack.copy());
                this.cookingTime[slot] = recipe.get().value().cookingTime();
                this.cookingProgress[slot] = 0;
                this.cooked[slot] = false;
                this.markUpdated();
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    // ---------- NBT ----------
    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items.clear();
        ContainerHelper.loadAllItems(input, this.items);
        input.getIntArray("CookingTimes").ifPresentOrElse(
                times -> System.arraycopy(times, 0, this.cookingProgress, 0, Math.min(this.cookingProgress.length, times.length)),
                () -> Arrays.fill(this.cookingProgress, 0));
        input.getIntArray("CookingTotalTimes").ifPresentOrElse(
                times -> System.arraycopy(times, 0, this.cookingTime, 0, Math.min(this.cookingTime.length, times.length)),
                () -> Arrays.fill(this.cookingTime, 0));
        input.getIntArray("CookedFlags").ifPresentOrElse(
                flags -> {
                    for (int i = 0; i < Math.min(this.cooked.length, flags.length); i++) {
                        this.cooked[i] = flags[i] != 0;
                    }
                },
                () -> Arrays.fill(this.cooked, false));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items, true);
        output.putIntArray("CookingTimes", this.cookingProgress);
        output.putIntArray("CookingTotalTimes", this.cookingTime);
        int[] cookedFlags = new int[NUM_SLOTS];
        for (int i = 0; i < NUM_SLOTS; i++) cookedFlags[i] = this.cooked[i] ? 1 : 0;
        output.putIntArray("CookedFlags", cookedFlags);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        Arrays.fill(this.cookingProgress, 0);
        Arrays.fill(this.cookingTime, 0);
        Arrays.fill(this.cooked, false);
    }

    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this.getItems());
        }
    }

    // ---------- Data components ----------
    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.getItems());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard("Items");
    }

    // ---------- Accessor for renderer ----------
    public NonNullList<ItemStack> getItems() { return this.items; }
}