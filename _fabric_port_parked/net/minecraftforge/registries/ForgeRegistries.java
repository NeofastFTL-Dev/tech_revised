package net.minecraftforge.registries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidType;
public final class ForgeRegistries {
    private ForgeRegistries() {}
    public static final IForgeRegistry<Block> BLOCKS = new VanillaBacked<>(BuiltInRegistries.BLOCK);
    public static final IForgeRegistry<Item> ITEMS = new VanillaBacked<>(BuiltInRegistries.ITEM);
    public static final IForgeRegistry<Fluid> FLUIDS = new VanillaBacked<>(BuiltInRegistries.FLUID);
    public static final IForgeRegistry<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new VanillaBacked<>(BuiltInRegistries.BLOCK_ENTITY_TYPE);
    public static final IForgeRegistry<MenuType<?>> MENU_TYPES = new VanillaBacked<>(BuiltInRegistries.MENU);
    public static final IForgeRegistry<RecipeSerializer<?>> RECIPE_SERIALIZERS = new VanillaBacked<>(BuiltInRegistries.RECIPE_SERIALIZER);
    public static final class Keys {
        public static final ResourceKey<Registry<FluidType>> FLUID_TYPES =
                ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("forge", "fluid_type"));
    }
    public interface IForgeRegistry<V> {
        V getValue(ResourceLocation id);
        ResourceLocation getKey(V value);
        Registry<V> vanilla();
    }
    private static final class VanillaBacked<V> implements IForgeRegistry<V> {
        private final Registry<V> registry;
        private VanillaBacked(Registry<V> registry) { this.registry = registry; }
        @Override public V getValue(ResourceLocation id) {
            V value = registry.get(id);
            if (value == null && registry == BuiltInRegistries.FLUID) {
                return (V) Fluids.EMPTY;
            }
            return value;
        }
        @Override public ResourceLocation getKey(V value) { return registry.getKey(value); }
        @Override public Registry<V> vanilla() { return registry; }
    }
}
