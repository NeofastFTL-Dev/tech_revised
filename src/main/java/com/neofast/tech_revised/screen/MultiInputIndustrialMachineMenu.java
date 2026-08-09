package com.neofast.tech_revised.screen;

import com.neofast.tech_revised.block.entity.custom.MultiInputIndustrialMachineBlockEntity;
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
import net.minecraftforge.items.SlotItemHandler;

public class MultiInputIndustrialMachineMenu extends AbstractContainerMenu {
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    private final MultiInputIndustrialMachineBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    private final int[] syncedData = new int[4];
    private final int teInventorySlotCount;
    private final int inputSlotCount;

    public MultiInputIndustrialMachineMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public MultiInputIndustrialMachineMenu(int containerId, Inventory inventory, BlockEntity entity) {
        super(ModMenuTypes.MULTI_INPUT_INDUSTRIAL_MACHINE_MENU.get(), containerId);
        this.blockEntity = (MultiInputIndustrialMachineBlockEntity) entity;
        this.level = inventory.player.level();
        this.inputSlotCount = blockEntity.getInputSlotCount();
        this.teInventorySlotCount = inputSlotCount + 1;

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (index < 0 || index >= syncedData.length) {
                    return 0;
                }
                if (level.isClientSide()) {
                    return syncedData[index];
                }
                return switch (index) {
                    case 0 -> blockEntity.getProgress();
                    case 1 -> blockEntity.getProcessTicks();
                    case 2 -> blockEntity.getEnergyStored();
                    case 3 -> blockEntity.getMaxEnergyStored();
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

        // Input slots centered left of the output
        int inputStartX = 44 - ((inputSlotCount - 1) * 9);
        for (int i = 0; i < inputSlotCount; i++) {
            this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), i, inputStartX + (i * 18), 35));
        }
        this.addSlot(new SlotItemHandler(blockEntity.getItemHandler(), blockEntity.getOutputSlot(), 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        int teFirst = TE_INVENTORY_FIRST_SLOT_INDEX;
        int teLast = teFirst + teInventorySlotCount;
        int inputLast = teFirst + inputSlotCount;

        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (blockEntity.isValidInput(sourceStack)) {
                if (!moveItemStackTo(sourceStack, teFirst, inputLast, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        } else if (index < teLast) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX,
                    VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(player, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, blockEntity.getBlockState().getBlock());
    }

    public int getEnergyStored() {
        return data.get(2);
    }

    public int getMaxEnergyStored() {
        return Math.max(1, data.get(3));
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getProcessTicks() {
        return Math.max(1, data.get(1));
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
