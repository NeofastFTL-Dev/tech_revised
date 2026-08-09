package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.recipe.GenericIndustrialRecipe;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GenericIndustrialJeiCategory implements IRecipeCategory<GenericIndustrialRecipe> {
    private final RecipeType<GenericIndustrialRecipe> recipeType;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public GenericIndustrialJeiCategory(IGuiHelper guiHelper, ResourceLocation uid, ItemStack icon, String titleKey) {
        this.recipeType = new RecipeType<>(uid, GenericIndustrialRecipe.class);
        this.title = Component.translatable(titleKey);
        this.background = guiHelper.createBlankDrawable(126, 52);
        this.icon = guiHelper.createDrawableItemStack(icon);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<GenericIndustrialRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    @SuppressWarnings("removal")
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GenericIndustrialRecipe recipe, IFocusGroup focuses) {
        ItemStack[] stacks = recipe.getInput().getItems();
        if (stacks.length > 0) {
            ItemStack display = stacks[0].copy();
            display.setCount(recipe.getInputCount());
            builder.addSlot(RecipeIngredientRole.INPUT, 8, 18).addItemStack(display);
        } else {
            builder.addSlot(RecipeIngredientRole.INPUT, 8, 18).addIngredients(recipe.getInput());
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 18)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(GenericIndustrialRecipe recipe,
                     mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {
        arrow.draw(guiGraphics, 58, 18);
        int totalEnergy = recipe.getProcessTicks() * recipe.getEnergyPerTick();
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font,
                Component.literal("Time: " + recipe.getProcessTicks() + " t"),
                8, 2, 0x8B8B8B, false);
        guiGraphics.drawString(font,
                Component.literal("Energy: " + recipe.getEnergyPerTick() + " FE/t (" + totalEnergy + " FE)"),
                8, 42, 0x8B8B8B, false);
    }
}
