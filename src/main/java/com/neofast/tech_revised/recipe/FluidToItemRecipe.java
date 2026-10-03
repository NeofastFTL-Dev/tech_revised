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

public class FluidToItemRecipe implements Recipe<com.neofast.tech_revised.recipe.ContainerInput> {
    private final ResourceLocation id;
    private final FluidStack inputFluid;
    private final ItemStack result;
    private final int processTicks;
    private final int energyPerTick;

    public FluidToItemRecipe(ResourceLocation id, FluidStack inputFluid, ItemStack result, int processTicks, int energyPerTick) {
        this.id = id;
        this.inputFluid = inputFluid;
        this.result = result;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(com.neofast.tech_revised.recipe.ContainerInput container, Level level) {
        return true; // Usually matched by machine's internal fluid tank
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
        return ModRecipes.EXTRUSION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public FluidStack getInputFluid() {
        return inputFluid.copy();
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }

    public static class Type implements RecipeType<FluidToItemRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "extrusion";

        @Override
        public String toString() {
            return TechRevised.MOD_ID + ":" + ID;
        }
    }

    public static class Serializer extends com.neofast.tech_revised.recipe.LegacyRecipeSerializer<FluidToItemRecipe> {
                public FluidToItemRecipe fromJson(JsonObject json) {
            FluidStack inputFluid = fluidStackFromJson(GsonHelper.getAsJsonObject(json, "input_fluid"));
            ItemStack result = com.neofast.tech_revised.recipe.RecipeHelper.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 100);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 20);
            return new FluidToItemRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), inputFluid, result, processTicks, energyPerTick);
        }

                public FluidToItemRecipe fromNetwork(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
            FluidStack inputFluid = com.neofast.tech_revised.recipe.RecipeHelper.readFluidStack(buffer);
            ItemStack result = com.neofast.tech_revised.recipe.RecipeHelper.readItem(buffer);
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            return new FluidToItemRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), inputFluid, result, processTicks, energyPerTick);
        }

        public void toNetwork(FriendlyByteBuf buffer, FluidToItemRecipe recipe) {
            com.neofast.tech_revised.recipe.RecipeHelper.writeFluidStack(buffer, recipe.inputFluid);
            com.neofast.tech_revised.recipe.RecipeHelper.writeItem(buffer, recipe.result);
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
