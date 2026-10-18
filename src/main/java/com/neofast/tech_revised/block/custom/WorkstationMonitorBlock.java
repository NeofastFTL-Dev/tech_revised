package com.neofast.tech_revised.block.custom;

import com.neofast.tech_revised.block.custom.WorkstationKitBlock;
import com.neofast.tech_revised.item.ModItems;
import com.neofast.tech_revised.multiblock.MultiblockAutoBuilder;
import com.neofast.tech_revised.multiblock.MultiblockPreviewHelper;
import com.neofast.tech_revised.multiblock.WorkstationStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WorkstationMonitorBlock extends ModBaseEntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final java.util.Map<Direction, VoxelShape> shapes = WorkstationKitBlock.rotateAll(WorkstationKitBlock.SHAPE_MONITOR);

    public WorkstationMonitorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ACTIVE, false)
                .setValue(FORMED, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.getOrDefault(state.getValue(FACING), WorkstationKitBlock.SHAPE_MONITOR);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
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
    protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return net.minecraft.world.ItemInteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }

        Direction front = state.getValue(FACING);
        ItemStack heldStack = player.getItemInHand(hand);

        if (hand == InteractionHand.MAIN_HAND
                && !heldStack.is(ModItems.CONFIGURATOR.get())
                && player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.CONFIGURATOR.get())) {
            return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (heldStack.is(ModItems.CONFIGURATOR.get())) {
            if (WorkstationStructure.validate(level, pos, front) != null) {
                return tryAutoBuildWithConfigurator(level, pos, front, player);
            }
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            MultiblockPreviewHelper.showPreview(player, pos, front, "workstation");
            player.displayClientMessage(Component.translatable("message.tech_revised.workstation.hologram"), true);
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }

        Component missing = WorkstationStructure.validate(level, pos, front);
        if (missing != null) {
            player.displayClientMessage(missing, true);
            MultiblockPreviewHelper.showPreview(player, pos, front, "workstation");
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }

        return net.minecraft.world.ItemInteractionResult.CONSUME;
    }

    private static net.minecraft.world.ItemInteractionResult tryAutoBuildWithConfigurator(Level level, BlockPos controllerPos, Direction front, Player player) {
        List<MultiblockAutoBuilder.Placement> placements = new ArrayList<>();
        for (WorkstationStructure.Part part : WorkstationStructure.PARTS) {
            placements.add(new MultiblockAutoBuilder.Placement(
                    WorkstationStructure.localToWorld(controllerPos, front, part.x(), part.y(), part.z()),
                    part.block().get()));
        }

        if (!MultiblockAutoBuilder.tryBuild(level, player, placements)) {
            return net.minecraft.world.ItemInteractionResult.CONSUME;
        }

        boolean formed = WorkstationStructure.isValid(level, controllerPos, front);
        WorkstationStructure.updateFormedStates(level, controllerPos, front, formed);
        player.displayClientMessage(Component.translatable(
                formed
                        ? "message.tech_revised.multiblock.autoconfig_success"
                        : "message.tech_revised.multiblock.autoconfig_incomplete"), true);
        return net.minecraft.world.ItemInteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, FORMED, FACING);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()) {
            WorkstationStructure.updateFormedStates(level, pos, state.getValue(FACING), false);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
