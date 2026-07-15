package com.neofast.tech_revised.item;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.fluid.ModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TechRevised.MOD_ID);

    public static final int STEEL_TINT = 0xFF8E949C;
    public static final int STAINLESS_STEEL_TINT = 0xFFC7CCD2;
    public static final int CHROMITE_TINT = 0xFF8D7260;
    public static final int FERROCHROMIUM_TINT = 0xFF6C7364;

    // Shared item tint map (ingots, nuggets, and tinted block items).
    private static final Map<RegistryObject<Item>, Integer> TINTED_ITEM_COLORS = new LinkedHashMap<>();

    public static final RegistryObject<Item> STEEL_INGOT = registerTintedItem("steel_ingot", STEEL_TINT);
    public static final RegistryObject<Item> STEEL_NUGGET = registerTintedItem("steel_nugget", STEEL_TINT);
    public static final RegistryObject<Item> STAINLESS_STEEL_INGOT = registerTintedItem("stainless_steel_ingot", STAINLESS_STEEL_TINT);
    public static final RegistryObject<Item> STAINLESS_STEEL_NUGGET = registerTintedItem("stainless_steel_nugget", STAINLESS_STEEL_TINT);
    public static final RegistryObject<Item> CHROMITE_INGOT = registerTintedItem("chromite_ingot", CHROMITE_TINT);
    public static final RegistryObject<Item> CHROMITE_NUGGET = registerTintedItem("chromite_nugget", CHROMITE_TINT);
    public static final RegistryObject<Item> FERROCHROMIUM_INGOT = registerTintedItem("ferrochromium_ingot", FERROCHROMIUM_TINT);
    public static final RegistryObject<Item> FERROCHROMIUM_NUGGET = registerTintedItem("ferrochromium_nugget", FERROCHROMIUM_TINT);
    public static final RegistryObject<Item> GRAPHITE_DUST = ITEMS.register("graphite_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHROMIUM_DUST = ITEMS.register("chromium_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> NICKEL_DUST = ITEMS.register("nickel_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CARBON_CHUNK = ITEMS.register("carbon_chunk",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STAINLESS_STEEL_BLEND = ITEMS.register("stainless_steel_blend",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SILICA_SAND = ITEMS.register("silica_sand",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LIMESTONE_DUST = ITEMS.register("limestone_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DOLOMITE_DUST = ITEMS.register("dolomite_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SODA_ASH = ITEMS.register("soda_ash",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CLAY_DUST = ITEMS.register("clay_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RAW_GLASS_BATCH = ITEMS.register("raw_glass_batch",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PLATINUM_ALLOY_BUSHING_PLATE = ITEMS.register("platinum_alloy_bushing_plate",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHEMICAL_LUBRICANT = ITEMS.register("chemical_lubricant",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLASS_FILAMENT = ITEMS.register("glass_filament",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SIZED_GLASS_FILAMENT = ITEMS.register("sized_glass_filament",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLASS_SPOOL = ITEMS.register("glass_spool",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DRIED_GLASS_SPOOL = ITEMS.register("dried_glass_spool",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GLASS_ROVING = ITEMS.register("glass_roving",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FIBERGLASS_FABRIC = ITEMS.register("fiberglass_fabric",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHOPPED_GLASS_STRAND = ITEMS.register("chopped_glass_strand",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CHOPPED_STRAND_MAT = ITEMS.register("chopped_strand_mat",
            () -> new Item(new Item.Properties()));

    // PCB Stage 1
    public static final RegistryObject<Item> CAD_LAYOUT = ITEMS.register("cad_layout",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> VERIFIED_GERBER_FILE = ITEMS.register("verified_gerber_file",
            () -> new Item(new Item.Properties()));

    // PCB Stage 2
    public static final RegistryObject<Item> RAW_FR4_LAMINATE = ITEMS.register("raw_fr4_laminate",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SHEARED_FR4_LAMINATE = ITEMS.register("sheared_fr4_laminate",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BAKED_FR4_LAMINATE = ITEMS.register("baked_fr4_laminate",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_FOIL = ITEMS.register("copper_foil",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COPPER_CLAD_LAMINATE = ITEMS.register("copper_clad_laminate",
            () -> new Item(new Item.Properties()));

    // PCB Stage 3
    public static final RegistryObject<Item> PHOTO_RESIST_POLYMER = ITEMS.register("photo_resist_polymer",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> INNER_LAYER_PATTERNED_BOARD = ITEMS.register("inner_layer_patterned_board",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> DEVELOPED_INNER_LAYER_BOARD = ITEMS.register("developed_inner_layer_board",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SODIUM_CARBONATE_DUST = ITEMS.register("sodium_carbonate_dust",
            () -> new Item(new Item.Properties()));

    // PCB Stage 4
    public static final RegistryObject<Item> CUPRIC_CHLORIDE_DUST = ITEMS.register("cupric_chloride_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SODIUM_HYDROXIDE_DUST = ITEMS.register("sodium_hydroxide_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ETCHED_INNER_LAYER_BOARD = ITEMS.register("etched_inner_layer_board",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STRIPPED_INNER_LAYER_BOARD = ITEMS.register("stripped_inner_layer_board",
            () -> new Item(new Item.Properties()));

    // PCB Stage 5
    public static final RegistryObject<Item> PREPREG_FABRIC = ITEMS.register("prepreg_fabric",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MULTI_LAYER_STACKUP = ITEMS.register("multi_layer_stackup",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> LAMINATED_PCB_CORE = ITEMS.register("laminated_pcb_core",
            () -> new Item(new Item.Properties()));

    // PCB Stage 6
    public static final RegistryObject<Item> DRILLED_PCB = ITEMS.register("drilled_pcb",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ELECTROLESS_COPPER_COATED_PCB = ITEMS.register("electroless_copper_coated_pcb",
            () -> new Item(new Item.Properties()));

    // PCB Stage 7
    public static final RegistryObject<Item> PATTERNED_OUTER_LAYER_PCB = ITEMS.register("patterned_outer_layer_pcb",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ELECTROPLATED_PCB = ITEMS.register("electroplated_pcb",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TIN_PLATING_DUST = ITEMS.register("tin_plating_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TIN_PLATED_PCB = ITEMS.register("tin_plated_pcb",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FINAL_ETCHED_PCB = ITEMS.register("final_etched_pcb",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TIN_STRIPPED_PCB = ITEMS.register("tin_stripped_pcb",
            () -> new Item(new Item.Properties()));

    // PCB Stage 8
    public static final RegistryObject<Item> SOLDERMASK_RESIN = ITEMS.register("soldermask_resin",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MASKED_PCB = ITEMS.register("masked_pcb",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SILKSCREENED_PCB = ITEMS.register("silkscreened_pcb",
            () -> new Item(new Item.Properties()));

    // PCB Stage 9
    public static final RegistryObject<Item> FINISHED_PCB_PANEL = ITEMS.register("finished_pcb_panel",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TESTED_PCB_PANEL = ITEMS.register("tested_pcb_panel",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ROUTED_PCB = ITEMS.register("routed_pcb",
            () -> new Item(new Item.Properties()));

    // PCB Stage 10
    public static final RegistryObject<Item> SOLDER_PASTE = ITEMS.register("solder_paste",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STENCIL_PRINTED_PCB = ITEMS.register("stencil_printed_pcb",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> PLACED_PCBA = ITEMS.register("placed_pcba",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> REFLOWED_PCBA = ITEMS.register("reflowed_pcba",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FINAL_PCBA = ITEMS.register("final_pcba",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ELECTRODE = ITEMS.register("electrode",
            () -> new Item(new Item.Properties().durability(256)));
    public static final RegistryObject<Item> CHROMITE_DUST = ITEMS.register("chromite_dust",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COAL_COKE = ITEMS.register("coal_coke",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CONFIGURATOR = ITEMS.register("configurator",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> HEAVY_CRUDE_OIL_BUCKET = ITEMS.register("heavy_crude_oil_bucket",
            () -> new BucketItem(ModFluids.HEAVY_CRUDE_OIL, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));
    public static final RegistryObject<Item> CREOSOTE_BUCKET = ITEMS.register("creosote_bucket",
            () -> new BucketItem(ModFluids.CREOSOTE, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));
    public static final RegistryObject<Item> OXYGEN_BUCKET = ITEMS.register("oxygen_bucket",
            () -> new BucketItem(ModFluids.OXYGEN, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));
    public static final RegistryObject<Item> HYDROGEN_BUCKET = ITEMS.register("hydrogen_bucket",
            () -> new BucketItem(ModFluids.HYDROGEN, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));
    public static final RegistryObject<Item> LIQUID_GLASS_BUCKET = ITEMS.register("liquid_glass_bucket",
            () -> new BucketItem(ModFluids.LIQUID_GLASS, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));

    public static final RegistryObject<Item> SODIUM_CARBONATE_SOLUTION_BUCKET = ITEMS.register("sodium_carbonate_solution_bucket",
            () -> new BucketItem(ModFluids.SODIUM_CARBONATE_SOLUTION, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));
    public static final RegistryObject<Item> CUPRIC_CHLORIDE_SOLUTION_BUCKET = ITEMS.register("cupric_chloride_solution_bucket",
            () -> new BucketItem(ModFluids.CUPRIC_CHLORIDE_SOLUTION, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));
    public static final RegistryObject<Item> SODIUM_HYDROXIDE_SOLUTION_BUCKET = ITEMS.register("sodium_hydroxide_solution_bucket",
            () -> new BucketItem(ModFluids.SODIUM_HYDROXIDE_SOLUTION, new Item.Properties()
                    .craftRemainder(Items.BUCKET)
                    .stacksTo(1)));

    private static RegistryObject<Item> registerTintedItem(String name, int color) {
        RegistryObject<Item> item = ITEMS.register(name, () -> new Item(new Item.Properties()));
        registerTintedItemColor(item, color);
        return item;
    }

    public static void registerTintedItemColor(RegistryObject<Item> item, int color) {
        TINTED_ITEM_COLORS.put(item, color);
    }

    public static Map<RegistryObject<Item>, Integer> getTintedItemColors() {
        return Collections.unmodifiableMap(TINTED_ITEM_COLORS);
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
