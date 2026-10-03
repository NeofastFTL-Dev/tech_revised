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
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class MeltingRecipe implements Recipe<com.neofast.tech_revised.recipe.ContainerInput> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final FluidStack outputFluid;
    private final int processTicks;
    private final int energyPerTick;

    public MeltingRecipe(ResourceLocation id, Ingredient input, FluidStack outputFluid, int processTicks, int energyPerTick) {
        this.id = id;
        this.input = input;
        this.outputFluid = outputFluid;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(com.neofast.tech_revised.recipe.ContainerInput container, Level level) {
        if (container.getContainerSize() < 1) return false;
        return input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(com.neofast.tech_revised.recipe.ContainerInput container, net.minecraft.core.HolderLookup.Provider registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider registryAccess) {
        return ItemStack.EMPTY;
    }

        public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MELTING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
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

    public int getEnergyPerTick() {
        return energyPerTick;
    }

    public static class Type implements RecipeType<MeltingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "melting";

        @Override
        public String toString() {
            return TechRevised.MOD_ID + ":" + ID;
        }
    }

    public static class Serializer extends com.neofast.tech_revised.recipe.LegacyRecipeSerializer<MeltingRecipe> {
                public MeltingRecipe fromJson(JsonObject json) {
            Ingredient input;
            if (json.has("ingredient")) {
                input = com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromJson(json.get("ingredient"));
            } else if (json.has("ingredients")) {
                input = com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromJson(GsonHelper.getAsJsonArray(json, "ingredients").get(0));
            } else {
                throw new JsonSyntaxException("Missing ingredient/ingredients for melting recipe " + "unknown");
            }

            JsonObject fluidJson;
            if (json.has("result_fluid")) {
                fluidJson = GsonHelper.getAsJsonObject(json, "result_fluid");
            } else if (json.has("output_fluid")) {
                fluidJson = GsonHelper.getAsJsonObject(json, "output_fluid");
            } else {
                throw new JsonSyntaxException("Missing result_fluid/output_fluid for melting recipe " + "unknown");
            }
            FluidStack outputFluid = fluidStackFromJson(fluidJson);
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 200);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 40);
            return new MeltingRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), input, outputFluid, processTicks, energyPerTick);
        }

                public MeltingRecipe fromNetwork(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
            Ingredient input = com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromNetwork(buffer);
            FluidStack outputFluid = com.neofast.tech_revised.recipe.RecipeHelper.readFluidStack(buffer);
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            return new MeltingRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), input, outputFluid, processTicks, energyPerTick);
        }

        public void toNetwork(FriendlyByteBuf buffer, MeltingRecipe recipe) {
            com.neofast.tech_revised.recipe.RecipeHelper.ingredientToNetwork(recipe.input, buffer);
            com.neofast.tech_revised.recipe.RecipeHelper.writeFluidStack(buffer, recipe.outputFluid);
            buffer.writeVarInt(recipe.processTicks);
            buffer.writeVarInt(recipe.energyPerTick);
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
