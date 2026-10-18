package com.neofast.tech_revised.multiblock;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.multiblock.MultiblockStructureBlueprint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class MachineLayout {
    public record Cell(int x, int y, int z, Supplier<Block> block) {
    }

    private final String id;
    private final String formedKey;
    private final String incompleteKey;
    private final Supplier<Block> controller;
    private final List<Cell> cells;

    private MachineLayout(String id, String formedKey, String incompleteKey, Supplier<Block> controller, List<Cell> cells) {
        this.id = id;
        this.formedKey = formedKey;
        this.incompleteKey = incompleteKey;
        this.controller = controller;
        this.cells = List.copyOf(cells);
    }

    public String getId() {
        return id;
    }

    public String getFormedKey() {
        return formedKey;
    }

    public String getIncompleteKey() {
        return incompleteKey;
    }

    public Block getController() {
        return controller.get();
    }

    public List<Cell> getCells() {
        return cells;
    }

    public boolean isValid(Level level, BlockPos origin, Direction front) {
        Block casing = null;
        Map<Block, Integer> requiredIo = new HashMap<>();
        for (Cell cell : cells) {
            Block expected = cell.block().get();
            if (isCasing(expected)) {
                casing = expected;
            } else if (isIoPart(expected)) {
                requiredIo.merge(expected, 1, Integer::sum);
            }
        }

        Map<Block, Integer> foundIo = new HashMap<>();
        for (Cell cell : cells) {
            if (cell.x() == 0 && cell.y() == 0 && cell.z() == 0) {
                continue;
            }
            Block expected = cell.block().get();
            BlockState state = level.getBlockState(localToWorld(origin, front, cell.x(), cell.y(), cell.z()));
            Block actual = state.getBlock();
            if (isCasing(expected) || isIoPart(expected)) {
                if (isIoPart(actual)) {
                    foundIo.merge(actual, 1, Integer::sum);
                } else if (casing == null || !state.is(casing)) {
                    return false;
                }
            } else if (!state.is(expected)) {
                return false;
            }
        }

        for (Map.Entry<Block, Integer> requirement : requiredIo.entrySet()) {
            if (foundIo.getOrDefault(requirement.getKey(), 0) < requirement.getValue()) {
                return false;
            }
        }
        return true;
    }

    public <T extends BlockEntity> T findBlockEntity(Level level, BlockPos origin, Direction front, Class<T> type) {
        List<T> found = findBlockEntities(level, origin, front, type);
        return found.isEmpty() ? null : found.get(0);
    }

    public <T extends BlockEntity> List<T> findBlockEntities(Level level, BlockPos origin, Direction front, Class<T> type) {
        List<T> found = new ArrayList<>();
        for (Cell cell : cells) {
            if (cell.x() == 0 && cell.y() == 0 && cell.z() == 0) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(localToWorld(origin, front, cell.x(), cell.y(), cell.z()));
            if (type.isInstance(blockEntity)) {
                found.add(type.cast(blockEntity));
            }
        }
        return found;
    }

    public static boolean isIoPart(Block block) {
        return block == ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get()
                || block == ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get()
                || block == ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get()
                || block == ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get()
                || block == ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get();
    }

    public static boolean isCasing(Block block) {
        return block == ModBlocks.TRANSPORT_FRAME.get()
                || block == ModBlocks.ELECTRIC_ARC_FURNACE_FRAME.get()
                || block == ModBlocks.DRILLING_PLATFORM_FRAME.get()
                || block == ModBlocks.COKE_OVEN_FRAME.get()
                || block == ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_FRAME.get()
                || block == ModBlocks.OXYGEN_CONVERTER_FRAME.get();
    }

    public String validate(Level level, BlockPos origin, Direction front) {
        return isValid(level, origin, front) ? formedKey : incompleteKey;
    }

    public List<MultiblockAutoBuilder.Placement> placements(BlockPos origin, Direction front) {
        List<MultiblockAutoBuilder.Placement> placements = new ArrayList<>();
        for (Cell cell : cells) {
            if (cell.x() == 0 && cell.y() == 0 && cell.z() == 0) {
                continue;
            }
            placements.add(new MultiblockAutoBuilder.Placement(
                    localToWorld(origin, front, cell.x(), cell.y(), cell.z()),
                    cell.block().get()));
        }
        return placements;
    }

    public AABB worldAabb(BlockPos origin, Direction front) {
        int minX = origin.getX();
        int minY = origin.getY();
        int minZ = origin.getZ();
        int maxX = origin.getX();
        int maxY = origin.getY();
        int maxZ = origin.getZ();
        for (Cell cell : cells) {
            BlockPos worldPos = localToWorld(origin, front, cell.x(), cell.y(), cell.z());
            minX = Math.min(minX, worldPos.getX());
            minY = Math.min(minY, worldPos.getY());
            minZ = Math.min(minZ, worldPos.getZ());
            maxX = Math.max(maxX, worldPos.getX());
            maxY = Math.max(maxY, worldPos.getY());
            maxZ = Math.max(maxZ, worldPos.getZ());
        }
        return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    public void updateFormedStates(Level level, BlockPos origin, Direction front, boolean formed) {
        if (level.isClientSide()) {
            return;
        }
        for (Cell cell : cells) {
            BlockPos worldPos = localToWorld(origin, front, cell.x(), cell.y(), cell.z());
            BlockState state = level.getBlockState(worldPos);
            for (var property : state.getProperties()) {
                if (property instanceof BooleanProperty formedProperty && "formed".equals(property.getName())) {
                    if (state.getValue(formedProperty) != formed) {
                        level.setBlock(worldPos, state.setValue(formedProperty, formed), Block.UPDATE_CLIENTS);
                    }
                    break;
                }
            }
        }
    }

    public MultiblockStructureBlueprint toBlueprint() {
        List<MultiblockStructureBlueprint.Cell> blueprintCells = new ArrayList<>();
        for (Cell cell : cells) {
            blueprintCells.add(new MultiblockStructureBlueprint.Cell(
                    cell.x(), cell.y(), cell.z(), new ItemStack(cell.block().get())));
        }
        return MultiblockStructureBlueprint.fromCells(id, blueprintCells);
    }

    public static BlockPos localToWorld(BlockPos origin, Direction front, int localX, int localY, int localZ) {
        Direction right = front.getClockWise();
        Direction back = front.getOpposite();
        return origin.relative(right, localX).relative(back, localZ).offset(0, localY, 0);
    }

    public static Builder builder(String id, Supplier<Block> controller) {
        return new Builder(id, controller);
    }

    public static final class Builder {
        private final String id;
        private final Supplier<Block> controller;
        private String formedKey = "message.tech_revised.multiblock.formed";
        private String incompleteKey = "message.tech_revised.multiblock.incomplete";
        private final Map<Long, Cell> cells = new LinkedHashMap<>();

        private Builder(String id, Supplier<Block> controller) {
            this.id = id;
            this.controller = controller;
            put(0, 0, 0, controller);
        }

        public Builder messages(String formedKey, String incompleteKey) {
            this.formedKey = formedKey;
            this.incompleteKey = incompleteKey;
            return this;
        }

        public Builder cell(int x, int y, int z, Supplier<Block> block) {
            put(x, y, z, block);
            return this;
        }

        public Builder fill(int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Supplier<Block> block) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int x = minX; x <= maxX; x++) {
                        put(x, y, z, block);
                    }
                }
            }
            return this;
        }

        public Builder shell(int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Supplier<Block> block) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int x = minX; x <= maxX; x++) {
                        if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                            put(x, y, z, block);
                        }
                    }
                }
            }
            return this;
        }

        public Builder ring(int minX, int maxX, int y, int minZ, int maxZ, Supplier<Block> block) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    if (x == minX || x == maxX || z == minZ || z == maxZ) {
                        put(x, y, z, block);
                    }
                }
            }
            return this;
        }

        public Builder cylinder(int minY, int maxY, int centerZ, int minDistSq, int maxDistSq, Supplier<Block> block) {
            int radius = (int) Math.ceil(Math.sqrt(maxDistSq));
            for (int y = minY; y <= maxY; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    for (int x = -radius; x <= radius; x++) {
                        int dist = x * x + (z - centerZ) * (z - centerZ);
                        if (dist >= minDistSq && dist <= maxDistSq) {
                            put(x, y, z, block);
                        }
                    }
                }
            }
            return this;
        }

        public Builder cylinderSolid(int y, int centerZ, int maxDistSq, Supplier<Block> block) {
            int radius = (int) Math.ceil(Math.sqrt(maxDistSq));
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                for (int x = -radius; x <= radius; x++) {
                    int dist = x * x + (z - centerZ) * (z - centerZ);
                    if (dist <= maxDistSq) {
                        put(x, y, z, block);
                    }
                }
            }
            return this;
        }

        public MachineLayout build() {
            put(0, 0, 0, controller);
            return new MachineLayout(id, formedKey, incompleteKey, controller, new ArrayList<>(cells.values()));
        }

        private void put(int x, int y, int z, Supplier<Block> block) {
            cells.put((((long) x & 0xFFFF) << 32) | (((long) y & 0xFFFF) << 16) | (z & 0xFFFF), new Cell(x, y, z, block));
        }
    }
}
