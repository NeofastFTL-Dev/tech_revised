package com.neofast.tech_revised.integration.jade;

import com.neofast.tech_revised.block.custom.ArgonOxygenDecarburizationConverterControllerBlock;
import com.neofast.tech_revised.block.custom.BatchingMixerControllerBlock;
import com.neofast.tech_revised.block.custom.BlastFurnaceControllerBlock;
import com.neofast.tech_revised.block.custom.CokeOvenControllerBlock;
import com.neofast.tech_revised.block.custom.CrusherBlock;
import com.neofast.tech_revised.block.custom.DrillingPlatformControllerBlock;
import com.neofast.tech_revised.block.custom.ElectricArcFurnaceControllerBlock;
import com.neofast.tech_revised.block.custom.ElectricArcFurnaceEnergyInputHatchBlock;
import com.neofast.tech_revised.block.custom.ElectricArcFurnaceInputBusBlock;
import com.neofast.tech_revised.block.custom.GenericIndustrialMachineBlock;
import com.neofast.tech_revised.block.custom.MeltingFurnaceControllerBlock;
import com.neofast.tech_revised.block.custom.MultiInputIndustrialMachineBlock;
import com.neofast.tech_revised.block.custom.OxygenConverterControllerBlock;
import com.neofast.tech_revised.block.custom.SolidFuelGeneratorBlock;
import com.neofast.tech_revised.block.custom.WorkstationMonitorBlock;
import com.neofast.tech_revised.block.entity.custom.ArgonOxygenDecarburizationConverterControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.BatchingMixerControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.BlastFurnaceControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.CokeOvenControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.CrusherBlockEntity;
import com.neofast.tech_revised.block.entity.custom.DrillingPlatformControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceEnergyInputHatchBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceInputBusBlockEntity;
import com.neofast.tech_revised.block.entity.custom.GenericIndustrialMachineBlockEntity;
import com.neofast.tech_revised.block.entity.custom.MeltingFurnaceControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.MultiInputIndustrialMachineBlockEntity;
import com.neofast.tech_revised.block.entity.custom.OxygenConverterControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.SolidFuelGeneratorBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade (WAILA fork) integration.
 *
 * <p>This class is only ever classloaded by Jade itself (Jade discovers it through
 * FML's annotation scan of {@code @WailaPlugin}), so the mod runs perfectly fine
 * when Jade is not installed - the JVM never touches Jade API classes in that case.</p>
 */
@WailaPlugin
public class TechRevisedJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        // Server side: collect machine state and push it to the client tooltip.
        // Keyed by BlockEntity class; base classes cover their subclasses
        // (drying oven, winder, creel, loom, chopper / PCB machines, lamination).
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                ElectricArcFurnaceControllerBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                ArgonOxygenDecarburizationConverterControllerBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                BlastFurnaceControllerBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                CokeOvenControllerBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                OxygenConverterControllerBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                DrillingPlatformControllerBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                CrusherBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                GenericIndustrialMachineBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                MultiInputIndustrialMachineBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                MeltingFurnaceControllerBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                BatchingMixerControllerBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                ElectricArcFurnaceEnergyInputHatchBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                ElectricArcFurnaceInputBusBlockEntity.class);
        registration.registerBlockDataProvider(TechRevisedDataProvider.INSTANCE,
                SolidFuelGeneratorBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        // Client side: keyed by Block class (per Jade's client API).
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                ElectricArcFurnaceControllerBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                ArgonOxygenDecarburizationConverterControllerBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                BlastFurnaceControllerBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                CokeOvenControllerBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                OxygenConverterControllerBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                DrillingPlatformControllerBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                CrusherBlock.class);
        // Base machine blocks cover every generic / multi-input machine instance
        // (textile line, PCB line, lamination press, ...).
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                GenericIndustrialMachineBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                MultiInputIndustrialMachineBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                MeltingFurnaceControllerBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                BatchingMixerControllerBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                ElectricArcFurnaceEnergyInputHatchBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                ElectricArcFurnaceInputBusBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                SolidFuelGeneratorBlock.class);
        registration.registerBlockComponent(TechRevisedDataProvider.INSTANCE,
                WorkstationMonitorBlock.class);
    }
}
