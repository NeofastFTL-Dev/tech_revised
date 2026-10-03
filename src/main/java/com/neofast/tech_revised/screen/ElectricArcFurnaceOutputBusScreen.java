package com.neofast.tech_revised.screen;

import com.neofast.tech_revised.client.IndustrialGuiStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ElectricArcFurnaceOutputBusScreen extends AbstractContainerScreen<ElectricArcFurnaceOutputBusMenu> {
    public ElectricArcFurnaceOutputBusScreen(ElectricArcFurnaceOutputBusMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = IndustrialGuiStyle.PANEL_WIDTH;
        this.imageHeight = IndustrialGuiStyle.PANEL_HEIGHT;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 74;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        IndustrialGuiStyle.blitStandardPanel(guiGraphics, x, y);
        IndustrialGuiStyle.drawMenuSlots(guiGraphics, menu, x, y, 36);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        IndustrialGuiStyle.drawTruncated(guiGraphics, font, title, titleLabelX, titleLabelY, 160, IndustrialGuiStyle.TITLE_COLOR);
        IndustrialGuiStyle.drawTruncated(guiGraphics, font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 160, IndustrialGuiStyle.MUTED_COLOR);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, 0, 0, 0);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
