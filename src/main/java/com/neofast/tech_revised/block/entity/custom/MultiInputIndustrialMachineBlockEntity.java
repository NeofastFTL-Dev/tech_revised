package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.custom.MultiInputIndustrialMachineBlock;
import com.neofast.tech_revised.screen.MultiInputIndustrialMachineMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
import java.util.List;

/**
 * Shared multi-input industrial machine used by the PCB and lamination lines.
 * Layout: {@code inputSlotCount} input slots followed by one output slot.
 */
public abstract class MultiInputIndustrialMachineBlockEntity extends BlockEntity implements MenuProvider {
    public static final int DEFAULT_PROCESS_TICKS = 100;
    private static final int ENERGY_CAPACITY = 100000;
    private static final int MAX_ENERGY_RECEIVE = 2000;
    private static final int DEFAULT_ENERGY_PER_TICK = 20;

    private final int inputSlotCount;
    private final int outputSlot;
    private final Component displayName;

    private int progress = 0;

    private final ItemStackHandler itemHandler;
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

    protected MultiInputIndustrialMachineBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState,
                                                     int inputSlotCount, String displayNameKey) {
        super(type, worldPosition, blockState);
        this.inputSlotCount = inputSlotCount;
        this.outputSlot = inputSlotCount;
        this.displayName = Component.translatable(displayNameKey);
        this.itemHandler = new ItemStackHandler(inputSlotCount + 1) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                if (slot >= 0 && slot < MultiInputIndustrialMachineBlockEntity.this.inputSlotCount) {
                    return isValidInput(stack);
                }
                return false;
            }

            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    protected abstract ResolvedRecipe findRecipe(SimpleContainer inventory);

    protected abstract boolean isIngredientAccepted(ItemStack stack);

    public int getInputSlotCount() {
        return inputSlotCount;
    }

    public int getOutputSlot() {
        return outputSlot;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergy = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergy.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.put("energy", energyStorage.serializeNBT());
        tag.putInt("progress", progress);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        if (tag.contains("energy")) {
            energyStorage.deserializeNBT(tag.get("energy"));
        }
        progress = tag.getInt("progress");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MultiInputIndustrialMachineBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        ResolvedRecipe recipe = blockEntity.getCurrentRecipe();
        boolean canProcess = recipe != null && blockEntity.canProcess(recipe);
        int energyPerTick = recipe != null ? recipe.energyPerTick() : DEFAULT_ENERGY_PER_TICK;
        boolean hasEnergy = blockEntity.energyStorage.getEnergyStored() >= energyPerTick;

        if (!canProcess || !hasEnergy) {
            setActive(level, pos, false);
            blockEntity.resetProgress();
            return;
        }

        setActive(level, pos, true);
        blockEntity.energyStorage.extractEnergy(energyPerTick, false);
        blockEntity.progress++;
        if (blockEntity.progress < recipe.processTicks()) {
            blockEntity.setChanged();
            return;
        }

        blockEntity.finishProcessing(recipe);
        blockEntity.progress = 0;
        blockEntity.setChanged();

        ResolvedRecipe nextRecipe = blockEntity.getCurrentRecipe();
        boolean canContinue = nextRecipe != null
                && blockEntity.canProcess(nextRecipe)
                && blockEntity.energyStorage.getEnergyStored() >= nextRecipe.energyPerTick();
        setActive(level, pos, canContinue);
    }

    private static void setActive(Level level, BlockPos pos, boolean active) {
        BlockState currentState = level.getBlockState(pos);
        if (!(currentState.getBlock() instanceof MultiInputIndustrialMachineBlock)) {
            return;
        }
        if (currentState.getValue(MultiInputIndustrialMachineBlock.ACTIVE) != active) {
            level.setBlock(pos, currentState.setValue(MultiInputIndustrialMachineBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }

    private boolean canProcess(ResolvedRecipe recipe) {
        if (recipe.result().isEmpty()) {
            return false;
        }

        SimpleContainer inventory = createInputContainer();
        for (int i = 0; i < recipe.inputs().size(); i++) {
            if (!recipe.inputs().get(i).test(inventory.getItem(i))) {
                return false;
            }
        }

        ItemStack output = itemHandler.getStackInSlot(outputSlot);
        if (output.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameTags(output, recipe.result())) {
            return false;
        }
        return output.getCount() + recipe.result().getCount() <= output.getMaxStackSize();
    }

    private void finishProcessing(ResolvedRecipe recipe) {
        SimpleContainer inventory = createInputContainer();
        ItemStack produced;
        try {
            produced = recipe.assemble(inventory);
        } catch (RuntimeException exception) {
            // Validation failures (moisture, shorts, bridges, lamination stackups, etc.)
            notifyNearby(exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage());
            resetProgress();
            setChanged();
            return;
        }

        if (produced.isEmpty()) {
            return;
        }

        for (int i = 0; i < recipe.inputs().size(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack remainder = stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem() : ItemStack.EMPTY;
            itemHandler.extractItem(i, 1, false);
            if (!remainder.isEmpty()) {
                ItemStack leftover = itemHandler.insertItem(i, remainder, false);
                if (!leftover.isEmpty() && level != null) {
                    Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), leftover);
                }
            }
        }

        ItemStack output = itemHandler.getStackInSlot(outputSlot);
        if (output.isEmpty()) {
            itemHandler.setStackInSlot(outputSlot, produced.copy());
        } else {
            output.grow(produced.getCount());
            itemHandler.setStackInSlot(outputSlot, output);
        }
    }

    private void notifyNearby(String message) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Component text = Component.literal("Process failed: " + message);
        serverLevel.getServer().getPlayerList().getPlayers().stream()
                .filter(player -> player.level() == serverLevel)
                .filter(player -> player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64)
                .forEach(player -> player.displayClientMessage(text, true));
    }

    private void resetProgress() {
        if (this.progress != 0) {
            this.progress = 0;
            setChanged();
        }
    }

    private SimpleContainer createInputContainer() {
        SimpleContainer inventory = new SimpleContainer(inputSlotCount);
        for (int i = 0; i < inputSlotCount; i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i).copy());
        }
        return inventory;
    }

    @Nullable
    private ResolvedRecipe getCurrentRecipe() {
        if (level == null) {
            return null;
        }
        return findRecipe(createInputContainer());
    }

    public boolean isValidInput(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (level == null) {
            return true;
        }
        return isIngredientAccepted(stack);
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTicks() {
        ResolvedRecipe recipe = getCurrentRecipe();
        return recipe != null ? recipe.processTicks() : DEFAULT_PROCESS_TICKS;
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
        return new MultiInputIndustrialMachineMenu(containerId, inventory, this);
    }

    public ItemStackHandler getItemHandler() {
        return itemHandler;
    }

    /**
     * Snapshot of a matched multi-input recipe for processing.
     */
    public record ResolvedRecipe(
            int processTicks,
            int energyPerTick,
            ItemStack result,
            NonNullList<Ingredient> inputs,
            AssembleFunction assembler
    ) {
        public ItemStack assemble(SimpleContainer inventory) {
            return assembler.assemble(inventory);
        }

        public static ResolvedRecipe of(int processTicks, int energyPerTick, ItemStack result,
                                        List<Ingredient> inputs, AssembleFunction assembler) {
            NonNullList<Ingredient> list = NonNullList.create();
            list.addAll(inputs);
            return new ResolvedRecipe(processTicks, energyPerTick, result.copy(), list, assembler);
        }
    }

    @FunctionalInterface
    public interface AssembleFunction {
        ItemStack assemble(SimpleContainer inventory);
    }
}
