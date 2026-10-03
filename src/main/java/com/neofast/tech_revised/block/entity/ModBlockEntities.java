package com.neofast.tech_revised.block.entity;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.custom.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TechRevised.MOD_ID);

    public static final RegistryObject<BlockEntityType<ElectricArcFurnaceControllerBlockEntity>> ELECTRIC_ARC_FURNACE_CONTROLLER =
            BLOCK_ENTITIES.register("electric_arc_furnace_controller_entity", () ->
                    BlockEntityType.Builder.of(ElectricArcFurnaceControllerBlockEntity::new,
                            ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get()).build());

    public static final RegistryObject<BlockEntityType<CokeOvenControllerBlockEntity>> COKE_OVEN_CONTROLLER =
            BLOCK_ENTITIES.register("coke_oven_controller_entity", () ->
                    BlockEntityType.Builder.of(CokeOvenControllerBlockEntity::new,
                            ModBlocks.COKE_OVEN_CONTROLLER.get()).build());

    public static final RegistryObject<BlockEntityType<ElectricArcFurnaceInputBusBlockEntity>> ELECTRIC_ARC_FURNACE_INPUT_BUS =
            BLOCK_ENTITIES.register("electric_arc_furnace_input_bus_entity", () ->
                    BlockEntityType.Builder.of(ElectricArcFurnaceInputBusBlockEntity::new,
                            ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get()).build());

    public static final RegistryObject<BlockEntityType<ElectricArcFurnaceOutputBusBlockEntity>> ELECTRIC_ARC_FURNACE_OUTPUT_BUS =
            BLOCK_ENTITIES.register("electric_arc_furnace_output_bus_entity", () ->
                    BlockEntityType.Builder.of(ElectricArcFurnaceOutputBusBlockEntity::new,
                            ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get()).build());

    public static final RegistryObject<BlockEntityType<ElectricArcFurnaceFluidInputBusBlockEntity>> ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS =
            BLOCK_ENTITIES.register("electric_arc_furnace_fluid_input_bus_entity", () ->
                    BlockEntityType.Builder.of(ElectricArcFurnaceFluidInputBusBlockEntity::new,
                            ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get()).build());

    public static final RegistryObject<BlockEntityType<ElectricArcFurnaceFluidOutputBusBlockEntity>> ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS =
            BLOCK_ENTITIES.register("electric_arc_furnace_fluid_output_bus_entity", () ->
                    BlockEntityType.Builder.of(ElectricArcFurnaceFluidOutputBusBlockEntity::new,
                            ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get()).build());

    public static final RegistryObject<BlockEntityType<ElectricArcFurnaceEnergyInputHatchBlockEntity>> ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH =
            BLOCK_ENTITIES.register("electric_arc_furnace_energy_input_hatch_entity", () ->
                    BlockEntityType.Builder.of(ElectricArcFurnaceEnergyInputHatchBlockEntity::new,
                            ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get()).build());

    public static final RegistryObject<BlockEntityType<CrusherBlockEntity>> CRUSHER =
            BLOCK_ENTITIES.register("crusher_entity", () ->
                    BlockEntityType.Builder.of(CrusherBlockEntity::new,
                            ModBlocks.CRUSHER.get()).build());

    public static final RegistryObject<BlockEntityType<SolidFuelGeneratorBlockEntity>> SOLID_FUEL_GENERATOR =
            BLOCK_ENTITIES.register("solid_fuel_generator_entity", () ->
                    BlockEntityType.Builder.of(SolidFuelGeneratorBlockEntity::new,
                            ModBlocks.SOLID_FUEL_GENERATOR.get()).build());

    public static final RegistryObject<BlockEntityType<Windows7VmBlockEntity>> WINDOWS_7_VM_BLOCK =
            BLOCK_ENTITIES.register("windows_7_vm_block_entity", () ->
                    BlockEntityType.Builder.of(Windows7VmBlockEntity::new,
                            ModBlocks.WINDOWS_7_VM_BLOCK.get(),
                            ModBlocks.TESTED_MONITOR.get()).build());

    public static final RegistryObject<BlockEntityType<DrillingPlatformControllerBlockEntity>> DRILLING_PLATFORM_CONTROLLER =
            BLOCK_ENTITIES.register("drilling_platform_controller_entity", () ->
                    BlockEntityType.Builder.of(DrillingPlatformControllerBlockEntity::new,
                            ModBlocks.DRILLING_PLATFORM_CONTROLLER.get()).build());

    public static final RegistryObject<BlockEntityType<ArgonOxygenDecarburizationConverterControllerBlockEntity>> ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER =
            BLOCK_ENTITIES.register("argon_oxygen_decarburization_converter_controller_entity", () ->
                    BlockEntityType.Builder.of(ArgonOxygenDecarburizationConverterControllerBlockEntity::new,
                            ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER.get()).build());

    public static final RegistryObject<BlockEntityType<OxygenConverterControllerBlockEntity>> OXYGEN_CONVERTER_CONTROLLER =
            BLOCK_ENTITIES.register("oxygen_converter_controller_entity", () ->
                    BlockEntityType.Builder.of(OxygenConverterControllerBlockEntity::new,
                            ModBlocks.OXYGEN_CONVERTER_CONTROLLER.get()).build());

    public static final RegistryObject<BlockEntityType<BlastFurnaceControllerBlockEntity>> BLAST_FURNACE_CONTROLLER =
            BLOCK_ENTITIES.register("blast_furnace_controller_entity", () ->
                    BlockEntityType.Builder.of(BlastFurnaceControllerBlockEntity::new,
                            ModBlocks.BLAST_FURNACE_CORE.get()).build());

    public static final RegistryObject<BlockEntityType<BatchingMixerControllerBlockEntity>> BATCHING_MIXER_CONTROLLER =
            BLOCK_ENTITIES.register("batching_mixer_controller_entity", () ->
                    BlockEntityType.Builder.of(BatchingMixerControllerBlockEntity::new,
                            ModBlocks.INDUSTRIAL_BATCHING_MIXER.get()).build());

    public static final RegistryObject<BlockEntityType<MeltingFurnaceControllerBlockEntity>> MELTING_FURNACE_CONTROLLER =
            BLOCK_ENTITIES.register("melting_furnace_controller_entity", () ->
                    BlockEntityType.Builder.of(MeltingFurnaceControllerBlockEntity::new,
                            ModBlocks.REFRACTORY_MELTING_FURNACE.get()).build());

    public static final RegistryObject<BlockEntityType<DryingOvenBlockEntity>> DRYING_OVEN =
            BLOCK_ENTITIES.register("drying_oven_entity", () ->
                    BlockEntityType.Builder.of(DryingOvenBlockEntity::new,
                            ModBlocks.INDUSTRIAL_DRYING_OVEN.get()).build());

    public static final RegistryObject<BlockEntityType<GatheringWinderBlockEntity>> GATHERING_WINDER =
            BLOCK_ENTITIES.register("gathering_winder_entity", () ->
                    BlockEntityType.Builder.of(GatheringWinderBlockEntity::new,
                            ModBlocks.HIGH_SPEED_GATHERING_WINDER.get()).build());

    public static final RegistryObject<BlockEntityType<CreelConverterBlockEntity>> CREEL_CONVERTER =
            BLOCK_ENTITIES.register("creel_converter_entity", () ->
                    BlockEntityType.Builder.of(CreelConverterBlockEntity::new,
                            ModBlocks.ROVING_CREEL_CONVERTER.get()).build());

    public static final RegistryObject<BlockEntityType<TextileLoomBlockEntity>> TEXTILE_LOOM =
            BLOCK_ENTITIES.register("textile_loom_entity", () ->
                    BlockEntityType.Builder.of(TextileLoomBlockEntity::new,
                            ModBlocks.INDUSTRIAL_TEXTILE_WEAVING_LOOM.get()).build());

    public static final RegistryObject<BlockEntityType<StrandChopperBlockEntity>> STRAND_CHOPPER =
            BLOCK_ENTITIES.register("strand_chopper_entity", () ->
                    BlockEntityType.Builder.of(StrandChopperBlockEntity::new,
                            ModBlocks.STRAND_CHOPPING_MACHINERY.get()).build());

    public static final RegistryObject<BlockEntityType<PcbMachineBlockEntity>> PCB_MACHINE =
            BLOCK_ENTITIES.register("pcb_machine_entity", () ->
                    BlockEntityType.Builder.of(PcbMachineBlockEntity::new, pcbValidBlocks()).build());

    private static Block[] pcbValidBlocks() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(ModBlocks.DESIGN_ENGINEERING_STATION.get());
        blocks.add(ModBlocks.INDUSTRIAL_SHEAR.get());
        blocks.add(ModBlocks.DECONTAMINATION_OVEN.get());
        blocks.add(ModBlocks.HYDRAULIC_CLADDING_PRESS.get());
        blocks.add(ModBlocks.PHOTO_RESIST_APPLICATOR.get());
        blocks.add(ModBlocks.UV_LDI_IMAGER.get());
        blocks.add(ModBlocks.CHEMICAL_DEVELOPING_WASH.get());
        blocks.add(ModBlocks.ACIDIC_ETCHING_SPRAYER.get());
        blocks.add(ModBlocks.ALKALINE_STRIPPING_STATION.get());
        blocks.add(ModBlocks.XRAY_ALIGNMENT_DRILL.get());
        blocks.add(ModBlocks.ELECTROLESS_PLATING_BATH.get());
        blocks.add(ModBlocks.COPPER_ELECTROPLATING_TANK.get());
        blocks.add(ModBlocks.TIN_PLATING_TANK.get());
        blocks.add(ModBlocks.SOLDERMASK_FLOODER.get());
        blocks.add(ModBlocks.UV_PAD_EXPOSURE_STATION.get());
        blocks.add(ModBlocks.INKJET_SILKSCREEN_PRINTER.get());
        blocks.add(ModBlocks.SURFACE_FINISH_STATION.get());
        blocks.add(ModBlocks.FLYING_PROBE_TESTER.get());
        blocks.add(ModBlocks.CNC_ROUTER.get());
        blocks.add(ModBlocks.SOLDER_PASTE_PRINTER.get());
        blocks.add(ModBlocks.PICK_AND_PLACE_ROBOT.get());
        blocks.add(ModBlocks.MULTI_ZONE_REFLOW_OVEN.get());
        blocks.add(ModBlocks.AOI_VERIFICATION_STATION.get());
        for (Block block : ModBlocks.pcProductionMachineBlocks()) {
            blocks.add(block);
        }
        return blocks.toArray(Block[]::new);
    }

    public static final RegistryObject<BlockEntityType<LaminationMachineBlockEntity>> LAMINATION_MACHINE =
            BLOCK_ENTITIES.register("lamination_machine_entity", () ->
                    BlockEntityType.Builder.of(LaminationMachineBlockEntity::new,
                            ModBlocks.VACUUM_LAMINATION_PRESS.get()).build());

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
