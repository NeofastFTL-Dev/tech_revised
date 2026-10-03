package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.multiblock.MultiblockRender;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class BatchingMixerControllerBlockEntity extends BlockEntity implements MenuProvider {
    private final EnergyStorage energyStorage = new EnergyStorage(50000, 2000, 0);
    private LazyOptional<IEnergyStorage> lazyEnergy = LazyOptional.empty();

    public BatchingMixerControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATCHING_MIXER_CONTROLLER.get(), pos, state);
    }

        public AABB getRenderBoundingBox() {
        return MultiblockRender.bounds(this);
    }

        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return lazyEnergy.cast();
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
        tag.put("energy", energyStorage.serializeNBT());
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("energy")) energyStorage.deserializeNBT(tag.get("energy"));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BatchingMixerControllerBlockEntity blockEntity) {
        // Tick logic
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tech_revised.industrial_batching_mixer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return null;
    }
}
