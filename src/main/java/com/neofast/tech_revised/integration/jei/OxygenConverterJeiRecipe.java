package com.neofast.tech_revised.integration.jei;

import net.minecraftforge.fluids.FluidStack;

public class OxygenConverterJeiRecipe {
    private final FluidStack input;
    private final FluidStack output1;
    private final FluidStack output2;
    private final int processTicks;
    private final int energyPerTick;

    public OxygenConverterJeiRecipe(FluidStack input, FluidStack output1, FluidStack output2, int processTicks, int energyPerTick) {
        this.input = input;
        this.output1 = output1;
        this.output2 = output2;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
    }

    public FluidStack getInput() {
        return input;
    }

    public FluidStack getOutput1() {
        return output1;
    }

    public FluidStack getOutput2() {
        return output2;
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }
}
