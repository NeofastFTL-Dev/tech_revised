package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class StrandChopperBlockEntity extends GenericIndustrialMachineBlockEntity {
    public StrandChopperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STRAND_CHOPPER.get(), pos, state, ModRecipes.ChoppingRecipeType.INSTANCE, "block.tech_revised.strand_chopping_machinery");
    }
}
