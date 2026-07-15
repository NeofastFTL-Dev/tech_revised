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

public class ItemFluidToItemRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient inputItem;
    private final FluidStack inputFluid;
    private final ItemStack result;
    private final int processTicks;
    private final int energyPerTick;

    public ItemFluidToItemRecipe(ResourceLocation id, Ingredient inputItem, FluidStack inputFluid, ItemStack result, int processTicks, int energyPerTick) {
        this.id = id;
        this.inputItem = inputItem;
        this.inputFluid = inputFluid;
        this.result = result;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < 1) return false;
        return inputItem.test(container.getItem(0));
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
        return ModRecipes.SIZING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public Ingredient getInputItem() {
        return inputItem;
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

    public static class Type implements RecipeType<ItemFluidToItemRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "sizing";

        @Override
        public String toString() {
            return TechRevised.MOD_ID + ":" + ID;
        }
    }

    public static class Serializer implements RecipeSerializer<ItemFluidToItemRecipe> {
        @Override
        public ItemFluidToItemRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient inputItem = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input_item"));
            FluidStack inputFluid = fluidStackFromJson(GsonHelper.getAsJsonObject(json, "input_fluid"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 100);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 20);
            return new ItemFluidToItemRecipe(recipeId, inputItem, inputFluid, result, processTicks, energyPerTick);
        }

        @Override
        public ItemFluidToItemRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient inputItem = Ingredient.fromNetwork(buffer);
            FluidStack inputFluid = buffer.readFluidStack();
            ItemStack result = buffer.readItem();
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            return new ItemFluidToItemRecipe(recipeId, inputItem, inputFluid, result, processTicks, energyPerTick);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ItemFluidToItemRecipe recipe) {
            recipe.inputItem.toNetwork(buffer);
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
