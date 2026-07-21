package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.recipe.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class GatheringWinderBlockEntity extends GenericIndustrialMachineBlockEntity {
    public GatheringWinderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GATHERING_WINDER.get(), pos, state, ModRecipes.WindingRecipeType.INSTANCE, "block.tech_revised.high_speed_gathering_winder");
    }
}
