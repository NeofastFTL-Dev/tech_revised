package net.minecraftforge.items;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
public class SlotItemHandler extends Slot {
    private final IItemHandler handler;
    private final int slotIndex;
    public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(new HandlerContainer(itemHandler), index, xPosition, yPosition);
        this.handler = itemHandler;
        this.slotIndex = index;
    }
    public IItemHandler getItemHandler() { return handler; }
    public int getSlotIndex() { return slotIndex; }
    @Override public boolean mayPlace(ItemStack stack) { return handler.isItemValid(slotIndex, stack); }
    @Override public ItemStack getItem() { return handler.getStackInSlot(slotIndex); }
    @Override public void set(ItemStack stack) {
        if (handler instanceof ItemStackHandler stackHandler) {
            stackHandler.setStackInSlot(slotIndex, stack);
        } else {
            handler.extractItem(slotIndex, 64, false);
            handler.insertItem(slotIndex, stack, false);
        }
        setChanged();
    }
    @Override public void setChanged() { super.setChanged(); }
    @Override public int getMaxStackSize() { return handler.getSlotLimit(slotIndex); }
    @Override public int getMaxStackSize(ItemStack stack) { return Math.min(stack.getMaxStackSize(), getMaxStackSize()); }
    @Override public boolean mayPickup(Player player) { return !handler.extractItem(slotIndex, 1, true).isEmpty(); }
    @Override public ItemStack remove(int amount) { return handler.extractItem(slotIndex, amount, false); }
    private static final class HandlerContainer extends SimpleContainer {
        private final IItemHandler handler;
        private HandlerContainer(IItemHandler handler) {
            super(handler.getSlots());
            this.handler = handler;
        }
        @Override public ItemStack getItem(int slot) { return handler.getStackInSlot(slot); }
        @Override public void setItem(int slot, ItemStack stack) {
            if (handler instanceof ItemStackHandler stackHandler) {
                stackHandler.setStackInSlot(slot, stack);
            }
        }
        @Override public int getContainerSize() { return handler.getSlots(); }
    }
}
