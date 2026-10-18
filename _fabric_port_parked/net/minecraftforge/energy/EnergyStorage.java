package net.minecraftforge.energy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
public class EnergyStorage implements IEnergyStorage {
    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;
    public EnergyStorage(int capacity) { this(capacity, capacity, capacity, 0); }
    public EnergyStorage(int capacity, int maxTransfer) { this(capacity, maxTransfer, maxTransfer, 0); }
    public EnergyStorage(int capacity, int maxReceive, int maxExtract) { this(capacity, maxReceive, maxExtract, 0); }
    public EnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.energy = Math.max(0, Math.min(capacity, energy));
    }
    @Override public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive()) return 0;
        int received = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) energy += received;
        return received;
    }
    @Override public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract()) return 0;
        int extracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) energy -= extracted;
        return extracted;
    }
    @Override public int getEnergyStored() { return energy; }
    @Override public int getMaxEnergyStored() { return capacity; }
    @Override public boolean canExtract() { return maxExtract > 0; }
    @Override public boolean canReceive() { return maxReceive > 0; }
    public Tag serializeNBT() {
        return IntTag.valueOf(energy);
    }
    public void deserializeNBT(Tag tag) {
        if (tag instanceof IntTag intTag) {
            this.energy = intTag.getAsInt();
        } else if (tag instanceof CompoundTag compound) {
            this.energy = compound.getInt("Energy");
        }
    }
}
