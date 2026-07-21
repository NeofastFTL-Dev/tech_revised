package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DryingOvenBlockEntity extends GenericIndustrialMachineBlockEntity {
    public DryingOvenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_OVEN.get(), pos, state, ModRecipes.DryingRecipeType.INSTANCE, "block.tech_revised.industrial_drying_oven");
    }
}
