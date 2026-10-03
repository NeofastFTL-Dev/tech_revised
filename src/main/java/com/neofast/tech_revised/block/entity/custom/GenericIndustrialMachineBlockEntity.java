package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.custom.GenericIndustrialMachineBlock;
import com.neofast.tech_revised.multiblock.MachineLayouts;
import com.neofast.tech_revised.multiblock.MultiblockEnergy;
import com.neofast.tech_revised.multiblock.MultiblockRender;
import com.neofast.tech_revised.recipe.GenericIndustrialRecipe;
import com.neofast.tech_revised.screen.GenericIndustrialMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class GenericIndustrialMachineBlockEntity extends BlockEntity implements MenuProvider {
    private static final int DEFAULT_PROCESS_TICKS = 100;
    private static final int ENERGY_CAPACITY = 30000;
    private static final int MAX_ENERGY_RECEIVE = 1000;
    private static final int DEFAULT_ENERGY_PER_TICK = 20;
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private int progress = 0;
    private final RecipeType<GenericIndustrialRecipe> recipeType;
    private final Component displayName;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == INPUT_SLOT) {
                return isValidInput(stack);
            }
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final EnergyStorage energyStorage = new EnergyStorage(ENERGY_CAPACITY, MAX_ENERGY_RECEIVE, ENERGY_CAPACITY) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) {
                setChanged();
            }
            return received;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<IEnergyStorage> lazyEnergy = LazyOptional.empty();

    public GenericIndustrialMachineBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState, 
                                               RecipeType<GenericIndustrialRecipe> recipeType, String displayNameKey) {
        super(type, worldPosition, blockState);
        this.recipeType = recipeType;
        this.displayName = Component.translatable(displayNameKey);
    }

        public AABB getRenderBoundingBox() {
        return MultiblockRender.bounds(this);
    }

        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergy.cast();
        }
        return net.minecraftforge.common.util.LazyOptional.empty();
    }

        public void onLoad() {
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergy = LazyOptional.of(() -> energyStorage);
    }

        public void invalidateCaps() {
        lazyItemHandler.invalidate();
        lazyEnergy.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.put("energy", energyStorage.serializeNBT());
        tag.putInt("progress", progress);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        if (tag.contains("energy")) {
            energyStorage.deserializeNBT(tag.get("energy"));
        }
        progress = tag.getInt("progress");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GenericIndustrialMachineBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }
        MultiblockRender.syncFormed(blockEntity);
        if (!MachineLayouts.isFormed(level, pos, state)) {
            setActive(level, pos, false);
            blockEntity.resetProgress();
            return;
        }
        MultiblockEnergy.pullFromHatches(level, pos, state, blockEntity.energyStorage, MAX_ENERGY_RECEIVE);

        GenericIndustrialRecipe recipe = blockEntity.getCurrentRecipe();
        boolean canProcess = recipe != null && blockEntity.canProcess(recipe);
        int energyPerTick = recipe != null ? recipe.getEnergyPerTick() : DEFAULT_ENERGY_PER_TICK;
        boolean hasEnergy = blockEntity.energyStorage.getEnergyStored() >= energyPerTick;

        if (!canProcess || !hasEnergy) {
            setActive(level, pos, false);
            blockEntity.resetProgress();
            return;
        }

        setActive(level, pos, true);

        blockEntity.energyStorage.extractEnergy(energyPerTick, false);
        blockEntity.progress++;
        if (blockEntity.progress < recipe.getProcessTicks()) {
            blockEntity.setChanged();
            return;
        }

        blockEntity.finishProcessing(recipe);
        blockEntity.progress = 0;
        blockEntity.setChanged();

        GenericIndustrialRecipe nextRecipe = blockEntity.getCurrentRecipe();
        boolean canContinue = nextRecipe != null &&
                blockEntity.canProcess(nextRecipe) &&
                blockEntity.energyStorage.getEnergyStored() >= nextRecipe.getEnergyPerTick();
        setActive(level, pos, canContinue);
    }

    private static void setActive(Level level, BlockPos pos, boolean active) {
        BlockState currentState = level.getBlockState(pos);
        if (!(currentState.getBlock() instanceof GenericIndustrialMachineBlock)) {
            return;
        }

        if (currentState.getValue(GenericIndustrialMachineBlock.ACTIVE) != active) {
            level.setBlock(pos, currentState.setValue(GenericIndustrialMachineBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }

    private boolean canProcess(GenericIndustrialRecipe recipe) {
        ItemStack input = itemHandler.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty() || input.getCount() < recipe.getInputCount()) {
            return false;
        }
        if (!recipe.getInput().test(input)) {
            return false;
        }

        ItemStack recipeOutput = recipe.getResultItem(level.registryAccess()).copy();
        if (recipeOutput.isEmpty()) {
            return false;
        }

        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(output, recipeOutput)) {
            return false;
        }

        return output.getCount() + recipeOutput.getCount() <= output.getMaxStackSize();
    }

    private void finishProcessing(GenericIndustrialRecipe recipe) {
        ItemStack recipeOutput = recipe.getResultItem(level.registryAccess()).copy();
        if (recipeOutput.isEmpty()) {
            return;
        }

        itemHandler.extractItem(INPUT_SLOT, recipe.getInputCount(), false);

        ItemStack output = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            itemHandler.setStackInSlot(OUTPUT_SLOT, recipeOutput);
            return;
        }

        output.grow(recipeOutput.getCount());
        itemHandler.setStackInSlot(OUTPUT_SLOT, output);
    }

    private void resetProgress() {
        if (this.progress != 0) {
            this.progress = 0;
            setChanged();
        }
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTicks() {
        GenericIndustrialRecipe recipe = getCurrentRecipe();
        return recipe != null ? recipe.getProcessTicks() : DEFAULT_PROCESS_TICKS;
    }

    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    public void drops() {
        if (level == null) {
            return;
        }
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new GenericIndustrialMachineMenu(containerId, inventory, this);
    }

    @Nullable
    private GenericIndustrialRecipe getCurrentRecipe() {
        if (level == null) {
            return null;
        }

        SimpleContainer inventory = new SimpleContainer(1);
        inventory.setItem(0, itemHandler.getStackInSlot(INPUT_SLOT).copy());
        Optional<GenericIndustrialRecipe> recipe = level.getRecipeManager().getRecipeFor(recipeType, new com.neofast.tech_revised.recipe.ContainerInput(inventory), level).map(net.minecraft.world.item.crafting.RecipeHolder::value);
        return recipe.orElse(null);
    }

    public boolean isValidInput(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return true;
        }
        for (var holder : level.getRecipeManager().getAllRecipesFor(recipeType)) {
            GenericIndustrialRecipe recipe = holder.value();
            if (recipe.getInput().test(stack)) {
                return true;
            }
        }
        return false;
    }
}
