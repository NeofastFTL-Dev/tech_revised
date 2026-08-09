package com.neofast.tech_revised.integration.jei;

import com.neofast.tech_revised.block.ModBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.function.ToIntFunction;

/**
 * Immutable 3D multiblock blueprint used by JEI for isometric layer previews.
 * Coordinates are controller-local: +X right, +Y up, +Z behind controller.
 */
public final class MultiblockStructureBlueprint {
    public record Cell(int x, int y, int z, ItemStack stack) {
        public Cell {
            stack = stack.copy();
        }

        public ItemStack stack() {
            return stack.copy();
        }
    }

    private final String id;
    private final List<Cell> cells;
    private final int minX;
    private final int maxX;
    private final int minY;
    private final int maxY;
    private final int minZ;
    private final int maxZ;
    private final List<ItemStack> materials;

    private MultiblockStructureBlueprint(String id, List<Cell> cells) {
        this.id = id;
        this.cells = List.copyOf(cells);
        this.minX = cells.stream().mapToInt(Cell::x).min().orElse(0);
        this.maxX = cells.stream().mapToInt(Cell::x).max().orElse(0);
        this.minY = cells.stream().mapToInt(Cell::y).min().orElse(0);
        this.maxY = cells.stream().mapToInt(Cell::y).max().orElse(0);
        this.minZ = cells.stream().mapToInt(Cell::z).min().orElse(0);
        this.maxZ = cells.stream().mapToInt(Cell::z).max().orElse(0);
        this.materials = computeMaterials(this.cells);
    }

    public String getId() {
        return id;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public List<Cell> getCellsForLayer(int layerY) {
        return cells.stream().filter(c -> c.y() == layerY).toList();
    }

    public List<Cell> getCellsSortedForDraw(IntPredicate layerFilter) {
        return cells.stream()
                .filter(c -> layerFilter.test(c.y()))
                .sorted(Comparator
                        .comparingInt(Cell::y)
                        .thenComparingInt((Cell c) -> c.x() + c.z())
                        .thenComparingInt(Cell::z)
                        .thenComparingInt(Cell::x))
                .toList();
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getSizeX() {
        return maxX - minX + 1;
    }

    public int getSizeY() {
        return maxY - minY + 1;
    }

    public int getSizeZ() {
        return maxZ - minZ + 1;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinZ() {
        return minZ;
    }

    public List<ItemStack> getMaterials() {
        return materials;
    }

    private static List<ItemStack> computeMaterials(List<Cell> cells) {
        Map<Item, Integer> counts = new LinkedHashMap<>();
        for (Cell cell : cells) {
            counts.merge(cell.stack.getItem(), 1, Integer::sum);
        }
        // Controllers first, then specialty hatches/buses, frames last - matches JEI "what you need" reading order.
        List<Map.Entry<Item, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort((a, b) -> {
            int ra = materialRank(a.getKey());
            int rb = materialRank(b.getKey());
            if (ra != rb) {
                return Integer.compare(ra, rb);
            }
            return a.getKey().toString().compareTo(b.getKey().toString());
        });
        List<ItemStack> out = new ArrayList<>();
        for (Map.Entry<Item, Integer> e : entries) {
            out.add(new ItemStack(e.getKey(), e.getValue()));
        }
        return List.copyOf(out);
    }

    private static int materialRank(Item item) {
        String path = item.builtInRegistryHolder().key().location().getPath();
        if (path.contains("controller")) {
            return 0;
        }
        if (path.contains("heater") || path.contains("drill")) {
            return 1;
        }
        if (path.contains("bus") || path.contains("hatch") || path.contains("energy")) {
            return 2;
        }
        if (path.contains("frame")) {
            return 4;
        }
        return 3;
    }

    private static MultiblockStructureBlueprint build(
            String id,
            int minX, int maxX, int minY, int maxY, int minZ, int maxZ,
            BiPredicate<Integer, Integer> includeXZ,
            ToIntFunction3 expectedBlock
    ) {
        List<Cell> cells = new ArrayList<>();
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    if (!includeXZ.test(x, z)) {
                        continue;
                    }
                    Block block = expectedBlock.apply(x, y, z);
                    if (block == null) {
                        continue;
                    }
                    cells.add(new Cell(x, y, z, new ItemStack(block)));
                }
            }
        }
        return new MultiblockStructureBlueprint(id, cells);
    }

    @FunctionalInterface
    private interface ToIntFunction3 {
        Block apply(int x, int y, int z);
    }

    public static MultiblockStructureBlueprint electricArcFurnace() {
        return build("eaf", -1, 1, -1, 2, 0, 2,
                (x, z) -> true,
                (x, y, z) -> {
                    if (x == 0 && y == 0 && z == 0) return ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get();
                    if (x == 0 && y == 0 && z == 1) return ModBlocks.ELECTRIC_ARC_FURNACE_HEATER.get();
                    if (x == -1 && y == 0 && z == 1) return ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get();
                    if (x == 1 && y == 0 && z == 1) return ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get();
                    if (x == -1 && y == 0 && z == 2) return ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get();
                    if (x == 0 && y == 0 && z == 2) return ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get();
                    if (x == 1 && y == 0 && z == 2) return ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get();
                    return ModBlocks.ELECTRIC_ARC_FURNACE_FRAME.get();
                });
    }

    public static MultiblockStructureBlueprint cokeOven() {
        return build("coke_oven", -1, 1, 0, 2, 0, 2,
                (x, z) -> true,
                (x, y, z) -> {
                    if (x == 0 && y == 0 && z == 0) return ModBlocks.COKE_OVEN_CONTROLLER.get();
                    if (x == -1 && y == 0 && z == 1) return ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get();
                    if (x == 1 && y == 0 && z == 1) return ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get();
                    if (x == 0 && y == 0 && z == 2) return ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get();
                    return ModBlocks.COKE_OVEN_FRAME.get();
                });
    }

    public static MultiblockStructureBlueprint drillingPlatform() {
        return build("drilling_platform", -2, 2, 0, 2, 0, 3,
                (x, z) -> true,
                (x, y, z) -> {
                    if (x == 0 && y == 0 && z == 0) return ModBlocks.DRILLING_PLATFORM_CONTROLLER.get();
                    if (x == 0 && y == 0 && z == 1) return ModBlocks.DRILLING_PLATFORM_DRILL_HEAD.get();
                    if (x == -2 && y == 0 && z == 3) return ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get();
                    if (x == 2 && y == 0 && z == 3) return ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get();
                    return ModBlocks.DRILLING_PLATFORM_FRAME.get();
                });
    }

    public static MultiblockStructureBlueprint oxygenConverter() {
        return build("oxygen_converter", -2, 2, 0, 6, 0, 4,
                MultiblockStructureBlueprint::isCylinderShell,
                (x, y, z) -> {
                    if (x == 0 && y == 0 && z == 0) return ModBlocks.OXYGEN_CONVERTER_CONTROLLER.get();
                    if (x == -1 && y == 0 && z == 0) return ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get();
                    if (x == 1 && y == 0 && z == 0) return ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get();
                    if (x == -2 && y == 0 && z == 2) return ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get();
                    if (x == 2 && y == 0 && z == 2) return ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_OUTPUT_BUS.get();
                    return ModBlocks.OXYGEN_CONVERTER_FRAME.get();
                });
    }

    public static MultiblockStructureBlueprint aodConverter() {
        return build("aod_converter", -2, 2, 0, 11, 0, 4,
                MultiblockStructureBlueprint::isCylinderShell,
                (x, y, z) -> {
                    if (x == 0 && y == 0 && z == 0) return ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER.get();
                    if (x == -2 && y == 0 && z == 2) return ModBlocks.ELECTRIC_ARC_FURNACE_INPUT_BUS.get();
                    if (x == 2 && y == 0 && z == 2) return ModBlocks.ELECTRIC_ARC_FURNACE_OUTPUT_BUS.get();
                    if (x == 1 && y == 0 && z == 0) return ModBlocks.ELECTRIC_ARC_FURNACE_FLUID_INPUT_BUS.get();
                    if (x == -1 && y == 0 && z == 0) return ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get();
                    return ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_FRAME.get();
                });
    }

    private static boolean isCylinderShell(int localX, int localZ) {
        int centeredZ = localZ - 2;
        int distanceSquared = localX * localX + centeredZ * centeredZ;
        return distanceSquared >= 2 && distanceSquared <= 5;
    }
}
