package com.neofast.tech_revised.recipe;

import com.google.gson.JsonObject;
import com.neofast.tech_revised.TechRevised;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class BatchingRecipe implements Recipe<com.neofast.tech_revised.recipe.ContainerInput> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    private final ItemStack result;
    private final int processTicks;
    private final int energyPerTick;

    public BatchingRecipe(ResourceLocation id, NonNullList<Ingredient> inputs, ItemStack result, int processTicks, int energyPerTick) {
        this.id = id;
        this.inputs = inputs;
        this.result = result;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(com.neofast.tech_revised.recipe.ContainerInput container, Level level) {
        if (level.isClientSide()) return false;
        
        java.util.List<ItemStack> containerItems = new java.util.ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                containerItems.add(stack);
            }
        }

        if (containerItems.size() != inputs.size()) return false;

        boolean[] matched = new boolean[inputs.size()];
        for (ItemStack stack : containerItems) {
            boolean foundMatch = false;
            for (int i = 0; i < inputs.size(); i++) {
                if (!matched[i] && inputs.get(i).test(stack)) {
                    matched[i] = true;
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) return false;
        }

        return true;
    }

    @Override
    public ItemStack assemble(com.neofast.tech_revised.recipe.ContainerInput container, net.minecraft.core.HolderLookup.Provider registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider registryAccess) {
        return result.copy();
    }

        public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BATCHING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public NonNullList<Ingredient> getInputs() {
        return inputs;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputs;
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }

    public static class Type implements RecipeType<BatchingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "batching";

        @Override
        public String toString() {
            return TechRevised.MOD_ID + ":" + ID;
        }
    }

    public static class Serializer extends com.neofast.tech_revised.recipe.LegacyRecipeSerializer<BatchingRecipe> {
                public BatchingRecipe fromJson(JsonObject json) {
            ItemStack result = com.neofast.tech_revised.recipe.RecipeHelper.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 200);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 40);

            NonNullList<Ingredient> inputs = NonNullList.create();
            if (json.has("ingredients")) {
                var jsonArray = GsonHelper.getAsJsonArray(json, "ingredients");
                for (int i = 0; i < jsonArray.size(); i++) {
                    inputs.add(com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromJson(jsonArray.get(i)));
                }
            }

            return new BatchingRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), inputs, result, processTicks, energyPerTick);
        }

                public BatchingRecipe fromNetwork(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
            int size = buffer.readVarInt();
            NonNullList<Ingredient> inputs = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                inputs.set(i, com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromNetwork(buffer));
            }
            ItemStack result = com.neofast.tech_revised.recipe.RecipeHelper.readItem(buffer);
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            return new BatchingRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), inputs, result, processTicks, energyPerTick);
        }

        public void toNetwork(FriendlyByteBuf buffer, BatchingRecipe recipe) {
            buffer.writeVarInt(recipe.inputs.size());
            for (Ingredient ingredient : recipe.inputs) {
                com.neofast.tech_revised.recipe.RecipeHelper.ingredientToNetwork(ingredient, buffer);
            }
            com.neofast.tech_revised.recipe.RecipeHelper.writeItem(buffer, recipe.result);
            buffer.writeVarInt(recipe.processTicks);
            buffer.writeVarInt(recipe.energyPerTick);
        }
    }
}
