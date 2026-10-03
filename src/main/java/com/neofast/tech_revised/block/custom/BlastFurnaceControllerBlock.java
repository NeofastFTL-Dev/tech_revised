package com.neofast.tech_revised.block.custom;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.block.entity.custom.BlastFurnaceControllerBlockEntity;
import com.neofast.tech_revised.item.ModItems;
import com.neofast.tech_revised.multiblock.MachineLayout;
import com.neofast.tech_revised.multiblock.MachineLayouts;
import com.neofast.tech_revised.multiblock.MultiblockAutoBuilder;
import com.neofast.tech_revised.multiblock.MultiblockPreviewHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlastFurnaceControllerBlock extends ModBaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public BlastFurnaceControllerBlock(Properties properties) {
        super(properties.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FORMED, false));
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED);
    }

    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return net.minecraft.world.ItemInteractionResult.SUCCESS;
        }

        Direction front = state.getValue(FACING);
        ItemStack heldStack = player.getItemInHand(hand);

        if (heldStack.is(ModItems.CONFIGURATOR.get())) {
            return tryAutoBuildWithConfigurator(level, pos, front, player);
        }

        if (player.isShiftKeyDown()) {
            MultiblockPreviewHelper.showPreview(player, pos, front, "blast_furnace");
            player.displayClientMessage(Component.translatable("message.tech_revised.blast_furnace.hologram"), true);
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BlastFurnaceControllerBlockEntity controller) {
            String validationKey = validateMultiblock(level, pos, front);
            boolean formed = "message.tech_revised.blast_furnace.formed".equals(validationKey);

            player.displayClientMessage(Component.translatable(validationKey), true);
            MultiblockAutoBuilder.openScreen(player, controller);
        }

        return net.minecraft.world.ItemInteractionResult.CONSUME;
    }

    public static boolean isStructureValid(Level level, BlockPos controllerPos, Direction front) {
        return "message.tech_revised.blast_furnace.formed".equals(validateMultiblock(level, controllerPos, front));
    }

    public static String validateMultiblock(Level level, BlockPos controllerPos, Direction front) {
        MachineLayout layout = MachineLayouts.get(ModBlocks.BLAST_FURNACE_CORE.get());
        return layout == null ? "message.tech_revised.blast_furnace.incomplete" : layout.validate(level, controllerPos, front);
    }

    private static Block getExpectedBlock(int x, int y, int z) {
        if (x == 0 && y == 0 && z == 2) return ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get();
        if (x == 1 && y == 0 && z == 0) return ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get();
        if (x == -1 && y == 0 && z == 0) return ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get();
        return ModBlocks.ELECTRIC_ARC_FURNACE_FRAME.get();
    }

    private net.minecraft.world.ItemInteractionResult tryAutoBuildWithConfigurator(Level level, BlockPos controllerPos, Direction front, Player player) {
        MachineLayout layout = MachineLayouts.get(ModBlocks.BLAST_FURNACE_CORE.get());
        if (layout == null || !MultiblockAutoBuilder.tryBuild(level, player, layout.placements(controllerPos, front))) {
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }

        boolean formed = isStructureValid(level, controllerPos, front);
        player.displayClientMessage(Component.translatable(
                formed
                        ? "message.tech_revised.multiblock.autoconfig_success"
                        : "message.tech_revised.multiblock.autoconfig_incomplete"), true);
        MultiblockAutoBuilder.openScreen(player, level.getBlockEntity(controllerPos));
        return net.minecraft.world.ItemInteractionResult.CONSUME;
    }

    private static BlockPos localToWorld(BlockPos origin, Direction front, int localX, int localY, int localZ) {
        Direction right = front.getClockWise();
        Direction back = front.getOpposite();
        return origin.relative(right, localX).relative(back, localZ).offset(0, localY, 0);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return com.neofast.tech_revised.multiblock.MultiblockRender.renderShape(state);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlastFurnaceControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.BLAST_FURNACE_CONTROLLER.get(), BlastFurnaceControllerBlockEntity::tick);
    }
}
