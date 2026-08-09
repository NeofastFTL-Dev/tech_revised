package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.recipe.MeltingRecipe;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;

public class MeltingJeiCategory implements IRecipeCategory<MeltingRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(TechRevised.MOD_ID, "melting");
    public static final RecipeType<MeltingRecipe> RECIPE_TYPE = new RecipeType<>(UID, MeltingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public MeltingJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(126, 52);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.REFRACTORY_MELTING_FURNACE.get()));
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<MeltingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.melting");
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
    public void setRecipe(IRecipeLayoutBuilder builder, MeltingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 18)
                .addIngredients(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 104, 18)
                .addIngredients(ForgeTypes.FLUID_STACK, Collections.singletonList(recipe.getOutputFluid()))
                .setFluidRenderer(Math.max(1000, recipe.getOutputFluid().getAmount()), false, 16, 16);
    }

    @Override
    public void draw(MeltingRecipe recipe,
                     mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {
        arrow.draw(guiGraphics, 58, 18);
        int totalEnergy = recipe.getProcessTicks() * recipe.getEnergyPerTick();
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, Component.literal("Time: " + recipe.getProcessTicks() + " t"), 8, 2, 0x8B8B8B, false);
        guiGraphics.drawString(font, Component.literal("Energy: " + totalEnergy + " FE"), 8, 42, 0x8B8B8B, false);
    }
}
