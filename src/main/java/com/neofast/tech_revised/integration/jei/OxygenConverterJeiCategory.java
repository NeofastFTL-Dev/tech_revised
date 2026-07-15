package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;

public class OxygenConverterJeiCategory implements IRecipeCategory<OxygenConverterJeiRecipe> {
    public static final RecipeType<OxygenConverterJeiRecipe> RECIPE_TYPE =
            RecipeType.create(TechRevised.MOD_ID, "oxygen_converter", OxygenConverterJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public OxygenConverterJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(146, 62);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.OXYGEN_CONVERTER_CONTROLLER.get()));
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<OxygenConverterJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.oxygen_converter");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, OxygenConverterJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 6, 22)
                .addIngredients(ForgeTypes.FLUID_STACK, Collections.singletonList(recipe.getInput()))
                .setFluidRenderer(recipe.getInput().getAmount(), false, 16, 16);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 102, 12)
                .addIngredients(ForgeTypes.FLUID_STACK, Collections.singletonList(recipe.getOutput1()))
                .setFluidRenderer(recipe.getOutput1().getAmount(), false, 16, 16);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 102, 32)
                .addIngredients(ForgeTypes.FLUID_STACK, Collections.singletonList(recipe.getOutput2()))
                .setFluidRenderer(recipe.getOutput2().getAmount(), false, 16, 16);
    }

    @Override
    public void draw(OxygenConverterJeiRecipe recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 65, 22);

        int totalEnergy = recipe.getProcessTicks() * recipe.getEnergyPerTick();
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal("Time: " + recipe.getProcessTicks() + " t"),
                6, 4, 0x8B8B8B, false);
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal("Energy: " + recipe.getEnergyPerTick() + " FE/t (" + totalEnergy + " FE)"),
                6, 54, 0x8B8B8B, false);
    }
}
