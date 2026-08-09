package com.neofast.tech_revised.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class GenericIndustrialRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final int inputCount;
    private final ItemStack result;
    private final int processTicks;
    private final int energyPerTick;
    private final Supplier<RecipeSerializer<?>> serializer;
    private final RecipeType<?> type;

    public GenericIndustrialRecipe(ResourceLocation id, Ingredient input, int inputCount, ItemStack result,
                                   int processTicks, int energyPerTick,
                                   Supplier<RecipeSerializer<?>> serializer, RecipeType<?> type) {
        this.id = id;
        this.input = input;
        this.inputCount = Math.max(1, inputCount);
        this.result = result;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
        this.serializer = serializer;
        this.type = type;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < 1) {
            return false;
        }
        ItemStack stack = container.getItem(0);
        return input.test(stack) && stack.getCount() >= inputCount;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.copy();
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

    public Ingredient getInput() {
        return input;
    }

    public int getInputCount() {
        return inputCount;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        // JEI shows one slot; count is drawn separately / via stack size simulation
        list.add(input);
        return list;
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }

    public static class Serializer implements RecipeSerializer<GenericIndustrialRecipe> {
        private final Supplier<RecipeSerializer<?>> serializerSupplier;
        private final RecipeType<?> type;

        public Serializer(Supplier<RecipeSerializer<?>> serializerSupplier, RecipeType<?> type) {
            this.serializerSupplier = serializerSupplier;
            this.type = type;
        }

        @Override
        public GenericIndustrialRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient input;
            int inputCount = 1;

            if (json.has("ingredient")) {
                input = Ingredient.fromJson(json.get("ingredient"));
                inputCount = GsonHelper.getAsInt(json, "input_count", 1);
            } else if (json.has("ingredients")) {
                JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
                if (array.isEmpty()) {
                    throw new IllegalArgumentException("Recipe " + recipeId + " has empty ingredients");
                }
                input = Ingredient.fromJson(array.get(0));
                // If multiple entries, treat as required count of the first ingredient type
                inputCount = GsonHelper.getAsInt(json, "input_count", array.size());
            } else {
                throw new IllegalArgumentException("Recipe " + recipeId + " missing ingredient/ingredients");
            }

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 100);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 20);
            return new GenericIndustrialRecipe(recipeId, input, inputCount, result, processTicks, energyPerTick, serializerSupplier, type);
        }

        @Override
        public GenericIndustrialRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            int inputCount = buffer.readVarInt();
            ItemStack result = buffer.readItem();
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            return new GenericIndustrialRecipe(recipeId, input, inputCount, result, processTicks, energyPerTick, serializerSupplier, type);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GenericIndustrialRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeVarInt(recipe.inputCount);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.processTicks);
            buffer.writeVarInt(recipe.energyPerTick);
        }
    }
}
