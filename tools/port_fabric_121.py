#!/usr/bin/env python3
"""Port tech_revised from Forge 1.20.1 to Fabric 1.21.1."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src" / "main" / "java"
RES = ROOT / "src" / "main" / "resources"
SHIM = JAVA / "net" / "minecraftforge"


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.replace("\r\n", "\n"), encoding="utf-8")


def write_shims() -> None:
    files = {}

    files["eventbus/api/IEventBus.java"] = """
package net.minecraftforge.eventbus.api;
public interface IEventBus {
    default void register(Object listener) {}
    default <T> void addListener(java.util.function.Consumer<T> listener) {}
}
"""

    files["eventbus/api/SubscribeEvent.java"] = """
package net.minecraftforge.eventbus.api;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {}
"""

    files["fml/common/Mod.java"] = """
package net.minecraftforge.fml.common;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.minecraftforge.api.distmarker.Dist;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    String value();
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface EventBusSubscriber {
        String modid() default "";
        Bus bus() default Bus.FORGE;
        Dist[] value() default {};
        enum Bus { FORGE, MOD }
    }
}
"""

    files["fml/javafmlmod/FMLJavaModLoadingContext.java"] = """
package net.minecraftforge.fml.javafmlmod;
import net.minecraftforge.eventbus.api.IEventBus;
public class FMLJavaModLoadingContext {
    private static final FMLJavaModLoadingContext INSTANCE = new FMLJavaModLoadingContext();
    private final IEventBus bus = new IEventBus() {};
    public static FMLJavaModLoadingContext get() { return INSTANCE; }
    public IEventBus getModEventBus() { return bus; }
}
"""

    files["fml/event/lifecycle/FMLCommonSetupEvent.java"] = """
package net.minecraftforge.fml.event.lifecycle;
public class FMLCommonSetupEvent {}
"""

    files["fml/event/lifecycle/FMLClientSetupEvent.java"] = """
package net.minecraftforge.fml.event.lifecycle;
public class FMLClientSetupEvent {
    public void enqueueWork(Runnable runnable) { runnable.run(); }
}
"""

    files["fml/DistExecutor.java"] = """
package net.minecraftforge.fml;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.api.distmarker.Dist;
import java.util.function.Supplier;
public class DistExecutor {
    public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> toRun) {
        EnvType env = FabricLoader.getInstance().getEnvironmentType();
        if (dist == Dist.CLIENT && env == EnvType.CLIENT) {
            toRun.get().run();
        } else if (dist == Dist.DEDICATED_SERVER && env == EnvType.SERVER) {
            toRun.get().run();
        }
    }
}
"""

    files["api/distmarker/Dist.java"] = """
package net.minecraftforge.api.distmarker;
public enum Dist { CLIENT, DEDICATED_SERVER }
"""

    files["common/MinecraftForge.java"] = """
package net.minecraftforge.common;
import net.minecraftforge.eventbus.api.IEventBus;
public class MinecraftForge {
    public static final IEventBus EVENT_BUS = new IEventBus() {};
}
"""

    files["common/ForgeHooks.java"] = """
package net.minecraftforge.common;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
public class ForgeHooks {
    public static int getBurnTime(ItemStack stack, RecipeType<?> type) {
        if (stack.isEmpty()) return 0;
        Integer time = FuelRegistry.INSTANCE.get(stack.getItem());
        return time == null ? 0 : time;
    }
}
"""

    files["registries/RegistryObject.java"] = """
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
"""

    files["registries/ForgeRegistries.java"] = """
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
"""

    files["registries/DeferredRegister.java"] = """
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
"""

    files["energy/IEnergyStorage.java"] = """
package net.minecraftforge.energy;
public interface IEnergyStorage {
    int receiveEnergy(int maxReceive, boolean simulate);
    int extractEnergy(int maxExtract, boolean simulate);
    int getEnergyStored();
    int getMaxEnergyStored();
    boolean canExtract();
    boolean canReceive();
}
"""

    files["energy/EnergyStorage.java"] = """
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
"""

    files["items/IItemHandler.java"] = """
package net.minecraftforge.items;
import net.minecraft.world.item.ItemStack;
public interface IItemHandler {
    int getSlots();
    ItemStack getStackInSlot(int slot);
    ItemStack insertItem(int slot, ItemStack stack, boolean simulate);
    ItemStack extractItem(int slot, int amount, boolean simulate);
    int getSlotLimit(int slot);
    boolean isItemValid(int slot, ItemStack stack);
}
"""

    files["items/ItemStackHandler.java"] = """
package net.minecraftforge.items;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
public class ItemStackHandler implements IItemHandler {
    protected NonNullList<ItemStack> stacks;
    public ItemStackHandler() { this(1); }
    public ItemStackHandler(int size) { stacks = NonNullList.withSize(size, ItemStack.EMPTY); }
    @Override public int getSlots() { return stacks.size(); }
    @Override public ItemStack getStackInSlot(int slot) { return stacks.get(slot); }
    public void setStackInSlot(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        onContentsChanged(slot);
    }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !isItemValid(slot, stack)) return stack;
        ItemStack existing = stacks.get(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(existing, stack)) return stack;
            limit -= existing.getCount();
        }
        if (limit <= 0) return stack;
        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack.copy());
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }
        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;
        ItemStack existing = stacks.get(slot);
        if (existing.isEmpty()) return ItemStack.EMPTY;
        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            }
            return existing.copy();
        }
        if (!simulate) {
            stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
            onContentsChanged(slot);
        }
        return existing.copyWithCount(toExtract);
    }
    @Override public int getSlotLimit(int slot) { return 64; }
    @Override public boolean isItemValid(int slot, ItemStack stack) { return true; }
    protected void onContentsChanged(int slot) {}
    public CompoundTag serializeNBT() {
        ListTag list = new ListTag();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = (CompoundTag) ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack).result().orElse(new CompoundTag());
                itemTag.putByte("Slot", (byte) i);
                list.add(itemTag);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Items", list);
        nbt.putInt("Size", stacks.size());
        return nbt;
    }
    public void deserializeNBT(CompoundTag nbt) {
        int size = nbt.contains("Size") ? nbt.getInt("Size") : stacks.size();
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        ListTag list = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < stacks.size()) {
                ItemStack.CODEC.parse(NbtOps.INSTANCE, itemTag).result().ifPresent(stack -> stacks.set(slot, stack));
            }
        }
    }
}
"""

    files["items/SlotItemHandler.java"] = """
package net.minecraftforge.items;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
public class SlotItemHandler extends Slot {
    private final IItemHandler handler;
    public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(new HandlerContainer(itemHandler), index, xPosition, yPosition);
        this.handler = itemHandler;
    }
    public IItemHandler getItemHandler() { return handler; }
    @Override public boolean mayPlace(ItemStack stack) { return handler.isItemValid(getSlotIndex(), stack); }
    @Override public ItemStack getItem() { return handler.getStackInSlot(getSlotIndex()); }
    @Override public void set(ItemStack stack) {
        if (handler instanceof ItemStackHandler stackHandler) {
            stackHandler.setStackInSlot(getSlotIndex(), stack);
        } else {
            handler.extractItem(getSlotIndex(), 64, false);
            handler.insertItem(getSlotIndex(), stack, false);
        }
        setChanged();
    }
    @Override public void setChanged() { super.setChanged(); }
    @Override public int getMaxStackSize() { return handler.getSlotLimit(getSlotIndex()); }
    @Override public int getMaxStackSize(ItemStack stack) { return Math.min(stack.getMaxStackSize(), getMaxStackSize()); }
    @Override public boolean mayPickup(Player player) { return !handler.extractItem(getSlotIndex(), 1, true).isEmpty(); }
    @Override public ItemStack remove(int amount) { return handler.extractItem(getSlotIndex(), amount, false); }
    private static final class HandlerContainer extends SimpleContainer {
        private final IItemHandler handler;
        private HandlerContainer(IItemHandler handler) {
            super(handler.getSlots());
            this.handler = handler;
        }
        @Override public ItemStack getItem(int slot) { return handler.getStackInSlot(slot); }
        @Override public void setItem(int slot, ItemStack stack) {
            if (handler instanceof ItemStackHandler stackHandler) {
                stackHandler.setStackInSlot(slot, stack);
            }
        }
        @Override public int getContainerSize() { return handler.getSlots(); }
    }
}
"""

    files["fluids/FluidStack.java"] = """
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
"""

    files["fluids/FluidType.java"] = """
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
"""

    files["fluids/ForgeFlowingFluid.java"] = """
package net.minecraftforge.fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import java.util.function.Supplier;
public abstract class ForgeFlowingFluid extends FlowingFluid {
    private final Properties properties;
    protected ForgeFlowingFluid(Properties properties) { this.properties = properties; }
    @Override public Fluid getFlowing() { return properties.flowing.get(); }
    @Override public Fluid getSource() { return properties.source.get(); }
    @Override protected boolean canConvertToSource(net.minecraft.world.level.Level level) { return false; }
    @Override protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockEntity);
    }
    @Override protected int getSlopeFindDistance(LevelReader level) { return properties.slopeFindDistance; }
    @Override protected int getDropOff(LevelReader level) { return properties.levelDecreasePerBlock; }
    @Override public Item getBucket() { return properties.bucket == null ? Items.AIR : properties.bucket.get(); }
    @Override public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return direction == Direction.DOWN && !fluid.is(FluidTags.WATER);
    }
    @Override public int getTickDelay(LevelReader level) { return properties.tickRate; }
    @Override protected float getExplosionResistance() { return 100.0F; }
    @Override protected BlockState createLegacyBlock(FluidState state) {
        if (properties.block == null) return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        return properties.block.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }
    @Override public boolean isSame(Fluid fluid) {
        return fluid == properties.source.get() || fluid == properties.flowing.get();
    }
    public static class Flowing extends ForgeFlowingFluid {
        public Flowing(Properties properties) { super(properties); }
        @Override protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }
        @Override public int getAmount(FluidState state) { return state.getValue(LEVEL); }
        @Override public boolean isSource(FluidState state) { return false; }
    }
    public static class Source extends ForgeFlowingFluid {
        public Source(Properties properties) { super(properties); }
        @Override public int getAmount(FluidState state) { return 8; }
        @Override public boolean isSource(FluidState state) { return true; }
    }
    public static class Properties {
        private final Supplier<? extends FluidType> type;
        private final Supplier<? extends Fluid> source;
        private final Supplier<? extends Fluid> flowing;
        private Supplier<? extends Item> bucket;
        private Supplier<? extends LiquidBlock> block;
        private int slopeFindDistance = 4;
        private int levelDecreasePerBlock = 1;
        private int tickRate = 5;
        public Properties(Supplier<? extends FluidType> type, Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
            this.type = type;
            this.source = still;
            this.flowing = flowing;
        }
        public Properties bucket(Supplier<? extends Item> bucket) { this.bucket = bucket; return this; }
        public Properties block(Supplier<? extends LiquidBlock> block) { this.block = block; return this; }
        public Properties slopeFindDistance(int slopeFindDistance) { this.slopeFindDistance = slopeFindDistance; return this; }
        public Properties levelDecreasePerBlock(int levelDecreasePerBlock) { this.levelDecreasePerBlock = levelDecreasePerBlock; return this; }
        public Properties tickRate(int tickRate) { this.tickRate = tickRate; return this; }
        public FluidType fluidType() { return type.get(); }
    }
}
"""

    files["fluids/capability/IFluidHandler.java"] = """
package net.minecraftforge.fluids.capability;
import net.minecraftforge.fluids.FluidStack;
public interface IFluidHandler {
    enum FluidAction { EXECUTE, SIMULATE;
        public boolean execute() { return this == EXECUTE; }
        public boolean simulate() { return this == SIMULATE; }
    }
    int getTanks();
    FluidStack getFluidInTank(int tank);
    int getTankCapacity(int tank);
    boolean isFluidValid(int tank, FluidStack stack);
    int fill(FluidStack resource, FluidAction action);
    FluidStack drain(FluidStack resource, FluidAction action);
    FluidStack drain(int maxDrain, FluidAction action);
}
"""

    files["fluids/capability/templates/FluidTank.java"] = """
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
"""

    files["fluids/FluidUtil.java"] = """
package net.minecraftforge.fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
public class FluidUtil {
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, Direction side) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return false;
        IFluidHandler handler = be.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(null);
        if (handler == null) {
            handler = be.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElse(null);
        }
        if (handler == null) return false;
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof BucketItem bucket) {
            Fluid fluid = bucket.content;
            if (fluid != Fluids.EMPTY) {
                int filled = handler.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.SIMULATE);
                if (filled >= 1000) {
                    handler.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.EXECUTE);
                    if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    return true;
                }
            } else {
                FluidStack drained = handler.drain(1000, IFluidHandler.FluidAction.SIMULATE);
                if (!drained.isEmpty() && drained.getAmount() >= 1000) {
                    handler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    if (!player.getAbilities().instabuild) {
                        player.setItemInHand(hand, new ItemStack(drained.getFluid().getBucket()));
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
"""

    files["client/extensions/common/IClientFluidTypeExtensions.java"] = """
package net.minecraftforge.client.extensions.common;
import net.minecraft.resources.ResourceLocation;
public interface IClientFluidTypeExtensions {
    ResourceLocation getStillTexture();
    ResourceLocation getFlowingTexture();
    default int getTintColor() { return 0xFFFFFFFF; }
}
"""

    files["common/capabilities/Capability.java"] = """
package net.minecraftforge.common.capabilities;
public class Capability<T> {
    private final String name;
    public Capability(String name) { this.name = name; }
    public String name() { return name; }
}
"""

    files["common/capabilities/ForgeCapabilities.java"] = """
package net.minecraftforge.common.capabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
public class ForgeCapabilities {
    public static final Capability<IItemHandler> ITEM_HANDLER = new Capability<>("item_handler");
    public static final Capability<IFluidHandler> FLUID_HANDLER = new Capability<>("fluid_handler");
    public static final Capability<IEnergyStorage> ENERGY = new Capability<>("energy");
}
"""

    files["common/util/LazyOptional.java"] = """
package net.minecraftforge.common.util;
import java.util.function.Supplier;
public class LazyOptional<T> {
    private static final LazyOptional<?> EMPTY = new LazyOptional<>(null);
    private Supplier<T> supplier;
    private T value;
    private boolean resolved;
    private boolean valid = true;
    private LazyOptional(Supplier<T> supplier) { this.supplier = supplier; }
    public static <T> LazyOptional<T> of(Supplier<T> supplier) { return new LazyOptional<>(supplier); }
    @SuppressWarnings("unchecked")
    public static <T> LazyOptional<T> empty() { return (LazyOptional<T>) EMPTY; }
    public boolean isPresent() { return valid && supplier != null; }
    public T orElse(T other) {
        T v = getValue();
        return v == null ? other : v;
    }
    public T orElseThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
        T v = getValue();
        if (v == null) throw exceptionSupplier.get();
        return v;
    }
    private T getValue() {
        if (!valid || supplier == null) return null;
        if (!resolved) {
            value = supplier.get();
            resolved = true;
        }
        return value;
    }
    @SuppressWarnings("unchecked")
    public <X> LazyOptional<X> cast() { return (LazyOptional<X>) this; }
    public void invalidate() { valid = false; value = null; }
}
"""

    files["network/IContainerFactory.java"] = """
package net.minecraftforge.network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
@FunctionalInterface
public interface IContainerFactory<T extends AbstractContainerMenu> {
    T create(int windowId, Inventory inv, FriendlyByteBuf data);
}
"""

    files["common/extensions/IForgeMenuType.java"] = """
package net.minecraftforge.common.extensions;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;
public class IForgeMenuType {
    public static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        return new ExtendedScreenHandlerType<>(factory::create);
    }
}
"""

    files["network/NetworkEvent.java"] = """
package net.minecraftforge.network;
import net.minecraft.server.level.ServerPlayer;
public class NetworkEvent {
    public static class Context {
        private final ServerPlayer sender;
        private final boolean client;
        public Context(ServerPlayer sender, boolean client) {
            this.sender = sender;
            this.client = client;
        }
        public ServerPlayer getSender() { return sender; }
        public boolean isClient() { return client; }
        public void enqueueWork(Runnable runnable) {
            if (sender != null && sender.getServer() != null) {
                sender.getServer().execute(runnable);
            } else {
                runnable.run();
            }
        }
        public void setPacketHandled(boolean handled) {}
    }
}
"""

    files["network/NetworkRegistry.java"] = """
package net.minecraftforge.network;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.function.Predicate;
import java.util.function.Supplier;
public class NetworkRegistry {
    public static SimpleChannel newSimpleChannel(ResourceLocation name, Supplier<String> networkProtocolVersion,
                                                 Predicate<String> clientAcceptedVersions, Predicate<String> serverAcceptedVersions) {
        return new SimpleChannel(name);
    }
}
"""

    files["network/PacketDistributor.java"] = """
package net.minecraftforge.network;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Supplier;
public class PacketDistributor {
    public static final PacketDistributor PLAYER = new PacketDistributor();
    public TargetPoint with(Supplier<ServerPlayer> player) { return new TargetPoint(player.get()); }
    public static final class TargetPoint {
        public final ServerPlayer player;
        public TargetPoint(ServerPlayer player) { this.player = player; }
    }
}
"""

    files["network/NetworkHooks.java"] = """
package net.minecraftforge.network;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
public class NetworkHooks {
    public static void openScreen(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        player.openMenu(new ExtendedScreenHandlerFactory() {
            @Override public void writeScreenOpeningData(ServerPlayer p, RegistryFriendlyByteBuf buf) {
                buf.writeBlockPos(pos);
            }
            @Override public Component getDisplayName() { return provider.getDisplayName(); }
            @Override public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                return provider.createMenu(syncId, inv, p);
            }
        });
    }
}
"""

    files["network/simple/SimpleChannel.java"] = """
package net.minecraftforge.network.simple;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
public class SimpleChannel {
    private final ResourceLocation name;
    private final Map<Class<?>, Integer> classToId = new HashMap<>();
    private final Map<Integer, Message<?>> messages = new HashMap<>();
    private boolean registeredPayload;
    public SimpleChannel(ResourceLocation name) { this.name = name; }
    public <MSG> void registerMessage(int index, Class<MSG> type, BiConsumer<MSG, FriendlyByteBuf> encoder,
                                      Function<FriendlyByteBuf, MSG> decoder,
                                      BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        classToId.put(type, index);
        messages.put(index, new Message<>(encoder, decoder, handler));
        ensurePayload();
    }
    private void ensurePayload() {
        if (registeredPayload) return;
        registeredPayload = true;
        PayloadTypeRegistry.playC2S().register(ChannelPayload.TYPE, ChannelPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ChannelPayload.TYPE, ChannelPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ChannelPayload.TYPE, (payload, context) -> {
            handle(payload, context.player(), false);
        });
        if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(ChannelPayload.TYPE, (payload, context) -> {
                handle(payload, null, true);
            });
        }
    }
    @SuppressWarnings("unchecked")
    private void handle(ChannelPayload payload, ServerPlayer sender, boolean client) {
        Message<Object> message = (Message<Object>) messages.get(payload.id());
        if (message == null) return;
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(payload.data()));
        Object packet = message.decoder.apply(buf);
        NetworkEvent.Context ctx = new NetworkEvent.Context(sender, client);
        message.handler.accept(packet, () -> ctx);
    }
    public <MSG> void sendToServer(MSG message) {
        ClientPlayNetworking.send(encode(message));
    }
    public <MSG> void send(PacketDistributor.TargetPoint target, MSG message) {
        if (target.player != null) {
            ServerPlayNetworking.send(target.player, encode(message));
        }
    }
    @SuppressWarnings("unchecked")
    private <MSG> ChannelPayload encode(MSG message) {
        Integer id = classToId.get(message.getClass());
        if (id == null) throw new IllegalArgumentException("Unregistered packet " + message.getClass());
        Message<MSG> registered = (Message<MSG>) messages.get(id);
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        registered.encoder.accept(message, buf);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new ChannelPayload(id, data);
    }
    public record ChannelPayload(int id, byte[] data) implements CustomPayload {
        public static final CustomPayload.Type<ChannelPayload> TYPE =
                new CustomPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tech_revised", "channel"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ChannelPayload> CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeVarInt(payload.id);
                    buf.writeByteArray(payload.data);
                },
                buf -> new ChannelPayload(buf.readVarInt(), buf.readByteArray())
        );
        @Override public Type<? extends CustomPayload> type() { return TYPE; }
    }
    private record Message<MSG>(BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder,
                                BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {}
}
"""

    files["client/event/RegisterColorHandlersEvent.java"] = """
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
"""

    files["client/event/RenderLevelStageEvent.java"] = """
package net.minecraftforge.client.event;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
public class RenderLevelStageEvent {
    public enum Stage { AFTER_TRANSLUCENT_BLOCKS }
    public Stage getStage() { return Stage.AFTER_TRANSLUCENT_BLOCKS; }
    public PoseStack getPoseStack() { return new PoseStack(); }
    public Camera getCamera() { return null; }
}
"""

    files["client/event/ModelEvent.java"] = """
package net.minecraftforge.client.event;
public class ModelEvent {
    public static class RegisterGeometryLoaders {
        public void register(String name, Object loader) {}
    }
}
"""

    files["client/model/DynamicFluidContainerModel.java"] = """
package net.minecraftforge.client.model;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;
public class DynamicFluidContainerModel {
    public static class Colors implements ItemColor {
        @Override public int getColor(ItemStack stack, int tintIndex) {
            return tintIndex == 1 ? 0xFFFFFFFF : 0xFFFFFFFF;
        }
    }
}
"""

    files["client/model/data/ModelData.java"] = """
package net.minecraftforge.client.model.data;
public class ModelData {
    public static final ModelData EMPTY = new ModelData();
}
"""

    files["client/model/geometry/IGeometryBakingContext.java"] = """
package net.minecraftforge.client.model.geometry;
public interface IGeometryBakingContext {}
"""

    files["client/model/geometry/IGeometryLoader.java"] = """
package net.minecraftforge.client.model.geometry;
public interface IGeometryLoader<T> {}
"""

    files["client/model/geometry/IUnbakedGeometry.java"] = """
package net.minecraftforge.client.model.geometry;
public interface IUnbakedGeometry<T extends IUnbakedGeometry<T>> {}
"""

    files["client/ChunkRenderTypeSet.java"] = """
package net.minecraftforge.client;
public class ChunkRenderTypeSet {}
"""

    for rel, content in files.items():
        write(SHIM / rel, content.strip() + "\n")


def transform_java(text: str, rel: str) -> str:
    text = text.replace("BlockBehaviour.Properties.copy(", "BlockBehaviour.Properties.ofFullCopy(")
    text = text.replace(".build(null)", ".build()")
    text = text.replace(".craftRemainder(Items.BUCKET)", "")
    text = text.replace("implements Recipe<Container>", "implements Recipe<com.neofast.tech_revised.recipe.ContainerInput>")
    text = text.replace("Recipe<Container>", "Recipe<com.neofast.tech_revised.recipe.ContainerInput>")
    text = text.replace("matches(Container container,", "matches(com.neofast.tech_revised.recipe.ContainerInput container,")
    text = text.replace("assemble(Container container, RegistryAccess", "assemble(com.neofast.tech_revised.recipe.ContainerInput container, net.minecraft.core.HolderLookup.Provider")
    text = text.replace("getResultItem(RegistryAccess", "getResultItem(net.minecraft.core.HolderLookup.Provider")
    text = text.replace("ShapedRecipe.itemStackFromJson", "com.neofast.tech_revised.recipe.RecipeHelper.itemStackFromJson")
    text = text.replace("Ingredient.fromJson", "com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromJson")
    text = text.replace("Ingredient.fromNetwork", "com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromNetwork")
    text = text.replace(".toNetwork(buffer)", "./*TO_NET*/")
    text = re.sub(r"([A-Za-z0-9_.]+)/\*TO_NET\*/", r"com.neofast.tech_revised.recipe.RecipeHelper.ingredientToNetwork(\1, buffer)", text)
    # fix ingredientToNetwork for recipe.field.toNetwork - already handled? The replace is too greedy.
    text = text.replace("buffer.readItem()", "com.neofast.tech_revised.recipe.RecipeHelper.readItem(buffer)")
    text = text.replace("buffer.writeItem(", "com.neofast.tech_revised.recipe.RecipeHelper.writeItem(buffer, ")

    def rl_repl(match: re.Match) -> str:
        args = match.group(1)
        depth = 0
        for i, ch in enumerate(args):
            if ch == '(':
                depth += 1
            elif ch == ')':
                depth -= 1
            elif ch == ',' and depth == 0:
                return "ResourceLocation.fromNamespaceAndPath(" + args + ")"
        return "ResourceLocation.parse(" + args + ")"

    text = re.sub(r"new ResourceLocation\((.*?)\)", rl_repl, text)

    text = re.sub(
        r"new DropExperienceBlock\(\s*(BlockBehaviour\.Properties\.ofFullCopy\([^;]*?requiresCorrectToolForDrops\(\))\s*,\s*(UniformInt\.of\([^)]+\))\s*\)",
        r"new DropExperienceBlock(\2, \1)",
        text,
        flags=re.S,
    )

    text = re.sub(
        r"protected void saveAdditional\((?:@NotNull |@Nonnull )?CompoundTag tag\)",
        r"protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries)",
        text,
    )
    text = text.replace("super.saveAdditional(tag);", "super.saveAdditional(tag, registries);")
    text = re.sub(
        r"public void load\((?:@NotNull |@Nonnull )?CompoundTag tag\)",
        r"protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries)",
        text,
    )
    text = text.replace("super.load(tag);", "super.loadAdditional(tag, registries);")

    text = text.replace("return super.getCapability(cap, side);", "return net.minecraftforge.common.util.LazyOptional.empty();")
    text = re.sub(r"[ \t]*super\.invalidateCaps\(\);\n", "", text)
    text = re.sub(r"[ \t]*super\.onLoad\(\);\n", "", text)
    text = re.sub(r"@Override\s*\n(\s*)public void onLoad\(\)", r"\1public void onLoad()", text)
    text = re.sub(r"@Override\s*\n(\s*)public void invalidateCaps\(\)", r"\1public void invalidateCaps()", text)
    text = re.sub(r"@Override\s*\n(\s*)public AABB getRenderBoundingBox\(\)", r"\1public AABB getRenderBoundingBox()", text)
    text = re.sub(
        r"@Nonnull\s*\n\s*@Override\s*\n(\s*)public <T> LazyOptional<T> getCapability",
        r"\1public <T> LazyOptional<T> getCapability",
        text,
    )
    text = re.sub(
        r"@Override\s*\n(\s*)public <T> LazyOptional<T> getCapability",
        r"\1public <T> LazyOptional<T> getCapability",
        text,
    )

    text = text.replace(
        "public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)",
        "protected net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)",
    )
    if "useItemOn(" in text and "block/custom" in rel.replace("\\", "/"):
        text = text.replace("return InteractionResult.SUCCESS;", "return net.minecraft.world.ItemInteractionResult.SUCCESS;")
        text = text.replace("return InteractionResult.CONSUME;", "return net.minecraft.world.ItemInteractionResult.CONSUME;")
        text = text.replace("return InteractionResult.PASS;", "return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;")
        text = text.replace(
            "return MachineMultiblockHelper.handleUse",
            "return com.neofast.tech_revised.recipe.RecipeHelper.mapUse(MachineMultiblockHelper.handleUse",
        )
        if "RecipeHelper.mapUse(MachineMultiblockHelper.handleUse" in text:
            text = re.sub(
                r"return com\.neofast\.tech_revised\.recipe\.RecipeHelper\.mapUse\(MachineMultiblockHelper\.handleUse\((.*?)\);",
                r"return com.neofast.tech_revised.recipe.RecipeHelper.mapUse(MachineMultiblockHelper.handleUse(\1);",
                text,
                flags=re.S,
            )
            # add closing paren before semicolon if mapUse opened
            text = text.replace(
                "return com.neofast.tech_revised.recipe.RecipeHelper.mapUse(MachineMultiblockHelper.handleUse",
                "return com.neofast.tech_revised.recipe.RecipeHelper.mapUse(MachineMultiblockHelper.handleUse",
            )

    text = re.sub(
        r"getRecipeFor\(([^,]+),\s*([^,]+),\s*([^)]+)\)",
        r"getRecipeFor(\1, new com.neofast.tech_revised.recipe.ContainerInput(\2), \3).map(net.minecraft.world.item.crafting.RecipeHolder::value)",
        text,
    )

    text = re.sub(
        r"public void appendHoverText\(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag\)",
        "public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag)",
        text,
    )

    text = text.replace("new LiquidBlock(ModFluids.", "new LiquidBlock(ModFluids.")
    text = re.sub(r"new LiquidBlock\((ModFluids\.[A-Z0-9_]+),", r"new LiquidBlock(\1.get(),", text)
    text = re.sub(r"new BucketItem\((ModFluids\.[A-Z0-9_]+),", r"new BucketItem(\1.get(),", text)

    text = re.sub(
        r"public \w+ fromJson\(ResourceLocation recipeId, JsonObject json\)",
        lambda m: m.group(0).replace("ResourceLocation recipeId, JsonObject json", "JsonObject json")
        if False else m.group(0),
        text,
    )
    text = text.replace("fromJson(ResourceLocation recipeId, JsonObject json)", "fromJson(JsonObject json)")
    text = text.replace("fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)", "fromNetwork(FriendlyByteBuf buffer)")
    text = text.replace("new CrusherRecipe(recipeId,", "new CrusherRecipe(net.minecraft.resources.ResourceLocation.parse(\"tech_revised:recipe\"),")
    # generic: leftover recipeId in constructors inside serializers
    text = re.sub(
        r"return new ([A-Za-z]+Recipe)\(recipeId,",
        r"return new \1(net.minecraft.resources.ResourceLocation.parse(\"tech_revised:recipe\"),",
        text,
    )

    text = text.replace("implements RecipeSerializer<", "extends com.neofast.tech_revised.recipe.LegacyRecipeSerializer<")
    # Serializer classes that already say implements and we changed to extends - fix extra
    text = text.replace("extends com.neofast.tech_revised.recipe.LegacyRecipeSerializer<", "extends com.neofast.tech_revised.recipe.LegacyRecipeSerializer<")

    # Fix accidental RecipeSerializer on non-serializer
    if "class Serializer" not in text:
        text = text.replace("extends com.neofast.tech_revised.recipe.LegacyRecipeSerializer<", "implements RecipeSerializer<")

    text = text.replace("@Override\n        public void toNetwork", "public void toNetwork")
    text = text.replace("@Override\n        public GenericIndustrialRecipe fromJson", "public GenericIndustrialRecipe fromJson")
    text = text.replace("@Override\n        public GenericIndustrialRecipe fromNetwork", "public GenericIndustrialRecipe fromNetwork")
    text = re.sub(r"@Override\n(\s+)public (\w+Recipe) fromJson", r"\1public \2 fromJson", text)
    text = re.sub(r"@Override\n(\s+)public (\w+Recipe) fromNetwork", r"\1public \2 fromNetwork", text)

    # ResourceLocation.getId override on Recipe is no longer an override
    text = re.sub(r"@Override\s*\n(\s*)public ResourceLocation getId\(\)", r"\1public ResourceLocation getId()", text)

    text = text.replace(
        "com.neofast.tech_revised.integration.jei.MultiblockStructureBlueprint",
        "com.neofast.tech_revised.multiblock.MultiblockStructureBlueprint",
    )

    return text


def fix_map_use(text: str) -> str:
    """Close RecipeHelper.mapUse(...) calls that wrap handleUse."""
    key = "com.neofast.tech_revised.recipe.RecipeHelper.mapUse(MachineMultiblockHelper.handleUse("
    out = []
    i = 0
    while True:
        j = text.find(key, i)
        if j < 0:
            out.append(text[i:])
            break
        out.append(text[i:j])
        # find matching close of handleUse, then insert extra )
        start = j + len(key)
        depth = 1
        k = start
        while k < len(text) and depth:
            if text[k] == '(':
                depth += 1
            elif text[k] == ')':
                depth -= 1
            k += 1
        out.append(key)
        out.append(text[start:k])  # includes closing paren of handleUse
        out.append(")")  # close mapUse
        i = k
    return "".join(out)


def transform_sources() -> None:
    for path in JAVA.rglob("*.java"):
        rel = str(path.relative_to(JAVA)).replace("\\", "/")
        if rel.startswith("net/minecraftforge/"):
            continue
        if rel.startswith("com/neofast/tech_revised/integration/jei/") and not rel.endswith("MultiblockStructureBlueprint.java"):
            continue
        if rel.startswith("com/neofast/tech_revised/integration/jade/"):
            continue
        original = path.read_text(encoding="utf-8")
        text = transform_java(original, rel)
        text = fix_map_use(text)
        if text != original:
            path.write_text(text, encoding="utf-8")


def write_helpers() -> None:
    write(JAVA / "com/neofast/tech_revised/recipe/ContainerInput.java", """
package com.neofast.tech_revised.recipe;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
public class ContainerInput implements RecipeInput {
    private final Container container;
    public ContainerInput(Container container) { this.container = container; }
    @Override public ItemStack getItem(int slot) { return container.getItem(slot); }
    @Override public int size() { return container.getContainerSize(); }
}
""")
    write(JAVA / "com/neofast/tech_revised/recipe/RecipeHelper.java", """
package com.neofast.tech_revised.recipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
public final class RecipeHelper {
    private RecipeHelper() {}
    public static ItemStack itemStackFromJson(JsonObject json) {
        if (json.has("item") && !json.has("id")) {
            json.add("id", json.get("item"));
        }
        return ItemStack.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    }
    public static Ingredient ingredientFromJson(JsonElement json) {
        return Ingredient.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    }
    public static Ingredient ingredientFromNetwork(FriendlyByteBuf buffer) {
        return Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
    }
    public static void ingredientToNetwork(Ingredient ingredient, FriendlyByteBuf buffer) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
    }
    public static ItemStack readItem(FriendlyByteBuf buffer) {
        return ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
    }
    public static void writeItem(FriendlyByteBuf buffer, ItemStack stack) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
    }
    public static ItemInteractionResult mapUse(InteractionResult result) {
        return switch (result) {
            case SUCCESS -> ItemInteractionResult.SUCCESS;
            case CONSUME, CONSUME_PARTIAL -> ItemInteractionResult.CONSUME;
            case FAIL -> ItemInteractionResult.FAIL;
            default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }
}
""")
    write(JAVA / "com/neofast/tech_revised/recipe/LegacyRecipeSerializer.java", """
package com.neofast.tech_revised.recipe;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import java.util.stream.Stream;
public abstract class LegacyRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    public abstract T fromJson(JsonObject json);
    public abstract T fromNetwork(FriendlyByteBuf buffer);
    public abstract void toNetwork(FriendlyByteBuf buffer, T recipe);
    @Override
    public MapCodec<T> codec() {
        return new MapCodec<>() {
            @Override public <T1> Stream<T1> keys(DynamicOps<T1> ops) { return Stream.empty(); }
            @Override public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input) {
                JsonObject json = ops.convertTo(JsonOps.INSTANCE, ops.createMap(input.entries())).getAsJsonObject();
                try {
                    return DataResult.success(fromJson(json));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }
            @Override public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
                return prefix;
            }
        };
    }
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return StreamCodec.of((buf, recipe) -> toNetwork(buf, recipe), this::fromNetwork);
    }
}
""")


def write_mod_entry() -> None:
    write(JAVA / "com/neofast/tech_revised/TechRevised.java", r'''
package com.neofast.tech_revised;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.fluid.ModFluids;
import com.neofast.tech_revised.item.ModCreativeModeTabs;
import com.neofast.tech_revised.item.ModItems;
import com.neofast.tech_revised.networking.ModNetworking;
import com.neofast.tech_revised.recipe.ModRecipes;
import com.neofast.tech_revised.screen.ModMenuTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TechRevised implements ModInitializer {
    public static final String MOD_ID = "tech_revised";
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModFluids.register(eventBus);
        ModBlocks.register(eventBus);
        ModItems.register(eventBus);
        ModCreativeModeTabs.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModMenuTypes.register(eventBus);
        ModRecipes.register(eventBus);
        ModNetworking.register();
        addOres();
        LOGGER.info("Tech Revised initialized on Fabric 1.21.1");
    }

    private static void addOres() {
        addOre("tin_ore_placed");
        addOre("nickel_ore_placed");
        addOre("graphite_ore_placed");
        addOre("chromite_ore_placed");
    }

    private static void addOre(String name) {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MOD_ID, name))
        );
    }
}
''')
    write(JAVA / "com/neofast/tech_revised/TechRevisedClient.java", r'''
package com.neofast.tech_revised;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.client.ModItemColorHandlers;
import com.neofast.tech_revised.client.MultiblockPreviewRenderer;
import com.neofast.tech_revised.fluid.BaseFluidType;
import com.neofast.tech_revised.fluid.ModFluids;
import com.neofast.tech_revised.item.ModItems;
import com.neofast.tech_revised.screen.CrusherScreen;
import com.neofast.tech_revised.screen.ElectricArcFurnaceControllerScreen;
import com.neofast.tech_revised.screen.ElectricArcFurnaceInputBusScreen;
import com.neofast.tech_revised.screen.ElectricArcFurnaceOutputBusScreen;
import com.neofast.tech_revised.screen.GenericIndustrialMachineScreen;
import com.neofast.tech_revised.screen.ModMenuTypes;
import com.neofast.tech_revised.screen.MultiInputIndustrialMachineScreen;
import com.neofast.tech_revised.screen.MultiblockControllerScreen;
import com.neofast.tech_revised.screen.Windows7VmScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.concurrent.atomic.AtomicReference;

public class TechRevisedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuTypes.CRUSHER_MENU.get(), CrusherScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_ARC_FURNACE_CONTROLLER_MENU.get(), ElectricArcFurnaceControllerScreen::new);
        MenuScreens.register(ModMenuTypes.MULTIBLOCK_CONTROLLER_MENU.get(), MultiblockControllerScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_ARC_FURNACE_INPUT_BUS_MENU.get(), ElectricArcFurnaceInputBusScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_ARC_FURNACE_OUTPUT_BUS_MENU.get(), ElectricArcFurnaceOutputBusScreen::new);
        MenuScreens.register(ModMenuTypes.WINDOWS_7_VM_MENU.get(), Windows7VmScreen::new);
        MenuScreens.register(ModMenuTypes.GENERIC_INDUSTRIAL_MACHINE_MENU.get(), GenericIndustrialMachineScreen::new);
        MenuScreens.register(ModMenuTypes.MULTI_INPUT_INDUSTRIAL_MACHINE_MENU.get(), MultiInputIndustrialMachineScreen::new);

        registerFluid(ModFluids.HEAVY_CRUDE_OIL.get(), ModFluids.FLOWING_HEAVY_CRUDE_OIL.get(), ModFluids.HEAVY_CRUDE_OIL_TYPE.get());
        registerFluid(ModFluids.CREOSOTE.get(), ModFluids.FLOWING_CREOSOTE.get(), ModFluids.CREOSOTE_TYPE.get());
        registerFluid(ModFluids.OXYGEN.get(), ModFluids.FLOWING_OXYGEN.get(), ModFluids.OXYGEN_TYPE.get());
        registerFluid(ModFluids.HYDROGEN.get(), ModFluids.FLOWING_HYDROGEN.get(), ModFluids.HYDROGEN_TYPE.get());
        registerFluid(ModFluids.NAPHTHA.get(), ModFluids.FLOWING_NAPHTHA.get(), ModFluids.NAPHTHA_TYPE.get());
        registerFluid(ModFluids.ETHYLENE.get(), ModFluids.FLOWING_ETHYLENE.get(), ModFluids.ETHYLENE_TYPE.get());
        registerFluid(ModFluids.CHLORINE.get(), ModFluids.FLOWING_CHLORINE.get(), ModFluids.CHLORINE_TYPE.get());
        registerFluid(ModFluids.VINYL_CHLORIDE.get(), ModFluids.FLOWING_VINYL_CHLORIDE.get(), ModFluids.VINYL_CHLORIDE_TYPE.get());
        registerFluid(ModFluids.LIQUID_GLASS.get(), ModFluids.FLOWING_LIQUID_GLASS.get(), ModFluids.LIQUID_GLASS_TYPE.get());
        registerFluid(ModFluids.SODIUM_CARBONATE_SOLUTION.get(), ModFluids.FLOWING_SODIUM_CARBONATE_SOLUTION.get(), ModFluids.SODIUM_CARBONATE_SOLUTION_TYPE.get());
        registerFluid(ModFluids.CUPRIC_CHLORIDE_SOLUTION.get(), ModFluids.FLOWING_CUPRIC_CHLORIDE_SOLUTION.get(), ModFluids.CUPRIC_CHLORIDE_SOLUTION_TYPE.get());
        registerFluid(ModFluids.SODIUM_HYDROXIDE_SOLUTION.get(), ModFluids.FLOWING_SODIUM_HYDROXIDE_SOLUTION.get(), ModFluids.SODIUM_HYDROXIDE_SOLUTION_TYPE.get());

        ModBlocks.getTintedBlockColors().forEach((block, color) ->
                ColorProviderRegistry.BLOCK.register((state, level, pos, tintIndex) -> tintIndex == 0 ? color : 0xFFFFFFFF, block.get()));
        ModItems.getTintedItemColors().forEach((item, color) ->
                ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? color : 0xFFFFFFFF, item.get()));

        MultiblockPreviewRenderer.register();
    }

    private static void registerFluid(FlowingFluid still, FlowingFluid flowing, FluidType type) {
        BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), still, flowing);
        AtomicReference<IClientFluidTypeExtensions> ext = new AtomicReference<>();
        type.initializeClient(ext::set);
        IClientFluidTypeExtensions client = ext.get();
        if (client != null) {
            FluidRenderHandlerRegistry.INSTANCE.register(still, flowing,
                    new SimpleFluidRenderHandler(client.getStillTexture(), client.getFlowingTexture(), client.getTintColor()));
        }
    }
}
''')


def rewrite_formed_hull() -> None:
    write(JAVA / "com/neofast/tech_revised/client/FormedHullGeometry.java", """
package com.neofast.tech_revised.client;
public final class FormedHullGeometry {
    private FormedHullGeometry() {}
    public static void registerLoader() {}
}
""")


def rewrite_preview_renderer() -> None:
    path = JAVA / "com/neofast/tech_revised/client/MultiblockPreviewRenderer.java"
    text = path.read_text(encoding="utf-8")
    text = text.replace("import net.minecraftforge.api.distmarker.Dist;\n", "")
    text = text.replace("import net.minecraftforge.client.event.RenderLevelStageEvent;\n", "")
    text = text.replace("import net.minecraftforge.client.model.data.ModelData;\n", "")
    text = text.replace("import net.minecraftforge.eventbus.api.SubscribeEvent;\n", "")
    text = text.replace("import net.minecraftforge.fml.common.Mod;\n", "")
    text = text.replace("@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)\n", "")
    text = text.replace(
        "    private MultiblockPreviewRenderer() {\n    }",
        """    private MultiblockPreviewRenderer() {
    }

    public static void register() {
        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            render(context.matrixStack(), context.camera().getPosition());
        });
    }"""
    )
    text = re.sub(
        r"    @SubscribeEvent\n    public static void onRenderLevel\(RenderLevelStageEvent event\) \{.*?\n    \}",
        """    private static void render(PoseStack poseStack, net.minecraft.world.phys.Vec3 camera) {
        if (PREVIEWS.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        java.util.Iterator<ActivePreview> it = PREVIEWS.iterator();
        while (it.hasNext()) {
            if (it.next().expireAtMs <= now) {
                it.remove();
            }
        }
        if (PREVIEWS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        for (ActivePreview preview : PREVIEWS) {
            for (GhostBlock ghost : preview.ghosts) {
                poseStack.pushPose();
                poseStack.translate(ghost.pos.getX(), ghost.pos.getY(), ghost.pos.getZ());
                try {
                    dispatcher.renderSingleBlock(ghost.state, poseStack, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY);
                } catch (Exception ignored) {
                }
                poseStack.popPose();
            }
        }
        buffers.endBatch();
        poseStack.popPose();
    }""",
        text,
        count=1,
        flags=re.S,
    )
    # Drop TintedVertexConsumer if leftover unused - keep file compiling even if class remains
    path.write_text(text, encoding="utf-8")


def move_blueprint() -> None:
    src = JAVA / "com/neofast/tech_revised/integration/jei/MultiblockStructureBlueprint.java"
    dst = JAVA / "com/neofast/tech_revised/multiblock/MultiblockStructureBlueprint.java"
    text = src.read_text(encoding="utf-8")
    text = text.replace("package com.neofast.tech_revised.integration.jei;", "package com.neofast.tech_revised.multiblock;")
    write(dst, text)
    src.unlink()


def transform_datapacks() -> None:
    data = RES / "data"
    # folder renames
    moves = [
        (data / "minecraft" / "tags" / "blocks", data / "minecraft" / "tags" / "block"),
        (data / "tech_revised" / "loot_tables", data / "tech_revised" / "loot_table"),
        (data / "tech_revised" / "advancements", data / "tech_revised" / "advancement"),
        (data / "forge" / "tags" / "items", data / "c" / "tags" / "item"),
        (data / "forge" / "tags" / "blocks", data / "c" / "tags" / "block"),
    ]
    for src, dst in moves:
        if src.exists():
            dst.parent.mkdir(parents=True, exist_ok=True)
            if dst.exists():
                for child in src.rglob("*"):
                    if child.is_file():
                        target = dst / child.relative_to(src)
                        target.parent.mkdir(parents=True, exist_ok=True)
                        shutil.copy2(child, target)
                shutil.rmtree(src)
            else:
                shutil.move(str(src), str(dst))

    forge_biome = data / "tech_revised" / "forge"
    if forge_biome.exists():
        shutil.rmtree(forge_biome)

    # recipe result.item -> result.id for vanilla recipe types
    recipes = data / "tech_revised" / "recipes"
    if recipes.exists():
        for path in recipes.rglob("*.json"):
            try:
                obj = json.loads(path.read_text(encoding="utf-8"))
            except Exception:
                continue
            rtype = obj.get("type", "")
            if isinstance(rtype, str) and rtype.startswith("minecraft:"):
                result = obj.get("result")
                if isinstance(result, dict) and "item" in result and "id" not in result:
                    result["id"] = result.pop("item")
                elif isinstance(result, str):
                    obj["result"] = {"id": result}
                path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")

    pack = RES / "pack.mcmeta"
    pack.write_text("""{
    "pack": {
        "description": "tech_revised resources",
        "pack_format": 48
    }
}
""", encoding="utf-8")


def rewrite_item_colors() -> None:
    write(JAVA / "com/neofast/tech_revised/client/ModItemColorHandlers.java", """
package com.neofast.tech_revised.client;
public class ModItemColorHandlers {
    private ModItemColorHandlers() {}
}
""")


def patch_mod_recipes_register() -> None:
    path = JAVA / "com/neofast/tech_revised/recipe/ModRecipes.java"
    text = path.read_text(encoding="utf-8")
    if "BuiltInRegistries.RECIPE_TYPE" in text:
        return
    text = text.replace(
        "import net.minecraft.world.item.crafting.RecipeSerializer;",
        "import net.minecraft.core.Registry;\nimport net.minecraft.core.registries.BuiltInRegistries;\nimport net.minecraft.resources.ResourceLocation;\nimport net.minecraft.world.item.crafting.RecipeSerializer;\nimport net.minecraft.world.item.crafting.RecipeType;",
    )
    extra = """
        registerType("crusher", CrusherRecipe.Type.INSTANCE);
        registerType("electric_arc_furnace", ElectricArcFurnaceRecipe.Type.INSTANCE);
        registerType("drying", DryingRecipeType.INSTANCE);
        registerType("winding", WindingRecipeType.INSTANCE);
        registerType("bundling", BundlingRecipeType.INSTANCE);
        registerType("weaving", WeavingRecipeType.INSTANCE);
        registerType("chopping", ChoppingRecipeType.INSTANCE);
        registerType("pcb", PcbRecipeType.INSTANCE);
        registerType("lamination", LaminationRecipeType.INSTANCE);
    }

    private static void registerType(String name, RecipeType<?> type) {
        Registry.register(BuiltInRegistries.RECIPE_TYPE, ResourceLocation.fromNamespaceAndPath(TechRevised.MOD_ID, name), type);
"""
    text = text.replace(
        "        SERIALIZERS.register(eventBus);\n        INDUSTRIAL_STEEL_SERIALIZERS.register(eventBus);\n    }",
        "        SERIALIZERS.register(eventBus);\n        INDUSTRIAL_STEEL_SERIALIZERS.register(eventBus);" + extra + "    }",
    )
    path.write_text(text, encoding="utf-8")


def main() -> None:
    write_shims()
    write_helpers()
    move_blueprint()
    transform_sources()
    write_mod_entry()
    rewrite_formed_hull()
    rewrite_preview_renderer()
    rewrite_item_colors()
    patch_mod_recipes_register()
    transform_datapacks()
    print("Fabric 1.21.1 port transforms applied.")


if __name__ == "__main__":
    main()
