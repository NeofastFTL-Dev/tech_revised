# IRL class multiblock layouts

## Goal

Replace the 8 abstract production templates (skid/tank/press/oven/line/hopper/mixer/reverberatory) with **17 compact IRL silhouettes**. Every core-factory, PCB, and PC production machine uses one class. Heavy industry unique layouts and the workstation desk are unchanged.

## Decisions

- Chassis: `TRANSPORT_FRAME` + one `ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH`
- Size: compact (3–5 wide, 2–4 tall, 2–4 deep)
- Assignment: explicit `id → class` map; **no substring matching**
- Missing map entry: throw in `ensureInit` (do not default to skid)
- No new frame blocks, no custom JSON models, no extra IO buses
- Hatch overwrites the front-left ground frame: `(minX, 0, 0)`
- Existing worlds unform until rebuilt (configurator / hologram still work)

## Source of truth

Edit `src/main/java/com/neofast/tech_revised/multiblock/MachineLayouts.java`.

Keep `MachineLayout` primitives (`cell` / `fill` / `shell` / `ring` / `cylinder` / `cylinderSolid`). JEI, hologram, auto-build, and formed-checks already read `MachineLayouts`.

Mirror counts in `tools/bom_exploder.py`. Count **placed** blocks: exclude controller `(0,0,0)`; hatch replaces a frame (do not count that cell as both).

## Class voxels

Local axes: `+X` right of facing, `+Y` up, `+Z` behind controller. Controller at `(0,0,0)`.

Hatch helper: after the frame shape, `.cell(minX, 0, 0, ENERGY_HATCH)`.

| Class | Bounds | Shape |
|---|---|---|
| **reactor** | 3×4×3 | `cylinderSolid(0,1,2)` + `cylinder(1,2,1,1,2)` + `cylinderSolid(3,1,2)`. Hatch `(-1,0,0)`. Tall capped vessel. |
| **vessel** | 3×3×3 | Same cylinder, y=0..2 only (cap at y=2). Hatch `(-1,0,0)`. |
| **bath** | 5×2×3 | Floor `fill(-2,2,0,0,0,2)`; walls y=1 on left `x=-2`, right `x=2`, back `z=2`. No roof, open front. Hatch `(-2,0,0)`. |
| **press** | 3×3×3 | Bed `fill(-1,1,0,0,0,2)`; columns `(-1,1..2,1)` and `(1,1..2,1)`; ram `(0,2,1)`. Hatch `(-1,0,0)`. |
| **molder** | 3×2×3 | Clamp `fill(-1,1,0,1,0,0)`; barrel `fill(0,0,0,0,1,2)`; hopper `(0,1,2)`. Hatch `(-1,0,0)`. |
| **extruder** | 3×3×3 | Base `fill(-1,1,0,0,0,2)`; barrel `fill(0,0,1,1,0,2)`; feed hopper `(0,2,0)`. Hatch `(-1,0,0)`. |
| **oven** | 5×3×3 | Floor `fill(-2,2,0,0,0,2)`; side walls y=1 at `x=±2`; roof `fill(-2,2,2,2,0,2)`. Open ends. Hatch `(-2,0,0)`. |
| **conveyor** | 5×2×3 | Belt `fill(-2,2,0,0,0,2)`; back wall `fill(-2,2,1,1,2,2)`. Hatch `(-2,0,0)`. |
| **smt** | 5×3×3 | Belt as conveyor; legs `(-2,1,1)` `(2,1,1)`; gantry `fill(-2,2,2,2,1,1)`. Hatch `(-2,0,0)`. |
| **cnc** | 3×3×3 | Bed `fill(-1,1,0,0,0,2)`; legs `(-1,1,1)` `(1,1,1)`; gantry `fill(-1,1,2,2,1,1)`. Hatch `(-1,0,0)`. |
| **crusher** | 3×3×3 | Body `fill(-1,1,0,1,0,2)`; hopper `ring(-1,1,2,0,2)`. Hatch `(-1,0,0)`. |
| **mixer** | 3×4×3 | `cylinderSolid(0,1,2)` + `cylinder(1,2,1,1,2)` + motor `(0,3,1)` (no top disc). Hatch `(-1,0,0)`. |
| **furnace** | 5×4×3 | Floor `fill(-2,2,0,0,0,2)`; y=1 ring walls on the 5×3 footprint; chimney `(0,2,2)` `(0,3,2)`. Hatch `(-2,0,0)`. |
| **winder** | 3×3×2 | Base `fill(-1,1,0,0,0,1)`; left spool `(-1,1,1)` `(-1,2,1)`; right spool `(1,1,1)` `(1,2,1)`; headstock `(0,1,0)`. Hatch `(-1,0,0)`. |
| **tester** | 3×3×2 | Bench `fill(-1,1,0,0,0,1)`; rack `(0,1,1)` `(0,2,1)`; probes `(-1,1,0)` `(1,1,0)`. Hatch `(-1,0,0)`. |
| **bench** | 5×2×2 | Table `fill(-2,2,0,0,0,1)`; backboard `fill(-2,2,1,1,1,1)`. Hatch `(-2,0,0)`. |
| **skid** | 3×2×3 | Pallet `fill(-1,1,0,0,0,2)`; rear module `fill(-1,1,1,1,2,2)`. Hatch `(-1,0,0)`. |

`build()` already restores the controller at `(0,0,0)` if a fill covered it.

## Machine → class map

Use one `Map<String, Class>` (or enum) covering core factory, PCB, and `PcCatalog.MACHINE_IDS`. Register core/PCB/PC from that map.

### Core factory

- crusher → crusher
- industrial_batching_mixer → mixer
- refractory_melting_furnace → furnace
- high_speed_gathering_winder → winder
- industrial_drying_oven → oven
- roving_creel_converter → skid
- industrial_textile_weaving_loom → conveyor
- strand_chopping_machinery → cnc

### PCB

- design_engineering_station → bench
- industrial_shear → cnc
- decontamination_oven → oven
- hydraulic_cladding_press → press
- photo_resist_applicator → conveyor
- uv_ldi_imager → smt
- chemical_developing_wash → bath
- acidic_etching_sprayer → bath
- alkaline_stripping_station → bath
- vacuum_lamination_press → press
- xray_alignment_drill → cnc
- electroless_plating_bath → bath
- copper_electroplating_tank → bath
- tin_plating_tank → bath
- soldermask_flooder → conveyor
- uv_pad_exposure_station → conveyor
- inkjet_silkscreen_printer → conveyor
- surface_finish_station → conveyor
- flying_probe_tester → tester
- cnc_router → cnc
- solder_paste_printer → smt
- pick_and_place_robot → smt
- multi_zone_reflow_oven → oven
- aoi_verification_station → tester

### PC production

**reactor:** crude_fractionator, naphtha_steam_cracker, chlor_alkali_cell, vcm_reactor, pvc_polymerizer, abs_polymerizer

**vessel:** plasticizer_unit, magnetic_sputter, liquid_crystal_filler

**bath:** wire_tinning_bath, lcd_glass_washer

**press:** wafer_blank_press, die_attach_bonder, cpu_socket_press, ihs_bonder, gpu_cooler_press, psu_case_stamper, hdd_hepa_sealer, nand_stacker, ssd_case_welder, case_stamping_press, dimm_install_press, polarizer_laminator, lcd_cell_bonder, keyboard_plate_stamper, hdmi_termination_press

**molder:** ic_transfer_mold, gpu_shroud_molder, front_panel_molder, monitor_bezel_molder, keycap_injection_molder, mouse_shell_molder, scroll_wheel_molder, hdmi_connector_molder, iec_connector_molder, power_cable_overmolder, cabinet_molder, cone_former

**extruder:** copper_wire_drawer, steel_sheet_roller, pair_twinner, hdmi_shield_braider, hdmi_jacket_extruder, power_conductor_strander, power_jacket_extruder

**oven:** cpu_burn_in_chamber, hdd_burn_in, powder_coater, monitor_burn_in, speaker_magnet_sinter

**conveyor:** psu_final_assembly, tower_final_assembly, color_filter_printer, monitor_final_assembly, membrane_sheet_printer, keyboard_assembly, mouse_assembly, speaker_final_assembly

**smt:** chipset_placer, gpu_substrate_smt, vram_populator, psu_smt, ssd_smt, monitor_controller_smt, keyboard_pcb_smt, optical_sensor_smt

**cnc:** wafer_dicing_saw

**mixer:** rubber_compounder

**winder:** transformer_winder, spindle_motor_winder, voice_coil_winder

**tester:** ic_tester, ic_binning_station, motherboard_ict, gpu_tester, psu_load_tester, hdd_servo_writer, ssd_firmware_programmer, ssd_validator, desktop_power_on_test, keyboard_tester, mouse_tester, hdmi_continuity_tester, hi_pot_tester, speaker_anechoic_test

**bench:** wire_bonding_machine, expansion_slot_inserter, gpu_assembly_jig, head_stack_assembler, case_assembly_jig, gpu_install_bench, storage_install_bay, psu_install_bench, backlight_assembler, driver_assembler

**skid:** platter_polisher, thermal_paste_dispenser

## Tasks

1. Replace `Template` + `templateFor` in `MachineLayouts.java` with the 17-class enum, voxel helpers, and explicit map. Route `registerCoreFactory`, `registerPcbLine`, and `registerPcProduction` through it. Assert every `PcCatalog.MACHINE_IDS` entry and every core/PCB controller id is mapped.
2. Port the 17 count functions and the same map into `tools/bom_exploder.py` (`CORE_FACTORY` + `template_for`).
3. Update the crusher line in `PcSurvivalManualPages.java` (hopper template → crusher class). Optional one-liner that production machines use class silhouettes, still Transport Frames + energy hatch.
4. Do **not** change heavy industry, `WorkstationStructure`, controller leftover AABBs, or `MultiblockStructureBlueprint` static builders.

## Validation

- `MachineLayouts.all()` size stays 136 (7 unique + 8 core + 24 PCB + 97 PC).
- Spot-check JEI / hologram / configurator on one machine per class (crusher, mixer, furnace, winder, oven, bath, press, molder, extruder, conveyor, smt, cnc, tester, bench, skid, reactor, vessel).
- Formed-check still requires exact block ids; processing remains gated on `MachineLayouts.isFormed`.
- BOM frame/hatch counts match placed cells for each class.

## Risk

Already-built template machines will show incomplete until rebuilt. No migration.
