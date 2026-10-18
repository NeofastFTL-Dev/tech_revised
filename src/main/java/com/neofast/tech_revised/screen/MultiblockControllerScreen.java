package com.neofast.tech_revised.screen;

import com.neofast.tech_revised.client.IndustrialGuiStyle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class MultiblockControllerScreen extends AbstractContainerScreen<MultiblockControllerMenu> {
    public MultiblockControllerScreen(MultiblockControllerMenu menu, Inventory playerInventory, Component title) {
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
        if (menu.getMaxEnergyStored() > 0) {
            IndustrialGuiStyle.drawEnergyBar(guiGraphics, x, y, menu.getEnergyStored(), menu.getMaxEnergyStored());
        }
        IndustrialGuiStyle.drawHorizontalBar(guiGraphics, x + 8, y + 48, 132, 6, menu.getProgress(), menu.getProcessTicks(), 0xFFC45A28);
        if (menu.getAuxiliaryCapacity() > 0) {
            IndustrialGuiStyle.drawHorizontalBar(guiGraphics, x + 8, y + 66, 132, 6, menu.getAuxiliaryAmount(), menu.getAuxiliaryCapacity(), 0xFF4A8CB8);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int maxWidth = menu.getMaxEnergyStored() > 0 ? 138 : 160;
        int y = IndustrialGuiStyle.drawTruncated(guiGraphics, font, title, titleLabelX, titleLabelY, maxWidth, IndustrialGuiStyle.TITLE_COLOR);
        boolean formed = menu.isStructureFormed();
        IndustrialGuiStyle.drawTruncated(guiGraphics, font,
                formed ? "Structure: Formed" : "Structure: Incomplete",
                8, y, maxWidth, formed ? IndustrialGuiStyle.GOOD_COLOR : IndustrialGuiStyle.BAD_COLOR);
        IndustrialGuiStyle.drawTruncated(guiGraphics, font, "Progress", 8, 38, 80, IndustrialGuiStyle.BODY_COLOR);
        if (menu.getAuxiliaryCapacity() > 0) {
            String label = menu.getAuxiliaryLabel().isEmpty() ? "Tank" : menu.getAuxiliaryLabel();
            IndustrialGuiStyle.drawTruncated(guiGraphics, font, label, 8, 56, 132, IndustrialGuiStyle.BODY_COLOR);
        }
        IndustrialGuiStyle.drawTruncated(guiGraphics, font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 138, IndustrialGuiStyle.MUTED_COLOR);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, 0, 0, 0);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        if (menu.getMaxEnergyStored() > 0) {
            IndustrialGuiStyle.renderEnergyTooltip(guiGraphics, font, x, y, mouseX, mouseY,
                    menu.getEnergyStored(), menu.getMaxEnergyStored());
        }
        if (isHovering(8, 48, 132, 6, mouseX, mouseY)) {
            int percent = menu.getProgress() * 100 / menu.getProcessTicks();
            guiGraphics.renderTooltip(font, Component.translatable("gui.tech_revised.progress_percent", percent), mouseX, mouseY);
        }
        if (menu.getAuxiliaryCapacity() > 0 && isHovering(8, 66, 132, 6, mouseX, mouseY)) {
            guiGraphics.renderTooltip(font,
                    Component.translatable("gui.tech_revised.fluid_mb", menu.getAuxiliaryAmount(), menu.getAuxiliaryCapacity()),
                    mouseX, mouseY);
        }
    }
}
