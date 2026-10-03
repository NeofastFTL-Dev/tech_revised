package com.neofast.tech_revised.recipe;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class AlloyingRecipe implements Recipe<com.neofast.tech_revised.recipe.ContainerInput> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    private final ItemStack output;

    public AlloyingRecipe(ResourceLocation id, NonNullList<Ingredient> inputs, ItemStack output) {
        this.id = id;
        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public boolean matches(ContainerInput pContainer, Level pLevel) {
        if (pLevel.isClientSide()) return false;
        
        // Match inputs in any order
        java.util.List<ItemStack> containerItems = new java.util.ArrayList<>();
        for (int i = 0; i < pContainer.size(); i++) {
            ItemStack stack = pContainer.getItem(i);
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
    public ItemStack assemble(ContainerInput pContainer, net.minecraft.core.HolderLookup.Provider pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider pRegistryAccess) {
        return output.copy();
    }

        public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ALLOYING_SERIALIZER.get();
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

    public static class Type implements RecipeType<AlloyingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "alloying";

        @Override
        public String toString() {
            return com.neofast.tech_revised.TechRevised.MOD_ID + ":" + ID;
        }
    }

    public static class Serializer extends com.neofast.tech_revised.recipe.LegacyRecipeSerializer<AlloyingRecipe> {
                public AlloyingRecipe fromJson(JsonObject pSerializedRecipe) {
            ItemStack output = com.neofast.tech_revised.recipe.RecipeHelper.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "result"));

            NonNullList<Ingredient> inputs = NonNullList.create();
            if (pSerializedRecipe.has("ingredients")) {
                var jsonArray = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
                for (int i = 0; i < jsonArray.size(); i++) {
                    inputs.add(com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromJson(jsonArray.get(i)));
                }
            }

            return new AlloyingRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), inputs, output);
        }

                public AlloyingRecipe fromNetwork(net.minecraft.network.RegistryFriendlyByteBuf pBuffer) {
            int size = pBuffer.readVarInt();
            NonNullList<Ingredient> inputs = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                inputs.set(i, com.neofast.tech_revised.recipe.RecipeHelper.ingredientFromNetwork(pBuffer));
            }
            ItemStack output = RecipeHelper.readItem(pBuffer);
            return new AlloyingRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"), inputs, output);
        }

        public void toNetwork(FriendlyByteBuf pBuffer, AlloyingRecipe pRecipe) {
            pBuffer.writeVarInt(pRecipe.inputs.size());
            for (Ingredient ingredient : pRecipe.inputs) {
                RecipeHelper.ingredientToNetwork(ingredient, pBuffer);
            }
            RecipeHelper.writeItem(pBuffer, pRecipe.output);
        }
    }
}
