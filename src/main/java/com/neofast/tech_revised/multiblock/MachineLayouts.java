package com.neofast.tech_revised.multiblock;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.item.PcCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class MachineLayouts {
    private static final Map<String, MachineLayout> BY_ID = new LinkedHashMap<>();
    private static final Map<Block, MachineLayout> BY_BLOCK = new LinkedHashMap<>();
    private static boolean initialized;

    private MachineLayouts() {
    }

    public static MachineLayout get(String id) {
        ensureInit();
        return BY_ID.get(id);
    }

    public static MachineLayout get(Block block) {
        ensureInit();
        return BY_BLOCK.get(block);
    }

    public static Collection<MachineLayout> all() {
        ensureInit();
        return BY_ID.values();
    }

    public static boolean isFormed(Level level, BlockPos pos, BlockState state) {
        MachineLayout layout = get(state.getBlock());
        if (layout == null || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return layout == null;
        }
        return layout.isValid(level, pos, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    private static void ensureInit() {
        if (initialized) {
            return;
        }
        initialized = true;
        registerHeavyIndustry();
        registerCoreFactory();
        registerPcbLine();
        registerPcProduction();
    }

    private static void registerHeavyIndustry() {
        Supplier<Block> eafFrame = ModBlocks.TRANSPORT_FRAME;
        register(MachineLayout.builder("eaf", ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER)
                .messages("message.tech_revised.electric_arc_furnace.formed", "message.tech_revised.electric_arc_furnace.incomplete")
                .cylinderSolid(0, 2, 5, eafFrame)
                .cylinder(1, 2, 2, 2, 5, eafFrame)
                .cylinderSolid(3, 2, 5, eafFrame)
                .cell(-1, 0, 1, ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS)
                .cell(1, 0, 1, ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS)
                .cell(-1, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS)
                .cell(0, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .cell(1, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS)
                .cell(0, 3, 2, ModBlocks.ELECTRIC_ARC_FURNACE_HEATER)
                .build());

        register(MachineLayout.builder("coke_oven", ModBlocks.COKE_OVEN_CONTROLLER)
                .messages("message.tech_revised.coke_oven.formed", "message.tech_revised.coke_oven.incomplete")
                .fill(-3, 3, 0, 0, 0, 2, ModBlocks.COKE_OVEN_FRAME)
                .fill(-3, 3, 3, 3, 0, 2, ModBlocks.COKE_OVEN_FRAME)
                .fill(-3, 3, 1, 2, 0, 0, ModBlocks.COKE_OVEN_FRAME)
                .fill(-3, 3, 1, 2, 2, 2, ModBlocks.COKE_OVEN_FRAME)
                .fill(-3, -3, 1, 2, 1, 1, ModBlocks.COKE_OVEN_FRAME)
                .fill(3, 3, 1, 2, 1, 1, ModBlocks.COKE_OVEN_FRAME)
                .cell(-1, 0, 1, ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS)
                .cell(1, 0, 1, ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS)
                .cell(0, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS)
                .build());

        register(MachineLayout.builder("drilling_platform", ModBlocks.DRILLING_PLATFORM_CONTROLLER)
                .messages("message.tech_revised.drilling_platform.formed", "message.tech_revised.drilling_platform.incomplete")
                .fill(-2, 2, 0, 0, 0, 3, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(0, 0, 1, ModBlocks.DRILLING_PLATFORM_DRILL_HEAD)
                .cell(-2, 0, 3, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .cell(2, 0, 3, ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS)
                .cell(-2, 1, 0, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(2, 1, 0, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(-2, 1, 3, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(2, 1, 3, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(-2, 2, 0, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(2, 2, 0, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(-2, 2, 3, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(2, 2, 3, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(-2, 3, 0, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(2, 3, 0, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(-2, 3, 3, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(2, 3, 3, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(-2, 4, 0, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(2, 4, 0, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(-2, 4, 3, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(2, 4, 3, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(-2, 5, 0, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(2, 5, 0, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(-2, 5, 3, ModBlocks.DRILLING_PLATFORM_FRAME)
                .cell(2, 5, 3, ModBlocks.DRILLING_PLATFORM_FRAME)
                .fill(-1, 1, 6, 6, 1, 2, ModBlocks.DRILLING_PLATFORM_FRAME)
                .build());

        register(MachineLayout.builder("oxygen_converter", ModBlocks.OXYGEN_CONVERTER_CONTROLLER)
                .messages("message.tech_revised.oxygen_converter.formed", "message.tech_revised.oxygen_converter.incomplete")
                .shell(-2, 2, 0, 2, 0, 2, ModBlocks.OXYGEN_CONVERTER_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .cell(1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS)
                .cell(-2, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS)
                .cell(2, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS)
                .build());

        register(MachineLayout.builder("aod_converter", ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER)
                .messages("message.tech_revised.argon_oxygen_decarburization_converter.formed",
                        "message.tech_revised.argon_oxygen_decarburization_converter.incomplete")
                .cylinderSolid(0, 2, 5, ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_FRAME)
                .cylinder(1, 6, 2, 2, 5, ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_FRAME)
                .cylinderSolid(7, 2, 5, ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_FRAME)
                .cell(-2, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS)
                .cell(2, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS)
                .cell(1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build());

        register(MachineLayout.builder("blast_furnace", ModBlocks.BLAST_FURNACE_CORE)
                .messages("message.tech_revised.blast_furnace.formed", "message.tech_revised.blast_furnace.incomplete")
                .fill(-1, 1, 0, 0, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FRAME)
                .ring(-1, 1, 1, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FRAME)
                .ring(-1, 1, 2, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FRAME)
                .ring(-1, 1, 3, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FRAME)
                .ring(-1, 1, 4, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FRAME)
                .ring(-1, 1, 5, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FRAME)
                .ring(-1, 1, 6, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FRAME)
                .fill(-1, 1, 7, 7, 0, 2, ModBlocks.ELECTRIC_ARC_FURNACE_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .cell(1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS)
                .cell(0, 7, 1, ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS)
                .build());

        register(MachineLayout.builder("workstation", ModBlocks.TESTED_MONITOR)
                .messages("message.tech_revised.workstation.formed", "message.tech_revised.workstation.incomplete")
                // --- desk surface (y = 0): keyboard toward the chair, speakers behind ---
                .cell(-1, 0, 1, ModBlocks.TESTED_SPEAKER)
                .cell(1, 0, 1, ModBlocks.TESTED_SPEAKER)
                .cell(-1, 0, 0, ModBlocks.TESTED_DESK)
                .cell(1, 0, 0, ModBlocks.TESTED_DESK)
                .cell(0, 0, 1, ModBlocks.TESTED_DESK)
                .cell(0, 0, -1, ModBlocks.TESTED_KEYBOARD)
                .cell(-1, 0, -1, ModBlocks.TESTED_MOUSE)
                .cell(1, 0, -1, ModBlocks.TESTED_HDMI_CABLE)
                // --- under-desk (y = -1): legs, tower under keyboard, power behind HDMI ---
                .fill(-1, 1, -1, -1, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, -1, 0, ModBlocks.TRANSPORT_FRAME)
                .cell(1, -1, 0, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, -1, -1, ModBlocks.TRANSPORT_FRAME)
                .cell(1, -1, -1, ModBlocks.TRANSPORT_FRAME)
                .cell(0, -1, -1, ModBlocks.TESTED_DESKTOP_TOWER)
                .cell(1, -1, 2, ModBlocks.TESTED_POWER_CABLE)
                // --- chair in front of the desk ---
                .cell(0, 0, -2, ModBlocks.TESTED_CHAIR_SEAT)
                .cell(0, 1, -2, ModBlocks.TESTED_CHAIR_BACKREST)
                .build());
    }

    private static void registerCoreFactory() {
        registerClassed("crusher", ModBlocks.CRUSHER);
        registerClassed("industrial_batching_mixer", ModBlocks.INDUSTRIAL_BATCHING_MIXER);
        registerClassed("refractory_melting_furnace", ModBlocks.REFRACTORY_MELTING_FURNACE);
        registerClassed("high_speed_gathering_winder", ModBlocks.HIGH_SPEED_GATHERING_WINDER);
        registerClassed("industrial_drying_oven", ModBlocks.INDUSTRIAL_DRYING_OVEN);
        registerClassed("roving_creel_converter", ModBlocks.ROVING_CREEL_CONVERTER);
        registerClassed("industrial_textile_weaving_loom", ModBlocks.INDUSTRIAL_TEXTILE_WEAVING_LOOM);
        registerClassed("strand_chopping_machinery", ModBlocks.STRAND_CHOPPING_MACHINERY);
    }

    private static void registerPcbLine() {
        registerClassed("design_engineering_station", ModBlocks.DESIGN_ENGINEERING_STATION);
        registerClassed("industrial_shear", ModBlocks.INDUSTRIAL_SHEAR);
        registerClassed("decontamination_oven", ModBlocks.DECONTAMINATION_OVEN);
        registerClassed("hydraulic_cladding_press", ModBlocks.HYDRAULIC_CLADDING_PRESS);
        registerClassed("photo_resist_applicator", ModBlocks.PHOTO_RESIST_APPLICATOR);
        registerClassed("uv_ldi_imager", ModBlocks.UV_LDI_IMAGER);
        registerClassed("chemical_developing_wash", ModBlocks.CHEMICAL_DEVELOPING_WASH);
        registerClassed("acidic_etching_sprayer", ModBlocks.ACIDIC_ETCHING_SPRAYER);
        registerClassed("alkaline_stripping_station", ModBlocks.ALKALINE_STRIPPING_STATION);
        registerClassed("vacuum_lamination_press", ModBlocks.VACUUM_LAMINATION_PRESS);
        registerClassed("xray_alignment_drill", ModBlocks.XRAY_ALIGNMENT_DRILL);
        registerClassed("electroless_plating_bath", ModBlocks.ELECTROLESS_PLATING_BATH);
        registerClassed("copper_electroplating_tank", ModBlocks.COPPER_ELECTROPLATING_TANK);
        registerClassed("tin_plating_tank", ModBlocks.TIN_PLATING_TANK);
        registerClassed("soldermask_flooder", ModBlocks.SOLDERMASK_FLOODER);
        registerClassed("uv_pad_exposure_station", ModBlocks.UV_PAD_EXPOSURE_STATION);
        registerClassed("inkjet_silkscreen_printer", ModBlocks.INKJET_SILKSCREEN_PRINTER);
        registerClassed("surface_finish_station", ModBlocks.SURFACE_FINISH_STATION);
        registerClassed("flying_probe_tester", ModBlocks.FLYING_PROBE_TESTER);
        registerClassed("cnc_router", ModBlocks.CNC_ROUTER);
        registerClassed("solder_paste_printer", ModBlocks.SOLDER_PASTE_PRINTER);
        registerClassed("pick_and_place_robot", ModBlocks.PICK_AND_PLACE_ROBOT);
        registerClassed("multi_zone_reflow_oven", ModBlocks.MULTI_ZONE_REFLOW_OVEN);
        registerClassed("aoi_verification_station", ModBlocks.AOI_VERIFICATION_STATION);
    }

    private static void registerPcProduction() {
        for (String id : PcCatalog.MACHINE_IDS) {
            if (!CLASS_BY_ID.containsKey(id)) {
                throw new IllegalStateException("No layout class for " + id);
            }
        }
        for (RegistryObject<Block> machine : ModBlocks.PC_PRODUCTION_MACHINES) {
            registerClassed(machine.getId().getPath(), machine);
        }
    }

    private enum LayoutClass {
        REACTOR, VESSEL, BATH, PRESS, MOLDER, EXTRUDER, OVEN, CONVEYOR, SMT, CNC,
        CRUSHER, MIXER, FURNACE, WINDER, TESTER, BENCH, SKID
    }

    private static final Map<String, LayoutClass> CLASS_BY_ID = classMap();

    private static Map<String, LayoutClass> classMap() {
        Map<String, LayoutClass> map = new HashMap<>();
        putAll(map, LayoutClass.CRUSHER, "crusher");
        putAll(map, LayoutClass.MIXER, "industrial_batching_mixer", "rubber_compounder");
        putAll(map, LayoutClass.FURNACE, "refractory_melting_furnace");
        putAll(map, LayoutClass.WINDER,
                "high_speed_gathering_winder", "transformer_winder", "spindle_motor_winder", "voice_coil_winder");
        putAll(map, LayoutClass.OVEN,
                "industrial_drying_oven", "decontamination_oven", "multi_zone_reflow_oven",
                "cpu_burn_in_chamber", "hdd_burn_in", "powder_coater", "monitor_burn_in", "speaker_magnet_sinter");
        putAll(map, LayoutClass.SKID, "roving_creel_converter", "platter_polisher", "thermal_paste_dispenser");
        putAll(map, LayoutClass.CONVEYOR,
                "industrial_textile_weaving_loom", "photo_resist_applicator", "soldermask_flooder",
                "uv_pad_exposure_station", "inkjet_silkscreen_printer", "surface_finish_station",
                "psu_final_assembly", "tower_final_assembly", "color_filter_printer", "monitor_final_assembly",
                "membrane_sheet_printer", "keyboard_assembly", "mouse_assembly", "speaker_final_assembly");
        putAll(map, LayoutClass.CNC,
                "strand_chopping_machinery", "industrial_shear", "xray_alignment_drill", "cnc_router",
                "wafer_dicing_saw");
        putAll(map, LayoutClass.BENCH,
                "design_engineering_station", "wire_bonding_machine", "expansion_slot_inserter",
                "gpu_assembly_jig", "head_stack_assembler", "case_assembly_jig", "gpu_install_bench",
                "storage_install_bay", "psu_install_bench", "backlight_assembler", "driver_assembler");
        putAll(map, LayoutClass.PRESS,
                "hydraulic_cladding_press", "vacuum_lamination_press", "wafer_blank_press", "die_attach_bonder",
                "cpu_socket_press", "ihs_bonder", "gpu_cooler_press", "psu_case_stamper", "hdd_hepa_sealer",
                "nand_stacker", "ssd_case_welder", "case_stamping_press", "dimm_install_press",
                "polarizer_laminator", "lcd_cell_bonder", "keyboard_plate_stamper", "hdmi_termination_press");
        putAll(map, LayoutClass.SMT,
                "uv_ldi_imager", "solder_paste_printer", "pick_and_place_robot", "chipset_placer",
                "gpu_substrate_smt", "vram_populator", "psu_smt", "ssd_smt", "monitor_controller_smt",
                "keyboard_pcb_smt", "optical_sensor_smt");
        putAll(map, LayoutClass.BATH,
                "chemical_developing_wash", "acidic_etching_sprayer", "alkaline_stripping_station",
                "electroless_plating_bath", "copper_electroplating_tank", "tin_plating_tank",
                "wire_tinning_bath", "lcd_glass_washer");
        putAll(map, LayoutClass.TESTER,
                "flying_probe_tester", "aoi_verification_station", "ic_tester", "ic_binning_station",
                "motherboard_ict", "gpu_tester", "psu_load_tester", "hdd_servo_writer",
                "ssd_firmware_programmer", "ssd_validator", "desktop_power_on_test", "keyboard_tester",
                "mouse_tester", "hdmi_continuity_tester", "hi_pot_tester", "speaker_anechoic_test");
        putAll(map, LayoutClass.REACTOR,
                "crude_fractionator", "naphtha_steam_cracker", "chlor_alkali_cell", "vcm_reactor",
                "pvc_polymerizer", "abs_polymerizer");
        putAll(map, LayoutClass.VESSEL, "plasticizer_unit", "magnetic_sputter", "liquid_crystal_filler");
        putAll(map, LayoutClass.MOLDER,
                "ic_transfer_mold", "gpu_shroud_molder", "front_panel_molder", "monitor_bezel_molder",
                "keycap_injection_molder", "mouse_shell_molder", "scroll_wheel_molder", "hdmi_connector_molder",
                "iec_connector_molder", "power_cable_overmolder", "cabinet_molder", "cone_former");
        putAll(map, LayoutClass.EXTRUDER,
                "copper_wire_drawer", "steel_sheet_roller", "pair_twinner", "hdmi_shield_braider",
                "hdmi_jacket_extruder", "power_conductor_strander", "power_jacket_extruder");
        return Map.copyOf(map);
    }

    private static void putAll(Map<String, LayoutClass> map, LayoutClass kind, String... ids) {
        for (String id : ids) {
            if (map.put(id, kind) != null) {
                throw new IllegalStateException("Duplicate layout class for " + id);
            }
        }
    }

    private static void registerClassed(String id, Supplier<Block> controller) {
        LayoutClass kind = CLASS_BY_ID.get(id);
        if (kind == null) {
            throw new IllegalStateException("No layout class for " + id);
        }
        register(layout(kind, id, controller));
    }

    private static MachineLayout layout(LayoutClass kind, String id, Supplier<Block> controller) {
        return switch (kind) {
            case REACTOR -> reactor(id, controller);
            case VESSEL -> vessel(id, controller);
            case BATH -> bath(id, controller);
            case PRESS -> press(id, controller);
            case MOLDER -> molder(id, controller);
            case EXTRUDER -> extruder(id, controller);
            case OVEN -> oven(id, controller);
            case CONVEYOR -> conveyor(id, controller);
            case SMT -> smt(id, controller);
            case CNC -> cnc(id, controller);
            case CRUSHER -> crusher(id, controller);
            case MIXER -> mixer(id, controller);
            case FURNACE -> furnace(id, controller);
            case WINDER -> winder(id, controller);
            case TESTER -> tester(id, controller);
            case BENCH -> bench(id, controller);
            case SKID -> skid(id, controller);
        };
    }

    private static MachineLayout reactor(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .cylinderSolid(0, 1, 2, ModBlocks.TRANSPORT_FRAME)
                .cylinder(1, 2, 1, 1, 2, ModBlocks.TRANSPORT_FRAME)
                .cylinderSolid(3, 1, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout vessel(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .cylinderSolid(0, 1, 2, ModBlocks.TRANSPORT_FRAME)
                .cylinder(1, 1, 1, 1, 2, ModBlocks.TRANSPORT_FRAME)
                .cylinderSolid(2, 1, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout bath(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-2, 2, 0, 0, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .fill(-2, -2, 1, 1, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .fill(2, 2, 1, 1, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .fill(-1, 1, 1, 1, 2, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(-2, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout press(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-1, 1, 0, 0, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 2, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(1, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(1, 2, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(0, 2, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout molder(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-1, 1, 0, 1, 0, 0, ModBlocks.TRANSPORT_FRAME)
                .fill(0, 0, 0, 0, 1, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(0, 1, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout extruder(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-1, 1, 0, 0, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .fill(0, 0, 1, 1, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(0, 2, 0, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout oven(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-2, 2, 0, 0, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .fill(-2, -2, 1, 1, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .fill(2, 2, 1, 1, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .fill(-2, 2, 2, 2, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(-2, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout conveyor(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-2, 2, 0, 0, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .fill(-2, 2, 1, 1, 2, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(-2, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout smt(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-2, 2, 0, 0, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(-2, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(2, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .fill(-2, 2, 2, 2, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(-2, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout cnc(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-1, 1, 0, 0, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(1, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .fill(-1, 1, 2, 2, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout crusher(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-1, 1, 0, 1, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .ring(-1, 1, 2, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout mixer(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .cylinderSolid(0, 1, 2, ModBlocks.TRANSPORT_FRAME)
                .cylinder(1, 2, 1, 1, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(0, 3, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout furnace(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-2, 2, 0, 0, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .ring(-2, 2, 1, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(0, 2, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(0, 3, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(-2, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout winder(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-1, 1, 0, 0, 0, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 2, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(1, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(1, 2, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(0, 1, 0, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout tester(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-1, 1, 0, 0, 0, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(0, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(0, 2, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 1, 0, ModBlocks.TRANSPORT_FRAME)
                .cell(1, 1, 0, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout bench(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-2, 2, 0, 0, 0, 1, ModBlocks.TRANSPORT_FRAME)
                .fill(-2, 2, 1, 1, 1, 1, ModBlocks.TRANSPORT_FRAME)
                .cell(-2, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static MachineLayout skid(String id, Supplier<Block> controller) {
        return MachineLayout.builder(id, controller)
                .fill(-1, 1, 0, 0, 0, 2, ModBlocks.TRANSPORT_FRAME)
                .fill(-1, 1, 1, 1, 2, 2, ModBlocks.TRANSPORT_FRAME)
                .cell(-1, 0, 0, ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH)
                .build();
    }

    private static void register(MachineLayout layout) {
        BY_ID.put(layout.getId(), layout);
        BY_BLOCK.put(layout.getController(), layout);
    }
}
