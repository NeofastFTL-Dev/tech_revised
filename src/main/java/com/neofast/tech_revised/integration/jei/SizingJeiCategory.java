package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.recipe.ItemFluidToItemRecipe;
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

public class SizingJeiCategory implements IRecipeCategory<ItemFluidToItemRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(TechRevised.MOD_ID, "sizing");
    public static final RecipeType<ItemFluidToItemRecipe> RECIPE_TYPE = new RecipeType<>(UID, ItemFluidToItemRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public SizingJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(140, 52);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CHEMICAL_SIZING_APPLICATOR.get()));
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<ItemFluidToItemRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.tech_revised.category.sizing");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ItemFluidToItemRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 18)
                .addIngredients(recipe.getInputItem());
        builder.addSlot(RecipeIngredientRole.INPUT, 36, 18)
                .addIngredients(ForgeTypes.FLUID_STACK, Collections.singletonList(recipe.getInputFluid()))
                .setFluidRenderer(Math.max(1000, recipe.getInputFluid().getAmount()), false, 16, 16);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 112, 18)
                .addItemStack(recipe.getResultItem(null));
    }

    @Override
    public void draw(ItemFluidToItemRecipe recipe,
                     mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {
        arrow.draw(guiGraphics, 72, 18);
        int totalEnergy = recipe.getProcessTicks() * recipe.getEnergyPerTick();
        var font = Minecraft.getInstance().font;
        guiGraphics.drawString(font, Component.literal("Time: " + recipe.getProcessTicks() + " t"), 8, 2, 0x8B8B8B, false);
        guiGraphics.drawString(font, Component.literal("Energy: " + totalEnergy + " FE"), 8, 42, 0x8B8B8B, false);
    }
}
