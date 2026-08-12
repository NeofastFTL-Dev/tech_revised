package com.neofast.tech_revised.block.custom;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Shared structure frame used by multiblock machines (EAF and others).
 * Replaces the old machine-specific frame blocks in structure validation and auto-build.
 */
public class UniversalFrameBlock extends Block {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public UniversalFrameBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }
}
