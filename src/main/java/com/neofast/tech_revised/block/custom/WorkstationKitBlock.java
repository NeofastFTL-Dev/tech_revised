package com.neofast.tech_revised.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.EnumMap;
import java.util.Map;

public class WorkstationKitBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public static final VoxelShape SHAPE_DESK = Block.box(0, 8, 0, 16, 12, 16);
    public static final VoxelShape SHAPE_KEYBOARD = Shapes.or(
            SHAPE_DESK,
            Block.box(0, 4, 0, 16, 8, 1),
            Block.box(1, 12, 3, 15, 14, 13));
    public static final VoxelShape SHAPE_MOUSE = Shapes.or(
            SHAPE_DESK,
            Block.box(0, 4, 0, 16, 8, 1),
            Block.box(5, 12, 5, 12, 15, 12));
    public static final VoxelShape SHAPE_HDMI = Shapes.or(
            SHAPE_DESK,
            Block.box(0, 4, 0, 16, 8, 1),
            Block.box(1, 12, 6, 15, 13, 10));
    public static final VoxelShape SHAPE_SPEAKER = Shapes.or(
            SHAPE_DESK,
            Block.box(3, 12, 4, 13, 16, 12));
    public static final VoxelShape SHAPE_TOWER = Block.box(3, 0, 4, 13, 16, 12);
    public static final VoxelShape SHAPE_POWER = Block.box(2, 0, 5, 14, 4, 12);
    public static final VoxelShape SHAPE_CHAIR_SEAT = Shapes.or(
            Block.box(3, 0, 3, 13, 2, 13),
            Block.box(7, 2, 7, 9, 8, 9),
            Block.box(1, 8, 1, 15, 11, 15));
    public static final VoxelShape SHAPE_CHAIR_BACK = Shapes.or(
            Block.box(6, 0, 1, 10, 4, 4),
            Block.box(2, 4, 1, 14, 16, 4));
    public static final VoxelShape SHAPE_MONITOR = Shapes.or(
            SHAPE_DESK,
            Block.box(6, 12, 7, 10, 14, 11),
            Block.box(1, 12, 6, 15, 16, 9));

    private final Map<Direction, VoxelShape> shapes;

    public WorkstationKitBlock(Properties properties, VoxelShape northShape) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FORMED, false));
        this.shapes = rotateAll(northShape);
    }

    public static Map<Direction, VoxelShape> rotateAll(VoxelShape north) {
        EnumMap<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        map.put(Direction.NORTH, north);
        map.put(Direction.EAST, rotateY(north, 1));
        map.put(Direction.SOUTH, rotateY(north, 2));
        map.put(Direction.WEST, rotateY(north, 3));
        return map;
    }

    public static VoxelShape rotateY(VoxelShape shape, int quarterTurns) {
        VoxelShape result = shape;
        for (int i = 0; i < quarterTurns; i++) {
            VoxelShape[] next = {Shapes.empty()};
            result.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    next[0] = Shapes.or(next[0], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            result = next[0];
        }
        return result;
    }

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
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(FORMED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.getOrDefault(state.getValue(FACING), SHAPE_DESK);
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED);
    }
}
