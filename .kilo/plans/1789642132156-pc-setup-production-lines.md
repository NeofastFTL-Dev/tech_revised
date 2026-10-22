# Full PC setup production lines

## Goal

Add staged industrial lines (unique machine per step, PCB style) that produce a placeable workstation kit: desktop tower, monitor, keyboard, mouse, HDMI cable, power cable, two speakers. A valid kit multiblock opens the existing Windows 7 VM GUI on the **monitor** (controller). `windows_7_vm_block` is replaced as the player-facing computer; keep its BE/GUI/network code.

## Locked decisions

- Desktop is a full internal BOM: motherboard, CPU, GPU, PSU, HDD **and** SSD, case, plus existing `ram_stick`.
- Peripherals each have their own unique-machine lines.
- No silicon fab from sand. Shared **IC packaging** line; `silicon_wafer` is a simplified item.
- Petrochem is **PC-feeding only**: naphtha, ethylene, chlorine, vinyl chloride, PVC, ABS, rubber, plasticizer. No fuels/fertilizer/extra polymers.
- Reuse existing FR4/PCB flow (`final_pcba` / `routed_pcb`) and steel/glass/fiberglass. Do not clone the 11 PCB stages.
- Recipes stay `tech_revised:pcb` with `"machine": "tech_revised:<block_id>"`. New machines are `MultiInputIndustrialMachineBlock` on the existing `PCB_MACHINE` BE (extend the `BlockEntityType` valid-blocks list). **Hard cap: 3 item inputs** (`PcbMachineBlockEntity.INPUT_SLOTS`). Chain any BOM that needs more.
- Fluids as **buckets**, same as `cupric_chloride_solution_bucket` (no new tank UI).
- Placeholder cube models (`_on` / idle) like other PCB machines; kit blocks need distinct placeable models.

## Out of scope

- Lithography/wafer fab, unbounded petrochem, mechanical keyboards, extra GPU SKUs, OS install items, changing VM op-permission (`hasPermissions(2)`).

## Constraints / reuse

- Register items in `ModItems`, blocks in `ModBlocks`, tab in `ModCreativeModeTabs`, lang `en_us.json`.
- Each machine: blockstate + models + loot table + lang + JEI catalyst in `TechRevisedJeiPlugin`.
- Follow `data/tech_revised/recipes/pcb/*.json` (`machine`, `ingredients`, `result`, `process_ticks`, `energy_per_tick`).
- Multiblock hologram: extend `MultiblockPreviewHelper` + blueprint id `workstation` (same pattern as EAF).

---

## Phase 0 — Infra

1. Add all new machines to `ModBlockEntities.PCB_MACHINE` valid blocks (same pattern as `PICK_AND_PLACE_ROBOT`).
2. Helper to register a PCB-style machine (block + default cube assets) to avoid copy-paste errors.
3. `dram_package` / `spd_eeprom` stay; add IC-line recipes so they are no longer dead-end items.

---

## Phase 1 — PC petrochem

Feedstock: `heavy_crude_oil_bucket`. Salt source: `soda_ash` + water bucket.

| Machine | Inputs | Output |
|---|---|---|
| `crude_fractionator` | heavy crude bucket | `naphtha_bucket` |
| `naphtha_steam_cracker` | naphtha bucket | `ethylene_bucket` |
| `chlor_alkali_cell` | water bucket + soda ash | `chlorine_bucket` |
| `vcm_reactor` | ethylene + chlorine buckets | `vinyl_chloride_bucket` |
| `plasticizer_unit` | naphtha bucket | `plasticizer` |
| `pvc_polymerizer` | VCM bucket + plasticizer | `pvc_pellets` |
| `abs_polymerizer` | naphtha bucket + rubber | `abs_pellets` |
| `rubber_compounder` | naphtha bucket | `rubber_sheet` |

Empty buckets as craft remainders like existing fluid buckets. Register still/flow fluids + liquid blocks + buckets for: naphtha, ethylene, chlorine, vinyl chloride.

---

## Phase 2 — Wire + IC packaging

**Wire (feeds HDMI, power, PSU, coils)**

| Machine | Inputs | Output |
|---|---|---|
| `copper_wire_drawer` | copper ingot | `copper_wire` |
| `wire_tinning_bath` | copper wire + tin plating dust | `tinned_copper_wire` |

**IC packaging (no fab)**

| Machine | Inputs | Output |
|---|---|---|
| `wafer_blank_press` | `silica_sand` | `silicon_wafer` |
| `wafer_dicing_saw` | silicon wafer | `silicon_die` |
| `die_attach_bonder` | die + copper foil | `attached_die` |
| `wire_bonding_machine` | attached die + tinned copper wire | `bonded_die` |
| `ic_transfer_mold` | bonded die + ABS | `molded_ic` |
| `ic_tester` | molded ic | `tested_ic` |
| `ic_binning_station` | tested_ic + `cad_layout` | specialized package |

Binning recipes (same machine, different cad_layout stacks or extra item): `cpu_package`, `gpu_package`, `chipset_package`, `nand_package`, `hdd_controller_package`, `display_controller_package`, `mcu_package`, `dram_package` (replace or add recipe for existing item).

---

## Phase 3 — Desktop internals

Motherboard **starts from** `final_pcba` (existing PCB line). Do not re-etch.

| Machine | Inputs | Output |
|---|---|---|
| `cpu_socket_press` | final_pcba + steel nugget | `socketed_motherboard` |
| `expansion_slot_inserter` | socketed_motherboard + copper foil | `slotted_motherboard` |
| `chipset_placer` | slotted_motherboard + chipset_package | `motherboard_pcba` |
| `motherboard_ict` | motherboard_pcba | `tested_motherboard` |

**CPU**

| Machine | Inputs | Output |
|---|---|---|
| `ihs_bonder` | cpu_package + stainless nugget | `lidded_cpu` |
| `cpu_burn_in_chamber` | lidded_cpu | `tested_cpu` |

**GPU**

| Machine | Inputs | Output |
|---|---|---|
| `gpu_substrate_smt` | final_pcba + gpu_package | `gpu_core_board` |
| `vram_populator` | gpu_core_board + dram_package | `gpu_populated_board` |
| `gpu_cooler_press` | steel + rubber_sheet | `gpu_cooler` |
| `gpu_shroud_molder` | abs_pellets | `gpu_shroud` |
| `gpu_assembly_jig` | populated board + cooler + shroud | `assembled_gpu` |
| `gpu_tester` | assembled_gpu | `tested_gpu` |

**PSU**

| Machine | Inputs | Output |
|---|---|---|
| `transformer_winder` | tinned_copper_wire + steel | `psu_transformer` |
| `psu_smt` | final_pcba + psu_transformer | `psu_board` |
| `psu_case_stamper` | steel ingot | `psu_shell` |
| `psu_final_assembly` | psu_board + psu_shell + pvc_pellets | `assembled_psu` |
| `psu_load_tester` | assembled_psu | `tested_psu` |

**HDD**

| Machine | Inputs | Output |
|---|---|---|
| `platter_polisher` | iron ingot | `hdd_platter` |
| `magnetic_sputter` | hdd_platter + redstone | `coated_platter` |
| `spindle_motor_winder` | tinned_copper_wire + steel nugget | `spindle_motor` |
| `head_stack_assembler` | steel nugget + silicon_die | `head_stack` |
| `hdd_hepa_sealer` | coated_platter + spindle_motor + head_stack | `sealed_hdd` |
| `hdd_servo_writer` | sealed_hdd + hdd_controller_package | `programmed_hdd` |
| `hdd_burn_in` | programmed_hdd | `tested_hdd` |

**SSD**

| Machine | Inputs | Output |
|---|---|---|
| `nand_stacker` | nand_package | `nand_stack` |
| `ssd_smt` | final_pcba + nand_stack + mcu_package | `ssd_board` |
| `ssd_firmware_programmer` | ssd_board | `programmed_ssd` |
| `ssd_case_welder` | programmed_ssd + abs_pellets | `assembled_ssd` |
| `ssd_validator` | assembled_ssd | `tested_ssd` |

**Case**

| Machine | Inputs | Output |
|---|---|---|
| `steel_sheet_roller` | steel ingot | `steel_sheet` |
| `case_stamping_press` | steel_sheet | `case_chassis` |
| `powder_coater` | case_chassis | `coated_chassis` |
| `front_panel_molder` | abs_pellets | `case_front_panel` |
| `case_assembly_jig` | coated_chassis + front_panel + rubber_sheet | `pc_case` |

---

## Phase 4 — Desktop tower (chained, 3-input cap)

| Machine | Inputs | Output |
|---|---|---|
| `thermal_paste_dispenser` | tested_motherboard + tested_cpu + chemical_lubricant | `board_with_cpu` |
| `dimm_install_press` | board_with_cpu + ram_stick | `board_with_memory` |
| `gpu_install_bench` | board_with_memory + tested_gpu | `board_with_gpu` |
| `storage_install_bay` | board_with_gpu + tested_hdd **or** tested_ssd (two recipes) | `board_with_storage` |
| `psu_install_bench` | board_with_storage + tested_psu | `wired_internals` |
| `tower_final_assembly` | wired_internals + pc_case | `assembled_desktop` |
| `desktop_power_on_test` | assembled_desktop | `tested_desktop_tower` (placeable **block item**) |

---

## Phase 5 — Peripherals

**Monitor (controller)**

| Machine | Inputs | Output |
|---|---|---|
| `lcd_glass_washer` | glass | `cleaned_lcd_glass` |
| `color_filter_printer` | cleaned_lcd_glass | `color_filter_glass` |
| `liquid_crystal_filler` | color_filter_glass + chemical_lubricant | `filled_lcd_cell` |
| `polarizer_laminator` | filled_lcd_cell | `polarized_panel` |
| `backlight_assembler` | abs_pellets + redstone | `led_backlight` |
| `lcd_cell_bonder` | polarized_panel + led_backlight | `lcd_module` |
| `monitor_controller_smt` | final_pcba + display_controller_package | `monitor_logic_board` |
| `monitor_bezel_molder` | abs_pellets | `monitor_bezel` |
| `monitor_final_assembly` | lcd_module + logic board + bezel | `assembled_monitor` |
| `monitor_burn_in` | assembled_monitor | `tested_monitor` (placeable **block**) |

**Keyboard (membrane)**

| Machine | Inputs | Output |
|---|---|---|
| `keycap_injection_molder` | abs_pellets | `keycaps` |
| `membrane_sheet_printer` | rubber_sheet | `key_membrane` |
| `keyboard_pcb_smt` | final_pcba + mcu_package | `keyboard_pcb` |
| `keyboard_plate_stamper` | steel_sheet | `keyboard_plate` |
| `keyboard_assembly` | pcb + membrane + keycaps | `assembled_keyboard` |
| `keyboard_tester` | assembled_keyboard + keyboard_plate | `tested_keyboard` (placeable **block**) |

**Mouse**

| Machine | Inputs | Output |
|---|---|---|
| `mouse_shell_molder` | abs_pellets | `mouse_shell` |
| `optical_sensor_smt` | final_pcba + mcu_package | `mouse_sensor_board` |
| `scroll_wheel_molder` | rubber_sheet | `scroll_wheel` |
| `mouse_assembly` | shell + sensor board + scroll_wheel | `assembled_mouse` |
| `mouse_tester` | assembled_mouse | `tested_mouse` (placeable **block**) |

**HDMI**

| Machine | Inputs | Output |
|---|---|---|
| `pair_twinner` | tinned_copper_wire | `twisted_pair` |
| `hdmi_shield_braider` | twisted_pair + copper_wire | `shielded_hdmi_core` |
| `hdmi_jacket_extruder` | shielded_hdmi_core + pvc_pellets | `jacketed_hdmi` |
| `hdmi_connector_molder` | abs_pellets + gold nugget (vanilla) | `hdmi_connector` |
| `hdmi_termination_press` | jacketed_hdmi + hdmi_connector | `assembled_hdmi` |
| `hdmi_continuity_tester` | assembled_hdmi | `tested_hdmi_cable` (placeable **block**) |

**Power cable**

| Machine | Inputs | Output |
|---|---|---|
| `power_conductor_strander` | tinned_copper_wire | `power_conductor` |
| `power_jacket_extruder` | power_conductor + pvc_pellets | `jacketed_power_cord` |
| `iec_connector_molder` | abs_pellets + steel nugget | `iec_connector` |
| `power_cable_overmolder` | jacketed_power_cord + iec_connector | `assembled_power_cable` |
| `hi_pot_tester` | assembled_power_cable | `tested_power_cable` (placeable **block**) |

**Speakers** (craft/place two)

| Machine | Inputs | Output |
|---|---|---|
| `speaker_magnet_sinter` | iron ingot + redstone | `speaker_magnet` |
| `voice_coil_winder` | tinned_copper_wire | `voice_coil` |
| `cone_former` | paper (vanilla) + chemical_lubricant | `speaker_cone` |
| `driver_assembler` | magnet + voice_coil + cone | `speaker_driver` |
| `cabinet_molder` | abs_pellets | `speaker_cabinet` |
| `speaker_final_assembly` | driver + cabinet + rubber_sheet | `assembled_speaker` |
| `speaker_anechoic_test` | assembled_speaker | `tested_speaker` (placeable **block**) |

---

## Phase 6 — Workstation multiblock + VM

**Layout** relative to monitor, `FACING` = screen direction (front = player). Offsets are `(right, up, back)`:

| Offset | Block |
|---|---|
| `(0,0,0)` | `tested_monitor` (controller) |
| `(-1,0,0)` | `tested_speaker` |
| `(1,0,0)` | `tested_speaker` |
| `(0,0,1)` | `tested_hdmi_cable` |
| `(0,0,2)` | `tested_desktop_tower` |
| `(1,0,2)` | `tested_power_cable` |
| `(0,0,-1)` | `tested_keyboard` |
| `(1,0,-1)` | `tested_mouse` |

1. Move VM GUI/open logic from `Windows7VmBlock` onto the monitor block. Reuse `Windows7VmBlockEntity`, packets, `Windows7VmMenu`/`Screen`.
2. `use` on monitor: if structure invalid, show missing-part message + hologram (shift-click like EAF). If valid, open VM GUI.
3. Dummy kit blocks (tower, speakers, cables, keyboard, mouse) are `BaseEntityBlock` only if needed; otherwise plain `Block` with facing.
4. Remove `windows_7_vm_block` from creative tab; keep class for world compat or map it to monitor.
5. Blueprint `workstation` in preview renderer + JEI multiblock category if EAF already has one.

---

## Phase 7 — Assets, JEI, validation

1. Lang keys for every item/block/message.
2. JEI: new machines as `PcbJeiCategory` catalysts (existing category is enough if recipe type stays `pcb`).
3. Loot tables drop self.
4. Creative tab: append in pipeline order (petrochem → IC → internals → peripherals → kit).
5. Advancements optional: first tested tower, first valid workstation.

**Validate**

- Each new machine only runs its recipes (`isAllowedOn`).
- 3-input recipes match slot order (`PcbRecipe.matches` is index-based).
- Bucket remainders do not delete buckets.
- HDD and SSD both produce `board_with_storage`.
- Invalid kit: no VM GUI; hologram shows missing blocks.
- Valid kit: monitor opens existing VM screen; input packets still target controller pos.
- `runClient` / existing recipe load; no JSON recipe errors.

## Risks

- `PCB_MACHINE` valid-block list will be long; missing a block in `BlockEntityType.Builder.of` = silent no-GUI.
- Petrochem buckets must follow `ModFluids` + `ModItems` + liquid block + client render layer in `TechRevised.clientSetup`.
- Kit models/hitboxes: 1-block cubes are OK for v1; do not block multiblock matching on shape.
- Do not raise `INPUT_SLOTS` globally (breaks PCB GUIs); split steps instead.
