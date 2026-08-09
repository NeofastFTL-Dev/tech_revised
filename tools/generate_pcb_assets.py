import json
import os
import struct
import zlib

ROOT = os.path.join("src", "main", "resources", "assets", "tech_revised")
ITEM_TEX = os.path.join(ROOT, "textures", "item")
BLOCK_TEX = os.path.join(ROOT, "textures", "block")
ITEM_MODEL = os.path.join(ROOT, "models", "item")
BLOCK_MODEL = os.path.join(ROOT, "models", "block")
BLOCKSTATE = os.path.join(ROOT, "blockstates")

for d in (ITEM_TEX, BLOCK_TEX, ITEM_MODEL, BLOCK_MODEL, BLOCKSTATE):
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


def vline(pixels, x, y0, y1, c, w=16):
    for y in range(y0, y1 + 1):
        setp(pixels, x, y, c, w)


def board_base(bg, trace, holes=False, mask=None, silk=None, components=False):
    p = solid(bg)
    rect(p, 1, 1, 14, 14, trace)
    rect(p, 2, 2, 13, 13, bg)
    for i in range(3, 13, 2):
        hline(p, 3, 12, i, trace)
    vline(p, 5, 3, 12, trace)
    vline(p, 10, 3, 12, trace)
    if holes:
        for cx, cy in ((4, 4), (11, 4), (4, 11), (11, 11), (8, 8)):
            setp(p, cx, cy, px(20, 20, 20))
            setp(p, cx + 1, cy, px(20, 20, 20))
    if mask:
        mr, mg, mb, _ = mask
        for y in range(2, 14):
            for x in range(2, 14):
                if (x + y) % 3 != 0:
                    r, g, b, a = p[y * 16 + x]
                    p[y * 16 + x] = px((r + mr * 2) // 3, (g + mg * 2) // 3, (b + mb * 2) // 3)
    if silk:
        hline(p, 3, 12, 2, silk)
        setp(p, 3, 3, silk)
        setp(p, 4, 3, silk)
    if components:
        rect(p, 6, 6, 9, 8, px(30, 30, 30))
        rect(p, 3, 9, 5, 11, px(180, 40, 40))
        rect(p, 10, 9, 12, 11, px(40, 40, 180))
    for i in range(16):
        setp(p, i, 0, px(0, 0, 0, 180))
        setp(p, i, 15, px(0, 0, 0, 180))
        setp(p, 0, i, px(0, 0, 0, 180))
        setp(p, 15, i, px(0, 0, 0, 180))
    return p


def dust_tex(base, spark):
    p = solid(base)
    points = [
        (2, 3), (5, 2), (8, 4), (11, 3), (3, 7), (7, 8),
        (12, 7), (4, 11), (9, 12), (13, 10), (6, 5), (10, 9),
    ]
    for i, (x, y) in enumerate(points):
        setp(p, x, y, spark if i % 2 == 0 else px(min(255, base[0] + 40), min(255, base[1] + 40), min(255, base[2] + 40)))
        setp(p, x + 1, y, spark)
    return p


def paper_doc(accent):
    p = solid(px(240, 240, 230))
    rect(p, 2, 1, 13, 14, px(250, 250, 245))
    for y in range(3, 13, 2):
        hline(p, 4, 11, y, accent)
    rect(p, 10, 1, 13, 4, accent)
    return p


def polymer_blob(c1, c2):
    p = solid(px(0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            if dx * dx + dy * dy < 42:
                p[y * 16 + x] = c1 if (x + y) % 2 == 0 else c2
            if dx * dx + dy * dy < 18:
                p[y * 16 + x] = c2
    return p


def foil_sheet(c):
    p = solid(c)
    for y in range(16):
        for x in range(16):
            shade = 20 if (x // 2 + y // 3) % 2 == 0 else 0
            p[y * 16 + x] = px(min(255, c[0] + shade), min(255, c[1] + shade), min(255, c[2] + shade))
    hline(p, 0, 15, 0, px(255, 255, 255, 100))
    hline(p, 0, 15, 15, px(0, 0, 0, 100))
    return p


def paste_dollop(c):
    p = solid(px(0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            dx, dy = x - 8, y - 9
            if dx * dx + dy * dy * 0.7 < 36:
                p[y * 16 + x] = c
            if dx * dx + dy * dy * 0.7 < 12:
                p[y * 16 + x] = px(min(255, c[0] + 40), min(255, c[1] + 40), min(255, c[2] + 40))
    return p


FR4 = px(45, 110, 55)
FR4_RAW = px(70, 90, 60)
FR4_BAKED = px(35, 95, 50)
COPPER = px(184, 115, 51)
COPPER_BRIGHT = px(210, 140, 70)
TIN = px(180, 180, 190)
GREEN_MASK = px(20, 120, 40)
SILK = px(245, 245, 245)
RESIST = px(90, 20, 110)
RESIST2 = px(140, 40, 160)
ETCHED = px(30, 80, 45)

ITEM_TEXTURES = {
    "cad_layout": paper_doc(px(40, 40, 40)),
    "verified_gerber_file": paper_doc(px(20, 140, 60)),
    "raw_fr4_laminate": board_base(FR4_RAW, px(60, 80, 50)),
    "sheared_fr4_laminate": board_base(FR4_RAW, px(80, 100, 70)),
    "baked_fr4_laminate": board_base(FR4_BAKED, px(50, 70, 40)),
    "copper_foil": foil_sheet(COPPER),
    "copper_clad_laminate": board_base(COPPER, COPPER_BRIGHT),
    "photo_resist_polymer": polymer_blob(RESIST, RESIST2),
    "inner_layer_patterned_board": board_base(COPPER, RESIST),
    "developed_inner_layer_board": board_base(COPPER, px(30, 30, 40)),
    "sodium_carbonate_dust": dust_tex(px(230, 230, 240), px(255, 255, 255)),
    "cupric_chloride_dust": dust_tex(px(30, 140, 90), px(80, 200, 140)),
    "sodium_hydroxide_dust": dust_tex(px(245, 245, 250), px(200, 220, 255)),
    "etched_inner_layer_board": board_base(ETCHED, COPPER),
    "stripped_inner_layer_board": board_base(FR4, COPPER),
    "prepreg_fabric": foil_sheet(px(200, 180, 120)),
    "multi_layer_stackup": board_base(FR4, COPPER),
    "laminated_pcb_core": board_base(FR4, COPPER_BRIGHT),
    "drilled_pcb": board_base(FR4, COPPER, holes=True),
    "electroless_copper_coated_pcb": board_base(COPPER_BRIGHT, COPPER, holes=True),
    "patterned_outer_layer_pcb": board_base(COPPER_BRIGHT, RESIST, holes=True),
    "electroplated_pcb": board_base(COPPER_BRIGHT, px(220, 160, 80), holes=True),
    "tin_plating_dust": dust_tex(TIN, px(230, 230, 240)),
    "tin_plated_pcb": board_base(TIN, px(200, 200, 210), holes=True),
    "final_etched_pcb": board_base(FR4, COPPER, holes=True),
    "tin_stripped_pcb": board_base(FR4, COPPER_BRIGHT, holes=True),
    "soldermask_resin": polymer_blob(GREEN_MASK, px(40, 160, 60)),
    "masked_pcb": board_base(FR4, COPPER, holes=True, mask=GREEN_MASK),
    "silkscreened_pcb": board_base(FR4, COPPER, holes=True, mask=GREEN_MASK, silk=SILK),
    "finished_pcb_panel": board_base(FR4, COPPER, holes=True, mask=GREEN_MASK, silk=SILK),
    "tested_pcb_panel": board_base(FR4, COPPER, holes=True, mask=GREEN_MASK, silk=px(255, 220, 40)),
    "routed_pcb": board_base(FR4, COPPER, holes=True, mask=GREEN_MASK, silk=SILK),
    "solder_paste": paste_dollop(px(150, 150, 160)),
    "stencil_printed_pcb": board_base(FR4, COPPER, holes=True, mask=GREEN_MASK, silk=SILK),
    "placed_pcba": board_base(FR4, COPPER, holes=True, mask=GREEN_MASK, silk=SILK, components=True),
    "reflowed_pcba": board_base(FR4, COPPER, holes=True, mask=GREEN_MASK, silk=SILK, components=True),
    "final_pcba": board_base(FR4, COPPER, holes=True, mask=GREEN_MASK, silk=SILK, components=True),
}

for y in range(1, 4):
    for x in range(12, 15):
        setp(ITEM_TEXTURES["final_pcba"], x, y, px(255, 200, 40))

for name, pix in ITEM_TEXTURES.items():
    write_png(os.path.join(ITEM_TEX, f"{name}.png"), pix)
    model = {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"tech_revised:item/{name}"},
    }
    with open(os.path.join(ITEM_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)
        f.write("\n")

STEEL = px(96, 96, 104)
STEEL_DK = px(70, 70, 78)
STEEL_LT = px(130, 130, 140)


def machine_side():
    p = solid(STEEL)
    for y in range(16):
        for x in range(16):
            if x in (0, 15) or y in (0, 15):
                p[y * 16 + x] = STEEL_DK
            elif (x + y) % 4 == 0:
                p[y * 16 + x] = STEEL_LT
    for cx, cy in ((2, 2), (13, 2), (2, 13), (13, 13)):
        setp(p, cx, cy, STEEL_LT)
    return p


def machine_top():
    p = solid(STEEL_DK)
    rect(p, 2, 2, 13, 13, STEEL)
    rect(p, 5, 5, 10, 10, px(40, 40, 48))
    return p


def machine_front(accent, symbol="default"):
    p = machine_side()
    rect(p, 3, 3, 12, 12, px(40, 44, 52))
    rect(p, 4, 4, 11, 11, accent)
    setp(p, 12, 3, px(40, 220, 80))
    setp(p, 13, 3, px(40, 220, 80))
    if symbol == "screen":
        rect(p, 5, 5, 10, 9, px(30, 80, 120))
        hline(p, 5, 10, 6, px(80, 200, 255))
    elif symbol == "nozzle":
        rect(p, 7, 8, 8, 12, STEEL_LT)
        rect(p, 6, 12, 9, 13, accent)
    elif symbol == "tank":
        rect(p, 5, 5, 10, 11, px(accent[0] // 2, accent[1] // 2, accent[2] // 2))
        hline(p, 5, 10, 8, px(200, 200, 220, 120))
    elif symbol == "drill":
        rect(p, 7, 4, 8, 11, STEEL_LT)
        setp(p, 7, 12, px(20, 20, 20))
        setp(p, 8, 12, px(20, 20, 20))
    elif symbol == "oven":
        rect(p, 5, 6, 10, 11, px(20, 20, 24))
        for x in range(5, 11):
            setp(p, x, 8, px(255, 120, 40))
    elif symbol == "robot":
        rect(p, 6, 4, 9, 6, STEEL_LT)
        rect(p, 7, 6, 8, 10, STEEL)
        rect(p, 5, 10, 10, 11, accent)
    elif symbol == "probe":
        vline(p, 6, 5, 11, STEEL_LT)
        vline(p, 9, 5, 11, STEEL_LT)
        setp(p, 6, 12, accent)
        setp(p, 9, 12, accent)
    elif symbol == "print":
        rect(p, 4, 5, 11, 7, STEEL_LT)
        hline(p, 4, 11, 9, accent)
        hline(p, 4, 11, 10, accent)
    return p


def machine_front_active(accent, symbol="default"):
    p = machine_front(accent, symbol)
    setp(p, 12, 3, px(180, 255, 180))
    setp(p, 13, 3, px(180, 255, 180))
    setp(p, 12, 4, px(80, 255, 120))
    for i in range(3, 13):
        r, g, b, a = p[3 * 16 + i]
        p[3 * 16 + i] = px(min(255, r + 30), min(255, g + 40), min(255, b + 20))
    return p


MACHINES = {
    "design_engineering_station": (px(60, 140, 220), "screen"),
    "industrial_shear": (px(180, 180, 190), "default"),
    "decontamination_oven": (px(220, 100, 40), "oven"),
    "hydraulic_cladding_press": (px(160, 90, 50), "default"),
    "photo_resist_applicator": (px(140, 40, 160), "nozzle"),
    "uv_ldi_imager": (px(160, 80, 255), "screen"),
    "chemical_developing_wash": (px(80, 180, 220), "tank"),
    "acidic_etching_sprayer": (px(40, 180, 100), "nozzle"),
    "alkaline_stripping_station": (px(220, 220, 240), "tank"),
    "vacuum_lamination_press": (px(100, 100, 120), "default"),
    "xray_alignment_drill": (px(200, 200, 80), "drill"),
    "electroless_plating_bath": (px(200, 130, 60), "tank"),
    "copper_electroplating_tank": (px(210, 140, 70), "tank"),
    "tin_plating_tank": (px(190, 190, 200), "tank"),
    "soldermask_flooder": (px(30, 140, 50), "nozzle"),
    "uv_pad_exposure_station": (px(180, 100, 255), "screen"),
    "inkjet_silkscreen_printer": (px(240, 240, 240), "print"),
    "surface_finish_station": (px(220, 180, 60), "default"),
    "flying_probe_tester": (px(80, 200, 220), "probe"),
    "cnc_router": (px(120, 120, 130), "drill"),
    "solder_paste_printer": (px(160, 160, 170), "print"),
    "pick_and_place_robot": (px(220, 60, 60), "robot"),
    "multi_zone_reflow_oven": (px(255, 100, 30), "oven"),
    "aoi_verification_station": (px(40, 200, 120), "screen"),
}

write_png(os.path.join(BLOCK_TEX, "pcb_machine_side.png"), machine_side())
write_png(os.path.join(BLOCK_TEX, "pcb_machine_top.png"), machine_top())

for name, (accent, symbol) in MACHINES.items():
    front = machine_front(accent, symbol)
    front_on = machine_front_active(accent, symbol)
    write_png(os.path.join(BLOCK_TEX, f"{name}_front.png"), front)
    write_png(os.path.join(BLOCK_TEX, f"{name}_front_on.png"), front_on)

    model_idle = {
        "parent": "minecraft:block/orientable",
        "textures": {
            "top": "tech_revised:block/pcb_machine_top",
            "front": f"tech_revised:block/{name}_front",
            "side": "tech_revised:block/pcb_machine_side",
        },
    }
    model_on = {
        "parent": "minecraft:block/orientable",
        "textures": {
            "top": "tech_revised:block/pcb_machine_top",
            "front": f"tech_revised:block/{name}_front_on",
            "side": "tech_revised:block/pcb_machine_side",
        },
    }
    with open(os.path.join(BLOCK_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump(model_idle, f, indent=2)
        f.write("\n")
    with open(os.path.join(BLOCK_MODEL, f"{name}_on.json"), "w", encoding="utf-8") as f:
        json.dump(model_on, f, indent=2)
        f.write("\n")

    variants = {}
    for facing, rot_y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for active, suffix in ((False, ""), (True, "_on")):
            key = f"facing={facing},active={str(active).lower()}"
            entry = {"model": f"tech_revised:block/{name}{suffix}"}
            if rot_y:
                entry["y"] = rot_y
            variants[key] = entry
    with open(os.path.join(BLOCKSTATE, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump({"variants": variants}, f, indent=2)
        f.write("\n")

    with open(os.path.join(ITEM_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump({"parent": f"tech_revised:block/{name}"}, f, indent=2)
        f.write("\n")

for fluid, item in [
    ("sodium_carbonate_solution", "sodium_carbonate_solution_bucket"),
    ("cupric_chloride_solution", "cupric_chloride_solution_bucket"),
    ("sodium_hydroxide_solution", "sodium_hydroxide_solution_bucket"),
]:
    model = {
        "parent": "forge:item/bucket",
        "loader": "forge:fluid_container",
        "fluid": f"tech_revised:{fluid}",
    }
    with open(os.path.join(ITEM_MODEL, f"{item}.json"), "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)
        f.write("\n")

print(f"Wrote {len(ITEM_TEXTURES)} item textures/models")
print(f"Wrote {len(MACHINES)} machine block assets")
print("Done")
