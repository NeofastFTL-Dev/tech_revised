package com.neofast.tech_revised.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class CokeOvenRecipe implements Recipe<com.neofast.tech_revised.recipe.ContainerInput> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack outputItem;
    private final FluidStack outputFluid;
    private final int processTicks;

    public CokeOvenRecipe(ResourceLocation id, Ingredient input, ItemStack outputItem, FluidStack outputFluid, int processTicks) {
        this.id = id;
        this.input = input;
        this.outputItem = outputItem;
        this.outputFluid = outputFluid;
        this.processTicks = processTicks;
    }

    @Override
    public boolean matches(com.neofast.tech_revised.recipe.ContainerInput container, Level level) {
        if (container.getContainerSize() < 2) {
            return false;
        }
        return input.test(container.getItem(0)) || input.test(container.getItem(1));
    }

    @Override
    public ItemStack assemble(com.neofast.tech_revised.recipe.ContainerInput container, net.minecraft.core.HolderLookup.Provider registryAccess) {
        return outputItem.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider registryAccess) {
        return outputItem.copy();
    }

        public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.COKE_OVEN_SERIALIZER.get();
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

    public FluidStack getOutputFluid() {
        return outputFluid.copy();
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int findMatchingSlot(ItemStack primary, ItemStack secondary) {
        if (input.test(primary)) {
            return 0;
        }
        if (input.test(secondary)) {
            return 1;
        }
        return -1;
    }

    public static class Type implements RecipeType<CokeOvenRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "coke_oven";

        @Override
        public String toString() {
            return TechRevised.MOD_ID + ":" + ID;
        }
    }

    public static class Serializer extends com.neofast.tech_revised.recipe.LegacyRecipeSerializer<CokeOvenRecipe> {
                public CokeOvenRecipe fromJson(JsonObject json) {
            Ingredient ingredient = com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            ItemStack outputItem = com.neofast.tech_revised.recipe.RecipeHelper.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result_item"));
            FluidStack outputFluid = fluidStackFromJson(GsonHelper.getAsJsonObject(json, "result_fluid"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 200);
            return new CokeOvenRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), ingredient, outputItem, outputFluid, processTicks);
        }

                public CokeOvenRecipe fromNetwork(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
            Ingredient ingredient = com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromNetwork(buffer);
            ItemStack outputItem = com.neofast.tech_revised.recipe.RecipeHelper.readItem(buffer);
            FluidStack outputFluid = com.neofast.tech_revised.recipe.RecipeHelper.readFluidStack(buffer);
            int processTicks = buffer.readVarInt();
            return new CokeOvenRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), ingredient, outputItem, outputFluid, processTicks);
        }

        public void toNetwork(FriendlyByteBuf buffer, CokeOvenRecipe recipe) {
            com.neofast.tech_revised.recipe.RecipeHelper.ingredientToNetwork(recipe.input, buffer);
            com.neofast.tech_revised.recipe.RecipeHelper.writeItem(buffer, recipe.outputItem);
            com.neofast.tech_revised.recipe.RecipeHelper.writeFluidStack(buffer, recipe.outputFluid);
            buffer.writeVarInt(recipe.processTicks);
        }

        private static FluidStack fluidStackFromJson(JsonObject json) {
            String fluidId = GsonHelper.getAsString(json, "fluid");
            int amount = GsonHelper.getAsInt(json, "amount");
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(ResourceLocation.parse(fluidId));
            if (fluid == null || fluid == Fluids.EMPTY) {
                throw new JsonSyntaxException("Unknown fluid '" + fluidId + "'");
            }
            return new FluidStack(fluid, amount);
        }
    }
}
