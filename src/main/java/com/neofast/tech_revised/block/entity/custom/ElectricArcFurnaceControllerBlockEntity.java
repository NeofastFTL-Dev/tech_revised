package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.custom.ElectricArcFurnaceControllerBlock;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.multiblock.MachineLayout;
import com.neofast.tech_revised.multiblock.MachineLayouts;
import com.neofast.tech_revised.multiblock.MultiblockControllerAccess;
import com.neofast.tech_revised.multiblock.MultiblockRender;
import com.neofast.tech_revised.recipe.ElectricArcFurnaceRecipe;
import com.neofast.tech_revised.screen.MultiblockControllerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ElectricArcFurnaceControllerBlockEntity extends BlockEntity implements MenuProvider, MultiblockControllerAccess {
    private static final int PROCESS_TICKS = 100;
    private static final int ENERGY_CAPACITY = 30000;
    private static final int ENERGY_PER_TICK = 40;
    private static final int ENERGY_TRANSFER_PER_TICK = 400;
    private static final int WATER_CAPACITY = 8000;
    private static final int WATER_PER_OPERATION = 250;
    private static final int WATER_TRANSFER_PER_TICK = 250;

    private int progress = 0;

    private final EnergyStorage energyStorage = new EnergyStorage(ENERGY_CAPACITY, 1000, 0) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) {
                setChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) {
                setChanged();
            }
            return extracted;
        }
    };

    private final FluidTank waterTank = new FluidTank(WATER_CAPACITY, fluidStack -> fluidStack.getFluid() == Fluids.WATER) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private LazyOptional<IEnergyStorage> lazyEnergy = LazyOptional.empty();
    private LazyOptional<IFluidHandler> lazyWaterHandler = LazyOptional.empty();

    public ElectricArcFurnaceControllerBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.ELECTRIC_ARC_FURNACE_CONTROLLER.get(), worldPosition, blockState);
    }

        public AABB getRenderBoundingBox() {
        return MultiblockRender.bounds(this);
    }

        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergy.cast();
        }
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyWaterHandler.cast();
        }
        return net.minecraftforge.common.util.LazyOptional.empty();
    }

        public void onLoad() {
        lazyEnergy = LazyOptional.of(() -> energyStorage);
        lazyWaterHandler = LazyOptional.of(() -> waterTank);
    }

        public void invalidateCaps() {
        lazyEnergy.invalidate();
        lazyWaterHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        tag.putInt("eaf_progress", progress);
        tag.put("eaf_energy", energyStorage.serializeNBT());
        tag.put("eaf_water", waterTank.writeToNBT(new CompoundTag()));
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("eaf_progress");
        if (tag.contains("eaf_energy")) {
            energyStorage.deserializeNBT(tag.get("eaf_energy"));
        }
        if (tag.contains("eaf_water")) {
            waterTank.readFromNBT(tag.getCompound("eaf_water"));
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectricArcFurnaceControllerBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        Direction front = state.getValue(ElectricArcFurnaceControllerBlock.FACING);
        boolean formed = ElectricArcFurnaceControllerBlock.isStructureValid(level, pos, front);
        ElectricArcFurnaceControllerBlock.updateFrameFormedStates(level, pos, front, formed);
        if (!formed) {
            blockEntity.resetProgress();
            return;
        }

        MachineLayout layout = MachineLayouts.get(state.getBlock());
        ElectricArcFurnaceInputBusBlockEntity inputBus = layout == null ? null
                : layout.findBlockEntity(level, pos, front, ElectricArcFurnaceInputBusBlockEntity.class);
        ElectricArcFurnaceOutputBusBlockEntity outputBus = layout == null ? null
                : layout.findBlockEntity(level, pos, front, ElectricArcFurnaceOutputBusBlockEntity.class);
        ElectricArcFurnaceFluidInputBusBlockEntity fluidInputBus = layout == null ? null
                : layout.findBlockEntity(level, pos, front, ElectricArcFurnaceFluidInputBusBlockEntity.class);
        ElectricArcFurnaceFluidOutputBusBlockEntity fluidOutputBus = layout == null ? null
                : layout.findBlockEntity(level, pos, front, ElectricArcFurnaceFluidOutputBusBlockEntity.class);
        ElectricArcFurnaceEnergyInputHatchBlockEntity energyInputHatch = layout == null ? null
                : layout.findBlockEntity(level, pos, front, ElectricArcFurnaceEnergyInputHatchBlockEntity.class);

        if (inputBus == null || outputBus == null || fluidInputBus == null || fluidOutputBus == null || energyInputHatch == null) {
            blockEntity.resetProgress();
            return;
        }

        blockEntity.pullEnergyFromHatch(energyInputHatch);
        blockEntity.pullWaterFromInputBus(fluidInputBus);

        Optional<ElectricArcFurnaceRecipe> eafRecipe = getElectricArcFurnaceRecipe(level,
                inputBus.getPrimaryInputStack(), inputBus.getSecondaryInputStack());

        int processTicks = PROCESS_TICKS;
        int energyPerTick = ENERGY_PER_TICK;
        int waterPerOperation = WATER_PER_OPERATION;
        boolean usesSecondaryInput = false;
        ItemStack outputStack;
        if (eafRecipe.isPresent()) {
            ElectricArcFurnaceRecipe recipe = eafRecipe.get();
            outputStack = recipe.getResultItem(level.registryAccess()).copy();
            processTicks = recipe.getProcessTicks();
            energyPerTick = recipe.getEnergyPerTick();
            waterPerOperation = recipe.getWaterAmount();
            usesSecondaryInput = recipe.hasSecondaryInput();
        } else {
            ItemStack primaryInput = inputBus.getPrimaryInputStack();
            if (primaryInput.isEmpty()) {
                blockEntity.resetProgress();
                return;
            }

            Optional<SmeltingRecipe> recipe = getSmeltingRecipe(level, primaryInput);
            if (recipe.isEmpty()) {
                blockEntity.resetProgress();
                return;
            }

            outputStack = recipe.get().getResultItem(level.registryAccess()).copy();
        }

        if (!outputBus.canAccept(outputStack)) {
            blockEntity.resetProgress();
            return;
        }

        if (!inputBus.hasElectrode()) {
            blockEntity.resetProgress();
            return;
        }

        if (!blockEntity.hasRequiredCoolingWater(waterPerOperation)
                || !blockEntity.hasRequiredEnergy(energyPerTick)
                || !blockEntity.hasSpaceForOutputWater(fluidOutputBus, waterPerOperation)) {
            blockEntity.resetProgress();
            return;
        }

        blockEntity.energyStorage.extractEnergy(energyPerTick, false);
        blockEntity.progress++;
        if (blockEntity.progress < processTicks) {
            blockEntity.setChanged();
            return;
        }

        if (usesSecondaryInput) {
            inputBus.extractPrimaryOne();
            inputBus.extractSecondaryOne();
        } else {
            inputBus.extractPrimaryOne();
        }
        outputBus.insert(outputStack);
        blockEntity.waterTank.drain(waterPerOperation, IFluidHandler.FluidAction.EXECUTE);
        fluidOutputBus.fill(new FluidStack(Fluids.WATER, waterPerOperation), IFluidHandler.FluidAction.EXECUTE);
        inputBus.damageElectrode();

        blockEntity.progress = 0;
        blockEntity.setChanged();
    }

    private void pullWaterFromInputBus(ElectricArcFurnaceFluidInputBusBlockEntity fluidInputBus) {
        int freeSpace = waterTank.getCapacity() - waterTank.getFluidAmount();
        if (freeSpace <= 0) {
            return;
        }

        int maxTransfer = Math.min(freeSpace, WATER_TRANSFER_PER_TICK);
        FluidStack simulatedDrain = fluidInputBus.drain(maxTransfer, IFluidHandler.FluidAction.SIMULATE);
        if (simulatedDrain.isEmpty()) {
            return;
        }

        int accepted = waterTank.fill(simulatedDrain, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) {
            return;
        }

        FluidStack drained = fluidInputBus.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
        if (!drained.isEmpty()) {
            waterTank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void pullEnergyFromHatch(ElectricArcFurnaceEnergyInputHatchBlockEntity energyInputHatch) {
        int freeSpace = energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored();
        if (freeSpace <= 0) {
            return;
        }

        int maxTransfer = Math.min(freeSpace, ENERGY_TRANSFER_PER_TICK);
        int simulatedExtract = energyInputHatch.extractEnergy(maxTransfer, true);
        if (simulatedExtract <= 0) {
            return;
        }

        int accepted = energyStorage.receiveEnergy(simulatedExtract, false);
        if (accepted > 0) {
            energyInputHatch.extractEnergy(accepted, false);
        }
    }

    private static Optional<SmeltingRecipe> getSmeltingRecipe(Level level, ItemStack inputStack) {
        SimpleContainer inventory = new SimpleContainer(1);
        inventory.setItem(0, inputStack.copy());
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new net.minecraft.world.item.crafting.SingleRecipeInput(inventory.getItem(0)), level).map(net.minecraft.world.item.crafting.RecipeHolder::value);
    }

    private static Optional<ElectricArcFurnaceRecipe> getElectricArcFurnaceRecipe(Level level, ItemStack primaryInput, ItemStack secondaryInput) {
        SimpleContainer inventory = new SimpleContainer(2);
        inventory.setItem(0, primaryInput.copy());
        inventory.setItem(1, secondaryInput.copy());
        return level.getRecipeManager().getRecipeFor(ElectricArcFurnaceRecipe.Type.INSTANCE, new com.neofast.tech_revised.recipe.ContainerInput(inventory), level).map(net.minecraft.world.item.crafting.RecipeHolder::value);
    }

    private boolean hasRequiredCoolingWater(int waterPerOperation) {
        return waterTank.getFluidAmount() >= waterPerOperation;
    }

    private boolean hasRequiredEnergy(int energyPerTick) {
        return energyStorage.getEnergyStored() >= energyPerTick;
    }

    private boolean hasSpaceForOutputWater(ElectricArcFurnaceFluidOutputBusBlockEntity fluidOutputBus, int amount) {
        return fluidOutputBus.fill(new FluidStack(Fluids.WATER, amount), IFluidHandler.FluidAction.SIMULATE) == amount;
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
        return PROCESS_TICKS;
    }

    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    public int getWaterAmount() {
        return waterTank.getFluidAmount();
    }

    public int getWaterCapacity() {
        return waterTank.getCapacity();
    }

    public int getCoolingWaterAmount() {
        int total = waterTank.getFluidAmount();
        ElectricArcFurnaceFluidInputBusBlockEntity fluidInputBus = getConnectedFluidInputBus();
        if (fluidInputBus != null) {
            total += fluidInputBus.getWaterAmount();
        }
        return total;
    }

    @Override
    public int getAuxiliaryAmount() {
        return getCoolingWaterAmount();
    }

    @Override
    public int getAuxiliaryCapacity() {
        return getCoolingWaterCapacity();
    }

    @Override
    public String getAuxiliaryLabel() {
        return "Cooling Water";
    }

    public int getCoolingWaterCapacity() {
        int total = waterTank.getCapacity();
        ElectricArcFurnaceFluidInputBusBlockEntity fluidInputBus = getConnectedFluidInputBus();
        if (fluidInputBus != null) {
            total += fluidInputBus.getCapacity();
        }
        return total;
    }

    @Nullable
    private ElectricArcFurnaceFluidInputBusBlockEntity getConnectedFluidInputBus() {
        if (level == null) {
            return null;
        }

        BlockState state = getBlockState();
        if (!state.hasProperty(ElectricArcFurnaceControllerBlock.FACING)) {
            return null;
        }

        Direction front = state.getValue(ElectricArcFurnaceControllerBlock.FACING);
        MachineLayout layout = MachineLayouts.get(state.getBlock());
        return layout == null ? null
                : layout.findBlockEntity(level, worldPosition, front, ElectricArcFurnaceFluidInputBusBlockEntity.class);
    }

    public boolean tryInsertWaterBucket(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.is(Items.WATER_BUCKET)) {
            return false;
        }

        int filled = waterTank.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        if (filled < 1000) {
            return false;
        }

        if (!player.isCreative()) {
            player.setItemInHand(hand, new ItemStack(Items.BUCKET));
        }

        setChanged();
        return true;
    }

    public boolean isStructureFormed() {
        if (level == null) {
            return false;
        }

        BlockState state = getBlockState();
        if (!state.hasProperty(ElectricArcFurnaceControllerBlock.FACING)) {
            return false;
        }

        Direction front = state.getValue(ElectricArcFurnaceControllerBlock.FACING);
        return ElectricArcFurnaceControllerBlock.isStructureValid(level, worldPosition, front);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tech_revised.electric_arc_furnace_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MultiblockControllerMenu(containerId, inventory, this);
    }
}
