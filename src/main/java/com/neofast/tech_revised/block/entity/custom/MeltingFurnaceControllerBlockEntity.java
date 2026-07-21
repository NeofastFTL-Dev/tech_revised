package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class MeltingFurnaceControllerBlockEntity extends BlockEntity implements MenuProvider {
    private final EnergyStorage energyStorage = new EnergyStorage(50000, 2000, 0);
    private LazyOptional<IEnergyStorage> lazyEnergy = LazyOptional.empty();

    public MeltingFurnaceControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MELTING_FURNACE_CONTROLLER.get(), pos, state);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return lazyEnergy.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyEnergy = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyEnergy.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("energy", energyStorage.serializeNBT());
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("energy")) energyStorage.deserializeNBT(tag.get("energy"));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MeltingFurnaceControllerBlockEntity blockEntity) {
        // Tick logic
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tech_revised.refractory_melting_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return null;
    }
}
