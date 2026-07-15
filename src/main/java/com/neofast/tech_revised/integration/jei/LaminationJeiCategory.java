package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.recipe.LaminationRecipe;
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
import net.minecraft.world.item.crafting.Ingredient;

public class LaminationJeiCategory implements IRecipeCategory<LaminationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(TechRevised.MOD_ID, "lamination");
    public static final RecipeType<LaminationRecipe> RECIPE_TYPE = new RecipeType<>(UID, LaminationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public LaminationJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(new ResourceLocation(TechRevised.MOD_ID, "textures/gui/jei_gui.png"), 0, 0, 176, 100);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.VACUUM_LAMINATION_PRESS.get()));
    }

    @Override
    public RecipeType<LaminationRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.lamination");
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
    public void setRecipe(IRecipeLayoutBuilder builder, LaminationRecipe recipe, IFocusGroup focuses) {
        int i = 0;
        for (Ingredient ingredient : recipe.getLayers()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 10, 10 + (i * 18)).addIngredients(ingredient);
            i++;
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 40).addItemStack(recipe.getResultItem(null));
    }
}
