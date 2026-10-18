import json
import os
import struct
import sys
import zlib

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from industrial_style import (  # noqa: E402
    machine_front,
    machine_front_active,
    machine_side,
    machine_top,
    pc_item_texture,
)

ROOT = os.path.join("src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", "tech_revised")
DATA = os.path.join(ROOT, "data", "tech_revised")
ITEM_TEX = os.path.join(ASSETS, "textures", "item")
BLOCK_TEX = os.path.join(ASSETS, "textures", "block")
ITEM_MODEL = os.path.join(ASSETS, "models", "item")
BLOCK_MODEL = os.path.join(ASSETS, "models", "block")
BLOCKSTATE = os.path.join(ASSETS, "blockstates")
LOOT = os.path.join(DATA, "loot_tables", "blocks")
RECIPES = os.path.join(DATA, "recipes", "pc")
LANG = os.path.join(ASSETS, "lang", "en_us.json")

for d in (ITEM_TEX, BLOCK_TEX, ITEM_MODEL, BLOCK_MODEL, BLOCKSTATE, LOOT, RECIPES):
    os.makedirs(d, exist_ok=True)


def write_png(path, pixels, w=16, h=16):
    def chunk(tag, data):
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    rows = []
    for y in range(h):
        row = bytearray([0])
        for x in range(w):
            r, g, b, a = pixels[y * w + x]
            row.extend([r, g, b, a])
        rows.append(bytes(row))
    raw = b"".join(rows)
    ihdr = struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)
    data = (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", ihdr)
        + chunk(b"IDAT", zlib.compress(raw, 9))
        + chunk(b"IEND", b"")
    )
    with open(path, "wb") as f:
        f.write(data)


def px(r, g, b, a=255):
    return (r, g, b, a)


def solid(color, w=16, h=16):
    return [color] * (w * h)


def setp(pixels, x, y, c, w=16):
    if 0 <= x < w and 0 <= y < w:
        pixels[y * w + x] = c


def rect(pixels, x0, y0, x1, y1, c, w=16):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            setp(pixels, x, y, c, w)


def hline(pixels, x0, x1, y, c, w=16):
    for x in range(x0, x1 + 1):
        setp(pixels, x, y, c, w)


def dump(path, obj):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def title(name):
    overrides = {
        "ic": "IC", "cpu": "CPU", "gpu": "GPU", "psu": "PSU", "hdd": "HDD", "ssd": "SSD",
        "pcb": "PCB", "pcba": "PCBA", "mcu": "MCU", "ihs": "IHS", "hdmi": "HDMI",
        "lcd": "LCD", "ict": "ICT", "smt": "SMT", "vram": "VRAM", "nand": "NAND",
        "dimm": "DIMM", "iec": "IEC", "pvc": "PVC", "abs": "ABS", "vcm": "VCM",
        "pc": "PC",
    }
    parts = []
    for p in name.split("_"):
        parts.append(overrides.get(p, p.capitalize()))
    return " ".join(parts)


def item_tex(name, seed):
    return pc_item_texture(name, seed)


MACHINES = {
    "crude_fractionator": (px(90, 60, 30), "tank"),
    "naphtha_steam_cracker": (px(210, 170, 70), "oven"),
    "chlor_alkali_cell": (px(180, 230, 80), "tank"),
    "vcm_reactor": (px(140, 200, 160), "tank"),
    "plasticizer_unit": (px(200, 140, 80), "tank"),
    "pvc_polymerizer": (px(80, 80, 90), "tank"),
    "abs_polymerizer": (px(220, 200, 80), "tank"),
    "rubber_compounder": (px(50, 50, 50), "default"),
    "copper_wire_drawer": (px(184, 115, 51), "default"),
    "wire_tinning_bath": (px(180, 180, 190), "tank"),
    "wafer_blank_press": (px(180, 200, 220), "default"),
    "wafer_dicing_saw": (px(160, 160, 170), "drill"),
    "die_attach_bonder": (px(220, 160, 60), "robot"),
    "wire_bonding_machine": (px(220, 180, 80), "robot"),
    "ic_transfer_mold": (px(80, 80, 90), "default"),
    "ic_tester": (px(40, 200, 120), "screen"),
    "ic_binning_station": (px(60, 140, 220), "screen"),
    "cpu_socket_press": (px(120, 120, 130), "default"),
    "expansion_slot_inserter": (px(80, 140, 200), "robot"),
    "chipset_placer": (px(200, 80, 80), "robot"),
    "motherboard_ict": (px(80, 200, 220), "probe") if False else (px(80, 200, 220), "screen"),
    "ihs_bonder": (px(190, 190, 200), "default"),
    "cpu_burn_in_chamber": (px(255, 100, 30), "oven"),
    "gpu_substrate_smt": (px(40, 160, 80), "robot"),
    "vram_populator": (px(80, 80, 200), "robot"),
    "gpu_cooler_press": (px(140, 140, 150), "default"),
    "gpu_shroud_molder": (px(30, 30, 40), "default"),
    "gpu_assembly_jig": (px(220, 60, 60), "robot"),
    "gpu_tester": (px(40, 200, 120), "screen"),
    "transformer_winder": (px(180, 140, 60), "default"),
    "psu_smt": (px(200, 80, 40), "robot"),
    "psu_case_stamper": (px(120, 120, 130), "default"),
    "psu_final_assembly": (px(80, 80, 90), "robot"),
    "psu_load_tester": (px(255, 180, 40), "screen"),
    "platter_polisher": (px(200, 200, 210), "default"),
    "magnetic_sputter": (px(120, 40, 40), "tank"),
    "spindle_motor_winder": (px(160, 120, 60), "default"),
    "head_stack_assembler": (px(90, 90, 100), "robot"),
    "hdd_hepa_sealer": (px(220, 220, 230), "default"),
    "hdd_servo_writer": (px(80, 160, 200), "screen"),
    "hdd_burn_in": (px(255, 100, 30), "oven"),
    "nand_stacker": (px(40, 40, 50), "robot"),
    "ssd_smt": (px(60, 60, 70), "robot"),
    "ssd_firmware_programmer": (px(60, 140, 220), "screen"),
    "ssd_case_welder": (px(140, 140, 150), "oven"),
    "ssd_validator": (px(40, 200, 120), "screen"),
    "steel_sheet_roller": (px(130, 130, 140), "default"),
    "case_stamping_press": (px(100, 100, 110), "default"),
    "powder_coater": (px(40, 80, 160), "nozzle") if False else (px(40, 80, 160), "default"),
    "front_panel_molder": (px(30, 30, 40), "default"),
    "case_assembly_jig": (px(90, 90, 100), "robot"),
    "thermal_paste_dispenser": (px(180, 180, 40), "default"),
    "dimm_install_press": (px(80, 160, 80), "robot"),
    "gpu_install_bench": (px(220, 60, 60), "robot"),
    "storage_install_bay": (px(80, 80, 90), "robot"),
    "psu_install_bench": (px(200, 80, 40), "robot"),
    "tower_final_assembly": (px(50, 50, 60), "robot"),
    "desktop_power_on_test": (px(40, 200, 80), "screen"),
    "lcd_glass_washer": (px(160, 220, 230), "tank"),
    "color_filter_printer": (px(200, 40, 40), "print"),
    "liquid_crystal_filler": (px(80, 180, 80), "tank"),
    "polarizer_laminator": (px(180, 180, 200), "default"),
    "backlight_assembler": (px(255, 240, 120), "default"),
    "lcd_cell_bonder": (px(80, 120, 180), "default"),
    "monitor_controller_smt": (px(60, 140, 220), "robot"),
    "monitor_bezel_molder": (px(30, 30, 40), "default"),
    "monitor_final_assembly": (px(40, 40, 50), "robot"),
    "monitor_burn_in": (px(40, 200, 120), "screen"),
    "keycap_injection_molder": (px(40, 40, 50), "default"),
    "membrane_sheet_printer": (px(80, 80, 90), "print"),
    "keyboard_pcb_smt": (px(40, 140, 80), "robot"),
    "keyboard_plate_stamper": (px(140, 140, 150), "default"),
    "keyboard_assembly": (px(60, 60, 70), "robot"),
    "keyboard_tester": (px(40, 200, 120), "screen"),
    "mouse_shell_molder": (px(40, 40, 50), "default"),
    "optical_sensor_smt": (px(200, 80, 40), "robot"),
    "scroll_wheel_molder": (px(30, 30, 30), "default"),
    "mouse_assembly": (px(60, 60, 70), "robot"),
    "mouse_tester": (px(40, 200, 120), "screen"),
    "pair_twinner": (px(184, 115, 51), "default"),
    "hdmi_shield_braider": (px(160, 160, 170), "default"),
    "hdmi_jacket_extruder": (px(40, 40, 50), "default"),
    "hdmi_connector_molder": (px(180, 160, 40), "default"),
    "hdmi_termination_press": (px(120, 120, 130), "default"),
    "hdmi_continuity_tester": (px(40, 200, 120), "screen"),
    "power_conductor_strander": (px(184, 115, 51), "default"),
    "power_jacket_extruder": (px(30, 30, 30), "default"),
    "iec_connector_molder": (px(40, 40, 50), "default"),
    "power_cable_overmolder": (px(80, 80, 90), "default"),
    "hi_pot_tester": (px(255, 180, 40), "screen"),
    "speaker_magnet_sinter": (px(80, 40, 40), "oven"),
    "voice_coil_winder": (px(184, 115, 51), "default"),
    "cone_former": (px(200, 180, 140), "default"),
    "driver_assembler": (px(90, 90, 100), "robot"),
    "cabinet_molder": (px(40, 40, 50), "default"),
    "speaker_final_assembly": (px(60, 60, 70), "robot"),
    "speaker_anechoic_test": (px(40, 200, 120), "screen"),
}

ITEMS = [
    "plasticizer", "pvc_pellets", "abs_pellets", "rubber_sheet",
    "copper_wire", "tinned_copper_wire",
    "silicon_wafer", "silicon_die", "attached_die", "bonded_die", "molded_ic", "tested_ic",
    "cpu_package", "gpu_package", "chipset_package", "nand_package",
    "hdd_controller_package", "display_controller_package", "mcu_package",
    "socketed_motherboard", "slotted_motherboard", "motherboard_pcba", "tested_motherboard",
    "lidded_cpu", "tested_cpu",
    "gpu_core_board", "gpu_populated_board", "gpu_cooler", "gpu_shroud", "assembled_gpu", "tested_gpu",
    "psu_transformer", "psu_board", "psu_shell", "assembled_psu", "tested_psu",
    "hdd_platter", "coated_platter", "spindle_motor", "head_stack", "sealed_hdd", "programmed_hdd", "tested_hdd",
    "nand_stack", "ssd_board", "programmed_ssd", "assembled_ssd", "tested_ssd",
    "steel_sheet", "case_chassis", "coated_chassis", "case_front_panel", "pc_case",
    "board_with_cpu", "board_with_memory", "board_with_gpu", "board_with_storage", "wired_internals", "assembled_desktop",
    "cleaned_lcd_glass", "color_filter_glass", "filled_lcd_cell", "polarized_panel", "led_backlight",
    "lcd_module", "monitor_logic_board", "monitor_bezel", "assembled_monitor",
    "keycaps", "key_membrane", "keyboard_pcb", "keyboard_plate", "assembled_keyboard",
    "mouse_shell", "mouse_sensor_board", "scroll_wheel", "assembled_mouse",
    "twisted_pair", "shielded_hdmi_core", "jacketed_hdmi", "hdmi_connector", "assembled_hdmi",
    "power_conductor", "jacketed_power_cord", "iec_connector", "assembled_power_cable",
    "speaker_magnet", "voice_coil", "speaker_cone", "speaker_driver", "speaker_cabinet", "assembled_speaker",
]

KIT_BLOCKS = [
    "tested_desktop_tower", "tested_keyboard", "tested_mouse",
    "tested_hdmi_cable", "tested_power_cable", "tested_speaker",
]

FLUID_BUCKETS = [
    ("naphtha", "naphtha_bucket"),
    ("ethylene", "ethylene_bucket"),
    ("chlorine", "chlorine_bucket"),
    ("vinyl_chloride", "vinyl_chloride_bucket"),
]


def ing(item):
    if ":" in item:
        return {"item": item}
    return {"item": f"tech_revised:{item}"}


def recipe(machine, ingredients, result, ticks=200, energy=40, name=None):
    body = {
        "type": "tech_revised:pcb",
        "machine": f"tech_revised:{machine}",
        "ingredients": [ing(i) for i in ingredients],
        "result": {"item": result if ":" in result else f"tech_revised:{result}"},
        "process_ticks": ticks,
        "energy_per_tick": energy,
    }
    fname = name or result.split(":")[-1]
    dump(os.path.join(RECIPES, f"{fname}.json"), body)


def loot(name):
    dump(os.path.join(LOOT, f"{name}.json"), {
        "type": "minecraft:block",
        "pools": [{
            "bonus_rolls": 0.0,
            "rolls": 1.0,
            "entries": [{"type": "minecraft:item", "name": f"tech_revised:{name}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    })


def facing_blockstate(name, active=False):
    variants = {}
    for facing, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        if active:
            for act, suffix in ((False, ""), (True, "_on")):
                key = f"facing={facing},active={str(act).lower()}"
                entry = {"model": f"tech_revised:block/{name}{suffix}"}
                if rot:
                    entry["y"] = rot
                variants[key] = entry
        else:
            entry = {"model": f"tech_revised:block/{name}"}
            if rot:
                entry["y"] = rot
            variants[f"facing={facing}"] = entry
    dump(os.path.join(BLOCKSTATE, f"{name}.json"), {"variants": variants})


def orientable_models(name, front, front_on=None):
    dump(os.path.join(BLOCK_MODEL, f"{name}.json"), {
        "parent": "minecraft:block/orientable",
        "textures": {
            "top": "tech_revised:block/pcb_machine_top",
            "front": front,
            "side": "tech_revised:block/pcb_machine_side",
        },
    })
    if front_on:
        dump(os.path.join(BLOCK_MODEL, f"{name}_on.json"), {
            "parent": "minecraft:block/orientable",
            "textures": {
                "top": "tech_revised:block/pcb_machine_top",
                "front": front_on,
                "side": "tech_revised:block/pcb_machine_side",
            },
        })
    dump(os.path.join(ITEM_MODEL, f"{name}.json"), {"parent": f"tech_revised:block/{name}"})


# --- items ---
for i, name in enumerate(ITEMS):
    write_png(os.path.join(ITEM_TEX, f"{name}.png"), item_tex(name, i + 11))
    dump(os.path.join(ITEM_MODEL, f"{name}.json"), {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"tech_revised:item/{name}"},
    })

for fluid, item in FLUID_BUCKETS:
    dump(os.path.join(ITEM_MODEL, f"{item}.json"), {
        "parent": "forge:item/bucket",
        "loader": "forge:fluid_container",
        "fluid": f"tech_revised:{fluid}",
    })

# --- machines ---
for name, (accent, symbol) in MACHINES.items():
    write_png(os.path.join(BLOCK_TEX, f"{name}_front.png"), machine_front(accent, symbol))
    write_png(os.path.join(BLOCK_TEX, f"{name}_front_on.png"), machine_front_active(accent, symbol))
    orientable_models(name, f"tech_revised:block/{name}_front", f"tech_revised:block/{name}_front_on")
    facing_blockstate(name, active=True)
    loot(name)

# --- kit blocks ---
kit_colors = {
    "tested_desktop_tower": px(40, 40, 48),
    "tested_keyboard": px(30, 30, 36),
    "tested_mouse": px(50, 50, 58),
    "tested_hdmi_cable": px(20, 20, 24),
    "tested_power_cable": px(20, 20, 20),
    "tested_speaker": px(70, 70, 78),
    "tested_monitor": px(20, 40, 80),
}
for name, color in kit_colors.items():
    front = machine_front(color, "screen" if "monitor" in name else "default")
    write_png(os.path.join(BLOCK_TEX, f"{name}_front.png"), front)
    if name == "tested_monitor":
        write_png(os.path.join(BLOCK_TEX, f"{name}_front_on.png"), machine_front_active(color, "screen"))
        orientable_models(name, f"tech_revised:block/{name}_front", f"tech_revised:block/{name}_front_on")
        facing_blockstate(name, active=True)
    else:
        orientable_models(name, f"tech_revised:block/{name}_front")
        facing_blockstate(name, active=False)
    loot(name)

# --- recipes ---
recipe("crude_fractionator", ["heavy_crude_oil_bucket"], "naphtha_bucket", 200, 40)
recipe("naphtha_steam_cracker", ["naphtha_bucket"], "ethylene_bucket", 200, 60)
recipe("chlor_alkali_cell", ["minecraft:water_bucket", "soda_ash"], "chlorine_bucket", 200, 80)
recipe("vcm_reactor", ["ethylene_bucket", "chlorine_bucket"], "vinyl_chloride_bucket", 200, 50)
recipe("plasticizer_unit", ["naphtha_bucket"], "plasticizer", 150, 30)
recipe("pvc_polymerizer", ["vinyl_chloride_bucket", "plasticizer"], "pvc_pellets", 250, 40)
recipe("rubber_compounder", ["naphtha_bucket"], "rubber_sheet", 200, 30)
recipe("abs_polymerizer", ["naphtha_bucket", "rubber_sheet"], "abs_pellets", 250, 40)

recipe("copper_wire_drawer", ["minecraft:copper_ingot"], "copper_wire", 100, 30)
recipe("wire_tinning_bath", ["copper_wire", "tin_plating_dust"], "tinned_copper_wire", 100, 30)

recipe("wafer_blank_press", ["silica_sand"], "silicon_wafer", 200, 40)
recipe("wafer_dicing_saw", ["silicon_wafer"], "silicon_die", 150, 50)
recipe("die_attach_bonder", ["silicon_die", "copper_foil"], "attached_die", 150, 40)
recipe("wire_bonding_machine", ["attached_die", "tinned_copper_wire"], "bonded_die", 150, 40)
recipe("ic_transfer_mold", ["bonded_die", "abs_pellets"], "molded_ic", 150, 30)
recipe("ic_tester", ["molded_ic"], "tested_ic", 200, 40)
recipe("ic_binning_station", ["tested_ic", "stainless_steel_nugget"], "cpu_package", 100, 20, "cpu_package")
recipe("ic_binning_station", ["tested_ic", "minecraft:redstone"], "gpu_package", 100, 20, "gpu_package")
recipe("ic_binning_station", ["tested_ic", "copper_foil"], "chipset_package", 100, 20, "chipset_package")
recipe("ic_binning_station", ["tested_ic", "carbon_chunk"], "nand_package", 100, 20, "nand_package")
recipe("ic_binning_station", ["tested_ic", "steel_nugget"], "hdd_controller_package", 100, 20, "hdd_controller_package")
recipe("ic_binning_station", ["tested_ic", "minecraft:glass"], "display_controller_package", 100, 20, "display_controller_package")
recipe("ic_binning_station", ["tested_ic", "minecraft:gold_nugget"], "mcu_package", 100, 20, "mcu_package")
recipe("ic_binning_station", ["tested_ic", "minecraft:quartz"], "dram_package", 100, 20, "dram_package_from_ic")

recipe("cpu_socket_press", ["final_pcba", "steel_nugget"], "socketed_motherboard", 200, 40)
recipe("expansion_slot_inserter", ["socketed_motherboard", "copper_foil"], "slotted_motherboard", 200, 40)
recipe("chipset_placer", ["slotted_motherboard", "chipset_package"], "motherboard_pcba", 250, 50)
recipe("motherboard_ict", ["motherboard_pcba"], "tested_motherboard", 200, 40)

recipe("ihs_bonder", ["cpu_package", "stainless_steel_nugget"], "lidded_cpu", 150, 30)
recipe("cpu_burn_in_chamber", ["lidded_cpu"], "tested_cpu", 300, 80)

recipe("gpu_substrate_smt", ["final_pcba", "gpu_package"], "gpu_core_board", 250, 50)
recipe("vram_populator", ["gpu_core_board", "dram_package"], "gpu_populated_board", 200, 40)
recipe("gpu_cooler_press", ["steel_ingot", "rubber_sheet"], "gpu_cooler", 150, 30)
recipe("gpu_shroud_molder", ["abs_pellets"], "gpu_shroud", 150, 30)
recipe("gpu_assembly_jig", ["gpu_populated_board", "gpu_cooler", "gpu_shroud"], "assembled_gpu", 250, 40)
recipe("gpu_tester", ["assembled_gpu"], "tested_gpu", 200, 40)

recipe("transformer_winder", ["tinned_copper_wire", "steel_ingot"], "psu_transformer", 200, 40)
recipe("psu_smt", ["final_pcba", "psu_transformer"], "psu_board", 250, 50)
recipe("psu_case_stamper", ["steel_ingot"], "psu_shell", 150, 30)
recipe("psu_final_assembly", ["psu_board", "psu_shell", "pvc_pellets"], "assembled_psu", 250, 40)
recipe("psu_load_tester", ["assembled_psu"], "tested_psu", 200, 60)

recipe("platter_polisher", ["minecraft:iron_ingot"], "hdd_platter", 150, 30)
recipe("magnetic_sputter", ["hdd_platter", "minecraft:redstone"], "coated_platter", 200, 50)
recipe("spindle_motor_winder", ["tinned_copper_wire", "steel_nugget"], "spindle_motor", 150, 30)
recipe("head_stack_assembler", ["steel_nugget", "silicon_die"], "head_stack", 200, 40)
recipe("hdd_hepa_sealer", ["coated_platter", "spindle_motor", "head_stack"], "sealed_hdd", 250, 40)
recipe("hdd_servo_writer", ["sealed_hdd", "hdd_controller_package"], "programmed_hdd", 250, 50)
recipe("hdd_burn_in", ["programmed_hdd"], "tested_hdd", 300, 40)

recipe("nand_stacker", ["nand_package"], "nand_stack", 150, 30)
recipe("ssd_smt", ["final_pcba", "nand_stack", "mcu_package"], "ssd_board", 250, 50)
recipe("ssd_firmware_programmer", ["ssd_board"], "programmed_ssd", 200, 40)
recipe("ssd_case_welder", ["programmed_ssd", "abs_pellets"], "assembled_ssd", 150, 30)
recipe("ssd_validator", ["assembled_ssd"], "tested_ssd", 200, 40)

recipe("steel_sheet_roller", ["steel_ingot"], "steel_sheet", 100, 30)
recipe("case_stamping_press", ["steel_sheet"], "case_chassis", 150, 40)
recipe("powder_coater", ["case_chassis"], "coated_chassis", 150, 30)
recipe("front_panel_molder", ["abs_pellets"], "case_front_panel", 150, 30)
recipe("case_assembly_jig", ["coated_chassis", "case_front_panel", "rubber_sheet"], "pc_case", 200, 30)

recipe("thermal_paste_dispenser", ["tested_motherboard", "tested_cpu", "chemical_lubricant"], "board_with_cpu", 200, 30)
recipe("dimm_install_press", ["board_with_cpu", "ram_stick"], "board_with_memory", 150, 20)
recipe("gpu_install_bench", ["board_with_memory", "tested_gpu"], "board_with_gpu", 200, 30)
recipe("storage_install_bay", ["board_with_gpu", "tested_hdd"], "board_with_storage", 150, 20, "storage_install_hdd")
recipe("storage_install_bay", ["board_with_gpu", "tested_ssd"], "board_with_storage", 150, 20, "storage_install_ssd")
recipe("psu_install_bench", ["board_with_storage", "tested_psu"], "wired_internals", 200, 30)
recipe("tower_final_assembly", ["wired_internals", "pc_case"], "assembled_desktop", 200, 30)
recipe("desktop_power_on_test", ["assembled_desktop"], "tested_desktop_tower", 300, 40)

recipe("lcd_glass_washer", ["minecraft:glass"], "cleaned_lcd_glass", 100, 20)
recipe("color_filter_printer", ["cleaned_lcd_glass"], "color_filter_glass", 200, 30)
recipe("liquid_crystal_filler", ["color_filter_glass", "chemical_lubricant"], "filled_lcd_cell", 200, 30)
recipe("polarizer_laminator", ["filled_lcd_cell"], "polarized_panel", 150, 30)
recipe("backlight_assembler", ["abs_pellets", "minecraft:redstone"], "led_backlight", 150, 30)
recipe("lcd_cell_bonder", ["polarized_panel", "led_backlight"], "lcd_module", 200, 40)
recipe("monitor_controller_smt", ["final_pcba", "display_controller_package"], "monitor_logic_board", 200, 40)
recipe("monitor_bezel_molder", ["abs_pellets"], "monitor_bezel", 150, 30)
recipe("monitor_final_assembly", ["lcd_module", "monitor_logic_board", "monitor_bezel"], "assembled_monitor", 250, 40)
recipe("monitor_burn_in", ["assembled_monitor"], "tested_monitor", 300, 40)

recipe("keycap_injection_molder", ["abs_pellets"], "keycaps", 150, 30)
recipe("membrane_sheet_printer", ["rubber_sheet"], "key_membrane", 150, 30)
recipe("keyboard_pcb_smt", ["final_pcba", "mcu_package"], "keyboard_pcb", 200, 40)
recipe("keyboard_plate_stamper", ["steel_sheet"], "keyboard_plate", 150, 30)
recipe("keyboard_assembly", ["keyboard_pcb", "key_membrane", "keycaps"], "assembled_keyboard", 200, 30)
recipe("keyboard_tester", ["assembled_keyboard", "keyboard_plate"], "tested_keyboard", 150, 20)

recipe("mouse_shell_molder", ["abs_pellets"], "mouse_shell", 150, 30)
recipe("optical_sensor_smt", ["final_pcba", "mcu_package"], "mouse_sensor_board", 200, 40)
recipe("scroll_wheel_molder", ["rubber_sheet"], "scroll_wheel", 100, 20)
recipe("mouse_assembly", ["mouse_shell", "mouse_sensor_board", "scroll_wheel"], "assembled_mouse", 200, 30)
recipe("mouse_tester", ["assembled_mouse"], "tested_mouse", 150, 20)

recipe("pair_twinner", ["tinned_copper_wire"], "twisted_pair", 100, 20)
recipe("hdmi_shield_braider", ["twisted_pair", "copper_wire"], "shielded_hdmi_core", 150, 30)
recipe("hdmi_jacket_extruder", ["shielded_hdmi_core", "pvc_pellets"], "jacketed_hdmi", 150, 30)
recipe("hdmi_connector_molder", ["abs_pellets", "minecraft:gold_nugget"], "hdmi_connector", 150, 30)
recipe("hdmi_termination_press", ["jacketed_hdmi", "hdmi_connector"], "assembled_hdmi", 150, 30)
recipe("hdmi_continuity_tester", ["assembled_hdmi"], "tested_hdmi_cable", 100, 20)

recipe("power_conductor_strander", ["tinned_copper_wire"], "power_conductor", 100, 20)
recipe("power_jacket_extruder", ["power_conductor", "pvc_pellets"], "jacketed_power_cord", 150, 30)
recipe("iec_connector_molder", ["abs_pellets", "steel_nugget"], "iec_connector", 150, 30)
recipe("power_cable_overmolder", ["jacketed_power_cord", "iec_connector"], "assembled_power_cable", 150, 30)
recipe("hi_pot_tester", ["assembled_power_cable"], "tested_power_cable", 150, 40)

recipe("speaker_magnet_sinter", ["minecraft:iron_ingot", "minecraft:redstone"], "speaker_magnet", 200, 40)
recipe("voice_coil_winder", ["tinned_copper_wire"], "voice_coil", 150, 30)
recipe("cone_former", ["minecraft:paper", "chemical_lubricant"], "speaker_cone", 150, 20)
recipe("driver_assembler", ["speaker_magnet", "voice_coil", "speaker_cone"], "speaker_driver", 200, 30)
recipe("cabinet_molder", ["abs_pellets"], "speaker_cabinet", 150, 30)
recipe("speaker_final_assembly", ["speaker_driver", "speaker_cabinet", "rubber_sheet"], "assembled_speaker", 200, 30)
recipe("speaker_anechoic_test", ["assembled_speaker"], "tested_speaker", 150, 20)

# --- tags ---
def merge_tag(path, extra):
    with open(path, encoding="utf-8") as f:
        data = json.load(f)
    values = list(data.get("values", []))
    for v in extra:
        if v not in values:
            values.append(v)
    data["values"] = values
    dump(path, data)

ids = [f"tech_revised:{n}" for n in list(MACHINES) + KIT_BLOCKS + ["tested_monitor"]]
merge_tag(os.path.join(ROOT, "data", "minecraft", "tags", "blocks", "mineable", "pickaxe.json"), ids)
merge_tag(os.path.join(ROOT, "data", "minecraft", "tags", "blocks", "needs_iron_tool.json"), ids)

print(f"machines={len(MACHINES)} items={len(ITEMS)} kit={len(KIT_BLOCKS)}")
