"""Shared futuristic hull + cyan/magenta neon helpers for 16x16 asset generators."""
import os
import struct
import zlib

HULL = (18, 24, 34, 255)
PANEL = (42, 54, 72, 255)
PANEL_HI = (72, 92, 118, 255)
EDGE = (8, 10, 16, 255)
RIVET = (150, 200, 220, 255)
CYAN = (48, 230, 255, 255)
MAGENTA = (190, 70, 255, 255)
AMBER = (255, 190, 70, 255)
LED_OFF = (30, 50, 60, 255)
LED_ON = CYAN
WELL = (6, 10, 18, 255)

PLATE = PANEL
RUST_DARK = EDGE
RUST_HI = PANEL_HI
STEEL = PANEL
STEEL_DK = HULL
STEEL_LT = PANEL_HI


def px(r, g, b, a=255):
    return (int(r), int(g), int(b), int(a))


def shade(c, d):
    a = c[3] if len(c) > 3 else 255
    return px(max(0, min(255, c[0] + d)), max(0, min(255, c[1] + d)), max(0, min(255, c[2] + d)), a)


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
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(data)


def riveted_plate():
    p = solid(HULL)
    rect(p, 1, 1, 14, 14, PANEL)
    for y in range(2, 14):
        for x in range(2, 14):
            n = (x * 7 + y * 13) % 13
            if n < 2:
                p[y * 16 + x] = shade(PANEL, -16)
            elif n > 11:
                p[y * 16 + x] = shade(PANEL, 18)
    hline(p, 1, 14, 1, PANEL_HI)
    vline(p, 1, 1, 14, PANEL_HI)
    hline(p, 1, 14, 14, EDGE)
    vline(p, 14, 1, 14, EDGE)
    for x in (0, 15):
        vline(p, x, 0, 15, EDGE)
    for y in (0, 15):
        hline(p, 0, 15, y, EDGE)
    for cx, cy in ((2, 2), (13, 2), (2, 13), (13, 13)):
        setp(p, cx, cy, CYAN)
        setp(p, cx, cy - 1, shade(CYAN, -80))
    return p


def neon_edge(p, color=CYAN, inset=1):
    hline(p, inset, 15 - inset, inset, color)
    hline(p, inset, 15 - inset, 15 - inset, color)
    vline(p, inset, inset, 15 - inset, color)
    vline(p, 15 - inset, inset, 15 - inset, color)


def hazard_stripe(p, y=14):
    for x in range(16):
        setp(p, x, y, CYAN if (x // 2) % 2 == 0 else MAGENTA)


def led_pair(p, on=False):
    c = LED_ON if on else LED_OFF
    setp(p, 12, 3, c)
    setp(p, 13, 3, c)
    if on:
        setp(p, 12, 4, shade(CYAN, -60))
        setp(p, 13, 2, MAGENTA)


def hologram_screen(p, x0=5, y0=5, x1=10, y1=9, active=False):
    rect(p, x0, y0, x1, y1, WELL)
    scan = CYAN if active else shade(CYAN, -120)
    hline(p, x0, x1, y0 + 1, scan)
    if active:
        hline(p, x0, x1, y0 + 3, MAGENTA)


def pcb_board(bg=None, trace=None, holes=False, mask=None, silk=None, components=False):
    bg = bg or px(36, 72, 44)
    trace = trace or CYAN
    p = solid(bg)
    rect(p, 1, 1, 14, 14, shade(trace, -80))
    rect(p, 2, 2, 13, 13, bg)
    for i in range(3, 13, 2):
        hline(p, 3, 12, i, trace)
    vline(p, 5, 3, 12, trace)
    vline(p, 10, 3, 12, MAGENTA)
    if holes:
        for cx, cy in ((4, 4), (11, 4), (4, 11), (11, 11), (8, 8)):
            setp(p, cx, cy, WELL)
            setp(p, cx + 1, cy, WELL)
    if mask:
        mr, mg, mb = mask[0], mask[1], mask[2]
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
        rect(p, 6, 6, 9, 8, px(24, 24, 28))
        rect(p, 3, 9, 5, 11, MAGENTA)
        rect(p, 10, 9, 12, 11, CYAN)
    neon_edge(p, shade(CYAN, -90))
    return p


def machine_side():
    p = riveted_plate()
    neon_edge(p, shade(CYAN, -90))
    hazard_stripe(p, 15)
    return p


def machine_top():
    p = solid(HULL)
    rect(p, 1, 1, 14, 14, PANEL)
    rect(p, 4, 4, 11, 11, WELL)
    hline(p, 4, 11, 7, shade(CYAN, -40))
    hline(p, 4, 11, 8, shade(MAGENTA, -40))
    for cx, cy in ((2, 2), (13, 2), (2, 13), (13, 13)):
        setp(p, cx, cy, CYAN)
    neon_edge(p, shade(CYAN, -100))
    return p


def machine_front(accent, symbol="default", active=False):
    p = riveted_plate()
    neon_edge(p, CYAN if active else shade(CYAN, -110))
    rect(p, 3, 3, 12, 12, WELL)
    inner = accent if len(accent) == 4 else px(accent[0], accent[1], accent[2])
    dim = shade(inner, -50)
    rect(p, 4, 4, 11, 11, inner if active else dim)
    hline(p, 3, 12, 3, CYAN if active else shade(CYAN, -90))
    hline(p, 3, 12, 12, MAGENTA if active else shade(MAGENTA, -90))
    led_pair(p, on=active)

    if symbol == "screen":
        hologram_screen(p, 5, 5, 10, 9, active)
    elif symbol == "nozzle":
        rect(p, 7, 8, 8, 12, STEEL_LT)
        rect(p, 6, 12, 9, 13, accent)
    elif symbol == "tank":
        rect(p, 5, 5, 10, 11, px(accent[0] // 2, accent[1] // 2, accent[2] // 2))
        hline(p, 5, 10, 8, CYAN if active else shade(CYAN, -80))
    elif symbol == "drill":
        rect(p, 7, 4, 8, 11, STEEL_LT)
        setp(p, 7, 12, WELL)
        setp(p, 8, 12, WELL)
    elif symbol == "oven":
        rect(p, 5, 6, 10, 11, WELL)
        for x in range(5, 11):
            setp(p, x, 8, AMBER if active else px(255, 120, 40))
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
        hologram_screen(p, 5, 4, 10, 8, active)
        setp(p, 6, 10, LED_ON if active else LED_OFF)
        setp(p, 8, 10, MAGENTA)
        setp(p, 10, 10, AMBER)
    elif symbol == "heater":
        rect(p, 5, 5, 10, 11, px(30, 20, 15))
        for y in range(6, 11):
            hline(p, 6, 9, y, px(255, 80 + y * 10, 20))
    elif symbol == "hatch":
        rect(p, 4, 4, 11, 11, STEEL_DK)
        rect(p, 6, 6, 9, 9, accent)
    elif symbol == "bus_in":
        rect(p, 5, 5, 10, 10, px(40, 80, 40))
        hline(p, 6, 9, 7, CYAN)
        hline(p, 6, 9, 8, CYAN)
    elif symbol == "bus_out":
        rect(p, 5, 5, 10, 10, px(80, 40, 40))
        hline(p, 6, 9, 7, MAGENTA)
        hline(p, 6, 9, 8, MAGENTA)
    elif symbol == "fluid":
        rect(p, 5, 5, 10, 11, px(30, 60, 100))
        hline(p, 5, 10, 9, CYAN)
    elif symbol == "energy":
        rect(p, 5, 5, 10, 10, px(40, 30, 10))
        vline(p, 7, 6, 9, AMBER)
        vline(p, 8, 6, 9, AMBER)
    elif symbol == "frame":
        rect(p, 2, 2, 13, 13, STEEL_DK)
        for y in range(4, 12):
            for x in range(4, 12):
                p[y * 16 + x] = WELL if (x + y) % 2 == 0 else PLATE
        neon_edge(p, shade(CYAN, -80) if not active else CYAN, 2)
    elif symbol == "vm":
        hologram_screen(p, 4, 4, 11, 9, True)
        rect(p, 5, 11, 10, 13, PLATE)
    elif symbol == "chopper":
        rect(p, 4, 6, 11, 9, STEEL_LT)
        for x in range(4, 12, 2):
            setp(p, x, 7, accent)
    elif symbol == "robot":
        rect(p, 6, 4, 9, 6, STEEL_LT)
        rect(p, 7, 6, 8, 10, PLATE)
        rect(p, 5, 10, 10, 11, accent)
    elif symbol == "probe":
        vline(p, 6, 5, 11, STEEL_LT)
        vline(p, 9, 5, 11, STEEL_LT)
        setp(p, 6, 12, CYAN if active else accent)
        setp(p, 9, 12, MAGENTA if active else accent)
    elif symbol == "print":
        rect(p, 4, 5, 11, 7, STEEL_LT)
        hline(p, 4, 11, 9, accent)
        hline(p, 4, 11, 10, CYAN if active else accent)
    return p


def machine_front_on(accent, symbol="default"):
    p = machine_front(accent, symbol, active=True)
    for i in range(3, 13):
        r, g, b, a = p[3 * 16 + i]
        p[3 * 16 + i] = px(min(255, r + 20), min(255, g + 50), min(255, b + 40))
    hline(p, 4, 11, 8, MAGENTA)
    return p


machine_front_active = machine_front_on


def _seed_color(seed, base_r, base_g, base_b):
    return px(
        40 + (base_r + seed * 37) % 180,
        40 + (base_g + seed * 53) % 160,
        40 + (base_b + seed * 19) % 140,
    )


def item_pcb(seed):
    p = pcb_board(px(32, 64, 40), CYAN, holes=seed % 2 == 0, components=seed % 3 == 0)
    if seed % 5 == 0:
        rect(p, 12, 1, 14, 3, AMBER)
    return p


def item_chip(seed):
    p = solid(px(0, 0, 0, 0))
    body = px(28, 28, 32)
    rect(p, 3, 4, 12, 11, body)
    rect(p, 4, 5, 11, 10, shade(body, 20))
    hline(p, 4, 11, 6, CYAN if seed % 2 == 0 else MAGENTA)
    for x in range(4, 12, 2):
        setp(p, x, 12, STEEL_LT)
        setp(p, x, 3, STEEL_LT)
    return p


def item_cable(seed):
    p = solid(px(0, 0, 0, 0))
    jacket = px(20 + (seed * 11) % 40, 20, 24)
    for i, y in enumerate(range(2, 14)):
        x = 3 + (i % 4)
        setp(p, x, y, jacket)
        setp(p, x + 1, y, shade(jacket, 30))
        setp(p, x + 6, y, CYAN if i % 3 == 0 else jacket)
    rect(p, 2, 1, 5, 3, STEEL_LT)
    rect(p, 10, 12, 13, 14, STEEL_LT)
    return p


def item_pellet(seed):
    p = solid(px(0, 0, 0, 0))
    c = _seed_color(seed, 80, 60, 40)
    for cx, cy in ((5, 5), (10, 6), (6, 10), (11, 11), (8, 8)):
        setp(p, cx, cy, c)
        setp(p, cx + 1, cy, shade(c, 30))
        setp(p, cx, cy + 1, shade(c, -20))
    return p


def item_case(seed):
    p = riveted_plate()
    rect(p, 3, 4, 12, 12, WELL)
    rect(p, 4, 5, 11, 11, shade(PLATE, 10))
    hline(p, 4, 11, 7, CYAN if seed % 2 == 0 else MAGENTA)
    setp(p, 10, 6, LED_ON)
    return p


def item_glass(seed):
    p = solid(px(0, 0, 0, 0))
    glass = px(40, 80, 110, 220)
    rect(p, 3, 3, 12, 12, glass)
    hline(p, 3, 12, 5, CYAN)
    hline(p, 3, 12, 8, MAGENTA if seed % 2 else shade(CYAN, -40))
    neon_edge(p, shade(CYAN, -60), 3)
    return p


def item_speaker(seed):
    p = solid(px(0, 0, 0, 0))
    rect(p, 3, 3, 12, 13, PLATE)
    rect(p, 5, 5, 10, 10, WELL)
    rect(p, 6, 6, 9, 9, shade(PLATE, 20))
    setp(p, 7, 7, CYAN if seed % 2 else AMBER)
    setp(p, 8, 8, MAGENTA)
    return p


def item_sheet(seed):
    p = solid(shade(PLATE, 20))
    rect(p, 1, 3, 14, 12, STEEL_LT)
    for y in range(4, 12, 2):
        hline(p, 2, 13, y, shade(STEEL_LT, 15 if y % 4 == 0 else -10))
    hline(p, 1, 14, 3, CYAN if seed % 3 == 0 else shade(STEEL_LT, 40))
    return p


def item_generic(seed):
    p = riveted_plate()
    accent = _seed_color(seed, 90, 70, 50)
    rect(p, 4, 4, 11, 11, accent)
    hline(p, 4, 11, 7, CYAN)
    return p


def pc_item_texture(name, seed):
    keys = name.split("_")
    joined = name
    if any(k in joined for k in ("pcb", "board", "pcba", "motherboard", "layout", "gerber")):
        return item_pcb(seed)
    if any(k in joined for k in ("package", "die", "wafer", "nand", "dram", "eeprom", "mcu", "chipset", "cpu", "gpu_package")):
        return item_chip(seed)
    if any(k in joined for k in ("cable", "wire", "hdmi", "cord", "pair", "conductor", "jacket", "connector")):
        return item_cable(seed)
    if "pellet" in joined or "plasticizer" in joined:
        return item_pellet(seed)
    if any(k in joined for k in ("case", "chassis", "shell", "shroud", "bezel", "cabinet", "panel", "desktop", "tower")):
        return item_case(seed)
    if any(k in joined for k in ("glass", "lcd", "polariz", "backlight", "monitor", "keycap")):
        return item_glass(seed)
    if any(k in joined for k in ("speaker", "cone", "coil", "magnet", "driver")):
        return item_speaker(seed)
    if "sheet" in joined or "plate" in joined or "membrane" in joined:
        return item_sheet(seed)
    if "keyboard" in joined or "mouse" in joined or "keycaps" in keys:
        return item_case(seed)
    return item_generic(seed)
