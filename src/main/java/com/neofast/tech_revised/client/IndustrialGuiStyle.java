package com.neofast.tech_revised.client;

import com.neofast.tech_revised.TechRevised;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public final class IndustrialGuiStyle {
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TechRevised.MOD_ID, "textures/gui/rusty_machine.png");

    public static final int PANEL_WIDTH = 176;
    public static final int PANEL_HEIGHT = 166;
    public static final int TITLE_COLOR = 0xB8E8F0;
    public static final int BODY_COLOR = 0xC8B496;
    public static final int MUTED_COLOR = 0xA89078;
    public static final int GOOD_COLOR = 0x28DCFF;
    public static final int BAD_COLOR = 0xDC28B4;
    public static final int LINE_HEIGHT = 11;

    public static final int ENERGY_BAR_X = 152;
    public static final int ENERGY_BAR_Y = 18;
    public static final int ENERGY_BAR_WIDTH = 12;
    public static final int ENERGY_BAR_HEIGHT = 52;

    private static final int RIVET = 0xFF6E5340;
    private static final int RIVET_CORE = 0xFFB08A68;
    private static final int PLATE = 0xFF3A261C;
    private static final int PLATE_DARK = 0xFF1C110C;
    private static final int PLATE_LIGHT = 0xFF6A4A36;
    private static final int WELL = 0xFF18100C;
    private static final int SLOT_FILL = 0xFF2A1C14;
    private static final int SLOT_DARK = 0xFF100A08;
    private static final int SLOT_LIGHT = 0xFF8A6A50;
    private static final int BAR_BG = 0xFF140C08;
    private static final int BAR_EDGE = 0xFF5A3A28;
    private static final int ENERGY_FILL = 0xFFFFA028;
    private static final int PROGRESS_FILL = 0xFF28DCFF;
    private static final int TANK_FILL = 0xFF28DCFF;

    private IndustrialGuiStyle() {
    }

    public static void blitStandardPanel(GuiGraphics graphics, int x, int y) {
        graphics.blit(TEXTURE, x, y, 0, 0, PANEL_WIDTH, PANEL_HEIGHT);
    }

    public static void drawMetalPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, PLATE_DARK);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PLATE);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, PLATE_LIGHT);
        graphics.fill(x + 1, y + 1, x + 2, y + height - 1, PLATE_LIGHT);
        graphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF100A08);
        graphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xFF100A08);
        for (int rx = x + 6; rx < x + width - 4; rx += 22) {
            drawRivet(graphics, rx, y + 5);
            drawRivet(graphics, rx, y + height - 6);
        }
        for (int ry = y + 24; ry < y + height - 8; ry += 22) {
            drawRivet(graphics, x + 5, ry);
            drawRivet(graphics, x + width - 6, ry);
        }
    }

    public static void drawRecess(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, WELL);
        graphics.fill(x, y, x + width, y + 1, 0xFF100A08);
        graphics.fill(x, y, x + 1, y + height, 0xFF100A08);
        graphics.fill(x, y + height - 1, x + width, y + height, PLATE_LIGHT);
        graphics.fill(x + width - 1, y, x + width, y + height, PLATE_LIGHT);
    }

    public static void drawSlot(GuiGraphics graphics, int sx, int sy) {
        graphics.fill(sx - 2, sy - 2, sx + 18, sy + 18, SLOT_DARK);
        graphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_LIGHT);
        graphics.fill(sx, sy, sx + 16, sy + 16, SLOT_FILL);
        graphics.fill(sx - 1, sy - 1, sx + 16, sy, 0xFFC4A078);
        graphics.fill(sx - 1, sy - 1, sx, sy + 16, 0xFFC4A078);
    }

    public static void drawMenuSlots(GuiGraphics graphics, AbstractContainerMenu menu, int left, int top, int teSlotStart) {
        for (int i = teSlotStart; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            drawSlot(graphics, left + slot.x, top + slot.y);
        }
    }

    public static void drawEnergyBar(GuiGraphics graphics, int left, int top, int stored, int max) {
        drawVerticalBar(graphics, left + ENERGY_BAR_X, top + ENERGY_BAR_Y, ENERGY_BAR_WIDTH, ENERGY_BAR_HEIGHT,
                stored, Math.max(0, max), ENERGY_FILL);
    }

    public static void drawVerticalBar(GuiGraphics graphics, int x, int y, int width, int height, int value, int max, int fillColor) {
        graphics.fill(x, y, x + width, y + height, BAR_EDGE);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, BAR_BG);
        if (max > 0 && value > 0) {
            int inner = height - 2;
            int scaled = Math.min(inner, (int) ((long) value * inner / max));
            graphics.fill(x + 1, y + height - 1 - scaled, x + width - 1, y + height - 1, fillColor);
        }
    }

    public static void drawHorizontalBar(GuiGraphics graphics, int x, int y, int width, int height, int value, int max, int fillColor) {
        graphics.fill(x, y, x + width, y + height, BAR_EDGE);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, BAR_BG);
        if (max > 0 && value > 0) {
            int inner = width - 2;
            int scaled = Math.min(inner, (int) ((long) value * inner / max));
            if (scaled > 0) {
                graphics.fill(x + 1, y + 1, x + 1 + scaled, y + height - 1, fillColor);
            }
        }
    }

    public static void drawProgressArrow(GuiGraphics graphics, int x, int y, int progress, int processTicks) {
        drawHorizontalBar(graphics, x, y, 26, 6, progress, Math.max(1, processTicks), PROGRESS_FILL);
    }

    public static boolean isHoveringEnergyBar(int left, int top, int mouseX, int mouseY) {
        int x = left + ENERGY_BAR_X;
        int y = top + ENERGY_BAR_Y;
        return mouseX >= x && mouseX < x + ENERGY_BAR_WIDTH && mouseY >= y && mouseY < y + ENERGY_BAR_HEIGHT;
    }

    public static void renderEnergyTooltip(GuiGraphics graphics, Font font, int left, int top, int mouseX, int mouseY,
                                           int stored, int max) {
        if (max > 0 && isHoveringEnergyBar(left, top, mouseX, mouseY)) {
            graphics.renderTooltip(font, Component.translatable("gui.tech_revised.energy", stored, max), mouseX, mouseY);
        }
    }

    public static int drawTruncated(GuiGraphics graphics, Font font, Component text, int x, int y, int maxWidth, int color) {
        String raw = text.getString();
        String shown = font.plainSubstrByWidth(raw, maxWidth);
        if (!shown.equals(raw) && shown.length() > 1) {
            shown = font.plainSubstrByWidth(raw, Math.max(8, maxWidth - font.width("..."))) + "...";
        }
        graphics.drawString(font, shown, x, y, color, false);
        return y + LINE_HEIGHT;
    }

    public static int drawTruncated(GuiGraphics graphics, Font font, String text, int x, int y, int maxWidth, int color) {
        return drawTruncated(graphics, font, Component.literal(text), x, y, maxWidth, color);
    }

    private static void drawRivet(GuiGraphics graphics, int cx, int cy) {
        graphics.fill(cx - 2, cy - 2, cx + 2, cy + 2, 0xFF100A08);
        graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, RIVET);
        graphics.fill(cx - 1, cy - 1, cx + 1, cy + 1, RIVET_CORE);
    }
}
