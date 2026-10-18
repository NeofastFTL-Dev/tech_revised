"""Regenerate every machine block texture with the futuristic hull style.

Does not touch workstation furniture (tested_desk, chair, etc.) or recipes.
Run from repo root:  python tools/regen_machine_textures.py
"""
import glob
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from industrial_style import (  # noqa: E402
    CYAN,
    machine_front,
    machine_front_on,
    machine_side,
    machine_top,
    write_png,
)

BLOCK_TEX = os.path.join("src", "main", "resources", "assets", "tech_revised", "textures", "block")

SKIP_PREFIX = ("tested_desk", "tested_chair", "tested_device", "workstation_")
SKIP_EXACT = {
    "tested_keyboard_front",
    "tested_mouse_front",
    "tested_speaker_front",
    "tested_hdmi_cable_front",
    "tested_desktop_tower_front",
    "tested_power_cable_front",
    "tested_monitor_front",
    "tested_monitor_front_on",
}


def symbol_for(name):
    n = name.lower()
    if any(k in n for k in ("controller", "ict", "tester", "validator", "programmer", "aoi", "vm")):
        return "screen"
    if "oven" in n or "furnace" in n or "burn" in n or "sinter" in n or "coater" in n or "welder" in n:
        return "oven"
    if "tank" in n or "bath" in n or "wash" in n or "reactor" in n or "polymer" in n or "fraction" in n or "cracker" in n or "cell" in n:
        return "tank"
    if "drill" in n or "router" in n or "saw" in n or "shear" in n:
        return "drill"
    if any(k in n for k in ("robot", "placer", "bonder", "jig", "assembler", "install", "populator")):
        return "robot"
    if "print" in n or "imager" in n or "flooder" in n or "applicator" in n:
        return "print"
    if "probe" in n:
        return "probe"
    if "nozzle" in n or "sprayer" in n or "dispenser" in n:
        return "nozzle"
    if "winder" in n or "creel" in n:
        return "winder"
    if "loom" in n:
        return "loom"
    if "mixer" in n or "crusher" in n or "compounder" in n:
        return "mixer"
    if "heater" in n:
        return "heater"
    if "hatch" in n or "energy" in n:
        return "energy"
    if "fluid" in n:
        return "fluid"
    if "output_bus" in n:
        return "bus_out"
    if "input_bus" in n:
        return "bus_in"
    if "frame" in n:
        return "frame"
    if "chop" in n:
        return "chopper"
    if "vm" in n:
        return "vm"
    return "default"


def accent_for(name):
    h = 0
    for ch in name:
        h = (h * 33 + ord(ch)) & 0xFFFFFF
    r = 40 + (h & 0x7F)
    g = 80 + ((h >> 8) & 0x7F)
    b = 140 + ((h >> 16) & 0x6F)
    n = name.lower()
    if "oven" in n or "furnace" in n or "heater" in n or "burn" in n:
        return (255, 110, 40, 255)
    if "screen" in symbol_for(name) or "tester" in n:
        return (40, 200, 255, 255)
    if "tank" in n or "fluid" in n:
        return (40, 160, 220, 255)
    if "energy" in n:
        return (255, 200, 50, 255)
    if "output" in n:
        return (220, 60, 90, 255)
    if "input" in n:
        return (60, 200, 90, 255)
    return (r, g, b, 255)


def skip(stem):
    if stem in SKIP_EXACT:
        return True
    return any(stem.startswith(p) for p in SKIP_PREFIX)


write_png(os.path.join(BLOCK_TEX, "machine_side.png"), machine_side())
write_png(os.path.join(BLOCK_TEX, "machine_top.png"), machine_top())
write_png(os.path.join(BLOCK_TEX, "pcb_machine_side.png"), machine_side())
write_png(os.path.join(BLOCK_TEX, "pcb_machine_top.png"), machine_top())
write_png(os.path.join(BLOCK_TEX, "transport_frame.png"), machine_side())
write_png(os.path.join(BLOCK_TEX, "steel_frame.png"), machine_side())
write_png(os.path.join(BLOCK_TEX, "steel_frame_normal.png"), machine_side())
uf = os.path.join(BLOCK_TEX, "universal_frame.png")
if os.path.exists(uf):
    write_png(uf, machine_side())

count = 0
stems = set()
for path in glob.glob(os.path.join(BLOCK_TEX, "*_front.png")):
    stem = os.path.basename(path)[:-10]
    if skip(stem):
        continue
    stems.add(stem)

for path in glob.glob(os.path.join(BLOCK_TEX, "*_formed.png")):
    stem = os.path.basename(path)[:-11]
    if skip(stem):
        continue
    accent = (80, 180, 255, 255)
    write_png(path, machine_front(accent, "frame", active=True))
    count += 1

for stem in sorted(stems):
    accent = accent_for(stem)
    symbol = symbol_for(stem)
    write_png(os.path.join(BLOCK_TEX, f"{stem}_front.png"), machine_front(accent, symbol, False))
    on_path = os.path.join(BLOCK_TEX, f"{stem}_front_on.png")
    if os.path.exists(on_path):
        write_png(on_path, machine_front_on(accent, symbol))
    idle_path = os.path.join(BLOCK_TEX, f"{stem}_front_idle.png")
    if os.path.exists(idle_path):
        write_png(idle_path, machine_front(accent, symbol, False))
    count += 1

extras = {
    "input_bus": ((60, 200, 90, 255), "bus_in"),
    "output_bus": ((220, 60, 90, 255), "bus_out"),
    "fluid_input_bus": ((40, 160, 220, 255), "fluid"),
    "fluid_output_bus": ((40, 90, 180, 255), "fluid"),
    "energy_input_hatch": ((255, 200, 50, 255), "energy"),
    "aod_controller": ((180, 100, 255, 255), "controller"),
    "windows_7_vm_block": ((40, 180, 255, 255), "vm"),
    "windows_7_vm_block_idle": ((40, 90, 160, 255), "vm"),
    "crusher_front_idle": ((140, 180, 220, 255), "mixer"),
    "universal_frame": ((70, 90, 120, 255), "frame"),
}
for name, (accent, symbol) in extras.items():
    path = os.path.join(BLOCK_TEX, f"{name}.png")
    if os.path.exists(path):
        write_png(path, machine_front(accent, symbol, active=("on" in name)))
        count += 1

formed_universal = os.path.join(BLOCK_TEX, "universal_frame_formed.png")
# model uses machine_top for formed; keep a frame texture if present
print(f"regenerated {count} machine textures")
