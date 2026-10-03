package com.neofast.tech_revised.recipe;

import com.google.gson.JsonObject;
import com.neofast.tech_revised.TechRevised;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class CrusherRecipe implements Recipe<com.neofast.tech_revised.recipe.ContainerInput> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack result;
    private final int processTicks;
    private final int energyPerTick;

    public CrusherRecipe(ResourceLocation id, Ingredient input, ItemStack result, int processTicks, int energyPerTick) {
        this.id = id;
        this.input = input;
        this.result = result;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(com.neofast.tech_revised.recipe.ContainerInput container, Level level) {
        if (container.getContainerSize() < 1) {
            return false;
        }
        return input.test(container.getItem(0));
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
        return ModRecipes.CRUSHER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
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

    public static class Type implements RecipeType<CrusherRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "crusher";

        @Override
        public String toString() {
            return TechRevised.MOD_ID + ":" + ID;
        }
    }

    public static class Serializer extends com.neofast.tech_revised.recipe.LegacyRecipeSerializer<CrusherRecipe> {
                public CrusherRecipe fromJson(JsonObject json) {
            Ingredient ingredient = com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ItemStack result = com.neofast.tech_revised.recipe.RecipeHelper.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 100);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 20);
            return new CrusherRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), ingredient, result, processTicks, energyPerTick);
        }

                public CrusherRecipe fromNetwork(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
            Ingredient ingredient = com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromNetwork(buffer);
            ItemStack result = com.neofast.tech_revised.recipe.RecipeHelper.readItem(buffer);
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            return new CrusherRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), ingredient, result, processTicks, energyPerTick);
        }

        public void toNetwork(FriendlyByteBuf buffer, CrusherRecipe recipe) {
            com.neofast.tech_revised.recipe.RecipeHelper.ingredientToNetwork(recipe.input, buffer);
            com.neofast.tech_revised.recipe.RecipeHelper.writeItem(buffer, recipe.result);
            buffer.writeVarInt(recipe.processTicks);
            buffer.writeVarInt(recipe.energyPerTick);
        }
    }
}
