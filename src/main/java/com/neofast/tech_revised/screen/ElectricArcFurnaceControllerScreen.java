package com.neofast.tech_revised.screen;

import com.neofast.tech_revised.client.IndustrialGuiStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ElectricArcFurnaceControllerScreen extends AbstractContainerScreen<ElectricArcFurnaceControllerMenu> {
    public ElectricArcFurnaceControllerScreen(ElectricArcFurnaceControllerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = IndustrialGuiStyle.PANEL_WIDTH;
        this.imageHeight = 86;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        IndustrialGuiStyle.drawMetalPanel(guiGraphics, x, y, imageWidth, imageHeight);
        IndustrialGuiStyle.drawRecess(guiGraphics, x + 8, y + 18, 136, 58);
        IndustrialGuiStyle.drawEnergyBar(guiGraphics, x, y, menu.getEnergyStored(), menu.getMaxEnergyStored());
        IndustrialGuiStyle.drawHorizontalBar(guiGraphics, x + 12, y + 44, 120, 6, menu.getProgress(), Math.max(1, menu.getProcessTicks()), 0xFFC45A28);
        IndustrialGuiStyle.drawHorizontalBar(guiGraphics, x + 12, y + 64, 120, 6, menu.getWaterAmount(), menu.getWaterCapacity(), 0xFF4A8CB8);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int y = IndustrialGuiStyle.drawTruncated(guiGraphics, font, title, 8, 6, 138, IndustrialGuiStyle.TITLE_COLOR);
        boolean formed = menu.isStructureFormed();
        IndustrialGuiStyle.drawTruncated(guiGraphics, font,
                formed ? "Structure: Formed" : "Structure: Incomplete",
                12, y + 2, 128, formed ? IndustrialGuiStyle.GOOD_COLOR : IndustrialGuiStyle.BAD_COLOR);
        IndustrialGuiStyle.drawTruncated(guiGraphics, font, "Progress", 12, 34, 80, IndustrialGuiStyle.BODY_COLOR);
        IndustrialGuiStyle.drawTruncated(guiGraphics, font, "Cooling Water", 12, 54, 120, IndustrialGuiStyle.BODY_COLOR);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, 0, 0, 0);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        IndustrialGuiStyle.renderEnergyTooltip(guiGraphics, font, x, y, mouseX, mouseY, menu.getEnergyStored(), menu.getMaxEnergyStored());
        if (isHovering(12, 44, 120, 6, mouseX, mouseY)) {
            int percent = menu.getProgress() * 100 / Math.max(1, menu.getProcessTicks());
            guiGraphics.renderTooltip(font, Component.translatable("gui.tech_revised.progress_percent", percent), mouseX, mouseY);
        }
        if (isHovering(12, 64, 120, 6, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font,
                    Component.translatable("gui.tech_revised.fluid_mb", menu.getWaterAmount(), menu.getWaterCapacity()),
                    mouseX, mouseY);
        }
    }
}
