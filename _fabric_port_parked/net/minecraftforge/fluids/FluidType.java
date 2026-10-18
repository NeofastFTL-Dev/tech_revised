package net.minecraftforge.fluids;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import java.util.function.Consumer;
public class FluidType {
    private final Properties properties;
    public FluidType(Properties properties) { this.properties = properties; }
    public Properties getProperties() { return properties; }
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {}
    public static class Properties {
        public int density = 1000;
        public int viscosity = 1000;
        public int temperature = 300;
        public String descriptionId = "";
        public static Properties create() { return new Properties(); }
        public Properties density(int density) { this.density = density; return this; }
        public Properties viscosity(int viscosity) { this.viscosity = viscosity; return this; }
        public Properties temperature(int temperature) { this.temperature = temperature; return this; }
        public Properties descriptionId(String descriptionId) { this.descriptionId = descriptionId; return this; }
    }
}
