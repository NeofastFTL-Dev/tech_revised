package net.minecraftforge.fluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import java.util.function.Supplier;
public class FluidStack {
    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);
    private final Fluid fluid;
    private int amount;
    public FluidStack(Fluid fluid, int amount) {
        this.fluid = fluid == null ? Fluids.EMPTY : fluid;
        this.amount = amount;
    }
    public FluidStack(Supplier<? extends Fluid> fluid, int amount) { this(fluid.get(), amount); }
    public Fluid getFluid() { return fluid; }
    public int getAmount() { return isEmpty() ? 0 : amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public void grow(int amount) { this.amount += amount; }
    public void shrink(int amount) { this.amount -= amount; }
    public boolean isEmpty() { return amount <= 0 || fluid == Fluids.EMPTY; }
    public FluidStack copy() { return new FluidStack(fluid, amount); }
    public boolean isFluidEqual(FluidStack other) { return other != null && getFluid() == other.getFluid(); }
    public net.minecraft.network.chat.Component getDisplayName() {
        net.minecraft.resources.ResourceLocation key = BuiltInRegistries.FLUID.getKey(getFluid());
        String id = key == null ? "minecraft.empty" : key.toLanguageKey();
        return net.minecraft.network.chat.Component.translatable("fluid." + id.replace(':', '.'));
    }
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putString("FluidName", BuiltInRegistries.FLUID.getKey(getFluid()).toString());
        nbt.putInt("Amount", amount);
        return nbt;
    }
    public static FluidStack loadFluidStackFromNBT(CompoundTag nbt) {
        if (nbt == null || !nbt.contains("FluidName")) return EMPTY.copy();
        Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(nbt.getString("FluidName")));
        if (fluid == null) fluid = Fluids.EMPTY;
        return new FluidStack(fluid, nbt.getInt("Amount"));
    }
}
