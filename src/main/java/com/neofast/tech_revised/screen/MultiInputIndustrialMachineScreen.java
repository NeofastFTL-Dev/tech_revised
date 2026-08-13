package com.neofast.tech_revised.screen;

import com.neofast.tech_revised.TechRevised;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class MultiInputIndustrialMachineScreen extends AbstractContainerScreen<MultiInputIndustrialMachineMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(TechRevised.MOD_ID, "textures/gui/crusher_gui.png");
    private static final int ENERGY_BAR_X = 154;
    private static final int ENERGY_BAR_Y = 18;
    private static final int ENERGY_BAR_WIDTH = 10;
    private static final int ENERGY_BAR_HEIGHT = 54;

    public MultiInputIndustrialMachineScreen(MultiInputIndustrialMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Draw slot backgrounds for machine slots (covers texture mismatch for any input count)
        int teSlotStart = 36; // vanilla player slots: 0-35, TE starts at 36
        for (int i = teSlotStart; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            drawSlotBackground(guiGraphics, x + slot.x, y + slot.y);
        }

        int left = x + ENERGY_BAR_X;
        int top = y + ENERGY_BAR_Y;
        int right = left + ENERGY_BAR_WIDTH;
        int bottom = top + ENERGY_BAR_HEIGHT;

        guiGraphics.fill(left, top, right, bottom, 0xFF3A3A3A);
        guiGraphics.fill(left + 1, top + 1, right - 1, bottom - 1, 0xFF111111);

        int scaledEnergy = getScaledEnergy(ENERGY_BAR_HEIGHT - 2);
        if (scaledEnergy > 0) {
            guiGraphics.fill(left + 1, bottom - 1 - scaledEnergy, right - 1, bottom - 1, 0xFF2ED05A);
        }

        int processTicks = menu.getProcessTicks();
        int progress = menu.getProgress();
        if (processTicks > 0 && progress > 0) {
            int barWidth = 24;
            int filled = Math.min(barWidth, progress * barWidth / processTicks);
            int barX = x + 84;
            int barY = y + 38;
            guiGraphics.fill(barX, barY, barX + barWidth, barY + 4, 0xFF3A3A3A);
            if (filled > 0) {
                guiGraphics.fill(barX, barY, barX + filled, barY + 4, 0xFF4FC3F7);
            }
        }
    }

    private static void drawSlotBackground(GuiGraphics guiGraphics, int sx, int sy) {
        // Vanilla-style 18x18 slot frame around the item position
        guiGraphics.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF373737);
        guiGraphics.fill(sx, sy, sx + 16, sy + 16, 0xFF8B8B8B);
        // top/left highlight
        guiGraphics.fill(sx - 1, sy - 1, sx + 16, sy, 0xFFFFFFFF);
        guiGraphics.fill(sx - 1, sy - 1, sx, sy + 16, 0xFFFFFFFF);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int left = x + ENERGY_BAR_X;
        int top = y + ENERGY_BAR_Y;
        int right = left + ENERGY_BAR_WIDTH;
        int bottom = top + ENERGY_BAR_HEIGHT;
        if (mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom) {
            guiGraphics.renderTooltip(this.font,
                    Component.literal(menu.getEnergyStored() + " / " + menu.getMaxEnergyStored() + " FE"),
                    mouseX, mouseY);
        }
    }

    private int getScaledEnergy(int height) {
        int energy = menu.getEnergyStored();
        int maxEnergy = menu.getMaxEnergyStored();
        return maxEnergy == 0 ? 0 : (int) ((long) energy * height / maxEnergy);
    }
}
