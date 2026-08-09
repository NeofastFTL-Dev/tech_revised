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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class LaminationJeiCategory implements IRecipeCategory<LaminationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(TechRevised.MOD_ID, "lamination");
    public static final RecipeType<LaminationRecipe> RECIPE_TYPE = new RecipeType<>(UID, LaminationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public LaminationJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 80);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.VACUUM_LAMINATION_PRESS.get()));
        this.arrow = guiHelper.getRecipeArrow();
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
    @SuppressWarnings("removal")
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
            builder.addSlot(RecipeIngredientRole.INPUT, 8, 8 + i * 20)
                    .addIngredients(ingredient);
            i++;
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 28)
                .addItemStack(recipe.getResultItem(null));
        builder.addSlot(RecipeIngredientRole.CATALYST, 120, 56)
                .addItemStack(new ItemStack(ModBlocks.VACUUM_LAMINATION_PRESS.get()));
    }

    @Override
    public void draw(LaminationRecipe recipe,
                     mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {
        arrow.draw(guiGraphics, 80, 28);
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, Component.literal("Layers (top -> bottom)"), 30, 4, 0x8B8B8B, false);
        int totalEnergy = recipe.getProcessTicks() * recipe.getEnergyPerTick();
        guiGraphics.drawString(font,
                Component.literal(recipe.getProcessTicks() + "t / " + totalEnergy + " FE"),
                30, 64, 0x8B8B8B, false);
    }
}
