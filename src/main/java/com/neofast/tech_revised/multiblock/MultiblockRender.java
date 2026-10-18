package com.neofast.tech_revised.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public final class MultiblockRender {
    private MultiblockRender() {
    }

    public static RenderShape renderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static AABB bounds(BlockEntity blockEntity) {
        BlockState state = blockEntity.getBlockState();
        MachineLayout layout = MachineLayouts.get(state.getBlock());
        if (layout == null || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return new AABB(blockEntity.getBlockPos());
        }
        return layout.worldAabb(blockEntity.getBlockPos(), state.getValue(BlockStateProperties.HORIZONTAL_FACING));
    }

    public static void syncFormed(BlockEntity blockEntity) {
        if (blockEntity.getLevel() == null || blockEntity.getLevel().isClientSide()) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        MachineLayout layout = MachineLayouts.get(state.getBlock());
        if (layout == null || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return;
        }
        Direction front = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos pos = blockEntity.getBlockPos();
        layout.updateFormedStates(blockEntity.getLevel(), pos, front, layout.isValid(blockEntity.getLevel(), pos, front));
    }
}
