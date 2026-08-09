package com.neofast.tech_revised.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.neofast.tech_revised.exception.MoistureInclusionException;
import com.neofast.tech_revised.exception.ShortCircuitException;
import com.neofast.tech_revised.exception.SolderBridgeException;
import com.neofast.tech_revised.item.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class PcbRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    private final FluidStack inputFluid;
    private final ItemStack result;
    private final int processTicks;
    private final int energyPerTick;
    /**
     * Block IDs allowed to run this recipe. Empty means any PCB machine (legacy/fallback).
     */
    private final List<ResourceLocation> machines;
    private final Supplier<RecipeSerializer<?>> serializer;
    private final RecipeType<?> type;

    public PcbRecipe(ResourceLocation id, NonNullList<Ingredient> inputs, FluidStack inputFluid, ItemStack result,
                     int processTicks, int energyPerTick, List<ResourceLocation> machines,
                     Supplier<RecipeSerializer<?>> serializer, RecipeType<?> type) {
        this.id = id;
        this.inputs = inputs;
        this.inputFluid = inputFluid;
        this.result = result;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
        this.machines = Collections.unmodifiableList(new ArrayList<>(machines));
        this.serializer = serializer;
        this.type = type;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < inputs.size()) return false;
        for (int i = 0; i < inputs.size(); i++) {
            if (!inputs.get(i).test(container.getItem(i))) return false;
        }
        return true;
    }

    /**
     * Whether this recipe may run on the given machine block.
     * If no machines are listed, any PCB machine is allowed.
     */
    public boolean isAllowedOn(Block block) {
        if (machines.isEmpty()) {
            return true;
        }
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        if (blockId == null) {
            return false;
        }
        return machines.contains(blockId);
    }

    public boolean isAllowedOn(ResourceLocation blockId) {
        if (machines.isEmpty()) {
            return true;
        }
        return machines.contains(blockId);
    }

    public List<ResourceLocation> getMachines() {
        return machines;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        validate(container);
        return result.copy();
    }

    private void validate(Container container) {
        if (id.getPath().contains("cladding")) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (stack.is(ModItems.SHEARED_FR4_LAMINATE.get())) {
                    throw new MoistureInclusionException("Substrate preparation bypassed the baking cycle. Internal core moisture detected!");
                }
            }
        }

        if (id.getPath().contains("electrical_test") && id.getPath().contains("fail")) {
            throw new ShortCircuitException("Electrical Flying Probe reports continuity between isolated copper paths!");
        }

        if (id.getPath().contains("aoi_verification") && id.getPath().contains("fail")) {
            throw new SolderBridgeException("AOI scan detected component pads overlapping due to soldermask misalignment!");
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer.get();
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }

    public NonNullList<Ingredient> getInputs() {
        return inputs;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputs;
    }

    public FluidStack getInputFluid() {
        return inputFluid;
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }

    public static class Serializer implements RecipeSerializer<PcbRecipe> {
        private final Supplier<RecipeSerializer<?>> serializerSupplier;
        private final RecipeType<?> type;

        public Serializer(Supplier<RecipeSerializer<?>> serializerSupplier, RecipeType<?> type) {
            this.serializerSupplier = serializerSupplier;
            this.type = type;
        }

        @Override
        public PcbRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            NonNullList<Ingredient> inputs = NonNullList.create();
            if (json.has("ingredients")) {
                JsonArray jsonArray = GsonHelper.getAsJsonArray(json, "ingredients");
                for (int i = 0; i < jsonArray.size(); i++) {
                    inputs.add(Ingredient.fromJson(jsonArray.get(i)));
                }
            } else if (json.has("ingredient")) {
                inputs.add(Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient")));
            }

            FluidStack inputFluid = FluidStack.EMPTY;
            if (json.has("fluid")) {
                inputFluid = fluidStackFromJson(GsonHelper.getAsJsonObject(json, "fluid"));
            }

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 100);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 20);
            List<ResourceLocation> machines = parseMachines(json);

            return new PcbRecipe(recipeId, inputs, inputFluid, result, processTicks, energyPerTick, machines, serializerSupplier, type);
        }

        @Override
        public PcbRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int inputCount = buffer.readVarInt();
            NonNullList<Ingredient> inputs = NonNullList.withSize(inputCount, Ingredient.EMPTY);
            for (int i = 0; i < inputCount; i++) {
                inputs.set(i, Ingredient.fromNetwork(buffer));
            }
            FluidStack inputFluid = buffer.readFluidStack();
            ItemStack result = buffer.readItem();
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            int machineCount = buffer.readVarInt();
            List<ResourceLocation> machines = new ArrayList<>(machineCount);
            for (int i = 0; i < machineCount; i++) {
                machines.add(buffer.readResourceLocation());
            }
            return new PcbRecipe(recipeId, inputs, inputFluid, result, processTicks, energyPerTick, machines, serializerSupplier, type);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, PcbRecipe recipe) {
            buffer.writeVarInt(recipe.inputs.size());
            for (Ingredient ingredient : recipe.inputs) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeFluidStack(recipe.inputFluid);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.processTicks);
            buffer.writeVarInt(recipe.energyPerTick);
            buffer.writeVarInt(recipe.machines.size());
            for (ResourceLocation machine : recipe.machines) {
                buffer.writeResourceLocation(machine);
            }
        }

        private static List<ResourceLocation> parseMachines(JsonObject json) {
            List<ResourceLocation> machines = new ArrayList<>();
            if (json.has("machines")) {
                JsonArray array = GsonHelper.getAsJsonArray(json, "machines");
                for (JsonElement element : array) {
                    machines.add(new ResourceLocation(element.getAsString()));
                }
            } else if (json.has("machine")) {
                machines.add(new ResourceLocation(GsonHelper.getAsString(json, "machine")));
            }
            return machines;
        }

        private static FluidStack fluidStackFromJson(JsonObject json) {
            String fluidId = GsonHelper.getAsString(json, "fluid");
            int amount = GsonHelper.getAsInt(json, "amount");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidId));
            if (fluid == null || fluid == Fluids.EMPTY) {
                throw new JsonSyntaxException("Unknown fluid '" + fluidId + "'");
            }
            return new FluidStack(fluid, amount);
        }
    }
}
