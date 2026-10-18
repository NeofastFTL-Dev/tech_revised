package net.minecraftforge.client.model;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
public class DynamicFluidContainerModel {
    public static class Colors implements ItemColor {
        @Override public int getColor(ItemStack stack, int tintIndex) {
            return tintIndex == 1 ? 0xFFFFFFFF : 0xFFFFFFFF;
        }
    }
}
