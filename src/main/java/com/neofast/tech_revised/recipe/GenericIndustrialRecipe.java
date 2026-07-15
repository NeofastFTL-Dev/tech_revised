package com.neofast.tech_revised.recipe;

import com.google.gson.JsonObject;
import com.neofast.tech_revised.TechRevised;
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
    private final ItemStack result;
    private final int processTicks;
    private final int energyPerTick;
    private final Supplier<RecipeSerializer<?>> serializer;
    private final RecipeType<?> type;

    public GenericIndustrialRecipe(ResourceLocation id, Ingredient input, ItemStack result, 
                                   int processTicks, int energyPerTick, 
                                   Supplier<RecipeSerializer<?>> serializer, RecipeType<?> type) {
        this.id = id;
        this.input = input;
        this.result = result;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
        this.serializer = serializer;
        this.type = type;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < 1) return false;
        return input.test(container.getItem(0));
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
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 100);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 20);
            return new GenericIndustrialRecipe(recipeId, input, result, processTicks, energyPerTick, serializerSupplier, type);
        }

        @Override
        public GenericIndustrialRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            return new GenericIndustrialRecipe(recipeId, input, result, processTicks, energyPerTick, serializerSupplier, type);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GenericIndustrialRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.processTicks);
            buffer.writeVarInt(recipe.energyPerTick);
        }
    }
}
