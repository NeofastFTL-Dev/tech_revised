package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.recipe.LaminationRecipe;
import com.neofast.tech_revised.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class LaminationMachineBlockEntity extends MultiInputIndustrialMachineBlockEntity {
    public static final int INPUT_SLOTS = 3;

    public LaminationMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LAMINATION_MACHINE.get(), pos, state, INPUT_SLOTS,
                "block.tech_revised.vacuum_lamination_press");
    }

    @Override
    protected ResolvedRecipe findRecipe(SimpleContainer inventory) {
        if (level == null) {
            return null;
        }
        Optional<LaminationRecipe> recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.LaminationRecipeType.INSTANCE, new com.neofast.tech_revised.recipe.ContainerInput(inventory), level).map(net.minecraft.world.item.crafting.RecipeHolder::value);
        return recipe.map(r -> ResolvedRecipe.of(
                r.getProcessTicks(),
                r.getEnergyPerTick(),
                r.getResultItem(level.registryAccess()),
                r.getLayers(),
                inv -> r.assemble(new com.neofast.tech_revised.recipe.ContainerInput(inv), level.registryAccess())
        )).orElse(null);
    }

    @Override
    protected boolean isIngredientAccepted(ItemStack stack) {
        if (level == null) {
            return true;
        }
        for (var holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.LaminationRecipeType.INSTANCE)) {
            LaminationRecipe recipe = holder.value();
            for (Ingredient ingredient : recipe.getLayers()) {
                if (ingredient.test(stack)) {
                    return true;
                }
            }
        }
        return false;
    }
}
