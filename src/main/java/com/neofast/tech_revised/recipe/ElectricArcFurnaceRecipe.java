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

public class ElectricArcFurnaceRecipe implements Recipe<com.neofast.tech_revised.recipe.ContainerInput> {
    private final ResourceLocation id;
    private final Ingredient primaryInput;
    private final Ingredient secondaryInput;
    private final ItemStack result;
    private final int processTicks;
    private final int energyPerTick;
    private final int waterAmount;

    public ElectricArcFurnaceRecipe(ResourceLocation id,
                                    Ingredient primaryInput,
                                    Ingredient secondaryInput,
                                    ItemStack result,
                                    int processTicks,
                                    int energyPerTick,
                                    int waterAmount) {
        this.id = id;
        this.primaryInput = primaryInput;
        this.secondaryInput = secondaryInput;
        this.result = result;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
        this.waterAmount = waterAmount;
    }

    @Override
    public boolean matches(com.neofast.tech_revised.recipe.ContainerInput container, Level level) {
        if (container.getContainerSize() < 2) {
            return false;
        }

        ItemStack first = container.getItem(0);
        ItemStack second = container.getItem(1);

        if (secondaryInput.isEmpty()) {
            return primaryInput.test(first);
        }

        return (primaryInput.test(first) && secondaryInput.test(second)) ||
                (primaryInput.test(second) && secondaryInput.test(first));
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
        return ModRecipes.ELECTRIC_ARC_FURNACE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public Ingredient getPrimaryInput() {
        return primaryInput;
    }

    public Ingredient getSecondaryInput() {
        return secondaryInput;
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }

    public int getWaterAmount() {
        return waterAmount;
    }

    public boolean hasSecondaryInput() {
        return !secondaryInput.isEmpty();
    }

    public static class Type implements RecipeType<ElectricArcFurnaceRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "electric_arc_furnace";

        @Override
        public String toString() {
            return TechRevised.MOD_ID + ":" + ID;
        }
    }

    public static class Serializer extends com.neofast.tech_revised.recipe.LegacyRecipeSerializer<ElectricArcFurnaceRecipe> {
                public ElectricArcFurnaceRecipe fromJson(JsonObject json) {
            Ingredient primaryInput = com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromJson(GsonHelper.getAsJsonObject(json, "primary_input"));
            Ingredient secondaryInput = json.has("secondary_input")
                    ? com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromJson(GsonHelper.getAsJsonObject(json, "secondary_input"))
                    : Ingredient.EMPTY;
            ItemStack result = com.neofast.tech_revised.recipe.RecipeHelper.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 100);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 40);
            int waterAmount = GsonHelper.getAsInt(json, "water_amount", 250);
            return new ElectricArcFurnaceRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), primaryInput, secondaryInput, result, processTicks, energyPerTick, waterAmount);
        }

                public ElectricArcFurnaceRecipe fromNetwork(net.minecraft.network.RegistryFriendlyByteBuf buffer) {
            Ingredient primaryInput = com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromNetwork(buffer);
            Ingredient secondaryInput = com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromNetwork(buffer);
            ItemStack result = com.neofast.tech_revised.recipe.RecipeHelper.readItem(buffer);
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            int waterAmount = buffer.readVarInt();
            return new ElectricArcFurnaceRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), primaryInput, secondaryInput, result, processTicks, energyPerTick, waterAmount);
        }

        public void toNetwork(FriendlyByteBuf buffer, ElectricArcFurnaceRecipe recipe) {
            com.neofast.tech_revised.recipe.RecipeHelper.ingredientToNetwork(recipe.primaryInput, buffer);
            com.neofast.tech_revised.recipe.RecipeHelper.ingredientToNetwork(recipe.secondaryInput, buffer);
            com.neofast.tech_revised.recipe.RecipeHelper.writeItem(buffer, recipe.result);
            buffer.writeVarInt(recipe.processTicks);
            buffer.writeVarInt(recipe.energyPerTick);
            buffer.writeVarInt(recipe.waterAmount);
        }
    }
}
