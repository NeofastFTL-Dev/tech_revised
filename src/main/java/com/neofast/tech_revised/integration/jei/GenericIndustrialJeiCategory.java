package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.recipe.GenericIndustrialRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GenericIndustrialJeiCategory implements IRecipeCategory<GenericIndustrialRecipe> {
    private final RecipeType<GenericIndustrialRecipe> recipeType;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;

    public GenericIndustrialJeiCategory(IGuiHelper guiHelper, ResourceLocation uid, ItemStack icon, String titleKey) {
        this.recipeType = new RecipeType<>(uid, GenericIndustrialRecipe.class);
        this.title = Component.translatable(titleKey);
        this.background = guiHelper.createDrawable(new ResourceLocation(TechRevised.MOD_ID, "textures/gui/jei_gui.png"), 0, 0, 176, 50);
        this.icon = guiHelper.createDrawableItemStack(icon);
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GenericIndustrialRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 10 + (i * 18), 10).addIngredients(recipe.getIngredients().get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 10).addItemStack(recipe.getResultItem(null));
    }
}
