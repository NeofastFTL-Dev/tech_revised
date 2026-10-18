package net.minecraftforge.common.capabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
public class ForgeCapabilities {
    public static final Capability<IItemHandler> ITEM_HANDLER = new Capability<>("item_handler");
    public static final Capability<IFluidHandler> FLUID_HANDLER = new Capability<>("fluid_handler");
    public static final Capability<IEnergyStorage> ENERGY = new Capability<>("energy");

    @SuppressWarnings("unchecked")
    public static <T> net.minecraftforge.common.util.LazyOptional<T> get(net.minecraft.world.level.block.entity.BlockEntity be, Capability<T> cap, net.minecraft.core.Direction side) {
        if (be == null) {
            return net.minecraftforge.common.util.LazyOptional.empty();
        }
        try {
            java.lang.reflect.Method method = be.getClass().getMethod("getCapability", Capability.class, net.minecraft.core.Direction.class);
            Object result = method.invoke(be, cap, side);
            if (result instanceof net.minecraftforge.common.util.LazyOptional<?> opt) {
                return (net.minecraftforge.common.util.LazyOptional<T>) opt;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return net.minecraftforge.common.util.LazyOptional.empty();
    }
}
