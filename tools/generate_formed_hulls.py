"""Generate spanning formed-hull models for every multiblock except the workstation.

When a machine forms, casing blocks hide and the controller switches to one
model whose cells share a single large texture — same approach as the PC desk.
"""
import json
import math
import os
import struct
import zlib

ROOT = os.path.join("src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", "tech_revised")
BLOCK_TEX = os.path.join(ASSETS, "textures", "block", "multiblock")
BLOCK_MODEL = os.path.join(ASSETS, "models", "block")
BLOCKSTATE = os.path.join(ASSETS, "blockstates")

HULL = (18, 24, 34, 255)
PANEL = (42, 54, 72, 255)
PANEL_HI = (72, 92, 118, 255)
EDGE = (8, 10, 16, 255)
CYAN = (48, 230, 255, 255)
MAGENTA = (190, 70, 255, 255)
AMBER = (255, 190, 70, 255)
WELL = (6, 10, 18, 255)

IO = "io"
SPECIAL = "special"
HULL_CELL = "hull"


class Layout:
    def __init__(self):
        self.cells = {}

    def put(self, x, y, z, kind=HULL_CELL):
        self.cells[(x, y, z)] = kind
        return self

    def fill(self, min_x, max_x, min_y, max_y, min_z, max_z, kind=HULL_CELL):
        for y in range(min_y, max_y + 1):
            for z in range(min_z, max_z + 1):
                for x in range(min_x, max_x + 1):
                    self.put(x, y, z, kind)
        return self

    def shell(self, min_x, max_x, min_y, max_y, min_z, max_z, kind=HULL_CELL):
        for y in range(min_y, max_y + 1):
            for z in range(min_z, max_z + 1):
                for x in range(min_x, max_x + 1):
                    if x in (min_x, max_x) or y in (min_y, max_y) or z in (min_z, max_z):
                        self.put(x, y, z, kind)
        return self

    def ring(self, min_x, max_x, y, min_z, max_z, kind=HULL_CELL):
        for z in range(min_z, max_z + 1):
            for x in range(min_x, max_x + 1):
                if x in (min_x, max_x) or z in (min_z, max_z):
                    self.put(x, y, z, kind)
        return self

    def cylinder(self, min_y, max_y, center_z, min_dist_sq, max_dist_sq, kind=HULL_CELL):
        radius = int(math.ceil(math.sqrt(max_dist_sq)))
        for y in range(min_y, max_y + 1):
            for z in range(center_z - radius, center_z + radius + 1):
                for x in range(-radius, radius + 1):
                    dist = x * x + (z - center_z) * (z - center_z)
                    if min_dist_sq <= dist <= max_dist_sq:
                        self.put(x, y, z, kind)
        return self

    def cylinder_solid(self, y, center_z, max_dist_sq, kind=HULL_CELL):
        return self.cylinder(y, y, center_z, 0, max_dist_sq, kind)

    def hatch(self, x, y, z):
        return self.put(x, y, z, IO)

    def special(self, x, y, z):
        return self.put(x, y, z, SPECIAL)

    def hull_cells(self):
        cells = [(x, y, z) for (x, y, z), kind in self.cells.items() if kind == HULL_CELL]
        if (0, 0, 0) not in self.cells or self.cells[(0, 0, 0)] != IO:
            if (0, 0, 0) not in {(c[0], c[1], c[2]) for c in cells}:
                cells.append((0, 0, 0))
        return cells


def eaf():
    return (Layout()
            .cylinder_solid(0, 2, 5)
            .cylinder(1, 2, 2, 2, 5)
            .cylinder_solid(3, 2, 5)
            .hatch(-1, 0, 1).hatch(1, 0, 1).hatch(-1, 0, 2).hatch(0, 0, 2).hatch(1, 0, 2)
            .special(0, 3, 2))


def coke_oven():
    return (Layout()
            .fill(-3, 3, 0, 0, 0, 2)
            .fill(-3, 3, 3, 3, 0, 2)
            .fill(-3, 3, 1, 2, 0, 0)
            .fill(-3, 3, 1, 2, 2, 2)
            .fill(-3, -3, 1, 2, 1, 1)
            .fill(3, 3, 1, 2, 1, 1)
            .hatch(-1, 0, 1).hatch(1, 0, 1).hatch(0, 0, 2))


def drilling_platform():
    layout = (Layout()
              .fill(-2, 2, 0, 0, 0, 3)
              .special(0, 0, 1)
              .hatch(-2, 0, 3).hatch(2, 0, 3)
              .fill(-1, 1, 6, 6, 1, 2))
    for y in range(1, 6):
        for x, z in ((-2, 0), (2, 0), (-2, 3), (2, 3)):
            layout.put(x, y, z)
    return layout


def oxygen_converter():
    return (Layout()
            .shell(-2, 2, 0, 2, 0, 2)
            .hatch(-1, 0, 0).hatch(1, 0, 0).hatch(-2, 0, 2).hatch(2, 0, 2))


def aod_converter():
    return (Layout()
            .cylinder_solid(0, 2, 5)
            .cylinder(1, 6, 2, 2, 5)
            .cylinder_solid(7, 2, 5)
            .hatch(-2, 0, 2).hatch(2, 0, 2).hatch(1, 0, 0).hatch(-1, 0, 0))


def blast_furnace():
    layout = (Layout()
              .fill(-1, 1, 0, 0, 0, 2)
              .fill(-1, 1, 7, 7, 0, 2)
              .hatch(-1, 0, 0).hatch(1, 0, 0).hatch(0, 7, 1))
    for y in range(1, 7):
        layout.ring(-1, 1, y, 0, 2)
    return layout


def reactor():
    return Layout().cylinder_solid(0, 1, 2).cylinder(1, 2, 1, 1, 2).cylinder_solid(3, 1, 2).hatch(-1, 0, 0)


def vessel():
    return Layout().cylinder_solid(0, 1, 2).cylinder(1, 1, 1, 1, 2).cylinder_solid(2, 1, 2).hatch(-1, 0, 0)


def bath():
    return (Layout()
            .fill(-2, 2, 0, 0, 0, 2)
            .fill(-2, -2, 1, 1, 0, 2)
            .fill(2, 2, 1, 1, 0, 2)
            .fill(-1, 1, 1, 1, 2, 2)
            .hatch(-2, 0, 0))


def press():
    return (Layout()
            .fill(-1, 1, 0, 0, 0, 2)
            .put(-1, 1, 1).put(-1, 2, 1).put(1, 1, 1).put(1, 2, 1).put(0, 2, 1)
            .hatch(-1, 0, 0))


def molder():
    return (Layout()
            .fill(-1, 1, 0, 1, 0, 0)
            .fill(0, 0, 0, 0, 1, 2)
            .put(0, 1, 2)
            .hatch(-1, 0, 0))


def extruder():
    return (Layout()
            .fill(-1, 1, 0, 0, 0, 2)
            .fill(0, 0, 1, 1, 0, 2)
            .put(0, 2, 0)
            .hatch(-1, 0, 0))


def oven():
    return (Layout()
            .fill(-2, 2, 0, 0, 0, 2)
            .fill(-2, -2, 1, 1, 0, 2)
            .fill(2, 2, 1, 1, 0, 2)
            .fill(-2, 2, 2, 2, 0, 2)
            .hatch(-2, 0, 0))


def conveyor():
    return (Layout()
            .fill(-2, 2, 0, 0, 0, 2)
            .fill(-2, 2, 1, 1, 2, 2)
            .hatch(-2, 0, 0))


def smt():
    return (Layout()
            .fill(-2, 2, 0, 0, 0, 2)
            .put(-2, 1, 1).put(2, 1, 1)
            .fill(-2, 2, 2, 2, 1, 1)
            .hatch(-2, 0, 0))


def cnc():
    return (Layout()
            .fill(-1, 1, 0, 0, 0, 2)
            .put(-1, 1, 1).put(1, 1, 1)
            .fill(-1, 1, 2, 2, 1, 1)
            .hatch(-1, 0, 0))


def crusher():
    return (Layout()
            .fill(-1, 1, 0, 1, 0, 2)
            .ring(-1, 1, 2, 0, 2)
            .hatch(-1, 0, 0))


def mixer():
    return (Layout()
            .cylinder_solid(0, 1, 2)
            .cylinder(1, 2, 1, 1, 2)
            .put(0, 3, 1)
            .hatch(-1, 0, 0))


def furnace():
    return (Layout()
            .fill(-2, 2, 0, 0, 0, 2)
            .ring(-2, 2, 1, 0, 2)
            .put(0, 2, 2).put(0, 3, 2)
            .hatch(-2, 0, 0))


def winder():
    return (Layout()
            .fill(-1, 1, 0, 0, 0, 1)
            .put(-1, 1, 1).put(-1, 2, 1).put(1, 1, 1).put(1, 2, 1).put(0, 1, 0)
            .hatch(-1, 0, 0))


def tester():
    return (Layout()
            .fill(-1, 1, 0, 0, 0, 1)
            .put(0, 1, 1).put(0, 2, 1).put(-1, 1, 0).put(1, 1, 0)
            .hatch(-1, 0, 0))


def bench():
    return (Layout()
            .fill(-2, 2, 0, 0, 0, 1)
            .fill(-2, 2, 1, 1, 1, 1)
            .hatch(-2, 0, 0))


def skid():
    return (Layout()
            .fill(-1, 1, 0, 0, 0, 2)
            .fill(-1, 1, 1, 1, 2, 2)
            .hatch(-1, 0, 0))


LAYOUTS = {
    "eaf": eaf,
    "coke_oven": coke_oven,
    "drilling_platform": drilling_platform,
    "oxygen_converter": oxygen_converter,
    "aod_converter": aod_converter,
    "blast_furnace": blast_furnace,
    "reactor": reactor,
    "vessel": vessel,
    "bath": bath,
    "press": press,
    "molder": molder,
    "extruder": extruder,
    "oven": oven,
    "conveyor": conveyor,
    "smt": smt,
    "cnc": cnc,
    "crusher": crusher,
    "mixer": mixer,
    "furnace": furnace,
    "winder": winder,
    "tester": tester,
    "bench": bench,
    "skid": skid,
}

CLASS_BY_ID = {}


def put_class(kind, *ids):
    for machine_id in ids:
        CLASS_BY_ID[machine_id] = kind


put_class("crusher", "crusher")
put_class("mixer", "industrial_batching_mixer", "rubber_compounder")
put_class("furnace", "refractory_melting_furnace")
put_class("winder", "high_speed_gathering_winder", "transformer_winder", "spindle_motor_winder", "voice_coil_winder")
put_class("oven", "industrial_drying_oven", "decontamination_oven", "multi_zone_reflow_oven",
          "cpu_burn_in_chamber", "hdd_burn_in", "powder_coater", "monitor_burn_in", "speaker_magnet_sinter")
put_class("skid", "roving_creel_converter", "platter_polisher", "thermal_paste_dispenser")
put_class("conveyor", "industrial_textile_weaving_loom", "photo_resist_applicator", "soldermask_flooder",
          "uv_pad_exposure_station", "inkjet_silkscreen_printer", "surface_finish_station",
          "psu_final_assembly", "tower_final_assembly", "color_filter_printer", "monitor_final_assembly",
          "membrane_sheet_printer", "keyboard_assembly", "mouse_assembly", "speaker_final_assembly")
put_class("cnc", "strand_chopping_machinery", "industrial_shear", "xray_alignment_drill", "cnc_router", "wafer_dicing_saw")
put_class("bench", "design_engineering_station", "wire_bonding_machine", "expansion_slot_inserter",
          "gpu_assembly_jig", "head_stack_assembler", "case_assembly_jig", "gpu_install_bench",
          "storage_install_bay", "psu_install_bench", "backlight_assembler", "driver_assembler")
put_class("press", "hydraulic_cladding_press", "vacuum_lamination_press", "wafer_blank_press", "die_attach_bonder",
          "cpu_socket_press", "ihs_bonder", "gpu_cooler_press", "psu_case_stamper", "hdd_hepa_sealer",
          "nand_stacker", "ssd_case_welder", "case_stamping_press", "dimm_install_press",
          "polarizer_laminator", "lcd_cell_bonder", "keyboard_plate_stamper", "hdmi_termination_press")
put_class("smt", "uv_ldi_imager", "solder_paste_printer", "pick_and_place_robot", "chipset_placer",
          "gpu_substrate_smt", "vram_populator", "psu_smt", "ssd_smt", "monitor_controller_smt",
          "keyboard_pcb_smt", "optical_sensor_smt")
put_class("bath", "chemical_developing_wash", "acidic_etching_sprayer", "alkaline_stripping_station",
          "electroless_plating_bath", "copper_electroplating_tank", "tin_plating_tank",
          "wire_tinning_bath", "lcd_glass_washer")
put_class("tester", "flying_probe_tester", "aoi_verification_station", "ic_tester", "ic_binning_station",
          "motherboard_ict", "gpu_tester", "psu_load_tester", "hdd_servo_writer",
          "ssd_firmware_programmer", "ssd_validator", "desktop_power_on_test", "keyboard_tester",
          "mouse_tester", "hdmi_continuity_tester", "hi_pot_tester", "speaker_anechoic_test")
put_class("reactor", "crude_fractionator", "naphtha_steam_cracker", "chlor_alkali_cell", "vcm_reactor",
          "pvc_polymerizer", "abs_polymerizer")
put_class("vessel", "plasticizer_unit", "magnetic_sputter", "liquid_crystal_filler")
put_class("molder", "ic_transfer_mold", "gpu_shroud_molder", "front_panel_molder", "monitor_bezel_molder",
          "keycap_injection_molder", "mouse_shell_molder", "scroll_wheel_molder", "hdmi_connector_molder",
          "iec_connector_molder", "power_cable_overmolder", "cabinet_molder", "cone_former")
put_class("extruder", "copper_wire_drawer", "steel_sheet_roller", "pair_twinner", "hdmi_shield_braider",
          "hdmi_jacket_extruder", "power_conductor_strander", "power_jacket_extruder")

HEAVY = {
    "electric_arc_furnace_controller": "eaf",
    "coke_oven_controller": "coke_oven",
    "drilling_platform_controller": "drilling_platform",
    "oxygen_converter_controller": "oxygen_converter",
    "argon_oxygen_decarburization_converter_controller": "aod_converter",
    "blast_furnace_core": "blast_furnace",
}

NO_ACTIVE = {
    "electric_arc_furnace_controller",
    "coke_oven_controller",
    "drilling_platform_controller",
    "oxygen_converter_controller",
    "argon_oxygen_decarburization_converter_controller",
    "blast_furnace_core",
    "industrial_batching_mixer",
    "refractory_melting_furnace",
}

FACING_Y = {"north": 0, "east": 90, "south": 180, "west": 270}


def write_png(path, pixels, w, h):
    def chunk(tag, data):
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    rows = []
    for y in range(h):
        row = bytearray([0])
        for x in range(w):
            r, g, b, a = pixels[y * w + x]
            row.extend([r, g, b])
        rows.append(bytes(row))
    ihdr = struct.pack(">IIBBBBB", w, h, 8, 2, 0, 0, 0)
    data = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(b"".join(rows), 9)) + chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as handle:
        handle.write(data)


def dump(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(obj, handle, indent=2)
        handle.write("\n")


def shade(color, delta):
    return tuple(max(0, min(255, color[i] + delta)) if i < 3 else color[3] for i in range(4))


def setp(pixels, x, y, color, w, h):
    if 0 <= x < w and 0 <= y < h:
        pixels[y * w + x] = color


def hull_texture(w, h, front=False):
    pixels = [HULL] * (w * h)
    for y in range(h):
        for x in range(w):
            mix = (x * 3 + y * 5) % 23
            tone = shade(PANEL, -8 if mix < 4 else (10 if mix > 19 else 0))
            fade = int(12 * (x / max(1, w - 1))) - 6
            tone = shade(tone, fade)
            setp(pixels, x, y, tone, w, h)
    for i in range(2):
        for x in range(w):
            setp(pixels, x, i, CYAN, w, h)
            setp(pixels, x, h - 1 - i, MAGENTA, w, h)
        for y in range(h):
            setp(pixels, i, y, CYAN, w, h)
            setp(pixels, w - 1 - i, y, MAGENTA, w, h)
    if front:
        cx0, cy0 = max(4, w // 2 - 8), max(4, h // 2 - 6)
        cx1, cy1 = min(w - 4, cx0 + 16), min(h - 4, cy0 + 12)
        for y in range(cy0, cy1):
            for x in range(cx0, cx1):
                setp(pixels, x, y, WELL, w, h)
        for x in range(cx0, cx1):
            setp(pixels, x, cy0, CYAN, w, h)
            setp(pixels, x, cy1 - 1, MAGENTA, w, h)
        for y in range(cy0, cy1):
            setp(pixels, cx0, y, AMBER, w, h)
            setp(pixels, cx1 - 1, y, AMBER, w, h)
    return pixels


def uv(u0, v0, u1, v1):
    def clamp(value):
        return max(0.0, min(16.0, round(value, 4)))

    return [clamp(u0), clamp(v0), clamp(u1), clamp(v1)]


def formed_model(cells, layout_id, machine_id, active):
    java_id = HEAVY.get(machine_id, machine_id)
    tex = machine_id if machine_id not in HEAVY else layout_id
    suffix = "_on" if active else ""
    ctrl_front = f"tech_revised:block/{machine_id}_front"
    if active and os.path.exists(os.path.join(ASSETS, "textures", "block", f"{machine_id}_front_on.png")):
        ctrl_front = f"tech_revised:block/{machine_id}_front_on"
    elif not os.path.exists(os.path.join(ASSETS, "textures", "block", f"{machine_id}_front.png")):
        ctrl_front = f"tech_revised:block/machine_side"
    return {
        "loader": "tech_revised:formed_hull",
        "render_type": "minecraft:solid",
        "layout": java_id,
        "textures": {
            "particle": f"tech_revised:block/multiblock/{tex}_side",
            "front": f"tech_revised:block/multiblock/{tex}_front{suffix}",
            "side": f"tech_revised:block/multiblock/{tex}_side",
            "top": f"tech_revised:block/multiblock/{tex}_top",
            "controller_front": ctrl_front,
            "controller_side": "tech_revised:block/machine_side",
            "controller_top": "tech_revised:block/machine_top",
        },
    }


def unformed_model(machine_id, active):
    if machine_id == "crusher":
        return "tech_revised:block/crusher" if active else "tech_revised:block/crusher_idle"
    on_path = os.path.join(BLOCK_MODEL, f"{machine_id}_on.json")
    if active and os.path.exists(on_path):
        return f"tech_revised:block/{machine_id}_on"
    return f"tech_revised:block/{machine_id}"


def blockstate(machine_id, has_active):
    variants = {}
    for facing, rot in FACING_Y.items():
        if has_active:
            keys = [
                (False, False, unformed_model(machine_id, False)),
                (True, False, unformed_model(machine_id, True)),
                (False, True, f"tech_revised:block/{machine_id}_formed"),
                (True, True, f"tech_revised:block/{machine_id}_formed_on"),
            ]
            for active, formed, model in keys:
                key = f"active={'true' if active else 'false'},facing={facing},formed={'true' if formed else 'false'}"
                entry = {"model": model}
                if rot:
                    entry["y"] = rot
                variants[key] = entry
        else:
            for formed, model in (
                (False, unformed_model(machine_id, False)),
                (True, f"tech_revised:block/{machine_id}_formed"),
            ):
                key = f"facing={facing},formed={'true' if formed else 'false'}"
                entry = {"model": model}
                if rot:
                    entry["y"] = rot
                variants[key] = entry
    return {"variants": variants}


def write_hull_textures(layout_id, cells):
    xs = [c[0] for c in cells]
    ys = [c[1] for c in cells]
    zs = [c[2] for c in cells]
    width = (max(xs) - min(xs) + 1) * 16
    height = (max(ys) - min(ys) + 1) * 16
    depth = (max(zs) - min(zs) + 1) * 16
    write_png(os.path.join(BLOCK_TEX, f"{layout_id}_front.png"), hull_texture(width, height, True), width, height)
    write_png(os.path.join(BLOCK_TEX, f"{layout_id}_front_on.png"), hull_texture(width, height, True), width, height)
    write_png(os.path.join(BLOCK_TEX, f"{layout_id}_side.png"), hull_texture(depth, height), depth, height)
    write_png(os.path.join(BLOCK_TEX, f"{layout_id}_top.png"), hull_texture(width, depth), width, depth)


def frame_blockstate(name):
    model = f"tech_revised:block/{name}"
    dump(os.path.join(BLOCKSTATE, f"{name}.json"), {
        "variants": {
            "formed=false": {"model": model},
            "formed=true": {"model": model},
        }
    })


def main():
    os.makedirs(BLOCK_TEX, exist_ok=True)
    hull_cells = {layout_id: factory().hull_cells() for layout_id, factory in LAYOUTS.items()}
    machines = dict(HEAVY)
    machines.update(CLASS_BY_ID)
    written = set()
    for machine_id, layout_id in machines.items():
        cells = hull_cells[layout_id]
        for name in (layout_id, machine_id):
            if name not in written:
                write_hull_textures(name, cells)
                written.add(name)

    for machine_id, layout_id in machines.items():
        cells = hull_cells[layout_id]
        has_active = machine_id not in NO_ACTIVE
        dump(os.path.join(BLOCK_MODEL, f"{machine_id}_formed.json"), formed_model(cells, layout_id, machine_id, False))
        if has_active:
            dump(os.path.join(BLOCK_MODEL, f"{machine_id}_formed_on.json"), formed_model(cells, layout_id, machine_id, True))
        dump(os.path.join(BLOCKSTATE, f"{machine_id}.json"), blockstate(machine_id, has_active))

    for name in (
        "transport_frame",
        "electric_arc_furnace_frame",
        "coke_oven_frame",
        "drilling_platform_frame",
        "oxygen_converter_frame",
        "argon_oxygen_decarburization_converter_frame",
        "universal_frame",
    ):
        frame_blockstate(name)
    print(f"generated formed hulls for {len(machines)} machines")


if __name__ == "__main__":
    main()
