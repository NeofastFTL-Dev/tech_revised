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

public class FluidToItemRecipe implements Recipe<Container> {
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
    public boolean matches(Container container, Level level) {
        return true; // Usually matched by machine's internal fluid tank
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

    public static class Serializer implements RecipeSerializer<FluidToItemRecipe> {
        @Override
        public FluidToItemRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            FluidStack inputFluid = fluidStackFromJson(GsonHelper.getAsJsonObject(json, "input_fluid"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 100);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 20);
            return new FluidToItemRecipe(recipeId, inputFluid, result, processTicks, energyPerTick);
        }

        @Override
        public FluidToItemRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            FluidStack inputFluid = buffer.readFluidStack();
            ItemStack result = buffer.readItem();
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            return new FluidToItemRecipe(recipeId, inputFluid, result, processTicks, energyPerTick);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FluidToItemRecipe recipe) {
            buffer.writeFluidStack(recipe.inputFluid);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.processTicks);
            buffer.writeVarInt(recipe.energyPerTick);
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
