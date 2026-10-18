"""Generate workstation assets.

Unformed parts are local furniture models. When the multiblock forms, those
parts hide and the monitor switches to one spanning model whose tabletop is a
single 48x48 texture — the Immersive Engineering split-texture approach, not
the IE steel theme.

Run from the repo root:  python tools/generate_workstation_desk.py
"""
import json
import os
import struct
import zlib

ROOT = os.path.join("src", "main", "resources")
ASSETS = os.path.join(ROOT, "assets", "tech_revised")
BLOCK_TEX = os.path.join(ASSETS, "textures", "block")
ITEM_MODEL = os.path.join(ASSETS, "models", "item")
BLOCK_MODEL = os.path.join(ASSETS, "models", "block")
BLOCKSTATE = os.path.join(ASSETS, "blockstates")
LOOT = os.path.join(ROOT, "data", "tech_revised", "loot_tables", "blocks")

for d in (BLOCK_TEX, ITEM_MODEL, BLOCK_MODEL, BLOCKSTATE, LOOT):
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


def setp(p, x, y, c, w=16, h=16):
    if 0 <= x < w and 0 <= y < h:
        p[y * w + x] = c


def rect(p, x0, y0, x1, y1, c, w=16, h=16):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            setp(p, x, y, c, w, h)


def noise(p, seed, amount=8, x0=0, y0=0, x1=None, y1=None, w=16, h=16):
    if x1 is None:
        x1 = w - 1
    if y1 is None:
        y1 = h - 1
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            i = y * w + x
            r, g, b, a = p[i]
            k = (x * 7 + y * 13 + seed * 31) % (amount * 2 + 1) - amount
            p[i] = (max(0, min(255, r + k)), max(0, min(255, g + k)), max(0, min(255, b + k)), a)


def dump(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


WOOD = px(142, 96, 55)
WOOD_DARK = px(98, 62, 34)
WOOD_LIGHT = px(168, 118, 72)
WOOD_GROOVE = px(78, 48, 26)
PLASTIC = px(36, 36, 40)
PLASTIC_LIGHT = px(58, 58, 64)
PLASTIC_DARK = px(22, 22, 26)
KEY = px(48, 50, 54)
KEY_TOP = px(72, 74, 80)
BEZEL = px(28, 28, 32)
SCREEN_OFF = px(18, 22, 28)
SCREEN_ON = px(40, 90, 140)
TASKBAR = px(24, 48, 90)
WINDOW = px(220, 228, 236)
FABRIC = px(52, 44, 40)
FABRIC_DARK = px(34, 28, 26)
FABRIC_LIGHT = px(78, 64, 56)
PAD = px(32, 36, 42)
LED_ON = px(70, 210, 90)
LED_OFF = px(40, 70, 40)
CABLE = px(40, 40, 48)
CONE = px(24, 24, 26)
CONE_RING = px(70, 70, 76)


def wood_planks(p, w, h, seed=3):
    for x in range(w):
        for y in range(h):
            col = x // 6
            tone = WOOD if col % 2 == 0 else WOOD_LIGHT
            setp(p, x, y, tone, w, h)
    for x in range(0, w, 6):
        rect(p, x, 0, x, h - 1, WOOD_GROOVE, w, h)
    noise(p, seed, 7, w=w, h=h)


desk_top = solid(WOOD, 48, 48)
wood_planks(desk_top, 48, 48, 3)
rect(desk_top, 2, 2, 13, 13, PAD, 48, 48)
noise(desk_top, 9, 5, 2, 2, 13, 13, 48, 48)
rect(desk_top, 16, 4, 31, 12, WOOD_DARK, 48, 48)
rect(desk_top, 17, 5, 30, 11, WOOD, 48, 48)
write_png(os.path.join(BLOCK_TEX, "workstation_desk_top.png"), desk_top, 48, 48)

desk_top_small = solid(WOOD)
wood_planks(desk_top_small, 16, 16, 4)
write_png(os.path.join(BLOCK_TEX, "tested_desk_top.png"), desk_top_small)

desk_front = solid(WOOD_DARK, 48, 16)
wood_planks(desk_front, 48, 16, 11)
rect(desk_front, 0, 10, 47, 15, WOOD_GROOVE, 48, 16)
rect(desk_front, 0, 0, 47, 1, WOOD_LIGHT, 48, 16)
write_png(os.path.join(BLOCK_TEX, "workstation_desk_front.png"), desk_front, 48, 16)

desk_side = solid(WOOD_DARK)
wood_planks(desk_side, 16, 16, 5)
rect(desk_side, 0, 0, 15, 1, WOOD_LIGHT)
rect(desk_side, 0, 12, 15, 15, WOOD_GROOVE)
write_png(os.path.join(BLOCK_TEX, "tested_desk_side.png"), desk_side)

shell = solid(PLASTIC)
noise(shell, 17, 5)
write_png(os.path.join(BLOCK_TEX, "tested_device_shell.png"), shell)

kbd = solid(PLASTIC_DARK)
rect(kbd, 1, 1, 14, 14, PLASTIC)
for row in range(5):
    for col in range(12):
        x = 2 + col
        y = 2 + row * 2
        if x > 13:
            continue
        setp(kbd, x, y, KEY_TOP)
        setp(kbd, x, y + 1, KEY)
rect(kbd, 4, 12, 11, 13, KEY)
write_png(os.path.join(BLOCK_TEX, "tested_keyboard_front.png"), kbd)

mouse = solid(PLASTIC_DARK)
rect(mouse, 4, 3, 11, 12, PLASTIC)
rect(mouse, 5, 4, 10, 11, PLASTIC_LIGHT)
rect(mouse, 7, 5, 8, 8, KEY)
setp(mouse, 7, 4, LED_OFF)
write_png(os.path.join(BLOCK_TEX, "tested_mouse_front.png"), mouse)

spk = solid(PLASTIC_DARK)
rect(spk, 2, 1, 13, 14, PLASTIC)
rect(spk, 4, 3, 11, 10, CONE)
rect(spk, 6, 5, 9, 8, CONE_RING)
rect(spk, 6, 12, 9, 13, CONE)
write_png(os.path.join(BLOCK_TEX, "tested_speaker_front.png"), spk)

hdmi = solid(PLASTIC_DARK)
rect(hdmi, 0, 6, 15, 9, CABLE)
rect(hdmi, 1, 5, 4, 10, PLASTIC_LIGHT)
rect(hdmi, 11, 5, 14, 10, PLASTIC_LIGHT)
write_png(os.path.join(BLOCK_TEX, "tested_hdmi_cable_front.png"), hdmi)

tower = solid(PLASTIC)
noise(tower, 21, 4)
rect(tower, 0, 0, 15, 15, PLASTIC_DARK)
rect(tower, 1, 1, 14, 14, PLASTIC)
rect(tower, 3, 2, 12, 5, PLASTIC_DARK)
rect(tower, 4, 3, 11, 4, KEY)
rect(tower, 3, 7, 12, 12, PLASTIC_DARK)
for y in range(8, 12):
    for x in range(4, 12):
        if (x + y) % 2 == 0:
            setp(tower, x, y, PLASTIC_LIGHT)
setp(tower, 12, 13, LED_OFF)
write_png(os.path.join(BLOCK_TEX, "tested_desktop_tower_front.png"), tower)

power = solid(PLASTIC_DARK)
rect(power, 1, 5, 14, 10, PLASTIC)
for x in (3, 6, 9, 12):
    rect(power, x, 6, x + 1, 8, PLASTIC_LIGHT)
write_png(os.path.join(BLOCK_TEX, "tested_power_cable_front.png"), power)


def monitor_front(lit):
    p = solid(BEZEL)
    rect(p, 1, 1, 14, 12, SCREEN_ON if lit else SCREEN_OFF)
    if lit:
        rect(p, 3, 3, 10, 9, WINDOW)
        rect(p, 1, 11, 14, 12, TASKBAR)
        setp(p, 3, 11, LED_ON)
    rect(p, 0, 13, 15, 15, PLASTIC)
    setp(p, 13, 14, LED_ON if lit else LED_OFF)
    return p


write_png(os.path.join(BLOCK_TEX, "tested_monitor_front.png"), monitor_front(False))
write_png(os.path.join(BLOCK_TEX, "tested_monitor_front_on.png"), monitor_front(True))

seat = solid(FABRIC)
noise(seat, 7, 6)
rect(seat, 0, 0, 15, 0, FABRIC_DARK)
rect(seat, 0, 15, 15, 15, FABRIC_DARK)
rect(seat, 0, 0, 0, 15, FABRIC_DARK)
rect(seat, 15, 0, 15, 15, FABRIC_DARK)
rect(seat, 2, 2, 13, 13, FABRIC_LIGHT)
rect(seat, 3, 3, 12, 12, FABRIC)
write_png(os.path.join(BLOCK_TEX, "tested_chair_seat.png"), seat)

chair_side = solid(PLASTIC)
noise(chair_side, 11, 4)
rect(chair_side, 6, 0, 9, 15, PLASTIC_DARK)
rect(chair_side, 2, 13, 13, 15, PLASTIC_DARK)
rect(chair_side, 5, 1, 10, 6, FABRIC_DARK)
write_png(os.path.join(BLOCK_TEX, "tested_chair_side.png"), chair_side)

back = solid(FABRIC)
noise(back, 13, 6)
rect(back, 1, 0, 14, 15, FABRIC_LIGHT)
rect(back, 2, 1, 13, 14, FABRIC)
rect(back, 6, 4, 9, 11, FABRIC_DARK)
write_png(os.path.join(BLOCK_TEX, "tested_chair_back_front.png"), back)


def faces(tex, extra=None):
    out = {}
    for side in ("north", "south", "east", "west", "up", "down"):
        ref = tex if isinstance(tex, str) else tex.get(side, tex.get("all", "#0"))
        out[side] = {"texture": ref}
        if extra:
            out[side].update(extra)
    return out


def cube(x0, y0, z0, x1, y1, z1, tex, rotation=None):
    el = {"from": [x0, y0, z0], "to": [x1, y1, z1], "faces": faces(tex)}
    if rotation:
        el["rotation"] = rotation
    return el


DESK_TEX = {
    "up": "#top",
    "down": "#side",
    "north": "#side",
    "south": "#side",
    "east": "#side",
    "west": "#side",
}

DESK_SLAB = cube(0, 8, 0, 16, 12, 16, DESK_TEX)
APRON = cube(0, 4, 0, 16, 8, 1, "#side")

DARK = {
    "all": "#dark",
}

models = {
    "tested_desk": {
        "gui_light": "front",
        "textures": {
            "top": "tech_revised:block/tested_desk_top",
            "side": "tech_revised:block/tested_desk_side",
            "particle": "tech_revised:block/tested_desk_top",
        },
        "elements": [DESK_SLAB],
    },
    "tested_keyboard": {
        "gui_light": "front",
        "textures": {
            "top": "tech_revised:block/tested_desk_top",
            "side": "tech_revised:block/tested_desk_side",
            "dark": "tech_revised:block/tested_device_shell",
            "front": "tech_revised:block/tested_keyboard_front",
            "particle": "tech_revised:block/tested_desk_top",
        },
        "elements": [
            DESK_SLAB,
            APRON,
            cube(1, 12, 3, 15, 14, 13, {"up": "#front", "all": "#dark"}),
        ],
    },
    "tested_mouse": {
        "gui_light": "front",
        "textures": {
            "top": "tech_revised:block/tested_desk_top",
            "side": "tech_revised:block/tested_desk_side",
            "dark": "tech_revised:block/tested_device_shell",
            "front": "tech_revised:block/tested_mouse_front",
            "particle": "tech_revised:block/tested_desk_top",
        },
        "elements": [
            DESK_SLAB,
            APRON,
            cube(5, 12, 5, 12, 13, 12, {"up": "#front", "all": "#dark"}),
            cube(6, 13, 6, 11, 15, 11, {"up": "#front", "all": "#dark"}),
        ],
    },
    "tested_speaker": {
        "gui_light": "front",
        "textures": {
            "top": "tech_revised:block/tested_desk_top",
            "side": "tech_revised:block/tested_desk_side",
            "dark": "tech_revised:block/tested_device_shell",
            "front": "tech_revised:block/tested_speaker_front",
            "particle": "tech_revised:block/tested_desk_top",
        },
        "elements": [
            DESK_SLAB,
            cube(3, 12, 4, 13, 16, 12, {"north": "#front", "all": "#dark"}),
        ],
    },
    "tested_hdmi_cable": {
        "gui_light": "front",
        "textures": {
            "top": "tech_revised:block/tested_desk_top",
            "side": "tech_revised:block/tested_desk_side",
            "dark": "tech_revised:block/tested_device_shell",
            "ribbon": "tech_revised:block/tested_hdmi_cable_front",
            "particle": "tech_revised:block/tested_desk_top",
        },
        "elements": [
            DESK_SLAB,
            APRON,
            cube(1, 12, 6, 15, 13, 10, {"up": "#ribbon", "all": "#dark"}),
        ],
    },
    "tested_desktop_tower": {
        "gui_light": "front",
        "textures": {
            "front": "tech_revised:block/tested_desktop_tower_front",
            "shell": "tech_revised:block/tested_device_shell",
            "particle": "tech_revised:block/tested_device_shell",
        },
        "elements": [
            cube(3, 0, 4, 13, 16, 12, {"north": "#front", "all": "#shell"}),
        ],
    },
    "tested_power_cable": {
        "gui_light": "front",
        "textures": {
            "strip": "tech_revised:block/tested_device_shell",
            "top": "tech_revised:block/tested_power_cable_front",
            "particle": "tech_revised:block/tested_device_shell",
        },
        "elements": [
            cube(2, 0, 5, 14, 3, 12, {"up": "#top", "all": "#strip"}),
            cube(7, 0, 12, 9, 2, 16, {"all": "#strip"}),
        ],
    },
    "tested_chair_seat": {
        "gui_light": "front",
        "textures": {
            "seat": "tech_revised:block/tested_chair_seat",
            "side": "tech_revised:block/tested_chair_side",
            "particle": "tech_revised:block/tested_chair_seat",
        },
        "elements": [
            cube(3, 0, 3, 13, 2, 13, "#side"),
            cube(7, 2, 7, 9, 8, 9, "#side"),
            cube(1, 8, 1, 15, 11, 15, {"up": "#seat", "all": "#seat"}),
        ],
    },
    "tested_chair_backrest": {
        "gui_light": "front",
        "textures": {
            "back": "tech_revised:block/tested_chair_back_front",
            "side": "tech_revised:block/tested_chair_side",
            "particle": "tech_revised:block/tested_chair_back_front",
        },
        "elements": [
            cube(6, 0, 1, 10, 4, 4, "#side"),
            cube(2, 4, 1, 14, 16, 4, {"north": "#back", "south": "#back", "all": "#side"}),
        ],
    },
}


def facing_formed_blockstate(name):
    variants = {}
    for facing, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for formed in (False, True):
            key = f"facing={facing},formed={str(formed).lower()}"
            entry = {"model": f"tech_revised:block/{name}"}
            if rot:
                entry["y"] = rot
            variants[key] = entry
    dump(os.path.join(BLOCKSTATE, f"{name}.json"), {"variants": variants})


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


def item_from_block(name):
    dump(os.path.join(ITEM_MODEL, f"{name}.json"), {"parent": f"tech_revised:block/{name}"})


for name, model in models.items():
    dump(os.path.join(BLOCK_MODEL, f"{name}.json"), model)
    facing_formed_blockstate(name)
    item_from_block(name)
    loot(name)


def monitor_local(front_tex):
    return {
        "gui_light": "front",
        "textures": {
            "front": front_tex,
            "shell": "tech_revised:block/tested_device_shell",
            "top": "tech_revised:block/tested_desk_top",
            "side": "tech_revised:block/tested_desk_side",
            "particle": front_tex,
        },
        "elements": [
            cube(0, 8, 0, 16, 12, 16, DESK_TEX),
            cube(6, 12, 7, 10, 14, 11, "#shell"),
            cube(1, 12, 6, 15, 16, 8, {"north": "#front", "all": "#shell"},
                 rotation={"origin": [8, 12, 8], "axis": "x", "angle": -22.5}),
        ],
    }


def monitor_formed(front_tex):
    return {
        "gui_light": "front",
        "textures": {
            "top": "tech_revised:block/workstation_desk_top",
            "front": "tech_revised:block/workstation_desk_front",
            "side": "tech_revised:block/tested_desk_side",
            "shell": "tech_revised:block/tested_device_shell",
            "screen": front_tex,
            "keys": "tech_revised:block/tested_keyboard_front",
            "mouse": "tech_revised:block/tested_mouse_front",
            "speaker": "tech_revised:block/tested_speaker_front",
            "hdmi": "tech_revised:block/tested_hdmi_cable_front",
            "tower": "tech_revised:block/tested_desktop_tower_front",
            "particle": "tech_revised:block/workstation_desk_top",
        },
        "elements": [
            {
                "from": [-16, 8, -16],
                "to": [32, 12, 32],
                "faces": {
                    "up": {"texture": "#top", "uv": [0, 0, 16, 16]},
                    "down": {"texture": "#side"},
                    "north": {"texture": "#front", "uv": [0, 0, 16, 16]},
                    "south": {"texture": "#front", "uv": [0, 0, 16, 16]},
                    "east": {"texture": "#side"},
                    "west": {"texture": "#side"},
                },
            },
            cube(-16, 4, -16, 32, 8, -15, "#front"),
            cube(6, 12, 6, 10, 14, 10, "#shell"),
            cube(1, 12, 5, 15, 16, 7, {"north": "#screen", "all": "#shell"},
                 rotation={"origin": [8, 12, 7], "axis": "x", "angle": -22.5}),
            cube(1, 12, -13, 15, 14, -3, {"up": "#keys", "all": "#shell"}),
            cube(-11, 12, -11, -4, 13, -4, {"up": "#mouse", "all": "#shell"}),
            cube(-10, 13, -10, -5, 15, -5, {"up": "#mouse", "all": "#shell"}),
            cube(17, 12, -10, 31, 13, -6, {"up": "#hdmi", "all": "#shell"}),
            cube(-13, 12, 20, -3, 16, 28, {"north": "#speaker", "all": "#shell"}),
            cube(19, 12, 20, 29, 16, 28, {"north": "#speaker", "all": "#shell"}),
            cube(3, -16, -12, 13, 8, -4, {"north": "#tower", "all": "#shell"}),
        ],
    }


dump(os.path.join(BLOCK_MODEL, "tested_monitor.json"), monitor_local("tech_revised:block/tested_monitor_front"))
dump(os.path.join(BLOCK_MODEL, "tested_monitor_on.json"), monitor_local("tech_revised:block/tested_monitor_front_on"))
dump(os.path.join(BLOCK_MODEL, "tested_monitor_formed.json"), monitor_formed("tech_revised:block/tested_monitor_front"))
dump(os.path.join(BLOCK_MODEL, "tested_monitor_formed_on.json"), monitor_formed("tech_revised:block/tested_monitor_front_on"))

monitor_variants = {}
for facing, rot in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
    for formed in (False, True):
        for active in (False, True):
            key = f"active={str(active).lower()},facing={facing},formed={str(formed).lower()}"
            if formed:
                model = "tech_revised:block/tested_monitor_formed_on" if active else "tech_revised:block/tested_monitor_formed"
            else:
                model = "tech_revised:block/tested_monitor_on" if active else "tech_revised:block/tested_monitor"
            entry = {"model": model}
            if rot:
                entry["y"] = rot
            monitor_variants[key] = entry
dump(os.path.join(BLOCKSTATE, "tested_monitor.json"), {"variants": monitor_variants})
item_from_block("tested_monitor")
loot("tested_monitor")

print("desk scene assets generated")
