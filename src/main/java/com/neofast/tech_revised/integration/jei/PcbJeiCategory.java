package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.recipe.PcbRecipe;
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

public class PcbJeiCategory implements IRecipeCategory<PcbRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(TechRevised.MOD_ID, "pcb");
    public static final RecipeType<PcbRecipe> RECIPE_TYPE = new RecipeType<>(UID, PcbRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public PcbJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(new ResourceLocation(TechRevised.MOD_ID, "textures/gui/jei_gui.png"), 0, 0, 176, 50);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.DESIGN_ENGINEERING_STATION.get()));
    }

    @Override
    public RecipeType<PcbRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.pcb");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PcbRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 10 + (i * 18), 10).addIngredients(recipe.getIngredients().get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 10).addItemStack(recipe.getResultItem(null));
    }
}
