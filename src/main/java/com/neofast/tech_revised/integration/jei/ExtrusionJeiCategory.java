package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.item.ModItems;
import com.neofast.tech_revised.recipe.FluidToItemRecipe;
import mezz.jei.api.forge.ForgeTypes;
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

public class ExtrusionJeiCategory implements IRecipeCategory<FluidToItemRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(TechRevised.MOD_ID, "extrusion");
    public static final RecipeType<FluidToItemRecipe> RECIPE_TYPE = new RecipeType<>(UID, FluidToItemRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ExtrusionJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(new ResourceLocation(TechRevised.MOD_ID, "textures/gui/jei_gui.png"), 0, 0, 176, 50);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.PLATINUM_ALLOY_BUSHING_PLATE.get()));
    }

    @Override
    public RecipeType<FluidToItemRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.extrusion");
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
    public void setRecipe(IRecipeLayoutBuilder builder, FluidToItemRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 10).addIngredient(ForgeTypes.FLUID_STACK, recipe.getInputFluid());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 10).addItemStack(recipe.getResultItem(null));
    }
}
