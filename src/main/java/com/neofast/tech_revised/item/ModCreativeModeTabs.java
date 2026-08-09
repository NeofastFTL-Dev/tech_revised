package com.neofast.tech_revised.item;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TechRevised.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TECH_REVISED_TAB = CREATIVE_MODE_TABS.register("tech_revised_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.tech_revised_tab"))
                    .icon(() -> new ItemStack(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModBlocks.GRAPHITE_ORE.get());
                        output.accept(ModItems.GRAPHITE_DUST.get());
                        output.accept(ModItems.ELECTRODE.get());
                        output.accept(ModBlocks.CHROMITE_ORE.get());
                        output.accept(ModItems.CHROMITE_DUST.get());
                        output.accept(ModBlocks.CRUSHER.get());
                        output.accept(ModBlocks.WINDOWS_7_VM_BLOCK.get());
                        output.accept(ModBlocks.STEEL_BLOCK.get());
                        output.accept(ModItems.STEEL_INGOT.get());
                        output.accept(ModItems.STEEL_NUGGET.get());
                        output.accept(ModBlocks.STAINLESS_STEEL_BLOCK.get());
                        output.accept(ModItems.STAINLESS_STEEL_INGOT.get());
                        output.accept(ModItems.STAINLESS_STEEL_NUGGET.get());
                        output.accept(ModBlocks.CHROMITE_BLOCK.get());
                        output.accept(ModItems.CHROMITE_INGOT.get());
                        output.accept(ModItems.CHROMITE_NUGGET.get());
                        output.accept(ModBlocks.FERROCHROMIUM_BLOCK.get());
                        output.accept(ModItems.FERROCHROMIUM_INGOT.get());
                        output.accept(ModItems.FERROCHROMIUM_NUGGET.get());
                        output.accept(ModItems.CONFIGURATOR.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get());
                        output.accept(ModBlocks.TRANSPORT_FRAME.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_HEATER.get());
                        output.accept(ModBlocks.ELECTRIC_ARC_FURNACE_FRAME.get());
                        output.accept(ModBlocks.DRILLING_PLATFORM_CONTROLLER.get());
                        output.accept(ModBlocks.DRILLING_PLATFORM_DRILL_HEAD.get());
                        output.accept(ModBlocks.DRILLING_PLATFORM_FRAME.get());
                        output.accept(ModBlocks.COKE_OVEN_CONTROLLER.get());
                        output.accept(ModBlocks.COKE_OVEN_FRAME.get());
                        output.accept(ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER.get());
                        output.accept(ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_FRAME.get());
                        output.accept(ModBlocks.OXYGEN_CONVERTER_CONTROLLER.get());
                        output.accept(ModBlocks.OXYGEN_CONVERTER_FRAME.get());
                        output.accept(ModItems.COAL_COKE.get());
                        output.accept(ModItems.HEAVY_CRUDE_OIL_BUCKET.get());
                        output.accept(ModItems.CREOSOTE_BUCKET.get());
                        output.accept(ModItems.OXYGEN_BUCKET.get());
                        output.accept(ModItems.HYDROGEN_BUCKET.get());
                        output.accept(ModItems.LIQUID_GLASS_BUCKET.get());
                        output.accept(ModItems.SODIUM_CARBONATE_SOLUTION_BUCKET.get());
                        output.accept(ModItems.CUPRIC_CHLORIDE_SOLUTION_BUCKET.get());
                        output.accept(ModItems.SODIUM_HYDROXIDE_SOLUTION_BUCKET.get());

                        // Stainless Steel Chain
                        output.accept(ModBlocks.BLAST_FURNACE_CORE.get());
                        output.accept(ModItems.CHROMIUM_DUST.get());
                        output.accept(ModItems.NICKEL_DUST.get());
                        output.accept(ModItems.CARBON_CHUNK.get());
                        output.accept(ModItems.STAINLESS_STEEL_BLEND.get());

                        // Fiberglass Chain
                        output.accept(ModBlocks.INDUSTRIAL_BATCHING_MIXER.get());
                        output.accept(ModBlocks.REFRACTORY_MELTING_FURNACE.get());
                        output.accept(ModBlocks.CHEMICAL_SIZING_APPLICATOR.get());
                        output.accept(ModBlocks.HIGH_SPEED_GATHERING_WINDER.get());
                        output.accept(ModBlocks.INDUSTRIAL_DRYING_OVEN.get());
                        output.accept(ModBlocks.ROVING_CREEL_CONVERTER.get());
                        output.accept(ModBlocks.INDUSTRIAL_TEXTILE_WEAVING_LOOM.get());
                        output.accept(ModBlocks.STRAND_CHOPPING_MACHINERY.get());
                        output.accept(ModItems.SILICA_SAND.get());
                        output.accept(ModItems.LIMESTONE_DUST.get());
                        output.accept(ModItems.DOLOMITE_DUST.get());
                        output.accept(ModItems.SODA_ASH.get());
                        output.accept(ModItems.CLAY_DUST.get());
                        output.accept(ModItems.RAW_GLASS_BATCH.get());
                        output.accept(ModItems.PLATINUM_ALLOY_BUSHING_PLATE.get());
                        output.accept(ModItems.CHEMICAL_LUBRICANT.get());
                        output.accept(ModItems.GLASS_FILAMENT.get());
                        output.accept(ModItems.SIZED_GLASS_FILAMENT.get());
                        output.accept(ModItems.GLASS_SPOOL.get());
                        output.accept(ModItems.DRIED_GLASS_SPOOL.get());
                        output.accept(ModItems.GLASS_ROVING.get());
                        output.accept(ModItems.FIBERGLASS_FABRIC.get());
                        output.accept(ModItems.CHOPPED_GLASS_STRAND.get());
                        output.accept(ModItems.CHOPPED_STRAND_MAT.get());

                        // PCB Chain
                        output.accept(ModBlocks.DESIGN_ENGINEERING_STATION.get());
                        output.accept(ModBlocks.INDUSTRIAL_SHEAR.get());
                        output.accept(ModBlocks.DECONTAMINATION_OVEN.get());
                        output.accept(ModBlocks.HYDRAULIC_CLADDING_PRESS.get());
                        output.accept(ModBlocks.PHOTO_RESIST_APPLICATOR.get());
                        output.accept(ModBlocks.UV_LDI_IMAGER.get());
                        output.accept(ModBlocks.CHEMICAL_DEVELOPING_WASH.get());
                        output.accept(ModBlocks.ACIDIC_ETCHING_SPRAYER.get());
                        output.accept(ModBlocks.ALKALINE_STRIPPING_STATION.get());
                        output.accept(ModBlocks.VACUUM_LAMINATION_PRESS.get());
                        output.accept(ModBlocks.XRAY_ALIGNMENT_DRILL.get());
                        output.accept(ModBlocks.ELECTROLESS_PLATING_BATH.get());
                        output.accept(ModBlocks.COPPER_ELECTROPLATING_TANK.get());
                        output.accept(ModBlocks.TIN_PLATING_TANK.get());
                        output.accept(ModBlocks.SOLDERMASK_FLOODER.get());
                        output.accept(ModBlocks.UV_PAD_EXPOSURE_STATION.get());
                        output.accept(ModBlocks.INKJET_SILKSCREEN_PRINTER.get());
                        output.accept(ModBlocks.SURFACE_FINISH_STATION.get());
                        output.accept(ModBlocks.FLYING_PROBE_TESTER.get());
                        output.accept(ModBlocks.CNC_ROUTER.get());
                        output.accept(ModBlocks.SOLDER_PASTE_PRINTER.get());
                        output.accept(ModBlocks.PICK_AND_PLACE_ROBOT.get());
                        output.accept(ModBlocks.MULTI_ZONE_REFLOW_OVEN.get());
                        output.accept(ModBlocks.AOI_VERIFICATION_STATION.get());

                        output.accept(ModItems.CAD_LAYOUT.get());
                        output.accept(ModItems.VERIFIED_GERBER_FILE.get());
                        output.accept(ModItems.RAW_FR4_LAMINATE.get());
                        output.accept(ModItems.SHEARED_FR4_LAMINATE.get());
                        output.accept(ModItems.BAKED_FR4_LAMINATE.get());
                        output.accept(ModItems.COPPER_FOIL.get());
                        output.accept(ModItems.COPPER_CLAD_LAMINATE.get());
                        output.accept(ModItems.PHOTO_RESIST_POLYMER.get());
                        output.accept(ModItems.INNER_LAYER_PATTERNED_BOARD.get());
                        output.accept(ModItems.DEVELOPED_INNER_LAYER_BOARD.get());
                        output.accept(ModItems.SODIUM_CARBONATE_DUST.get());
                        output.accept(ModItems.CUPRIC_CHLORIDE_DUST.get());
                        output.accept(ModItems.SODIUM_HYDROXIDE_DUST.get());
                        output.accept(ModItems.ETCHED_INNER_LAYER_BOARD.get());
                        output.accept(ModItems.STRIPPED_INNER_LAYER_BOARD.get());
                        output.accept(ModItems.PREPREG_FABRIC.get());
                        output.accept(ModItems.MULTI_LAYER_STACKUP.get());
                        output.accept(ModItems.LAMINATED_PCB_CORE.get());
                        output.accept(ModItems.DRILLED_PCB.get());
                        output.accept(ModItems.ELECTROLESS_COPPER_COATED_PCB.get());
                        output.accept(ModItems.PATTERNED_OUTER_LAYER_PCB.get());
                        output.accept(ModItems.ELECTROPLATED_PCB.get());
                        output.accept(ModItems.TIN_PLATING_DUST.get());
                        output.accept(ModItems.TIN_PLATED_PCB.get());
                        output.accept(ModItems.FINAL_ETCHED_PCB.get());
                        output.accept(ModItems.TIN_STRIPPED_PCB.get());
                        output.accept(ModItems.SOLDERMASK_RESIN.get());
                        output.accept(ModItems.MASKED_PCB.get());
                        output.accept(ModItems.SILKSCREENED_PCB.get());
                        output.accept(ModItems.FINISHED_PCB_PANEL.get());
                        output.accept(ModItems.TESTED_PCB_PANEL.get());
                        output.accept(ModItems.ROUTED_PCB.get());
                        output.accept(ModItems.SOLDER_PASTE.get());
                        output.accept(ModItems.STENCIL_PRINTED_PCB.get());
                        output.accept(ModItems.PLACED_PCBA.get());
                        output.accept(ModItems.REFLOWED_PCBA.get());
                        output.accept(ModItems.FINAL_PCBA.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
