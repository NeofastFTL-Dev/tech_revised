package com.neofast.tech_revised.block.entity.custom;

import com.neofast.tech_revised.block.custom.CokeOvenControllerBlock;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.multiblock.MachineLayout;
import com.neofast.tech_revised.multiblock.MachineLayouts;
import com.neofast.tech_revised.multiblock.MultiblockControllerAccess;
import com.neofast.tech_revised.multiblock.MultiblockRender;
import com.neofast.tech_revised.recipe.CokeOvenRecipe;
import com.neofast.tech_revised.screen.MultiblockControllerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Optional;

public class CokeOvenControllerBlockEntity extends BlockEntity implements MenuProvider, MultiblockControllerAccess {
    private static final int DEFAULT_PROCESS_TICKS = 200;

    private int progress = 0;

    public CokeOvenControllerBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.COKE_OVEN_CONTROLLER.get(), worldPosition, blockState);
    }

        public AABB getRenderBoundingBox() {
        return MultiblockRender.bounds(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        tag.putInt("coke_oven_progress", progress);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        progress = tag.getInt("coke_oven_progress");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CokeOvenControllerBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        Direction front = state.getValue(CokeOvenControllerBlock.FACING);
        boolean formed = CokeOvenControllerBlock.isStructureValid(level, pos, front);
        CokeOvenControllerBlock.updateFrameFormedStates(level, pos, front, formed);
        if (!formed) {
            blockEntity.resetProgress();
            return;
        }

        MachineLayout layout = MachineLayouts.get(state.getBlock());
        ElectricArcFurnaceInputBusBlockEntity inputBus = layout == null ? null
                : layout.findBlockEntity(level, pos, front, ElectricArcFurnaceInputBusBlockEntity.class);
        ElectricArcFurnaceOutputBusBlockEntity outputBus = layout == null ? null
                : layout.findBlockEntity(level, pos, front, ElectricArcFurnaceOutputBusBlockEntity.class);
        ElectricArcFurnaceFluidOutputBusBlockEntity fluidOutputBus = layout == null ? null
                : layout.findBlockEntity(level, pos, front, ElectricArcFurnaceFluidOutputBusBlockEntity.class);

        if (inputBus == null || outputBus == null || fluidOutputBus == null) {
            blockEntity.resetProgress();
            return;
        }

        Optional<CokeOvenRecipe> recipe = getRecipe(level, inputBus);
        if (recipe.isEmpty()) {
            blockEntity.resetProgress();
            return;
        }

        CokeOvenRecipe currentRecipe = recipe.get();
        int matchingSlot = currentRecipe.findMatchingSlot(inputBus.getPrimaryInputStack(), inputBus.getSecondaryInputStack());
        if (matchingSlot < 0) {
            blockEntity.resetProgress();
            return;
        }

        ItemStack cokeOutput = currentRecipe.getResultItem(level.registryAccess()).copy();
        if (!outputBus.canAccept(cokeOutput)) {
            blockEntity.resetProgress();
            return;
        }

        FluidStack creosote = currentRecipe.getOutputFluid();
        if (fluidOutputBus.fill(creosote, IFluidHandler.FluidAction.SIMULATE) < creosote.getAmount()) {
            blockEntity.resetProgress();
            return;
        }

        blockEntity.progress++;
        if (blockEntity.progress < currentRecipe.getProcessTicks()) {
            blockEntity.setChanged();
            return;
        }

        if (matchingSlot == 0) {
            inputBus.extractPrimaryOne();
        } else {
            inputBus.extractSecondaryOne();
        }

        outputBus.insert(cokeOutput);
        fluidOutputBus.fill(creosote, IFluidHandler.FluidAction.EXECUTE);

        blockEntity.progress = 0;
        blockEntity.setChanged();
    }

    private static Optional<CokeOvenRecipe> getRecipe(Level level, ElectricArcFurnaceInputBusBlockEntity inputBus) {
        SimpleContainer inventory = new SimpleContainer(2);
        inventory.setItem(0, inputBus.getPrimaryInputStack().copy());
        inventory.setItem(1, inputBus.getSecondaryInputStack().copy());
        return level.getRecipeManager().getRecipeFor(CokeOvenRecipe.Type.INSTANCE, new com.neofast.tech_revised.recipe.ContainerInput(inventory), level).map(net.minecraft.world.item.crafting.RecipeHolder::value);
    }

    private void resetProgress() {
        if (progress != 0) {
            progress = 0;
            setChanged();
        }
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTicks() {
        return DEFAULT_PROCESS_TICKS;
    }

    @Override
    public boolean isStructureFormed() {
        if (level == null) {
            return false;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(CokeOvenControllerBlock.FACING)) {
            return false;
        }
        return CokeOvenControllerBlock.isStructureValid(level, worldPosition, state.getValue(CokeOvenControllerBlock.FACING));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.tech_revised.coke_oven_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MultiblockControllerMenu(containerId, inventory, this);
    }
}
