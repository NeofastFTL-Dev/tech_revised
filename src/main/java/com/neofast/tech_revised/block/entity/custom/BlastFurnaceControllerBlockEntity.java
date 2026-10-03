package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.custom.BlastFurnaceControllerBlock;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.multiblock.MachineLayout;
import com.neofast.tech_revised.multiblock.MachineLayouts;
import com.neofast.tech_revised.multiblock.MultiblockControllerAccess;
import com.neofast.tech_revised.multiblock.MultiblockRender;
import com.neofast.tech_revised.recipe.AlloyingRecipe;
import com.neofast.tech_revised.screen.MultiblockControllerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class BlastFurnaceControllerBlockEntity extends BlockEntity implements MenuProvider, MultiblockControllerAccess {
    private static final int PROCESS_TICKS = 200;
    private static final int ENERGY_CAPACITY = 50000;
    private static final int ENERGY_PER_TICK = 50;
    private static final int ENERGY_TRANSFER_PER_TICK = 1000;

    private int progress = 0;
    private final EnergyStorage energyStorage = new EnergyStorage(ENERGY_CAPACITY, 2000, 0);
    private LazyOptional<IEnergyStorage> lazyEnergy = LazyOptional.empty();

    public BlastFurnaceControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLAST_FURNACE_CONTROLLER.get(), pos, state);
    }

        public AABB getRenderBoundingBox() {
        return MultiblockRender.bounds(this);
    }

        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergy.cast();
        }
        return net.minecraftforge.common.util.LazyOptional.empty();
    }

        public void onLoad() {
        lazyEnergy = LazyOptional.of(() -> energyStorage);
    }

        public void invalidateCaps() {
        lazyEnergy.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        tag.putInt("progress", progress);
        tag.put("energy", energyStorage.serializeNBT());
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("progress");
        if (tag.contains("energy")) {
            energyStorage.deserializeNBT(tag.get("energy"));
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlastFurnaceControllerBlockEntity blockEntity) {
        if (level.isClientSide()) return;

        Direction front = state.getValue(BlastFurnaceControllerBlock.FACING);
        boolean formed = BlastFurnaceControllerBlock.isStructureValid(level, pos, front);
        MultiblockRender.syncFormed(blockEntity);
        if (!formed) {
            blockEntity.resetProgress();
            return;
        }

        MachineLayout layout = MachineLayouts.get(state.getBlock());
        ElectricArcFurnaceInputBusBlockEntity inputBus = layout == null ? null
                : layout.findBlockEntity(level, pos, front, ElectricArcFurnaceInputBusBlockEntity.class);
        ElectricArcFurnaceOutputBusBlockEntity outputBus = layout == null ? null
                : layout.findBlockEntity(level, pos, front, ElectricArcFurnaceOutputBusBlockEntity.class);
        ElectricArcFurnaceEnergyInputHatchBlockEntity energyHatch = layout == null ? null
                : layout.findBlockEntity(level, pos, front, ElectricArcFurnaceEnergyInputHatchBlockEntity.class);

        if (inputBus == null || outputBus == null || energyHatch == null) {
            blockEntity.resetProgress();
            return;
        }

        blockEntity.pullEnergyFromHatch(energyHatch);

        ItemStack primaryInput = inputBus.getPrimaryInputStack();
        if (primaryInput.isEmpty()) {
            blockEntity.resetProgress();
            return;
        }

        Optional<AlloyingRecipe> alloyRecipe = getAlloyingRecipe(level, primaryInput, inputBus.getSecondaryInputStack());
        ItemStack outputStack;
        boolean consumesSecondary;
        if (alloyRecipe.isPresent()) {
            outputStack = alloyRecipe.get().getResultItem(level.registryAccess()).copy();
            consumesSecondary = true;
        } else {
            Optional<SmeltingRecipe> smeltRecipe = getSmeltingRecipe(level, primaryInput);
            if (smeltRecipe.isEmpty()) {
                blockEntity.resetProgress();
                return;
            }
            outputStack = smeltRecipe.get().getResultItem(level.registryAccess()).copy();
            consumesSecondary = false;
        }

        if (!outputBus.canAccept(outputStack)) {
            blockEntity.resetProgress();
            return;
        }

        if (!blockEntity.hasRequiredEnergy(ENERGY_PER_TICK)) {
            blockEntity.resetProgress();
            return;
        }

        blockEntity.energyStorage.extractEnergy(ENERGY_PER_TICK, false);
        blockEntity.progress++;
        if (blockEntity.progress < PROCESS_TICKS) {
            blockEntity.setChanged();
            return;
        }

        if (consumesSecondary) {
            inputBus.extractPrimaryOne();
            inputBus.extractSecondaryOne();
        } else {
            inputBus.extractPrimaryOne();
        }
        outputBus.insert(outputStack);

        blockEntity.progress = 0;
        blockEntity.setChanged();
    }

    private void pullEnergyFromHatch(ElectricArcFurnaceEnergyInputHatchBlockEntity energyHatch) {
        int freeSpace = energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored();
        if (freeSpace <= 0) {
            return;
        }

        int maxTransfer = Math.min(freeSpace, ENERGY_TRANSFER_PER_TICK);
        int simulatedExtract = energyHatch.extractEnergy(maxTransfer, true);
        if (simulatedExtract <= 0) {
            return;
        }

        int accepted = energyStorage.receiveEnergy(simulatedExtract, false);
        if (accepted > 0) {
            energyHatch.extractEnergy(accepted, false);
        }
    }

    private static Optional<AlloyingRecipe> getAlloyingRecipe(Level level, ItemStack primaryInput, ItemStack secondaryInput) {
        SimpleContainer inventory = new SimpleContainer(2);
        inventory.setItem(0, primaryInput.copy());
        inventory.setItem(1, secondaryInput.copy());
        return level.getRecipeManager().getRecipeFor(AlloyingRecipe.Type.INSTANCE, new com.neofast.tech_revised.recipe.ContainerInput(inventory), level).map(net.minecraft.world.item.crafting.RecipeHolder::value);
    }

    private static Optional<SmeltingRecipe> getSmeltingRecipe(Level level, ItemStack inputStack) {
        SimpleContainer inventory = new SimpleContainer(1);
        inventory.setItem(0, inputStack.copy());
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new net.minecraft.world.item.crafting.SingleRecipeInput(inventory.getItem(0)), level).map(net.minecraft.world.item.crafting.RecipeHolder::value);
    }

    private boolean hasRequiredEnergy(int energyPerTick) {
        return energyStorage.getEnergyStored() >= energyPerTick;
    }

    private void resetProgress() {
        if (this.progress != 0) {
            this.progress = 0;
            setChanged();
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tech_revised.blast_furnace");
    }

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public int getProcessTicks() {
        return PROCESS_TICKS;
    }

    @Override
    public boolean isStructureFormed() {
        if (level == null) {
            return false;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(BlastFurnaceControllerBlock.FACING)) {
            return false;
        }
        return BlastFurnaceControllerBlock.isStructureValid(level, worldPosition, state.getValue(BlastFurnaceControllerBlock.FACING));
    }

    @Override
    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MultiblockControllerMenu(containerId, inventory, this);
    }
}
