
package com.neofast.tech_revised.recipe;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
public class ContainerInput implements RecipeInput {
    private final Container container;
    public ContainerInput(Container container) { this.container = container; }
    @Override public ItemStack getItem(int slot) { return container.getItem(slot); }
    @Override public int size() { return container.getContainerSize(); }
    public int getContainerSize() { return size(); }
    public Container asContainer() { return container; }
}
