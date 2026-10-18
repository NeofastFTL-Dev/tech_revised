package com.neofast.tech_revised.multiblock;

public interface MultiblockControllerAccess {
    int getProgress();

    int getProcessTicks();

    boolean isStructureFormed();

    default int getEnergyStored() {
        return 0;
    }

    default int getMaxEnergyStored() {
        return 0;
    }

    default int getAuxiliaryAmount() {
        return 0;
    }

    default int getAuxiliaryCapacity() {
        return 0;
    }

    default String getAuxiliaryLabel() {
        return "";
    }
}
