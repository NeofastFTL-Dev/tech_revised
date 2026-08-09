package com.neofast.tech_revised.block;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.custom.*;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.fluid.ModFluids;
import com.neofast.tech_revised.item.ModItems;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, TechRevised.MOD_ID);

    private static final Map<RegistryObject<? extends Block>, Integer> TINTED_BLOCK_COLORS = new LinkedHashMap<>();

    public static final RegistryObject<Block> STEEL_BLOCK = registerTintedBlock("steel_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()),
            ModItems.STEEL_TINT);

    public static final RegistryObject<Block> STAINLESS_STEEL_BLOCK = registerTintedBlock("stainless_steel_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()),
            ModItems.STAINLESS_STEEL_TINT);

    public static final RegistryObject<Block> CHROMITE_BLOCK = registerTintedBlock("chromite_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()),
            ModItems.CHROMITE_TINT);

    public static final RegistryObject<Block> FERROCHROMIUM_BLOCK = registerTintedBlock("ferrochromium_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()),
            ModItems.FERROCHROMIUM_TINT);

    public static final RegistryObject<Block> GRAPHITE_ORE = registerBlock("graphite_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.COAL_ORE).strength(3f).requiresCorrectToolForDrops(),
                    UniformInt.of(1, 3)));

    public static final RegistryObject<Block> CHROMITE_ORE = registerBlock("chromite_ore",
            () -> new DropExperienceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_ORE).strength(3.5f).requiresCorrectToolForDrops(),
                    UniformInt.of(1, 4)));

    public static final RegistryObject<Block> CRUSHER = registerBlock("crusher",
            () -> new CrusherBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(6f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> WINDOWS_7_VM_BLOCK = registerBlock("windows_7_vm_block",
            () -> new Windows7VmBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)
                    .strength(8f)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(Windows7VmBlock.ACTIVE) ? 12 : 0)));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_CONTROLLER = registerBlock("electric_arc_furnace_controller",
            () -> new ElectricArcFurnaceControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_INPUT_BUS = registerBlock("electric_arc_furnace_input_bus",
            () -> new ElectricArcFurnaceInputBusBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_OUTPUT_BUS = registerBlock("electric_arc_furnace_output_bus",
            () -> new ElectricArcFurnaceOutputBusBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS = registerBlock("electric_arc_furnace_fluid_input_bus",
            () -> new ElectricArcFurnaceFluidInputBusBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS = registerBlock("electric_arc_furnace_fluid_output_bus",
            () -> new ElectricArcFurnaceFluidOutputBusBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH = registerBlock("electric_arc_furnace_energy_input_hatch",
            () -> new ElectricArcFurnaceEnergyInputHatchBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_HEATER = registerBlock("electric_arc_furnace_heater",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops().lightLevel(state -> 12)));

    public static final RegistryObject<Block> BLAST_FURNACE_CORE = registerBlock("blast_furnace_core",
            () -> new BlastFurnaceControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> INDUSTRIAL_BATCHING_MIXER = registerBlock("industrial_batching_mixer",
            () -> new BatchingMixerControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> REFRACTORY_MELTING_FURNACE = registerBlock("refractory_melting_furnace",
            () -> new MeltingFurnaceControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops().lightLevel(state -> 13)));

    public static final RegistryObject<Block> CHEMICAL_SIZING_APPLICATOR = registerBlock("chemical_sizing_applicator",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> HIGH_SPEED_GATHERING_WINDER = registerBlock("high_speed_gathering_winder",
            () -> new GenericIndustrialMachineBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops(),
                    ModBlockEntities.GATHERING_WINDER));

    public static final RegistryObject<Block> INDUSTRIAL_DRYING_OVEN = registerBlock("industrial_drying_oven",
            () -> new GenericIndustrialMachineBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops().lightLevel(state -> 10),
                    ModBlockEntities.DRYING_OVEN));

    public static final RegistryObject<Block> ROVING_CREEL_CONVERTER = registerBlock("roving_creel_converter",
            () -> new GenericIndustrialMachineBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops(),
                    ModBlockEntities.CREEL_CONVERTER));

    public static final RegistryObject<Block> INDUSTRIAL_TEXTILE_WEAVING_LOOM = registerBlock("industrial_textile_weaving_loom",
            () -> new GenericIndustrialMachineBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops(),
                    ModBlockEntities.TEXTILE_LOOM));

    public static final RegistryObject<Block> STRAND_CHOPPING_MACHINERY = registerBlock("strand_chopping_machinery",
            () -> new GenericIndustrialMachineBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops(),
                    ModBlockEntities.STRAND_CHOPPER));

    // PCB Machines (multi-input processing; vacuum lamination uses a dedicated recipe type)
    private static BlockBehaviour.Properties pcbMachineProperties() {
        return BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(8f).requiresCorrectToolForDrops();
    }

    public static final RegistryObject<Block> DESIGN_ENGINEERING_STATION = registerBlock("design_engineering_station",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> INDUSTRIAL_SHEAR = registerBlock("industrial_shear",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> DECONTAMINATION_OVEN = registerBlock("decontamination_oven",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> HYDRAULIC_CLADDING_PRESS = registerBlock("hydraulic_cladding_press",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> PHOTO_RESIST_APPLICATOR = registerBlock("photo_resist_applicator",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> UV_LDI_IMAGER = registerBlock("uv_ldi_imager",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> CHEMICAL_DEVELOPING_WASH = registerBlock("chemical_developing_wash",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> ACIDIC_ETCHING_SPRAYER = registerBlock("acidic_etching_sprayer",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> ALKALINE_STRIPPING_STATION = registerBlock("alkaline_stripping_station",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> VACUUM_LAMINATION_PRESS = registerBlock("vacuum_lamination_press",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.LAMINATION_MACHINE));
    public static final RegistryObject<Block> XRAY_ALIGNMENT_DRILL = registerBlock("xray_alignment_drill",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> ELECTROLESS_PLATING_BATH = registerBlock("electroless_plating_bath",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> COPPER_ELECTROPLATING_TANK = registerBlock("copper_electroplating_tank",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> TIN_PLATING_TANK = registerBlock("tin_plating_tank",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> SOLDERMASK_FLOODER = registerBlock("soldermask_flooder",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> UV_PAD_EXPOSURE_STATION = registerBlock("uv_pad_exposure_station",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> INKJET_SILKSCREEN_PRINTER = registerBlock("inkjet_silkscreen_printer",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> SURFACE_FINISH_STATION = registerBlock("surface_finish_station",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> FLYING_PROBE_TESTER = registerBlock("flying_probe_tester",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> CNC_ROUTER = registerBlock("cnc_router",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> SOLDER_PASTE_PRINTER = registerBlock("solder_paste_printer",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> PICK_AND_PLACE_ROBOT = registerBlock("pick_and_place_robot",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> MULTI_ZONE_REFLOW_OVEN = registerBlock("multi_zone_reflow_oven",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));
    public static final RegistryObject<Block> AOI_VERIFICATION_STATION = registerBlock("aoi_verification_station",
            () -> new MultiInputIndustrialMachineBlock(pcbMachineProperties(), ModBlockEntities.PCB_MACHINE));

    public static final RegistryObject<Block> ELECTRIC_ARC_FURNACE_FRAME = registerBlock("electric_arc_furnace_frame",
            () -> new ElectricArcFurnaceFrameBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(9f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> DRILLING_PLATFORM_CONTROLLER = registerBlock("drilling_platform_controller",
            () -> new DrillingPlatformControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> DRILLING_PLATFORM_DRILL_HEAD = registerBlock("drilling_platform_drill_head",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(9f).requiresCorrectToolForDrops().lightLevel(state -> 8)));

    public static final RegistryObject<Block> DRILLING_PLATFORM_FRAME = registerBlock("drilling_platform_frame",
            () -> new DrillingPlatformFrameBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(9f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> COKE_OVEN_CONTROLLER = registerBlock("coke_oven_controller",
            () -> new CokeOvenControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> COKE_OVEN_FRAME = registerBlock("coke_oven_frame",
            () -> new CokeOvenFrameBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(9f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER = registerBlock(
            "argon_oxygen_decarburization_converter_controller",
            () -> new ArgonOxygenDecarburizationConverterControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> ARGON_OXYGEN_DECARBURIZATION_CONVERTER_FRAME = registerBlock(
            "argon_oxygen_decarburization_converter_frame",
            () -> new ArgonOxygenDecarburizationConverterFrameBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(9f).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> OXYGEN_CONVERTER_CONTROLLER = registerBlock("oxygen_converter_controller",
            () -> new OxygenConverterControllerBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    public static final RegistryObject<Block> OXYGEN_CONVERTER_FRAME = registerBlock("oxygen_converter_frame",
            () -> new OxygenConverterFrameBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(9f).requiresCorrectToolForDrops()));

    public static final RegistryObject<LiquidBlock> HEAVY_CRUDE_OIL_BLOCK = registerBlockWithoutItem("heavy_crude_oil_block",
            () -> new LiquidBlock(ModFluids.HEAVY_CRUDE_OIL, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    public static final RegistryObject<LiquidBlock> CREOSOTE_BLOCK = registerBlockWithoutItem("creosote_block",
            () -> new LiquidBlock(ModFluids.CREOSOTE, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    public static final RegistryObject<LiquidBlock> OXYGEN_BLOCK = registerBlockWithoutItem("oxygen_block",
            () -> new LiquidBlock(ModFluids.OXYGEN, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    public static final RegistryObject<LiquidBlock> HYDROGEN_BLOCK = registerBlockWithoutItem("hydrogen_block",
            () -> new LiquidBlock(ModFluids.HYDROGEN, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    public static final RegistryObject<LiquidBlock> LIQUID_GLASS_BLOCK = registerBlockWithoutItem("liquid_glass_block",
            () -> new LiquidBlock(ModFluids.LIQUID_GLASS, BlockBehaviour.Properties.copy(Blocks.LAVA).noLootTable()));

    public static final RegistryObject<LiquidBlock> SODIUM_CARBONATE_SOLUTION_BLOCK = registerBlockWithoutItem("sodium_carbonate_solution_block",
            () -> new LiquidBlock(ModFluids.SODIUM_CARBONATE_SOLUTION, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    public static final RegistryObject<LiquidBlock> CUPRIC_CHLORIDE_SOLUTION_BLOCK = registerBlockWithoutItem("cupric_chloride_solution_block",
            () -> new LiquidBlock(ModFluids.CUPRIC_CHLORIDE_SOLUTION, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    public static final RegistryObject<LiquidBlock> SODIUM_HYDROXIDE_SOLUTION_BLOCK = registerBlockWithoutItem("sodium_hydroxide_solution_block",
            () -> new LiquidBlock(ModFluids.SODIUM_HYDROXIDE_SOLUTION, BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<T> registerTintedBlock(String name, Supplier<T> block, int color) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        RegistryObject<Item> blockItem = registerBlockItem(name, toReturn);
        TINTED_BLOCK_COLORS.put(toReturn, color);
        ModItems.registerTintedItemColor(blockItem, color);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<T> registerBlockWithoutItem(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static Map<RegistryObject<? extends Block>, Integer> getTintedBlockColors() {
        return Collections.unmodifiableMap(TINTED_BLOCK_COLORS);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
