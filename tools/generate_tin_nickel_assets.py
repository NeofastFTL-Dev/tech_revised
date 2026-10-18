"""Generate textures for the tin and nickel ore chain (one-shot, idempotent).

Follows the dependency-free PNG writer convention of the other tools/ scripts.
Run from repo root:  python tools/generate_tin_nickel_assets.py
"""
import os
import struct
import zlib

ROOT = os.path.join("src", "main", "resources", "assets", "tech_revised", "textures")
ITEM_TEX = os.path.join(ROOT, "item")
BLOCK_TEX = os.path.join(ROOT, "block")

os.makedirs(ITEM_TEX, exist_ok=True)
os.makedirs(BLOCK_TEX, exist_ok=True)


def px(r, g, b, a=255):
    return (r, g, b, a)


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
        + chunk(b"IDAT", zlib.compress(raw))
        + chunk(b"IEND", b"")
    )
    with open(path, "wb") as f:
        f.write(data)


def shade(c, d):
    return (max(0, min(255, c[0] + d)), max(0, min(255, c[1] + d)),
            max(0, min(255, c[2] + d)), c[3])


def solid(base):
    return [base for _ in range(256)]


def setp(p, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        p[y * 16 + x] = c


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


def raw_chunk(base):
    """Raw ore item: mineral blob cluster on transparent background."""
    p = [(0, 0, 0, 0) for _ in range(256)]
    blobs = ((5, 4), (10, 6), (6, 10), (11, 11), (8, 8), (4, 7), (12, 4))
    for cx, cy in blobs:
        for dy in (-1, 0, 1):
            for dx in (-1, 0, 1):
                if abs(dx) + abs(dy) < 2 or (abs(dx) + abs(dy) == 2 and (cx + dx + cy + dy) % 2 == 0):
                    setp(p, cx + dx, cy + dy, base)
        setp(p, cx + 1, cy - 1, shade(base, 25))
        setp(p, cx - 1, cy + 1, shade(base, -25))
    return p


def dust_tex(base, spark):
    p = solid(base)
    for y in range(16):
        for x in range(16):
            n = (x * 5 + y * 11) % 7
            if n == 0:
                p[y * 16 + x] = shade(base, -18)
            elif n == 3:
                p[y * 16 + x] = shade(base, 10)
    for x, y in ((3, 3), (11, 5), (6, 9), (13, 12), (8, 13), (2, 12)):
        setp(p, x, y, spark)
    return p


def ingot_tex(base):
    p = solid(base)
    for x in range(16):
        setp(p, x, 4, shade(base, 35))
        setp(p, x, 11, shade(base, -35))
    for y in range(16):
        setp(p, 3, y, shade(base, -20))
        setp(p, 12, y, shade(base, -20))
    return p


def nugget_tex(base):
    p = solid(base)
    for y in range(16):
        for x in range(16):
            if (x * 3 + y * 7) % 5 == 0:
                p[y * 16 + x] = shade(base, -22)
    setp(p, 4, 4, shade(base, 40))
    setp(p, 11, 9, shade(base, 40))
    return p


STONE = px(96, 78, 68)
CASSITERITE = px(94, 78, 66)
GARNIERITE = px(90, 148, 96)
TIN_RAW = px(110, 92, 80)
NICKEL_RAW = px(104, 158, 110)
TIN_DUST = px(168, 168, 176)
TIN_SPARK = px(40, 220, 255)
TIN_INGOT = px(196, 180, 165)
NICKEL_DUST = px(150, 152, 158)
NICKEL_SPARK = px(220, 40, 180)
NICKEL_INGOT = px(174, 160, 148)
NICKEL_NUGGET = px(150, 132, 120)

write_png(os.path.join(BLOCK_TEX, "tin_ore.png"), ore_block(STONE, CASSITERITE))
write_png(os.path.join(BLOCK_TEX, "nickel_ore.png"), ore_block(STONE, GARNIERITE))
write_png(os.path.join(ITEM_TEX, "raw_tin.png"), raw_chunk(TIN_RAW))
write_png(os.path.join(ITEM_TEX, "raw_nickel.png"), raw_chunk(NICKEL_RAW))
write_png(os.path.join(ITEM_TEX, "tin_dust.png"), dust_tex(TIN_DUST, TIN_SPARK))
write_png(os.path.join(ITEM_TEX, "nickel_dust.png"), dust_tex(NICKEL_DUST, NICKEL_SPARK))
write_png(os.path.join(ITEM_TEX, "tin_ingot.png"), ingot_tex(TIN_INGOT))
write_png(os.path.join(ITEM_TEX, "nickel_ingot.png"), ingot_tex(NICKEL_INGOT))
write_png(os.path.join(ITEM_TEX, "tin_nugget.png"), nugget_tex(TIN_INGOT))
write_png(os.path.join(ITEM_TEX, "nickel_nugget.png"), nugget_tex(NICKEL_NUGGET))

TIN_BLOCK = px(178, 182, 189)
NICKEL_BLOCK = px(158, 160, 167)
write_png(os.path.join(BLOCK_TEX, "tin_block.png"), ingot_tex(TIN_BLOCK))
write_png(os.path.join(BLOCK_TEX, "nickel_block.png"), ingot_tex(NICKEL_BLOCK))
print("wrote 2 storage block textures")


DEEPSLATE = px(80, 80, 84)
write_png(os.path.join(BLOCK_TEX, "deepslate_graphite_ore.png"), ore_block(DEEPSLATE, px(38, 38, 43)))
write_png(os.path.join(BLOCK_TEX, "deepslate_chromite_ore.png"), ore_block(DEEPSLATE, px(125, 100, 84)))
write_png(os.path.join(BLOCK_TEX, "deepslate_tin_ore.png"), ore_block(DEEPSLATE, px(94, 78, 66)))
write_png(os.path.join(BLOCK_TEX, "deepslate_nickel_ore.png"), ore_block(DEEPSLATE, px(90, 148, 96)))
print("wrote 4 deepslate ore textures")


def generator_tex(base, lit=False):
    p = solid(base)
    for y in range(16):
        for x in range(16):
            n = (x * 7 + y * 13) % 11
            if n < 2:
                p[y * 16 + x] = shade(base, -15)
            elif n > 9:
                p[y * 16 + x] = shade(base, 12)
    for y in (3, 4, 10, 11):
        for x in range(3, 13):
            p[y * 16 + x] = shade(base, -45)
    for y in (6, 7, 8):
        for x in (6, 7, 8, 9):
            p[y * 16 + x] = px(255, 140 + (y - 6) * 20, 40, 255)
    setp(p, 12, 2, px(40, 220, 255) if lit else px(40, 80, 50))
    setp(p, 13, 2, px(40, 220, 255) if lit else px(40, 80, 50))
    if lit:
        setp(p, 2, 2, px(220, 40, 180))
    return p

write_png(os.path.join(BLOCK_TEX, "solid_fuel_generator.png"), generator_tex(px(86, 52, 34), lit=False))
write_png(os.path.join(BLOCK_TEX, "solid_fuel_generator_lit.png"), generator_tex(px(110, 60, 36), lit=True))
print("wrote generator textures")

print("wrote 10 textures: 2 ore blocks, raw/dust/ingot/nugget items for tin + nickel")
