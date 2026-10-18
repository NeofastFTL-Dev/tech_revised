package com.neofast.tech_revised.screen;

import com.neofast.tech_revised.multiblock.MultiblockControllerAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MultiblockControllerMenu extends AbstractContainerMenu {
    private final BlockEntity blockEntity;
    private final MultiblockControllerAccess controller;
    private final Level level;
    private final ContainerData data;
    private final int[] syncedData = new int[7];

    public MultiblockControllerMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public MultiblockControllerMenu(int containerId, Inventory inventory, BlockEntity entity) {
        super(ModMenuTypes.MULTIBLOCK_CONTROLLER_MENU.get(), containerId);
        this.blockEntity = entity;
        this.controller = entity instanceof MultiblockControllerAccess access ? access : null;
        this.level = inventory.player.level();

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (index < 0 || index >= syncedData.length) {
                    return 0;
                }
                if (level.isClientSide()) {
                    return syncedData[index];
                }
                if (controller == null) {
                    return 0;
                }
                return switch (index) {
                    case 0 -> controller.getProgress();
                    case 1 -> controller.getProcessTicks();
                    case 2 -> controller.isStructureFormed() ? 1 : 0;
                    case 3 -> controller.getEnergyStored();
                    case 4 -> controller.getMaxEnergyStored();
                    case 5 -> controller.getAuxiliaryAmount();
                    case 6 -> controller.getAuxiliaryCapacity();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < syncedData.length) {
                    syncedData[index] = value;
                }
            }

            @Override
            public int getCount() {
                return syncedData.length;
            }
        };
        addDataSlots(this.data);
        addPlayerInventory(inventory);
        addPlayerHotbar(inventory);
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getProcessTicks() {
        return Math.max(1, data.get(1));
    }

    public boolean isStructureFormed() {
        return data.get(2) == 1;
    }

    public int getEnergyStored() {
        return data.get(3);
    }

    public int getMaxEnergyStored() {
        return data.get(4);
    }

    public int getAuxiliaryAmount() {
        return data.get(5);
    }

    public int getAuxiliaryCapacity() {
        return data.get(6);
    }

    public String getAuxiliaryLabel() {
        return controller == null ? "" : controller.getAuxiliaryLabel();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null) {
            return false;
        }
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, blockEntity.getBlockState().getBlock());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
