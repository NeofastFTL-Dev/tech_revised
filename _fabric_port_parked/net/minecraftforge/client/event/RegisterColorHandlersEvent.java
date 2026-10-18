package net.minecraftforge.client.event;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
public class RegisterColorHandlersEvent {
    public static class Block {
        private final BlockColors colors;
        public Block(BlockColors colors) { this.colors = colors; }
        public BlockColors getBlockColors() { return colors; }
    }
    public static class Item {
        private final ItemColors colors;
        public Item(ItemColors colors) { this.colors = colors; }
        public ItemColors getItemColors() { return colors; }
    }
}
