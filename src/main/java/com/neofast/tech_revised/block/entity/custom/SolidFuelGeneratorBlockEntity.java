package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.custom.SolidFuelGeneratorBlock;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

/**
 * Solid Fuel Generator: burns any furnace-fuel item (coal, charcoal, coal coke...)
 * into Forge Energy. Energy is pushed to adjacent energy hatches and machine
 * energy storages first, then exposed to cable mods through the ENERGY capability.
 */
public class SolidFuelGeneratorBlockEntity extends BlockEntity {
    public static final int CAPACITY = 60000;
    public static final int PRODUCTION_PER_TICK = 120;
    public static final int PUSH_PER_TICK = 2000;

    private static final String ENERGY_TAG = "energy";
    private static final String ITEMS_TAG = "inventory";
    private static final String BURN_TAG = "burnTimeLeft";
    private static final String DURATION_TAG = "burnDuration";

    private final ItemStackHandler fuelInventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return getBurnTime(stack) > 0;
        }
    };
    private LazyOptional<IItemHandler> lazyFuel = LazyOptional.empty();

    private final EnergyStorage energyStorage = new EnergyStorage(CAPACITY, PRODUCTION_PER_TICK, PUSH_PER_TICK);
    private LazyOptional<IEnergyStorage> lazyEnergy = LazyOptional.empty();

    private int burnTimeLeft;
    private int burnDuration;

    public SolidFuelGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLID_FUEL_GENERATOR.get(), pos, state);
    }

    private static int getBurnTime(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SolidFuelGeneratorBlockEntity generator) {
        if (level.isClientSide()) {
            return;
        }

        boolean burning = generator.burnTimeLeft > 0;
        if (generator.burnTimeLeft > 0) {
            generator.burnTimeLeft--;
            generator.energyStorage.receiveEnergy(PRODUCTION_PER_TICK, false);
        }

        // Ignite new fuel when the buffer has room
        ItemStack fuel = generator.fuelInventory.getStackInSlot(0);
        if (generator.burnTimeLeft <= 0 && !fuel.isEmpty()) {
            int burn = getBurnTime(fuel);
            if (burn > 0 && generator.energyStorage.getEnergyStored() < CAPACITY) {
                generator.fuelInventory.extractItem(0, 1, false);
                generator.burnDuration = burn;
                generator.burnTimeLeft = burn;
                generator.setChanged();
                burning = true;
            }
        }

        // Push energy to neighbors (hatches expose receiveEnergy through pullEnergyFromHatch-compatible extracts;
        // we simulate-then-commit receiveEnergy on them so both hatches and direct machines work)
        if (generator.energyStorage.getEnergyStored() > 0) {
            for (Direction side : Direction.values()) {
                BlockEntity neighbor = level.getBlockEntity(pos.relative(side));
                if (neighbor == null) {
                    continue;
                }
                net.minecraftforge.common.capabilities.ForgeCapabilities.get(neighbor, ForgeCapabilities.ENERGY, side.getOpposite()).ifPresent(storage -> {
                    int free = storage.getMaxEnergyStored() - storage.getEnergyStored();
                    if (free <= 0) {
                        return;
                    }
                    int toSend = Math.min(free, PUSH_PER_TICK);
                    int accepted = storage.receiveEnergy(toSend, true);
                    if (accepted <= 0) {
                        return;
                    }
                    int extracted = generator.energyStorage.extractEnergy(accepted, false);
                    if (extracted > 0) {
                        storage.receiveEnergy(extracted, false);
                    }
                });
            }
        }

        boolean lit = generator.burnTimeLeft > 0 || burning;
        if (state.getValue(SolidFuelGeneratorBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(SolidFuelGeneratorBlock.LIT, lit), 3);
        }
        generator.setChanged();
    }

    public ItemStack insertFuel(ItemStack stack, boolean simulate) {
        if (getBurnTime(stack) <= 0) {
            return stack;
        }
        ItemStack remainder = fuelInventory.insertItem(0, stack, simulate);
        if (!simulate) {
            setChanged();
        }
        return remainder;
    }

    public boolean isFuel(ItemStack stack) {
        return getBurnTime(stack) > 0;
    }

    public int getBurnTimeLeft() {
        return burnTimeLeft;
    }

    public int getBurnDuration() {
        return burnDuration;
    }

    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return CAPACITY;
    }

    public void dropContents(Level level, double x, double y, double z) {
        for (int i = 0; i < fuelInventory.getSlots(); i++) {
            ItemStack stack = fuelInventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, x, y, z, stack);
            }
        }
    }

        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return lazyEnergy.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyFuel.cast();
        }
        return net.minecraftforge.common.util.LazyOptional.empty();
    }

        public void onLoad() {
        // Expose an extraction-only view so cables can drain but nothing can charge the generator.
        lazyEnergy = LazyOptional.of(() -> new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                return 0;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return energyStorage.extractEnergy(maxExtract, simulate);
            }

            @Override
            public int getEnergyStored() {
                return energyStorage.getEnergyStored();
            }

            @Override
            public int getMaxEnergyStored() {
                return energyStorage.getMaxEnergyStored();
            }

            @Override
            public boolean canExtract() {
                return true;
            }

            @Override
            public boolean canReceive() {
                return false;
            }
        });
        lazyFuel = LazyOptional.of(() -> fuelInventory);
    }

        public void invalidateCaps() {
        lazyEnergy.invalidate();
        lazyFuel.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        tag.put(ITEMS_TAG, fuelInventory.serializeNBT());
        tag.put(ENERGY_TAG, energyStorage.serializeNBT());
        tag.putInt(BURN_TAG, burnTimeLeft);
        tag.putInt(DURATION_TAG, burnDuration);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(ITEMS_TAG)) {
            fuelInventory.deserializeNBT(tag.getCompound(ITEMS_TAG));
        }
        if (tag.contains(ENERGY_TAG)) {
            energyStorage.deserializeNBT(tag.get(ENERGY_TAG));
        }
        burnTimeLeft = tag.getInt(BURN_TAG);
        burnDuration = tag.getInt(DURATION_TAG);
    }
}
