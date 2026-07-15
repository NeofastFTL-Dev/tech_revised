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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class OxygenConverterRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final FluidStack inputFluid;
    private final FluidStack outputFluid1;
    private final FluidStack outputFluid2;
    private final int processTicks;
    private final int energyPerTick;

    public OxygenConverterRecipe(ResourceLocation id, FluidStack inputFluid, FluidStack outputFluid1, FluidStack outputFluid2, int processTicks, int energyPerTick) {
        this.id = id;
        this.inputFluid = inputFluid;
        this.outputFluid1 = outputFluid1;
        this.outputFluid2 = outputFluid2;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return true;
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
        return ModRecipes.OXYGEN_CONVERTER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public FluidStack getInputFluid() {
        return inputFluid.copy();
    }

    public FluidStack getOutputFluid1() {
        return outputFluid1.copy();
    }

    public FluidStack getOutputFluid2() {
        return outputFluid2.copy();
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }

    public static class Type implements RecipeType<OxygenConverterRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "oxygen_converter";

        @Override
        public String toString() {
            return TechRevised.MOD_ID + ":" + ID;
        }
    }

    public static class Serializer implements RecipeSerializer<OxygenConverterRecipe> {
        @Override
        public OxygenConverterRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            FluidStack inputFluid = fluidStackFromJson(GsonHelper.getAsJsonObject(json, "input_fluid"));
            FluidStack outputFluid1 = fluidStackFromJson(GsonHelper.getAsJsonObject(json, "output_fluid1"));
            FluidStack outputFluid2 = fluidStackFromJson(GsonHelper.getAsJsonObject(json, "output_fluid2"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 100);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 60);
            return new OxygenConverterRecipe(recipeId, inputFluid, outputFluid1, outputFluid2, processTicks, energyPerTick);
        }

        @Override
        public OxygenConverterRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            FluidStack inputFluid = buffer.readFluidStack();
            FluidStack outputFluid1 = buffer.readFluidStack();
            FluidStack outputFluid2 = buffer.readFluidStack();
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            return new OxygenConverterRecipe(recipeId, inputFluid, outputFluid1, outputFluid2, processTicks, energyPerTick);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, OxygenConverterRecipe recipe) {
            buffer.writeFluidStack(recipe.inputFluid);
            buffer.writeFluidStack(recipe.outputFluid1);
            buffer.writeFluidStack(recipe.outputFluid2);
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
