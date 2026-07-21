package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TextileLoomBlockEntity extends GenericIndustrialMachineBlockEntity {
    public TextileLoomBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TEXTILE_LOOM.get(), pos, state, ModRecipes.WeavingRecipeType.INSTANCE, "block.tech_revised.industrial_textile_weaving_loom");
    }
}
