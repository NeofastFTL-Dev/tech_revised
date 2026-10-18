package com.neofast.tech_revised.multiblock;

import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceEnergyInputHatchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.energy.EnergyStorage;

public final class MultiblockEnergy {
    private MultiblockEnergy() {
    }

    public static void pullFromHatches(Level level, BlockPos origin, BlockState state, EnergyStorage dest, int maxTransfer) {
        if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return;
        }
        MachineLayout layout = MachineLayouts.get(state.getBlock());
        if (layout == null) {
            return;
        }
        Direction front = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        for (ElectricArcFurnaceEnergyInputHatchBlockEntity hatch : layout.findBlockEntities(
                level, origin, front, ElectricArcFurnaceEnergyInputHatchBlockEntity.class)) {
            int freeSpace = dest.getMaxEnergyStored() - dest.getEnergyStored();
            if (freeSpace <= 0) {
                return;
            }
            int simulated = hatch.extractEnergy(Math.min(freeSpace, maxTransfer), true);
            if (simulated <= 0) {
                continue;
            }
            int accepted = dest.receiveEnergy(simulated, false);
            if (accepted > 0) {
                hatch.extractEnergy(accepted, false);
            }
        }
    }
}
