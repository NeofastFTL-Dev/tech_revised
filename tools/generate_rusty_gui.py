from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter
import random

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/tech_revised/textures/gui"
W, H = 256, 256
GW, GH = 176, 166
rng = random.Random(42)

CYAN = (40, 220, 255, 220)
MAGENTA = (220, 40, 180, 220)
AMBER = (255, 160, 40, 255)


def clamp(v):
    return max(0, min(255, int(v)))


def mix(a, b, t):
    return tuple(clamp(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def noise_pixel(x, y, seed):
    n = (x * 374761393 + y * 668265263 + seed * 1274126177) & 0xFFFFFFFF
    n = (n ^ (n >> 13)) * 1274126177
    return ((n ^ (n >> 16)) & 255) / 255.0


def paint_panel(seed=1):
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    px = img.load()
    dark = (28, 16, 12)
    mid = (86, 52, 34)
    rust = (148, 64, 28)
    orange = (168, 82, 32)

    for y in range(GH):
        for x in range(GW):
            n1 = noise_pixel(x, y, seed)
            n2 = noise_pixel(x // 3, y // 3, seed + 6)
            n3 = noise_pixel(x // 8, y // 8, seed + 12)
            stain = noise_pixel(x // 11 + 4, y // 9, seed + 20)
            t = n1 * 0.35 + n2 * 0.4 + n3 * 0.25
            c = mix(dark, mid, t)
            if stain > 0.72:
                c = mix(c, rust, 0.45 + (stain - 0.72))
            if n2 > 0.82:
                c = mix(c, orange, 0.25)
            if y % 22 < 1:
                c = mix(c, dark, 0.45)
            if x % 44 < 1 and 8 < y < GH - 8:
                c = mix(c, dark, 0.3)
            px[x, y] = c

    draw = ImageDraw.Draw(img)

    def bevel_rect(x0, y0, x1, y1, outer=True):
        hi = (158, 112, 78, 220) if outer else (110, 78, 52, 180)
        sh = (18, 10, 8, 230) if outer else (12, 8, 6, 200)
        draw.line([(x0, y0), (x1 - 1, y0)], fill=hi)
        draw.line([(x0, y0), (x0, y1 - 1)], fill=hi)
        draw.line([(x0, y1 - 1), (x1 - 1, y1 - 1)], fill=sh)
        draw.line([(x1 - 1, y0), (x1 - 1, y1 - 1)], fill=sh)

    bevel_rect(0, 0, GW, GH, True)
    bevel_rect(1, 1, GW - 1, GH - 1, True)
    bevel_rect(3, 3, GW - 3, GH - 3, False)

    draw.rectangle([7, 16, GW - 8, 78], fill=(24, 16, 12, 255))
    bevel_rect(7, 16, GW - 7, 79, False)
    draw.rectangle([7, 82, GW - 8, GH - 8], fill=(22, 14, 11, 255))
    bevel_rect(7, 82, GW - 7, GH - 7, False)

    draw.rectangle([150, 18, 164, 70], fill=(20, 12, 8, 255))
    draw.rectangle([151, 40, 163, 69], fill=AMBER)

    def rivet(cx, cy):
        draw.ellipse([cx - 2, cy - 2, cx + 2, cy + 2], fill=(22, 14, 10, 255))
        draw.ellipse([cx - 1, cy - 1, cx + 1, cy + 1], fill=(118, 86, 62, 255))
        draw.point((cx - 1, cy - 1), fill=(176, 140, 104, 255))

    for x in (6, 28, 50, 72, 98, 124, 148, 169):
        rivet(x, 6)
        rivet(x, GH - 7)
    for y in (24, 46, 68, 98, 120, 142):
        rivet(6, y)
        rivet(GW - 7, y)

    draw.line([(2, 2), (GW - 3, 2)], fill=CYAN)
    draw.line([(2, GH - 3), (GW - 3, GH - 3)], fill=MAGENTA)
    draw.line([(2, 2), (2, GH - 3)], fill=CYAN)
    draw.line([(GW - 3, 2), (GW - 3, GH - 3)], fill=MAGENTA)
    for cx, cy in ((4, 4), (GW - 5, 4), (4, GH - 5), (GW - 5, GH - 5)):
        draw.point((cx, cy), fill=MAGENTA)
        draw.point((cx + 1, cy), fill=CYAN)

    local_rng = random.Random(seed)
    for _ in range(18):
        x0 = local_rng.randint(10, GW - 20)
        y0 = local_rng.randint(18, GH - 12)
        x1 = x0 + local_rng.randint(8, 28)
        y1 = y0 + local_rng.randint(-3, 3)
        draw.line([(x0, y0), (x1, y1)], fill=(18, 10, 8, 90), width=1)

    return img.filter(ImageFilter.SMOOTH)


ROOT.mkdir(parents=True, exist_ok=True)
for name, seed in (("rusty_machine.png", 1), ("crusher_gui.png", 7), ("jei_gui.png", 13)):
    out = ROOT / name
    paint_panel(seed).save(out)
    print(f"wrote {out}")
