package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.recipe.AlloyingRecipe;
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
import net.minecraft.world.item.crafting.Ingredient;

public class AlloyingJeiCategory implements IRecipeCategory<AlloyingRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(TechRevised.MOD_ID, "alloying");
    public static final RecipeType<AlloyingRecipe> RECIPE_TYPE = new RecipeType<>(UID, AlloyingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public AlloyingJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 52);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.BLAST_FURNACE_CORE.get()));
        this.arrow = guiHelper.getRecipeArrow();
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
    @SuppressWarnings("removal")
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AlloyingRecipe recipe, IFocusGroup focuses) {
        int i = 0;
        for (Ingredient ingredient : recipe.getInputs()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 8 + i * 18, 18)
                    .addIngredients(ingredient);
            i++;
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 18)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(AlloyingRecipe recipe,
                     mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {
        arrow.draw(guiGraphics, 90, 18);
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.literal("Any order"),
                8, 4, 0x8B8B8B, false);
    }
}
