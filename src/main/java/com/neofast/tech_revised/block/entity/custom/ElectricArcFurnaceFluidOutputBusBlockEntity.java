package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class ElectricArcFurnaceFluidOutputBusBlockEntity extends BlockEntity {
    private static final int CAPACITY = 16000;

    private final FluidTank fluidTank = new FluidTank(CAPACITY, fluid -> true) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private LazyOptional<IFluidHandler> lazyFluidHandler = LazyOptional.empty();

    public ElectricArcFurnaceFluidOutputBusBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get(), worldPosition, blockState);
    }

        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return lazyFluidHandler.cast();
        }
        return net.minecraftforge.common.util.LazyOptional.empty();
    }

        public void onLoad() {
        lazyFluidHandler = LazyOptional.of(() -> fluidTank);
    }

        public void invalidateCaps() {
        lazyFluidHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        tag.put("tank", fluidTank.writeToNBT(new CompoundTag()));
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("tank")) {
            fluidTank.readFromNBT(tag.getCompound("tank"));
        }
    }

    public int fill(FluidStack stack, IFluidHandler.FluidAction action) {
        return fluidTank.fill(stack, action);
    }

    public int getFluidAmount() {
        return fluidTank.getFluidAmount();
    }

    public FluidStack getStoredFluid() {
        return fluidTank.getFluid().copy();
    }

    public int getCapacity() {
        return fluidTank.getCapacity();
    }
}
