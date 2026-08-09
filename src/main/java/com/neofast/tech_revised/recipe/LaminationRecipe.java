package com.neofast.tech_revised.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.item.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class LaminationRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> layers;
    private final ItemStack result;
    private final int processTicks;
    private final int energyPerTick;
    private final Supplier<RecipeSerializer<?>> serializer;

    public LaminationRecipe(ResourceLocation id, NonNullList<Ingredient> layers, ItemStack result,
                            int processTicks, int energyPerTick, Supplier<RecipeSerializer<?>> serializer) {
        this.id = id;
        this.layers = layers;
        this.result = result;
        this.processTicks = processTicks;
        this.energyPerTick = energyPerTick;
        this.serializer = serializer;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < layers.size()) return false;
        for (int i = 0; i < layers.size(); i++) {
            if (!layers.get(i).test(container.getItem(i))) return false;
        }
        return true;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        validate(container);
        return result.copy();
    }

    private void validate(Container container) {
        // Multi-layer laminates must fail validation if core steps alternate without a designated Pre-preg buffer layer.
        // We assume layers 0, 2, 4... are cores and 1, 3, 5... are pre-pregs (or vice versa).
        // For simplicity, let's check if any two cores are adjacent.
        boolean lastWasCore = false;
        for (int i = 0; i < layers.size(); i++) {
            ItemStack stack = container.getItem(i);
            boolean isCore = isCore(stack);
            boolean isPrepreg = isPrepreg(stack);
            
            if (isCore && lastWasCore) {
                throw new IllegalStateException("Multi-layer laminate validation failed: Cores cannot be adjacent without a Pre-preg buffer layer!");
            }
            lastWasCore = isCore;
        }
    }

    private boolean isCore(ItemStack stack) {
        // multi_layer_stackup is a pre-assembled core/prepreg sandwich - not a bare core.
        return stack.is(ModItems.STRIPPED_INNER_LAYER_BOARD.get()) || stack.is(ModItems.COPPER_CLAD_LAMINATE.get());
    }

    private boolean isPrepreg(ItemStack stack) {
        return stack.is(ModItems.PREPREG_FABRIC.get()) || stack.is(ModItems.MULTI_LAYER_STACKUP.get());
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
        return serializer.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.LaminationRecipeType.INSTANCE;
    }

    public NonNullList<Ingredient> getLayers() {
        return layers;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return layers;
    }

    public int getProcessTicks() {
        return processTicks;
    }

    public int getEnergyPerTick() {
        return energyPerTick;
    }


    public static class Serializer implements RecipeSerializer<LaminationRecipe> {
        private final Supplier<RecipeSerializer<?>> serializerSupplier;

        public Serializer(Supplier<RecipeSerializer<?>> serializerSupplier) {
            this.serializerSupplier = serializerSupplier;
        }

        @Override
        public LaminationRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            NonNullList<Ingredient> layers = NonNullList.create();
            JsonArray jsonArray = GsonHelper.getAsJsonArray(json, "layers");
            for (int i = 0; i < jsonArray.size(); i++) {
                layers.add(Ingredient.fromJson(jsonArray.get(i)));
            }

            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int processTicks = GsonHelper.getAsInt(json, "process_ticks", 400);
            int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 100);

            return new LaminationRecipe(recipeId, layers, result, processTicks, energyPerTick, serializerSupplier);
        }

        @Override
        public LaminationRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int layerCount = buffer.readVarInt();
            NonNullList<Ingredient> layers = NonNullList.withSize(layerCount, Ingredient.EMPTY);
            for (int i = 0; i < layerCount; i++) {
                layers.set(i, Ingredient.fromNetwork(buffer));
            }
            ItemStack result = buffer.readItem();
            int processTicks = buffer.readVarInt();
            int energyPerTick = buffer.readVarInt();
            return new LaminationRecipe(recipeId, layers, result, processTicks, energyPerTick, serializerSupplier);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, LaminationRecipe recipe) {
            buffer.writeVarInt(recipe.layers.size());
            for (Ingredient ingredient : recipe.layers) {
                ingredient.toNetwork(buffer);
            }
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.processTicks);
            buffer.writeVarInt(recipe.energyPerTick);
        }
    }
}
