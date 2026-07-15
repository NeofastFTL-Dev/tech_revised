package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.recipe.AlloyingRecipe;
import mezz.jei.api.constants.VanillaTypes;
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

public class AlloyingJeiCategory implements IRecipeCategory<AlloyingRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(TechRevised.MOD_ID, "alloying");
    public static final RecipeType<AlloyingRecipe> RECIPE_TYPE = new RecipeType<>(UID, AlloyingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public AlloyingJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(new ResourceLocation(TechRevised.MOD_ID, "textures/gui/jei_gui.png"), 0, 0, 176, 50);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.BLAST_FURNACE_CORE.get()));
    }

    @Override
    public RecipeType<AlloyingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.alloying");
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
    public void setRecipe(IRecipeLayoutBuilder builder, AlloyingRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.getInputs().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 10 + (i * 18), 10).addIngredients(recipe.getInputs().get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 10).addItemStack(recipe.getResultItem(null));
    }
}
