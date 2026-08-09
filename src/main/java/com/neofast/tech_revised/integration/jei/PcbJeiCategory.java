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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class PcbJeiCategory implements IRecipeCategory<PcbRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(TechRevised.MOD_ID, "pcb");
    public static final RecipeType<PcbRecipe> RECIPE_TYPE = new RecipeType<>(UID, PcbRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public PcbJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 70);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.DESIGN_ENGINEERING_STATION.get()));
        this.arrow = guiHelper.getRecipeArrow();
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
    @SuppressWarnings("removal")
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PcbRecipe recipe, IFocusGroup focuses) {
        var inputs = recipe.getInputs();
        int count = Math.min(inputs.size(), 3);
        int startX = 8;
        for (int i = 0; i < count; i++) {
            Ingredient ingredient = inputs.get(i);
            builder.addSlot(RecipeIngredientRole.INPUT, startX + i * 20, 18)
                    .addIngredients(ingredient);
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 18)
                .addItemStack(recipe.getResultItem(null));

        int machineIndex = 0;
        for (ResourceLocation machineId : recipe.getMachines()) {
            Block block = ForgeRegistries.BLOCKS.getValue(machineId);
            if (block != null && machineIndex < 4) {
                builder.addSlot(RecipeIngredientRole.CATALYST, 8 + machineIndex * 20, 46)
                        .addItemStack(new ItemStack(block));
                machineIndex++;
            }
        }
    }

    @Override
    public void draw(PcbRecipe recipe,
                     mezz.jei.api.gui.ingredient.IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics,
                     double mouseX,
                     double mouseY) {
        arrow.draw(guiGraphics, 90, 18);
        var font = Minecraft.getInstance().font;
        int totalEnergy = recipe.getProcessTicks() * recipe.getEnergyPerTick();
        guiGraphics.drawString(font,
                Component.literal(recipe.getProcessTicks() + "t / " + recipe.getEnergyPerTick() + " FE/t (" + totalEnergy + " FE)"),
                8, 4, 0x8B8B8B, false);
        if (!recipe.getMachines().isEmpty()) {
            guiGraphics.drawString(font, Component.literal("Machine"), 8, 36, 0x8B8B8B, false);
        }
    }
}
