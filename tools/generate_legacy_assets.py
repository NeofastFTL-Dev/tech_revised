"""Generate procedural 16x16 textures + models for all non-PCB legacy assets."""
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
    return (int(r), int(g), int(b), int(a))


def solid(c, w=16, h=16):
    return [c] * (w * h)


def setp(p, x, y, c, w=16):
    if 0 <= x < w and 0 <= y < w:
        p[y * w + x] = c


def rect(p, x0, y0, x1, y1, c, w=16):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            setp(p, x, y, c, w)


def hline(p, x0, x1, y, c, w=16):
    for x in range(x0, x1 + 1):
        setp(p, x, y, c, w)


def vline(p, x, y0, y1, c, w=16):
    for y in range(y0, y1 + 1):
        setp(p, x, y, c, w)


def shade(c, d):
    return px(max(0, min(255, c[0] + d)), max(0, min(255, c[1] + d)), max(0, min(255, c[2] + d)), c[3] if len(c) > 3 else 255)


def dust(base, spark):
    p = solid(base)
    pts = [(2, 3), (5, 2), (8, 4), (11, 3), (3, 7), (7, 8), (12, 7), (4, 11), (9, 12), (13, 10), (6, 5), (10, 9), (1, 12), (14, 5)]
    for i, (x, y) in enumerate(pts):
        setp(p, x, y, spark if i % 2 == 0 else shade(base, 35))
        setp(p, x + 1, y, spark)
    return p


def chunk_item(base, edge):
    p = solid(px(0, 0, 0, 0))
    rect(p, 3, 4, 12, 12, base)
    rect(p, 4, 3, 11, 4, base)
    rect(p, 5, 5, 10, 10, shade(base, 25))
    hline(p, 3, 12, 4, edge)
    vline(p, 3, 4, 12, edge)
    return p


def plate(base):
    p = solid(shade(base, -20))
    rect(p, 1, 3, 14, 12, base)
    for y in range(4, 12, 2):
        hline(p, 2, 13, y, shade(base, 15))
    hline(p, 1, 14, 3, shade(base, 40))
    hline(p, 1, 14, 12, shade(base, -30))
    return p


def filament(base, highlight):
    p = solid(px(0, 0, 0, 0))
    for i, y in enumerate(range(2, 14)):
        x = 4 + (i % 5)
        setp(p, x, y, base)
        setp(p, x + 1, y, highlight)
        setp(p, x + 6, y, base)
        setp(p, x + 7, y, highlight)
    return p


def spool(core, thread):
    p = solid(px(0, 0, 0, 0))
    rect(p, 3, 2, 12, 4, core)
    rect(p, 3, 11, 12, 13, core)
    rect(p, 4, 4, 11, 11, thread)
    for y in range(5, 11):
        hline(p, 5, 10, y, shade(thread, 20 if y % 2 == 0 else -10))
    vline(p, 7, 2, 13, shade(core, -30))
    vline(p, 8, 2, 13, shade(core, -30))
    return p


def fabric(base, weave):
    p = solid(base)
    for y in range(16):
        for x in range(16):
            if (x + y) % 2 == 0:
                p[y * 16 + x] = weave
            if x in (0, 15) or y in (0, 15):
                p[y * 16 + x] = shade(base, -40)
    return p


def mat(base):
    p = solid(base)
    for y in range(2, 14):
        for x in range(2, 14):
            if (x * 3 + y * 5) % 4 == 0:
                setp(p, x, y, shade(base, 30))
            if (x + y * 2) % 5 == 0:
                setp(p, x, y, shade(base, -20))
    rect(p, 0, 0, 15, 15, shade(base, -40))
    rect(p, 1, 1, 14, 14, base)
    return p


def bottle(liquid):
    p = solid(px(0, 0, 0, 0))
    rect(p, 6, 1, 9, 3, px(200, 200, 210))
    rect(p, 4, 3, 11, 14, px(180, 200, 220, 180))
    rect(p, 5, 6, 10, 13, liquid)
    hline(p, 5, 10, 6, shade(liquid, 40))
    return p


def ingot_gray():
    """Grayscale ingot for item tinting."""
    p = solid(px(0, 0, 0, 0))
    body = px(160, 160, 160)
    rect(p, 2, 5, 13, 11, body)
    rect(p, 3, 4, 12, 5, shade(body, 30))
    rect(p, 3, 11, 12, 12, shade(body, -25))
    hline(p, 2, 13, 5, shade(body, 45))
    hline(p, 2, 13, 11, shade(body, -40))
    for x in range(4, 13, 3):
        vline(p, x, 6, 10, shade(body, 15))
    return p


def nugget_gray():
    p = solid(px(0, 0, 0, 0))
    body = px(170, 170, 170)
    rect(p, 5, 5, 10, 10, body)
    rect(p, 6, 4, 9, 5, shade(body, 25))
    rect(p, 6, 10, 9, 11, shade(body, -25))
    setp(p, 5, 5, shade(body, 40))
    setp(p, 10, 10, shade(body, -30))
    return p


def metal_block_gray():
    """Grayscale metal block for block tinting."""
    p = solid(px(140, 140, 140))
    for y in range(16):
        for x in range(16):
            if (x // 4 + y // 4) % 2 == 0:
                p[y * 16 + x] = px(155, 155, 155)
            if x in (0, 15) or y in (0, 15):
                p[y * 16 + x] = px(90, 90, 90)
            elif x in (1, 14) or y in (1, 14):
                p[y * 16 + x] = px(120, 120, 120)
    # panel lines
    hline(p, 2, 13, 5, px(110, 110, 110))
    hline(p, 2, 13, 10, px(110, 110, 110))
    vline(p, 5, 2, 13, px(110, 110, 110))
    vline(p, 10, 2, 13, px(110, 110, 110))
    return p


def ore_block(stone, mineral):
    p = solid(stone)
    for y in range(16):
        for x in range(16):
            n = (x * 7 + y * 13) % 11
            if n < 2:
                p[y * 16 + x] = shade(stone, -15)
            elif n > 9:
                p[y * 16 + x] = shade(stone, 12)
    for cx, cy in ((3, 4), (8, 3), (12, 6), (5, 9), (10, 11), (4, 13), (13, 12), (7, 7), (11, 9)):
        setp(p, cx, cy, mineral)
        setp(p, cx + 1, cy, shade(mineral, 20))
        setp(p, cx, cy + 1, shade(mineral, -15))
    return p


def sand_item(base, grain):
    p = solid(base)
    for i, (x, y) in enumerate([(2, 4), (5, 3), (9, 5), (12, 4), (3, 8), (7, 9), (11, 8), (4, 12), (8, 13), (13, 11), (6, 6), (10, 10)]):
        setp(p, x, y, grain if i % 2 == 0 else shade(base, 25))
    return p


def coke():
    p = solid(px(25, 25, 28))
    rect(p, 3, 4, 12, 12, px(35, 35, 40))
    for x, y in ((5, 6), (9, 5), (7, 9), (10, 10), (4, 10)):
        setp(p, x, y, px(60, 60, 65))
        setp(p, x + 1, y, px(15, 15, 18))
    return p


def electrode():
    p = solid(px(0, 0, 0, 0))
    # angled graphite rod
    for i in range(12):
        x, y = 3 + i // 2, 2 + i
        setp(p, x, y, px(40, 40, 45))
        setp(p, x + 1, y, px(70, 70, 78))
        setp(p, x + 2, y, px(35, 35, 40))
    rect(p, 2, 1, 5, 3, px(90, 90, 100))
    return p


def configurator():
    p = solid(px(0, 0, 0, 0))
    rect(p, 3, 2, 12, 13, px(50, 55, 65))
    rect(p, 4, 3, 11, 9, px(30, 120, 180))
    hline(p, 4, 11, 5, px(80, 200, 255))
    rect(p, 5, 10, 7, 12, px(40, 200, 80))
    rect(p, 9, 10, 11, 12, px(200, 60, 60))
    return p


# ---------- Items ----------
ITEMS = {
    "graphite_dust": dust(px(40, 40, 45), px(90, 90, 100)),
    "chromium_dust": dust(px(160, 170, 185), px(220, 230, 240)),
    "nickel_dust": dust(px(170, 175, 160), px(220, 225, 200)),
    "chromite_dust": dust(px(90, 70, 55), px(140, 110, 80)),
    "carbon_chunk": chunk_item(px(35, 35, 40), px(15, 15, 18)),
    "stainless_steel_blend": dust(px(180, 185, 190), px(100, 110, 90)),
    "silica_sand": sand_item(px(220, 210, 170), px(245, 235, 200)),
    "limestone_dust": dust(px(210, 205, 185), px(240, 235, 220)),
    "dolomite_dust": dust(px(200, 195, 190), px(160, 150, 140)),
    "soda_ash": dust(px(235, 235, 240), px(255, 255, 255)),
    "clay_dust": dust(px(160, 120, 90), px(200, 160, 120)),
    "raw_glass_batch": dust(px(180, 190, 200), px(120, 140, 160)),
    "platinum_alloy_bushing_plate": plate(px(200, 205, 215)),
    "chemical_lubricant": bottle(px(80, 160, 60)),
    "glass_filament": filament(px(180, 210, 230), px(230, 245, 255)),
    "sized_glass_filament": filament(px(160, 200, 170), px(210, 240, 200)),
    "glass_spool": spool(px(90, 90, 100), px(180, 210, 230)),
    "dried_glass_spool": spool(px(90, 90, 100), px(200, 220, 230)),
    "glass_roving": filament(px(170, 200, 220), px(220, 235, 245)),
    "fiberglass_fabric": fabric(px(190, 210, 220), px(160, 185, 200)),
    "chopped_glass_strand": dust(px(200, 220, 230), px(240, 250, 255)),
    "chopped_strand_mat": mat(px(180, 200, 210)),
    "coal_coke": coke(),
    "electrode": electrode(),
    "configurator": configurator(),
    "template_tinted_ingot": ingot_gray(),
    "template_tinted_nugget": nugget_gray(),
}

for name, pix in ITEMS.items():
    write_png(os.path.join(ITEM_TEX, f"{name}.png"), pix)
    if name.startswith("template_tinted"):
        model = {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"tech_revised:item/{name}"},
        }
    else:
        model = {
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"tech_revised:item/{name}"},
        }
    with open(os.path.join(ITEM_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)
        f.write("\n")

# Tinted metal items keep parent templates
for name in ("steel_ingot", "stainless_steel_ingot", "chromite_ingot", "ferrochromium_ingot"):
    with open(os.path.join(ITEM_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump({"parent": "tech_revised:item/template_tinted_ingot"}, f, indent=2)
        f.write("\n")
for name in ("steel_nugget", "stainless_steel_nugget", "chromite_nugget", "ferrochromium_nugget"):
    with open(os.path.join(ITEM_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump({"parent": "tech_revised:item/template_tinted_nugget"}, f, indent=2)
        f.write("\n")

print(f"Items: {len(ITEMS)}")

# ---------- Shared machine steel ----------
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
    elif symbol == "mixer":
        rect(p, 5, 5, 10, 10, shade(accent, -40))
        for a in range(4, 12):
            setp(p, a, a, STEEL_LT)
            setp(p, a, 15 - a, STEEL_LT)
    elif symbol == "loom":
        for x in range(4, 12, 2):
            vline(p, x, 4, 11, STEEL_LT)
        hline(p, 4, 11, 7, accent)
    elif symbol == "winder":
        rect(p, 5, 5, 10, 10, STEEL_DK)
        for y in range(6, 10):
            hline(p, 6, 9, y, accent)
    elif symbol == "controller":
        rect(p, 5, 4, 10, 8, px(20, 30, 40))
        hline(p, 5, 10, 5, px(80, 220, 255))
        setp(p, 6, 10, px(40, 220, 80))
        setp(p, 8, 10, px(220, 60, 60))
        setp(p, 10, 10, px(220, 180, 40))
    elif symbol == "heater":
        rect(p, 5, 5, 10, 11, px(30, 20, 15))
        for y in range(6, 11):
            hline(p, 6, 9, y, px(255, 80 + y * 10, 20))
    elif symbol == "hatch":
        rect(p, 4, 4, 11, 11, STEEL_DK)
        rect(p, 6, 6, 9, 9, accent)
    elif symbol == "bus_in":
        rect(p, 5, 5, 10, 10, px(40, 80, 40))
        hline(p, 6, 9, 7, px(80, 220, 80))
        hline(p, 6, 9, 8, px(80, 220, 80))
    elif symbol == "bus_out":
        rect(p, 5, 5, 10, 10, px(80, 40, 40))
        hline(p, 6, 9, 7, px(220, 80, 80))
        hline(p, 6, 9, 8, px(220, 80, 80))
    elif symbol == "fluid":
        rect(p, 5, 5, 10, 11, px(30, 60, 100))
        hline(p, 5, 10, 9, px(80, 160, 220))
    elif symbol == "energy":
        rect(p, 5, 5, 10, 10, px(40, 30, 10))
        vline(p, 7, 6, 9, px(255, 220, 40))
        vline(p, 8, 6, 9, px(255, 220, 40))
    elif symbol == "frame":
        rect(p, 2, 2, 13, 13, STEEL_DK)
        rect(p, 4, 4, 11, 11, px(0, 0, 0, 0))
        for y in range(4, 12):
            for x in range(4, 12):
                p[y * 16 + x] = px(0, 0, 0, 0) if (x + y) % 2 == 0 else STEEL
    elif symbol == "vm":
        rect(p, 3, 3, 12, 10, px(20, 30, 50))
        rect(p, 4, 4, 11, 9, px(40, 90, 160))
        hline(p, 4, 11, 6, px(100, 200, 255))
        rect(p, 5, 11, 10, 13, STEEL)
    elif symbol == "chopper":
        rect(p, 4, 6, 11, 9, STEEL_LT)
        for x in range(4, 12, 2):
            setp(p, x, 7, accent)
    return p


def machine_front_on(accent, symbol="default"):
    p = machine_front(accent, symbol)
    setp(p, 12, 3, px(180, 255, 180))
    setp(p, 13, 3, px(180, 255, 180))
    setp(p, 12, 4, px(80, 255, 120))
    for i in range(3, 13):
        r, g, b, a = p[3 * 16 + i]
        p[3 * 16 + i] = px(min(255, r + 30), min(255, g + 40), min(255, b + 20))
    return p


write_png(os.path.join(BLOCK_TEX, "machine_side.png"), machine_side())
write_png(os.path.join(BLOCK_TEX, "machine_top.png"), machine_top())
# keep pcb shared names in sync
write_png(os.path.join(BLOCK_TEX, "pcb_machine_side.png"), machine_side())
write_png(os.path.join(BLOCK_TEX, "pcb_machine_top.png"), machine_top())

# Improved steel frame textures
write_png(os.path.join(BLOCK_TEX, "steel_frame_normal.png"), machine_side())
write_png(os.path.join(BLOCK_TEX, "steel_frame.png"), machine_side())
frame_ctm = machine_side()
for y in range(16):
    for x in range(16):
        if 4 <= x <= 11 and 4 <= y <= 11:
            frame_ctm[y * 16 + x] = px(50, 50, 58)
write_png(os.path.join(BLOCK_TEX, "steel_frame_ctm.png"), frame_ctm)

# Metal block grayscale for tinting
metal = metal_block_gray()
write_png(os.path.join(BLOCK_TEX, "metal_block.png"), metal)
for name in ("steel_block", "stainless_steel_block", "chromite_block", "ferrochromium_block"):
    write_png(os.path.join(BLOCK_TEX, f"{name}.png"), metal)
    model = {
        "parent": "minecraft:block/block",
        "textures": {"all": f"tech_revised:block/{name}", "particle": f"tech_revised:block/{name}"},
        "elements": [{
            "from": [0, 0, 0],
            "to": [16, 16, 16],
            "faces": {
                face: {"uv": [0, 0, 16, 16], "texture": "#all", "cullface": face, "tintindex": 0}
                for face in ("down", "up", "north", "south", "west", "east")
            },
        }],
    }
    with open(os.path.join(BLOCK_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)
        f.write("\n")
    with open(os.path.join(ITEM_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump({"parent": f"tech_revised:block/{name}"}, f, indent=2)
        f.write("\n")
    with open(os.path.join(BLOCKSTATE, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump({"variants": {"": {"model": f"tech_revised:block/{name}"}}}, f, indent=2)
        f.write("\n")

# Ores
write_png(os.path.join(BLOCK_TEX, "graphite_ore.png"), ore_block(px(90, 90, 95), px(30, 30, 35)))
write_png(os.path.join(BLOCK_TEX, "chromite_ore.png"), ore_block(px(100, 95, 90), px(80, 55, 40)))
for name in ("graphite_ore", "chromite_ore"):
    with open(os.path.join(BLOCK_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump({
            "parent": "minecraft:block/cube_all",
            "textures": {"all": f"tech_revised:block/{name}"},
        }, f, indent=2)
        f.write("\n")
    with open(os.path.join(ITEM_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump({"parent": f"tech_revised:block/{name}"}, f, indent=2)
        f.write("\n")
    with open(os.path.join(BLOCKSTATE, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump({"variants": {"": {"model": f"tech_revised:block/{name}"}}}, f, indent=2)
        f.write("\n")

# Machines that use GenericIndustrialMachineBlock (active only, no facing currently)
GENERIC_ACTIVE = {
    "industrial_drying_oven": (px(220, 120, 40), "oven"),
    "high_speed_gathering_winder": (px(80, 160, 200), "winder"),
    "roving_creel_converter": (px(120, 140, 180), "winder"),
    "industrial_textile_weaving_loom": (px(180, 160, 100), "loom"),
    "strand_chopping_machinery": (px(200, 80, 80), "chopper"),
}

# Controllers / process machines (static or active where applicable)
STATIC_MACHINES = {
    "industrial_batching_mixer": (px(100, 160, 80), "mixer"),
    "refractory_melting_furnace": (px(220, 80, 30), "oven"),
    "chemical_sizing_applicator": (px(80, 180, 120), "nozzle"),
    "blast_furnace_core": (px(180, 60, 30), "oven"),
    "electric_arc_furnace_controller": (px(80, 160, 255), "controller"),
    "electric_arc_furnace_heater": (px(255, 100, 20), "heater"),
    "electric_arc_furnace_input_bus": (px(60, 180, 60), "bus_in"),
    "electric_arc_furnace_output_bus": (px(180, 60, 60), "bus_out"),
    "electric_arc_furnace_fluid_input_bus": (px(60, 120, 200), "fluid"),
    "electric_arc_furnace_fluid_output_bus": (px(40, 90, 180), "fluid"),
    "electric_arc_furnace_energy_input_hatch": (px(255, 200, 40), "energy"),
    "electric_arc_furnace_frame": (px(100, 100, 110), "frame"),
    "coke_oven_controller": (px(90, 70, 50), "controller"),
    "coke_oven_frame": (px(100, 100, 110), "frame"),
    "drilling_platform_controller": (px(60, 140, 180), "controller"),
    "drilling_platform_frame": (px(100, 100, 110), "frame"),
    "drilling_platform_drill_head": (px(200, 180, 60), "drill"),
    "argon_oxygen_decarburization_converter_controller": (px(180, 100, 220), "controller"),
    "argon_oxygen_decarburization_converter_frame": (px(100, 100, 110), "frame"),
    "oxygen_converter_controller": (px(100, 180, 255), "controller"),
    "oxygen_converter_frame": (px(100, 100, 110), "frame"),
    "windows_7_vm_block": (px(40, 90, 160), "vm"),
    "crusher": (px(140, 140, 150), "mixer"),
}

# Also regenerate bus/hatch dedicated textures used by existing models
write_png(os.path.join(BLOCK_TEX, "input_bus.png"), machine_front(px(60, 180, 60), "bus_in"))
write_png(os.path.join(BLOCK_TEX, "output_bus.png"), machine_front(px(180, 60, 60), "bus_out"))
write_png(os.path.join(BLOCK_TEX, "fluid_input_bus.png"), machine_front(px(60, 120, 200), "fluid"))
write_png(os.path.join(BLOCK_TEX, "fluid_output_bus.png"), machine_front(px(40, 90, 180), "fluid"))
write_png(os.path.join(BLOCK_TEX, "energy_input_hatch.png"), machine_front(px(255, 200, 40), "energy"))
write_png(os.path.join(BLOCK_TEX, "aod_controller.png"), machine_front(px(180, 100, 220), "controller"))
write_png(os.path.join(BLOCK_TEX, "crusher_front.png"), machine_front(px(140, 140, 150), "mixer"))
write_png(os.path.join(BLOCK_TEX, "crusher_front_on.png"), machine_front_on(px(140, 140, 150), "mixer"))


def write_orientable(name, accent, symbol, active_variants=False):
    front = machine_front(accent, symbol)
    front_on = machine_front_on(accent, symbol)
    write_png(os.path.join(BLOCK_TEX, f"{name}_front.png"), front)
    write_png(os.path.join(BLOCK_TEX, f"{name}_front_on.png"), front_on)
    # also write plain name for cube-style fallbacks
    write_png(os.path.join(BLOCK_TEX, f"{name}.png"), front)

    model_idle = {
        "parent": "minecraft:block/orientable",
        "textures": {
            "top": "tech_revised:block/machine_top",
            "front": f"tech_revised:block/{name}_front",
            "side": "tech_revised:block/machine_side",
        },
    }
    model_on = {
        "parent": "minecraft:block/orientable",
        "textures": {
            "top": "tech_revised:block/machine_top",
            "front": f"tech_revised:block/{name}_front_on",
            "side": "tech_revised:block/machine_side",
        },
    }
    with open(os.path.join(BLOCK_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump(model_idle, f, indent=2)
        f.write("\n")
    with open(os.path.join(BLOCK_MODEL, f"{name}_on.json"), "w", encoding="utf-8") as f:
        json.dump(model_on, f, indent=2)
        f.write("\n")

    if active_variants:
        variants = {
            "active=false": {"model": f"tech_revised:block/{name}"},
            "active=true": {"model": f"tech_revised:block/{name}_on"},
        }
    else:
        variants = {"": {"model": f"tech_revised:block/{name}"}}
    with open(os.path.join(BLOCKSTATE, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump({"variants": variants}, f, indent=2)
        f.write("\n")

    with open(os.path.join(ITEM_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump({"parent": f"tech_revised:block/{name}"}, f, indent=2)
        f.write("\n")


for name, (accent, symbol) in GENERIC_ACTIVE.items():
    write_orientable(name, accent, symbol, active_variants=True)

for name, (accent, symbol) in STATIC_MACHINES.items():
    # crusher already has active blockstate pattern via crusher/crusher_idle
    if name == "crusher":
        front = machine_front(accent, symbol)
        front_on = machine_front_on(accent, symbol)
        write_png(os.path.join(BLOCK_TEX, "crusher_front.png"), front_on)
        write_png(os.path.join(BLOCK_TEX, "crusher_front_idle.png"), front)
        # Keep existing crusher model structure if present; update textures via crusher.json
        crusher_on = {
            "parent": "block/cube",
            "textures": {
                "down": "tech_revised:block/machine_top",
                "up": "tech_revised:block/machine_top",
                "north": "tech_revised:block/crusher_front",
                "south": "tech_revised:block/machine_side",
                "west": "tech_revised:block/machine_side",
                "east": "tech_revised:block/machine_side",
                "particle": "tech_revised:block/machine_side",
            },
        }
        crusher_idle = {
            "parent": "block/cube",
            "textures": {
                "down": "tech_revised:block/machine_top",
                "up": "tech_revised:block/machine_top",
                "north": "tech_revised:block/crusher_front_idle",
                "south": "tech_revised:block/machine_side",
                "west": "tech_revised:block/machine_side",
                "east": "tech_revised:block/machine_side",
                "particle": "tech_revised:block/machine_side",
            },
        }
        with open(os.path.join(BLOCK_MODEL, "crusher.json"), "w", encoding="utf-8") as f:
            json.dump(crusher_on, f, indent=2)
            f.write("\n")
        with open(os.path.join(BLOCK_MODEL, "crusher_idle.json"), "w", encoding="utf-8") as f:
            json.dump(crusher_idle, f, indent=2)
            f.write("\n")
        with open(os.path.join(BLOCKSTATE, "crusher.json"), "w", encoding="utf-8") as f:
            json.dump({
                "variants": {
                    "active=false": {"model": "tech_revised:block/crusher_idle"},
                    "active=true": {"model": "tech_revised:block/crusher"},
                }
            }, f, indent=2)
            f.write("\n")
        with open(os.path.join(ITEM_MODEL, "crusher.json"), "w", encoding="utf-8") as f:
            json.dump({"parent": "tech_revised:block/crusher_idle"}, f, indent=2)
            f.write("\n")
        continue

    if name == "windows_7_vm_block":
        front = machine_front(accent, symbol)
        front_on = machine_front_on(accent, symbol)
        write_png(os.path.join(BLOCK_TEX, "windows_7_vm_block.png"), front_on)
        write_png(os.path.join(BLOCK_TEX, "windows_7_vm_block_idle.png"), front)
        for mname, tex in (("windows_7_vm_block", "windows_7_vm_block"), ("windows_7_vm_block_idle", "windows_7_vm_block_idle")):
            with open(os.path.join(BLOCK_MODEL, f"{mname}.json"), "w", encoding="utf-8") as f:
                json.dump({
                    "parent": "minecraft:block/orientable",
                    "textures": {
                        "top": "tech_revised:block/machine_top",
                        "front": f"tech_revised:block/{tex}",
                        "side": "tech_revised:block/machine_side",
                    },
                }, f, indent=2)
                f.write("\n")
        # keep simple blockstate if no active property - check existing
        with open(os.path.join(BLOCKSTATE, "windows_7_vm_block.json"), "w", encoding="utf-8") as f:
            json.dump({"variants": {"": {"model": "tech_revised:block/windows_7_vm_block"}}}, f, indent=2)
            f.write("\n")
        with open(os.path.join(ITEM_MODEL, "windows_7_vm_block.json"), "w", encoding="utf-8") as f:
            json.dump({"parent": "tech_revised:block/windows_7_vm_block"}, f, indent=2)
            f.write("\n")
        continue

    write_orientable(name, accent, symbol, active_variants=False)

# Formed frame variants (reuse frame texture with brighter edge)
for base in (
    "electric_arc_furnace_frame",
    "coke_oven_frame",
    "drilling_platform_frame",
    "argon_oxygen_decarburization_converter_frame",
    "oxygen_converter_frame",
):
    formed = machine_front(px(120, 160, 200), "frame")
    write_png(os.path.join(BLOCK_TEX, f"{base}_formed.png"), formed)
    model_path = os.path.join(BLOCK_MODEL, f"{base}_formed.json")
    with open(model_path, "w", encoding="utf-8") as f:
        json.dump({
            "parent": "minecraft:block/orientable",
            "textures": {
                "top": "tech_revised:block/machine_top",
                "front": f"tech_revised:block/{base}_formed",
                "side": "tech_revised:block/machine_side",
            },
        }, f, indent=2)
        f.write("\n")

# Update EAF bus models that reference dedicated textures
BUS_MODELS = {
    "electric_arc_furnace_input_bus": "input_bus",
    "electric_arc_furnace_output_bus": "output_bus",
    "electric_arc_furnace_fluid_input_bus": "fluid_input_bus",
    "electric_arc_furnace_fluid_output_bus": "fluid_output_bus",
    "electric_arc_furnace_energy_input_hatch": "energy_input_hatch",
}
for name, tex in BUS_MODELS.items():
    # overwrite with orientable using shared machine + bus face
    write_png(os.path.join(BLOCK_TEX, f"{name}_front.png"), machine_front(
        px(60, 180, 60) if "input_bus" in name and "fluid" not in name else
        px(180, 60, 60) if "output_bus" in name and "fluid" not in name else
        px(60, 120, 200) if "fluid_input" in name else
        px(40, 90, 180) if "fluid_output" in name else
        px(255, 200, 40),
        "bus_in" if "input_bus" in name and "fluid" not in name else
        "bus_out" if "output_bus" in name and "fluid" not in name else
        "fluid" if "fluid" in name else "energy"
    ))
    with open(os.path.join(BLOCK_MODEL, f"{name}.json"), "w", encoding="utf-8") as f:
        json.dump({
            "parent": "minecraft:block/orientable",
            "textures": {
                "top": "tech_revised:block/machine_top",
                "front": f"tech_revised:block/{name}_front",
                "side": "tech_revised:block/machine_side",
            },
        }, f, indent=2)
        f.write("\n")

print(f"Generic active machines: {len(GENERIC_ACTIVE)}")
print(f"Static machines: {len(STATIC_MACHINES)}")
print("Done - legacy assets refreshed")
