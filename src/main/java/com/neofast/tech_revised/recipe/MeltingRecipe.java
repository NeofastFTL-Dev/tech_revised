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

public class MeltingRecipe implements Recipe<Container> {
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
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < 1) return false;
        return input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
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

    public static class Serializer implements RecipeSerializer<MeltingRecipe> {
        @Override
        public MeltingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));
            FluidStack outputFluid = fluidStackFromJson(GsonHelper.getAsJsonObject(json, "result_fluid"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 200);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 40);
            return new MeltingRecipe(recipeId, input, outputFluid, processTicks, energyPerTick);
        }

        @Override
        public MeltingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            FluidStack outputFluid = buffer.readFluidStack();
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            return new MeltingRecipe(recipeId, input, outputFluid, processTicks, energyPerTick);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MeltingRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeFluidStack(recipe.outputFluid);
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
