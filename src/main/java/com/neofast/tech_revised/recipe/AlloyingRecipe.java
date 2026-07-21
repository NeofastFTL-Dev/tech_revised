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

public class AlloyingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    private final ItemStack output;

    public AlloyingRecipe(ResourceLocation id, NonNullList<Ingredient> inputs, ItemStack output) {
        this.id = id;
        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public boolean matches(Container pContainer, Level pLevel) {
        if (pLevel.isClientSide()) return false;
        
        // Match inputs in any order
        java.util.List<ItemStack> containerItems = new java.util.ArrayList<>();
        for (int i = 0; i < pContainer.getContainerSize(); i++) {
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
    public ItemStack assemble(Container pContainer, RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
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

    public static class Type implements RecipeType<AlloyingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "alloying";

        @Override
        public String toString() {
            return com.neofast.tech_revised.TechRevised.MOD_ID + ":" + ID;
        }
    }

    public static class Serializer implements RecipeSerializer<AlloyingRecipe> {
        @Override
        public AlloyingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "result"));

            NonNullList<Ingredient> inputs = NonNullList.create();
            if (pSerializedRecipe.has("ingredients")) {
                var jsonArray = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
                for (int i = 0; i < jsonArray.size(); i++) {
                    inputs.add(Ingredient.fromJson(jsonArray.get(i)));
                }
            }

            return new AlloyingRecipe(pRecipeId, inputs, output);
        }

        @Override
        public AlloyingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            int size = pBuffer.readVarInt();
            NonNullList<Ingredient> inputs = NonNullList.withSize(size, Ingredient.EMPTY);
            for (int i = 0; i < size; i++) {
                inputs.set(i, Ingredient.fromNetwork(pBuffer));
            }
            ItemStack output = pBuffer.readItem();
            return new AlloyingRecipe(pRecipeId, inputs, output);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, AlloyingRecipe pRecipe) {
            pBuffer.writeVarInt(pRecipe.inputs.size());
            for (Ingredient ingredient : pRecipe.inputs) {
                ingredient.toNetwork(pBuffer);
            }
            pBuffer.writeItem(pRecipe.output);
        }
    }
}
