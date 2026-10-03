package com.neofast.tech_revised.block.custom;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.block.entity.custom.CokeOvenControllerBlockEntity;
import com.neofast.tech_revised.block.entity.custom.ElectricArcFurnaceFluidOutputBusBlockEntity;
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

public class CokeOvenControllerBlock extends ModBaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    private static final int MIN_X = -1;
    private static final int MAX_X = 1;
    private static final int MIN_Y = 0;
    private static final int MAX_Y = 2;
    private static final int MIN_Z = 0;
    private static final int MAX_Z = 2;

    public CokeOvenControllerBlock(Properties properties) {
        super(properties);
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

        if (hand == InteractionHand.MAIN_HAND
                && !heldStack.is(ModItems.CONFIGURATOR.get())
                && player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.CONFIGURATOR.get())) {
            return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (heldStack.is(ModItems.CONFIGURATOR.get())) {
            return tryAutoBuildWithConfigurator(level, pos, front, player);
        }

        if (player.isShiftKeyDown()) {
            MultiblockPreviewHelper.showPreview(player, pos, front, "coke_oven");
            player.displayClientMessage(Component.translatable("message.tech_revised.coke_oven.hologram"), true);
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }

        String validationKey = validateMultiblock(level, pos, front);
        boolean formed = "message.tech_revised.coke_oven.formed".equals(validationKey);
        updateFrameFormedStates(level, pos, front, formed);
        player.displayClientMessage(Component.translatable(validationKey), true);

        MultiblockAutoBuilder.openScreen(player, level.getBlockEntity(pos));
        return net.minecraft.world.ItemInteractionResult.CONSUME;
    }

    public static boolean isStructureValid(Level level, BlockPos controllerPos, Direction front) {
        return "message.tech_revised.coke_oven.formed".equals(validateMultiblock(level, controllerPos, front));
    }

    public static void updateFrameFormedStates(Level level, BlockPos controllerPos, Direction front, boolean formed) {
        MachineLayout layout = MachineLayouts.get(ModBlocks.COKE_OVEN_CONTROLLER.get());
        if (layout != null) {
            layout.updateFormedStates(level, controllerPos, front, formed);
        }
    }

    public static BlockPos getInputBusPos(BlockPos controllerPos, Direction front) {
        return localToWorld(controllerPos, front, -1, 0, 1);
    }

    public static BlockPos getOutputBusPos(BlockPos controllerPos, Direction front) {
        return localToWorld(controllerPos, front, 1, 0, 1);
    }

    public static BlockPos getFluidOutputBusPos(BlockPos controllerPos, Direction front) {
        return localToWorld(controllerPos, front, 0, 0, 2);
    }

    public static String validateMultiblock(Level level, BlockPos controllerPos, Direction front) {
        MachineLayout layout = MachineLayouts.get(ModBlocks.COKE_OVEN_CONTROLLER.get());
        return layout == null ? "message.tech_revised.coke_oven.incomplete" : layout.validate(level, controllerPos, front);
    }

    private static net.minecraft.world.ItemInteractionResult tryAutoBuildWithConfigurator(Level level, BlockPos controllerPos, Direction front, Player player) {
        MachineLayout layout = MachineLayouts.get(ModBlocks.COKE_OVEN_CONTROLLER.get());
        if (layout == null || !MultiblockAutoBuilder.tryBuild(level, player, layout.placements(controllerPos, front))) {
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }

        boolean formed = isStructureValid(level, controllerPos, front);
        updateFrameFormedStates(level, controllerPos, front, formed);
        player.displayClientMessage(Component.translatable(
                formed
                        ? "message.tech_revised.multiblock.autoconfig_success"
                        : "message.tech_revised.multiblock.autoconfig_incomplete"), true);
        MultiblockAutoBuilder.openScreen(player, level.getBlockEntity(controllerPos));
        return net.minecraft.world.ItemInteractionResult.CONSUME;
    }

    private static boolean matches(Level level, BlockPos controllerPos, Direction front,
                                   int localX, int localY, int localZ, Block expectedBlock) {
        BlockPos checkPos = localToWorld(controllerPos, front, localX, localY, localZ);
        return level.getBlockState(checkPos).is(expectedBlock);
    }

    private static boolean isControllerPosition(int localX, int localY, int localZ) {
        return localX == 0 && localY == 0 && localZ == 0;
    }

    private static Block getExpectedBlock(int localX, int localY, int localZ) {
        if (localX == -1 && localY == 0 && localZ == 1) {
            return ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get();
        }
        if (localX == 1 && localY == 0 && localZ == 1) {
            return ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get();
        }
        if (localX == 0 && localY == 0 && localZ == 2) {
            return ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get();
        }
        return ModBlocks.COKE_OVEN_FRAME.get();
    }

    private static String getMissingMessageForExpectedBlock(Block expectedBlock) {
        if (expectedBlock == ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get()) {
            return "message.tech_revised.coke_oven.missing_input_bus";
        }
        if (expectedBlock == ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get()) {
            return "message.tech_revised.coke_oven.missing_output_bus";
        }
        if (expectedBlock == ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get()) {
            return "message.tech_revised.coke_oven.missing_fluid_output_bus";
        }
        return "message.tech_revised.coke_oven.missing_frame";
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
        return new CokeOvenControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.COKE_OVEN_CONTROLLER.get(),
                CokeOvenControllerBlockEntity::tick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            updateFrameFormedStates(level, pos, state.getValue(FACING), false);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
