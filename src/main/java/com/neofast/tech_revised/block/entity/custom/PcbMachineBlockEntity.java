package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.recipe.ModRecipes;
import com.neofast.tech_revised.recipe.PcbRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PcbMachineBlockEntity extends MultiInputIndustrialMachineBlockEntity {
    public static final int INPUT_SLOTS = 3;

    public PcbMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PCB_MACHINE.get(), pos, state, INPUT_SLOTS, state.getBlock().getDescriptionId());
    }

    private Block hostBlock() {
        return getBlockState().getBlock();
    }

    @Override
    protected ResolvedRecipe findRecipe(SimpleContainer inventory) {
        if (level == null) {
            return null;
        }

        Block machine = hostBlock();
        for (PcbRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.PcbRecipeType.INSTANCE)) {
            if (!recipe.isAllowedOn(machine)) {
                continue;
            }
            if (!recipe.matches(inventory, level)) {
                continue;
            }
            return ResolvedRecipe.of(
                    recipe.getProcessTicks(),
                    recipe.getEnergyPerTick(),
                    recipe.getResultItem(level.registryAccess()),
                    recipe.getInputs(),
                    inv -> recipe.assemble(inv, level.registryAccess())
            );
        }
        return null;
    }

    @Override
    protected boolean isIngredientAccepted(ItemStack stack) {
        if (level == null) {
            return true;
        }

        Block machine = hostBlock();
        for (PcbRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.PcbRecipeType.INSTANCE)) {
            if (!recipe.isAllowedOn(machine)) {
                continue;
            }
            for (Ingredient ingredient : recipe.getInputs()) {
                if (ingredient.test(stack)) {
                    return true;
                }
            }
        }
        return false;
    }
}
