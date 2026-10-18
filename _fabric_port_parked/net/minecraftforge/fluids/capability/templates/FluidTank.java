package net.minecraftforge.fluids.capability.templates;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import java.util.function.Predicate;
public class FluidTank implements IFluidHandler {
    protected Predicate<FluidStack> validator;
    protected FluidStack fluid = FluidStack.EMPTY;
    protected int capacity;
    public FluidTank(int capacity) { this(capacity, stack -> true); }
    public FluidTank(int capacity, Predicate<FluidStack> validator) {
        this.capacity = capacity;
        this.validator = validator;
    }
    public FluidTank setValidator(Predicate<FluidStack> validator) { this.validator = validator; return this; }
    @Override public int getTanks() { return 1; }
    @Override public FluidStack getFluidInTank(int tank) { return getFluid(); }
    public FluidStack getFluid() { return fluid; }
    public int getFluidAmount() { return fluid.getAmount(); }
    @Override public int getTankCapacity(int tank) { return capacity; }
    public int getCapacity() { return capacity; }
    @Override public boolean isFluidValid(int tank, FluidStack stack) { return validator.test(stack); }
    @Override public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(0, resource)) return 0;
        if (action.simulate()) {
            if (fluid.isEmpty()) return Math.min(capacity, resource.getAmount());
            if (!fluid.isFluidEqual(resource)) return 0;
            return Math.min(capacity - fluid.getAmount(), resource.getAmount());
        }
        if (fluid.isEmpty()) {
            fluid = new FluidStack(resource.getFluid(), Math.min(capacity, resource.getAmount()));
            onContentsChanged();
            return fluid.getAmount();
        }
        if (!fluid.isFluidEqual(resource)) return 0;
        int filled = capacity - fluid.getAmount();
        if (resource.getAmount() < filled) {
            fluid.grow(resource.getAmount());
            filled = resource.getAmount();
        } else {
            fluid.setAmount(capacity);
        }
        if (filled > 0) onContentsChanged();
        return filled;
    }
    @Override public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !resource.isFluidEqual(fluid)) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }
    @Override public FluidStack drain(int maxDrain, FluidAction action) {
        int drained = Math.min(fluid.getAmount(), maxDrain);
        FluidStack stack = new FluidStack(fluid.getFluid(), drained);
        if (action.execute() && drained > 0) {
            fluid.shrink(drained);
            if (fluid.getAmount() <= 0) fluid = FluidStack.EMPTY;
            onContentsChanged();
        }
        return stack;
    }
    public void setFluid(FluidStack stack) { this.fluid = stack == null ? FluidStack.EMPTY : stack; }
    public FluidTank readFromNBT(CompoundTag nbt) {
        fluid = FluidStack.loadFluidStackFromNBT(nbt);
        return this;
    }
    public CompoundTag writeToNBT(CompoundTag nbt) {
        fluid.writeToNBT(nbt);
        return nbt;
    }
    protected void onContentsChanged() {}
}
