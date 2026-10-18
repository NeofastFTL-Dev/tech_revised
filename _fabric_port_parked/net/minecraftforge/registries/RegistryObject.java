package net.minecraftforge.registries;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;
public class RegistryObject<T> implements Supplier<T> {
    private final ResourceLocation id;
    private T value;
    public RegistryObject(ResourceLocation id) { this.id = id; }
    public void setValue(T value) { this.value = value; }
    @Override public T get() {
        if (value == null) throw new IllegalStateException("Registry object not present: " + id);
        return value;
    }
    public ResourceLocation getId() { return id; }
    public boolean isPresent() { return value != null; }
}
