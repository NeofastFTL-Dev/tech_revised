package com.neofast.tech_revised.block.custom;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.block.entity.custom.BlastFurnaceControllerBlockEntity;
import com.neofast.tech_revised.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlastFurnaceControllerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BlastFurnaceControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
        builder.add(FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Direction front = state.getValue(FACING);
        ItemStack heldStack = player.getItemInHand(hand);

        if (heldStack.is(ModItems.CONFIGURATOR.get())) {
            return tryAutoBuildWithConfigurator(level, pos, front, player);
        }

        if (player.isShiftKeyDown()) {
            player.displayClientMessage(Component.translatable("message.tech_revised.blast_furnace.hologram"), true);
            return InteractionResult.CONSUME;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BlastFurnaceControllerBlockEntity controller) {
            String validationKey = validateMultiblock(level, pos, front);
            boolean formed = "message.tech_revised.blast_furnace.formed".equals(validationKey);

            if (formed) {
                NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player, controller, pos);
            } else {
                player.displayClientMessage(Component.translatable(validationKey), true);
            }
        }

        return InteractionResult.CONSUME;
    }

    public static boolean isStructureValid(Level level, BlockPos controllerPos, Direction front) {
        return "message.tech_revised.blast_furnace.formed".equals(validateMultiblock(level, controllerPos, front));
    }

    public static String validateMultiblock(Level level, BlockPos controllerPos, Direction front) {
        for (int y = 0; y < 4; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = 0; z < 3; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    Block expected = getExpectedBlock(x, y, z);
                    BlockPos target = localToWorld(controllerPos, front, x, y, z);
                    if (!level.getBlockState(target).is(expected)) {
                        return "message.tech_revised.blast_furnace.incomplete";
                    }
                }
            }
        }
        return "message.tech_revised.blast_furnace.formed";
    }

    private static Block getExpectedBlock(int x, int y, int z) {
        if (x == 0 && y == 0 && z == 2) return ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get();
        if (x == 1 && y == 0 && z == 0) return ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get();
        if (x == -1 && y == 0 && z == 0) return ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get();
        return ModBlocks.ELECTRIC_ARC_FURNACE_FRAME.get();
    }

    private InteractionResult tryAutoBuildWithConfigurator(Level level, BlockPos controllerPos, Direction front, Player player) {
        return InteractionResult.SUCCESS;
    }

    private static BlockPos localToWorld(BlockPos origin, Direction front, int localX, int localY, int localZ) {
        Direction right = front.getClockWise();
        Direction back = front.getOpposite();
        return origin.relative(right, localX).relative(back, localZ).offset(0, localY, 0);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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
