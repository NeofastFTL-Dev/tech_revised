package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class CreelConverterBlockEntity extends GenericIndustrialMachineBlockEntity {
    public CreelConverterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREEL_CONVERTER.get(), pos, state, ModRecipes.BundlingRecipeType.INSTANCE, "block.tech_revised.roving_creel_converter");
    }
}
