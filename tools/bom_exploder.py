"""Explode the full tech_revised recipe tree to raw-resource counts for ONE complete PC build.

Run from repo root:  python tools/bom_exploder.py

Counts: every crafting machine + every PC-chain machine operation + desk furniture,
exploded down to vanilla resources and mod ore blocks. Fluid intermediates (water,
oxygen, oil, naphtha...) are treated as machine outputs, not mining.
"""
import json
import glob
import math
import os
import re
from collections import Counter

R = "src/main/resources/data/tech_revised/recipes"

# ---------------------------------------------------------------- recipe db
RECIPES = {}  # result item name -> list of (path, recipe dict)

for p in glob.glob(R + "/**/*.json", recursive=True):
    rel = p.replace("\\", "/").split("/recipes/")[-1]
    try:
        d = json.load(open(p, encoding="utf-8"))
    except Exception:
        continue
    if not isinstance(d, dict):
        continue
    # normalize results: vanilla smelting uses a string; coke oven uses result_item
    res = d.get("result", {})
    if isinstance(res, str):
        d["result"] = {"item": res, "count": 1}
        res = d["result"]
    if not (isinstance(res, dict) and res.get("item")) and isinstance(d.get("result_item"), dict):
        d["result"] = d["result_item"]
        res = d["result"]
    item = res.get("item") if isinstance(res, dict) else None
    if item and item.startswith("tech_revised:"):
        RECIPES.setdefault(item.split(":")[-1], []).append((rel, d))

# producer preference: real smelting/machine paths first; storage compression last
DIR_PRIORITY = ["blast_furnace", "eaf", "aod", "oxygen_converter", "drilling_platform",
                "crusher", "coke_oven", "pc", "pcb", "fiberglass", "materials", "items",
                "machines", "controllers", "mbs", "blasting", "de_compression"]

# items that a machine both consumes and re-emits (in-place treatment steps);
# the re-emission recipe is a transformer, never the producer
TRANSFORM_ITEMS = {"patterned_outer_layer_pcb"}
# fluid products the chain needs by the bucket; produced by machines, not mining
FLUID_ITEMS = {"heavy_crude_oil_bucket", "naphtha_bucket", "ethylene_bucket", "chlorine_bucket",
               "vinyl_chloride_bucket", "oxygen_bucket", "creosote_bucket", "water_bucket"}


def producer(name):
    cands = RECIPES.get(name)
    if not cands:
        return None
    if name in TRANSFORM_ITEMS:
        def is_self_consuming(c):
            names = [i.get("item", i.get("tag", "")) for i in c[1].get("ingredients", [])
                     if isinstance(i, dict)]
            return name in names or ("tech_revised:" + name) in names
        cands = [c for c in cands if not is_self_consuming(c)]
    def prio(rel):
        top = rel.split("/")[0] if "/" in rel else ""
        return DIR_PRIORITY.index(top) if top in DIR_PRIORITY else 99
    ranked = sorted(cands, key=lambda c: prio(c[0]))
    # prefer stone-variant ore inputs over deepslate duplicates (same yield either way)
    non_ds = [c for c in ranked if "deepslate" not in json.dumps(c[1])]
    return (non_ds or ranked)[0]


def recipe_inputs(d):
    """Return list of ingredient names (items or tags), 1 entry per unit consumed."""
    out = []
    if "pattern" in d and "key" in d:
        counts = Counter()
        for row in d["pattern"]:
            counts.update(row)
        for ch, n in counts.items():
            spec = d["key"].get(ch, {})
            name = spec.get("item", spec.get("tag"))
            if name:
                out.extend([name] * n)
        return out
    for i in d.get("ingredients", []):
        if isinstance(i, dict):
            name = i.get("item", i.get("tag"))
            if name:
                out.append(name)
    for key in ("ingredient", "primary_input", "secondary_input"):
        spec = d.get(key)
        if isinstance(spec, dict):
            name = spec.get("item", spec.get("tag"))
            if name:
                out.append(name)
    return out


# ---------------------------------------------------------------- structure layouts
# Ported from MachineLayouts.java: exact frame/hatch counts per structure.
def fill(x0, x1, y0, y1, z0, z1):
    return (x1 - x0 + 1) * (y1 - y0 + 1) * (z1 - z0 + 1)


def shell(x0, x1, y0, y1, z0, z1):
    return fill(x0, x1, y0, y1, z0, z1) - fill(x0 + 1, x1 - 1, y0 + 1, y1 - 1, z0 + 1, z1 - 1)


def ring(x0, x1, y, z0, z1):
    dx = x1 - x0 + 1
    dz = z1 - z0 + 1
    return 2 * dx + 2 * dz - 4


def disc(r2):
    n = 0
    r = -int(r2 ** 0.5) - 1
    for x in range(-r, r + 1):
        for z in range(-r, r + 1):
            if x * x + z * z <= r2:
                n += 1
    return n


def ring_annulus(r2min, r2max):
    n = 0
    r = -int(r2max ** 0.5) - 1
    for x in range(-r, r + 1):
        for z in range(-r, r + 1):
            d = x * x + z * z
            if r2min <= d <= r2max:
                n += 1
    return n


def eaf_structure():
    return {"electric_arc_furnace_frame": disc(5) + 2 * ring_annulus(2, 5) + disc(5),
            "electric_arc_furnace_input_bus": 1, "electric_arc_furnace_output_bus": 1,
            "electric_arc_furnace_fluid_input_bus": 1, "electric_arc_furnace_fluid_output_bus": 1,
            "electric_arc_furnace_energy_input_hatch": 1, "electric_arc_furnace_heater": 1}


def coke_oven_structure():
    return {"coke_oven_frame": fill(-3, 3, 0, 0, 0, 2) + fill(-3, 3, 3, 3, 0, 2)
            + fill(-3, 3, 1, 2, 0, 0) + fill(-3, 3, 1, 2, 2, 2)
            + fill(-3, -3, 1, 2, 1, 1) + fill(3, 3, 1, 2, 1, 1),
            "electric_arc_furnace_input_bus": 1, "electric_arc_furnace_output_bus": 1,
            "electric_arc_furnace_fluid_output_bus": 1}


def drilling_structure():
    corners = 8 * 5  # 8 corner columns, y = 1..5
    return {"drilling_platform_frame": fill(-2, 2, 0, 0, 0, 3) + corners + fill(-1, 1, 6, 6, 1, 2),
            "drilling_platform_drill_head": 1,
            "electric_arc_furnace_energy_input_hatch": 1, "electric_arc_furnace_fluid_output_bus": 1}


def oxygen_structure():
    return {"oxygen_converter_frame": shell(-2, 2, 0, 2, 0, 2),
            "electric_arc_furnace_energy_input_hatch": 1, "electric_arc_furnace_fluid_input_bus": 1,
            "electric_arc_furnace_fluid_output_bus": 2}


def aod_structure():
    return {"argon_oxygen_decarburization_converter_frame": disc(5) + 6 * ring_annulus(2, 5) + disc(5),
            "electric_arc_furnace_input_bus": 1, "electric_arc_furnace_output_bus": 1,
            "electric_arc_furnace_fluid_input_bus": 1, "electric_arc_furnace_energy_input_hatch": 1}


def blast_furnace_structure():
    return {"electric_arc_furnace_frame": fill(-1, 1, 0, 0, 0, 2) + 6 * ring(-1, 1, 1, 0, 2) + fill(-1, 1, 7, 7, 0, 2),
            "electric_arc_furnace_energy_input_hatch": 1, "electric_arc_furnace_output_bus": 1,
            "electric_arc_furnace_input_bus": 1}


HEAVY = {
    "electric_arc_furnace_controller": eaf_structure(),
    "coke_oven_controller": coke_oven_structure(),
    "drilling_platform_controller": drilling_structure(),
    "oxygen_converter_controller": oxygen_structure(),
    "argon_oxygen_decarburization_converter_controller": aod_structure(),
    "blast_furnace_core": blast_furnace_structure(),
}

# production class layouts (from MachineLayouts). Counts are placed blocks:
# controller origin is not a frame; the energy hatch replaces one frame cell.
def cyl_solid(max_d2, cz=1):
    r = math.ceil(math.sqrt(max_d2))
    n = 0
    for z in range(cz - r, cz + r + 1):
        for x in range(-r, r + 1):
            if x * x + (z - cz) ** 2 <= max_d2:
                n += 1
    return n


def cyl_shell(min_d2, max_d2, cz=1):
    r = math.ceil(math.sqrt(max_d2))
    n = 0
    for z in range(cz - r, cz + r + 1):
        for x in range(-r, r + 1):
            d = x * x + (z - cz) ** 2
            if min_d2 <= d <= max_d2:
                n += 1
    return n


def placed(frame_cells):
    return {"transport_frame": frame_cells - 2, "electric_arc_furnace_energy_input_hatch": 1}


def reactor():
    return placed(cyl_solid(2) + 2 * cyl_shell(1, 2) + cyl_solid(2))


def vessel():
    return placed(cyl_solid(2) + cyl_shell(1, 2) + cyl_solid(2))


def bath():
    return placed(fill(-2, 2, 0, 0, 0, 2) + fill(-2, -2, 1, 1, 0, 2)
                  + fill(2, 2, 1, 1, 0, 2) + fill(-1, 1, 1, 1, 2, 2))


def press_t():
    return placed(fill(-1, 1, 0, 0, 0, 2) + 5)


def molder():
    return placed(fill(-1, 1, 0, 1, 0, 0) + fill(0, 0, 0, 0, 1, 2) + 1)


def extruder():
    return placed(fill(-1, 1, 0, 0, 0, 2) + fill(0, 0, 1, 1, 0, 2) + 1)


def oven_t():
    return placed(fill(-2, 2, 0, 0, 0, 2) + fill(-2, -2, 1, 1, 0, 2)
                  + fill(2, 2, 1, 1, 0, 2) + fill(-2, 2, 2, 2, 0, 2))


def conveyor():
    return placed(fill(-2, 2, 0, 0, 0, 2) + fill(-2, 2, 1, 1, 2, 2))


def smt():
    return placed(fill(-2, 2, 0, 0, 0, 2) + 2 + fill(-2, 2, 2, 2, 1, 1))


def cnc():
    return placed(fill(-1, 1, 0, 0, 0, 2) + 2 + fill(-1, 1, 2, 2, 1, 1))


def crusher():
    return placed(fill(-1, 1, 0, 1, 0, 2) + ring(-1, 1, 2, 0, 2))


def mixer():
    return placed(cyl_solid(2) + 2 * cyl_shell(1, 2) + 1)


def furnace():
    return placed(fill(-2, 2, 0, 0, 0, 2) + ring(-2, 2, 1, 0, 2) + 2)


def winder():
    return placed(fill(-1, 1, 0, 0, 0, 1) + 5)


def tester():
    return placed(fill(-1, 1, 0, 0, 0, 1) + 4)


def bench():
    return placed(fill(-2, 2, 0, 0, 0, 1) + fill(-2, 2, 1, 1, 1, 1))


def skid():
    return placed(fill(-1, 1, 0, 0, 0, 2) + fill(-1, 1, 1, 1, 2, 2))


KIND = {
    "reactor": reactor, "vessel": vessel, "bath": bath, "press": press_t, "molder": molder,
    "extruder": extruder, "oven": oven_t, "conveyor": conveyor, "smt": smt, "cnc": cnc,
    "crusher": crusher, "mixer": mixer, "furnace": furnace, "winder": winder,
    "tester": tester, "bench": bench, "skid": skid,
}

CLASS_BY_ID = {}


def _put(kind, *ids):
    for i in ids:
        if i in CLASS_BY_ID:
            raise SystemExit("duplicate layout class for " + i)
        CLASS_BY_ID[i] = kind


_put("crusher", "crusher")
_put("mixer", "industrial_batching_mixer", "rubber_compounder")
_put("furnace", "refractory_melting_furnace")
_put("winder", "high_speed_gathering_winder", "transformer_winder", "spindle_motor_winder", "voice_coil_winder")
_put("oven", "industrial_drying_oven", "decontamination_oven", "multi_zone_reflow_oven",
     "cpu_burn_in_chamber", "hdd_burn_in", "powder_coater", "monitor_burn_in", "speaker_magnet_sinter")
_put("skid", "roving_creel_converter", "platter_polisher", "thermal_paste_dispenser")
_put("conveyor", "industrial_textile_weaving_loom", "photo_resist_applicator", "soldermask_flooder",
     "uv_pad_exposure_station", "inkjet_silkscreen_printer", "surface_finish_station",
     "psu_final_assembly", "tower_final_assembly", "color_filter_printer", "monitor_final_assembly",
     "membrane_sheet_printer", "keyboard_assembly", "mouse_assembly", "speaker_final_assembly")
_put("cnc", "strand_chopping_machinery", "industrial_shear", "xray_alignment_drill", "cnc_router",
     "wafer_dicing_saw")
_put("bench", "design_engineering_station", "wire_bonding_machine", "expansion_slot_inserter",
     "gpu_assembly_jig", "head_stack_assembler", "case_assembly_jig", "gpu_install_bench",
     "storage_install_bay", "psu_install_bench", "backlight_assembler", "driver_assembler")
_put("press", "hydraulic_cladding_press", "vacuum_lamination_press", "wafer_blank_press", "die_attach_bonder",
     "cpu_socket_press", "ihs_bonder", "gpu_cooler_press", "psu_case_stamper", "hdd_hepa_sealer",
     "nand_stacker", "ssd_case_welder", "case_stamping_press", "dimm_install_press",
     "polarizer_laminator", "lcd_cell_bonder", "keyboard_plate_stamper", "hdmi_termination_press")
_put("smt", "uv_ldi_imager", "solder_paste_printer", "pick_and_place_robot", "chipset_placer",
     "gpu_substrate_smt", "vram_populator", "psu_smt", "ssd_smt", "monitor_controller_smt",
     "keyboard_pcb_smt", "optical_sensor_smt")
_put("bath", "chemical_developing_wash", "acidic_etching_sprayer", "alkaline_stripping_station",
     "electroless_plating_bath", "copper_electroplating_tank", "tin_plating_tank",
     "wire_tinning_bath", "lcd_glass_washer")
_put("tester", "flying_probe_tester", "aoi_verification_station", "ic_tester", "ic_binning_station",
     "motherboard_ict", "gpu_tester", "psu_load_tester", "hdd_servo_writer",
     "ssd_firmware_programmer", "ssd_validator", "desktop_power_on_test", "keyboard_tester",
     "mouse_tester", "hdmi_continuity_tester", "hi_pot_tester", "speaker_anechoic_test")
_put("reactor", "crude_fractionator", "naphtha_steam_cracker", "chlor_alkali_cell", "vcm_reactor",
     "pvc_polymerizer", "abs_polymerizer")
_put("vessel", "plasticizer_unit", "magnetic_sputter", "liquid_crystal_filler")
_put("molder", "ic_transfer_mold", "gpu_shroud_molder", "front_panel_molder", "monitor_bezel_molder",
     "keycap_injection_molder", "mouse_shell_molder", "scroll_wheel_molder", "hdmi_connector_molder",
     "iec_connector_molder", "power_cable_overmolder", "cabinet_molder", "cone_former")
_put("extruder", "copper_wire_drawer", "steel_sheet_roller", "pair_twinner", "hdmi_shield_braider",
     "hdmi_jacket_extruder", "power_conductor_strander", "power_jacket_extruder")

CORE_FACTORY = {
    "crusher": crusher(),
    "industrial_batching_mixer": mixer(),
    "refractory_melting_furnace": furnace(),
    "high_speed_gathering_winder": winder(),
    "industrial_drying_oven": oven_t(),
    "roving_creel_converter": skid(),
    "industrial_textile_weaving_loom": conveyor(),
    "strand_chopping_machinery": cnc(),
}


def template_for(mid):
    kind = CLASS_BY_ID.get(mid)
    if kind:
        return KIND[kind]()
    return {}


# machine list: core factory 8 + every machine recipe in machines/ + pcb machines + catalog ids
machine_ids = set(CORE_FACTORY)
for p in glob.glob(R + "/machines/*.json"):
    machine_ids.add(os.path.basename(p)[:-5])

# ---------------------------------------------------------------- demand
demand = Counter()
MEMO = {}
IN_STACK = set()
CYCLES = []
ATTRIB = []  # (source, item, qty) for per-source iron/copper attribution
import os
DEBUG = os.environ.get("BOM_DEBUG") == "1"


def explode_attrib(source, name, qty):
    before = demand.copy()
    explode(name, qty)
    for k in ("minecraft:iron_ingot", "minecraft:copper_ingot", "tech_revised:graphite_ore",
              "tech_revised:chromite_ore", "minecraft:redstone", "minecraft:charcoal", "minecraft:coal"):
        d = demand[k] - before[k]
        if d:
            ATTRIB.append((source, k, d))


def unit_leaves(name):
    """Counter of leaf (raw) demand for producing ONE unit of `name`. Memoized."""
    if name.startswith("tech_revised:"):
        name = name.split(":", 1)[1]
    if name in MEMO:
        return MEMO[name]
    prod = producer(name)
    if prod is None or name.startswith("minecraft:"):
        leaf = Counter({"tech_revised:" + name if not name.startswith(("minecraft:", "tag:")) else name: 1})
        MEMO[name] = leaf
        return leaf
    if name in IN_STACK:
        if name not in CYCLES:
            CYCLES.append(name)
        return Counter({"tech_revised:" + name: 1})

    rel, d = prod
    res = d["result"]
    count = res.get("count", 1) if isinstance(res, dict) else 1
    ops = 1.0 / max(count, 1)  # fraction of one craft per output unit

    total = Counter()
    IN_STACK.add(name)
    for ing in recipe_inputs(d):
        sub = unit_leaves(ing)
        for k, v in sub.items():
            total[k] += v * ops
    IN_STACK.discard(name)
    MEMO[name] = total
    return total


def show_tree(name, indent=0):
    name = name.split(":")[-1]
    prod = producer(name)
    if prod is None:
        print("  " * indent + f"LEAF {name}")
        return
    rel, d = prod
    res = d["result"]
    count = res.get("count", 1) if isinstance(res, dict) else 1
    print("  " * indent + f"{name}  <- {rel}  (yields {count})")
    for ing in recipe_inputs(d):
        show_tree(ing, indent + 1)


import sys as _sys
if len(_sys.argv) > 2 and _sys.argv[1] == "--trace":
    show_tree(_sys.argv[2])
    print()
    print("leaf cost of 1x", _sys.argv[2], "=>", dict(unit_leaves(_sys.argv[2])))
    _sys.exit(0)


def explode(name, qty):
    for k, v in unit_leaves(name).items():
        demand[k] += v * qty


# --- shopping list: machines once, PC hardware, furniture
for mid in sorted(machine_ids):
    explode_attrib("machine:" + mid, mid, 1)
    struct = CORE_FACTORY.get(mid) or template_for(mid)
    for block, n in struct.items():
        if block == "transport_frame_top":
            explode_attrib("machine:" + mid + ":top", "transport_frame", n)
        else:
            explode_attrib("machine:" + mid + ":" + block, block, n)

for cid, struct in HEAVY.items():
    explode_attrib("structure:" + cid, cid, 1)
    for block, n in struct.items():
        explode_attrib("structure:" + cid + ":" + block, block, n)

FINAL_ITEMS = ["tested_desktop_tower", "tested_monitor", "tested_keyboard", "tested_mouse",
               "tested_speaker", "tested_hdmi_cable", "tested_power_cable",
               "tested_desk", "tested_chair_seat", "tested_chair_backrest", "configurator"]
for f in FINAL_ITEMS:
    explode_attrib("final:" + f, f, 3 if f == "tested_desk" else (2 if f == "tested_speaker" else 1))

if DEBUG:
    if os.environ.get("BOM_DEBUG_MIN"):
        cutoff = int(os.environ["BOM_DEBUG_MIN"])
    else:
        cutoff = 0
    print("== iron attribution ==")
    for s, k, v in sorted(ATTRIB, key=lambda a: -a[2]):
        if k == "minecraft:iron_ingot" and v >= cutoff:
            print(f"{v:8.1f}  {s}")
    print("== charcoal attribution ==")
    for s, k, v in sorted(ATTRIB, key=lambda a: -a[2]):
        if k == "minecraft:charcoal" and v >= cutoff:
            print(f"{v:8.1f}  {s}")
    print("== copper attribution ==")
    for s, k, v in sorted(ATTRIB, key=lambda a: -a[2]):
        if k == "minecraft:copper_ingot" and v >= cutoff:
            print(f"{v:8.1f}  {s}")
    print()

if os.environ.get("BOM_PROFILE") == "1":
    rows = []
    for name in MEMO:
        if name.startswith(("minecraft:", "tag:")):
            continue
        c = MEMO[name].get("minecraft:copper_ingot", 0)
        i = MEMO[name].get("minecraft:iron_ingot", 0)
        if c or i:
            rows.append((max(c, i), name, c, i))
    rows.sort(reverse=True)
    print("== most metal-intensive intermediates (per 1 unit) ==")
    for tot, name, c, i in rows[:20]:
        print(f"cu={c:<7.1f} fe={i:<7.1f}  {name}")
    print()

# ---------------------------------------------------------------- report
ORES = {"graphite_ore", "chromite_ore", "tin_ore", "nickel_ore"}
MINEABLE_VANILLA = {"minecraft:iron_ingot": "iron ore", "minecraft:copper_ingot": "copper ore",
                    "minecraft:gold_ingot": "gold ore", "minecraft:coal": "coal",
                    "minecraft:redstone": "redstone ore", "minecraft:diamond": "diamond ore",
                    "minecraft:quartz": "nether quartz"}
print("=" * 62)
print("RAW RESOURCES FOR ONE COMPLETE PC BUILD (machines + structures + PC)")
print("=" * 62)
raw = {k: v for k, v in sorted(demand.items(), key=lambda kv: -kv[1])}
for k, v in raw.items():
    print(f"{v:7.1f}  {k}")
print("-" * 62)
# ore-block equivalents (rounded up: leftovers carry to the next craft)
def need(key):
    import math
    return math.ceil(demand.get(key, 0))
iron = need("minecraft:iron_ingot")
copper = need("minecraft:copper_ingot")
gold = need("minecraft:gold_ingot") + need("minecraft:gold_nugget") / 9
coal = need("minecraft:coal") + need("minecraft:coal_block") * 9 + need("minecraft:charcoal")
redstone = need("minecraft:redstone")
diamond = need("minecraft:diamond")
print(f"iron ore blocks needed   ~{iron} (raw iron per ingot)")
print(f"copper ore blocks needed ~{copper}")
print(f"gold ore blocks needed   ~{gold:.0f}")
print(f"coal                     ~{coal:.0f}")
print(f"redstone dust            ~{redstone}  diamonds ~{diamond}")
print()
g_ore = demand.get("tech_revised:graphite_ore", 0)
c_ore = demand.get("tech_revised:chromite_ore", 0)
g_dust = demand.get("tech_revised:graphite_dust", 0)
c_dust = demand.get("tech_revised:chromite_dust", 0)
print(f"graphite: {g_ore} ore blocks mined (+{g_dust} dust from coal route)")
print(f"chromite: {c_ore} ore blocks mined (+{c_dust} dust)")
unresolved = {k: v for k, v in demand.items() if k.startswith("tech_revised:")
              and k.split(":")[1] not in ORES and k.split(":")[1] not in FLUID_ITEMS
              and producer(k.split(":")[1]) is None}
fluid_leaves = {k: v for k, v in demand.items() if k.startswith("tech_revised:")
                and k.split(":")[1] in FLUID_ITEMS}
if fluid_leaves:
    print()
    print("fluids (machine outputs, bucketed):")
    for k, v in sorted(fluid_leaves.items()):
        print(f"   {v:6.1f}  {k}")
if CYCLES:
    print()
    print("!! recipe cycles detected (items treated as leaves):", CYCLES)
if unresolved:
    print()
    print("!! mod items with no producer (should be empty):")
    for k, v in sorted(unresolved.items()):
        print(f"   {v:6.1f}  {k}")
