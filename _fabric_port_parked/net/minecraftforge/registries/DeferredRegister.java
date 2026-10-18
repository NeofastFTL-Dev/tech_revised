package net.minecraftforge.registries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
public class DeferredRegister<T> {
    private static final Map<ResourceLocation, FluidType> FLUID_TYPES = new HashMap<>();
    private final String modid;
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final List<Pending<?>> entries = new ArrayList<>();
    private DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String modid) {
        this.registryKey = registryKey;
        this.modid = modid;
    }
    public static <T> DeferredRegister<T> create(ForgeRegistries.IForgeRegistry<T> registry, String modid) {
        return new DeferredRegister<>(registry.vanilla().key(), modid);
    }
    public static <T> DeferredRegister<T> create(Registry<T> registry, String modid) {
        return new DeferredRegister<>(registry.key(), modid);
    }
    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String modid) {
        return new DeferredRegister<>(key, modid);
    }
    @SuppressWarnings("unchecked")
    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> supplier) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modid, name);
        RegistryObject<I> obj = new RegistryObject<>(id);
        entries.add(new Pending<>(id, supplier, (RegistryObject<T>) obj));
        return obj;
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void register(IEventBus bus) {
        if (registryKey.location().equals(ForgeRegistries.Keys.FLUID_TYPES.location())) {
            for (Pending<?> pending : entries) {
                FluidType type = (FluidType) pending.supplier.get();
                ((RegistryObject) pending.object).setValue(type);
                FLUID_TYPES.put(pending.id, type);
            }
            return;
        }
        Registry registry = resolveRegistry();
        for (Pending<?> pending : entries) {
            Object value = pending.supplier.get();
            Registry.register(registry, pending.id, value);
            ((RegistryObject) pending.object).setValue(value);
        }
    }
    private Registry<?> resolveRegistry() {
        ResourceLocation loc = registryKey.location();
        if (loc.equals(Registries.CREATIVE_MODE_TAB.location())) return BuiltInRegistries.CREATIVE_MODE_TAB;
        if (loc.equals(Registries.BLOCK.location())) return BuiltInRegistries.BLOCK;
        if (loc.equals(Registries.ITEM.location())) return BuiltInRegistries.ITEM;
        if (loc.equals(Registries.FLUID.location())) return BuiltInRegistries.FLUID;
        if (loc.equals(Registries.BLOCK_ENTITY_TYPE.location())) return BuiltInRegistries.BLOCK_ENTITY_TYPE;
        if (loc.equals(Registries.MENU.location())) return BuiltInRegistries.MENU;
        if (loc.equals(Registries.RECIPE_SERIALIZER.location())) return BuiltInRegistries.RECIPE_SERIALIZER;
        if (loc.equals(Registries.RECIPE_TYPE.location())) return BuiltInRegistries.RECIPE_TYPE;
        Registry<?> found = BuiltInRegistries.REGISTRY.get(loc);
        if (found == null) throw new IllegalStateException("Unknown registry " + loc);
        return found;
    }
    public static FluidType getFluidType(ResourceLocation id) { return FLUID_TYPES.get(id); }
    private record Pending<T>(ResourceLocation id, Supplier<? extends T> supplier, RegistryObject<T> object) {}
}
