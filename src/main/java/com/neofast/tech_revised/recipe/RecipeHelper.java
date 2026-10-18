
package com.neofast.tech_revised.recipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
public final class RecipeHelper {
    private RecipeHelper() {}
    public static ItemStack itemStackFromJson(JsonObject json) {
        if (json.has("item") && !json.has("id")) {
            json.add("id", json.get("item"));
        }
        return ItemStack.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    }
    public static Ingredient ingredientFromJson(JsonElement json) {
        return Ingredient.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    }
    public static Ingredient ingredientFromNetwork(FriendlyByteBuf buffer) {
        return Ingredient.CONTENTS_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer);
    }
    public static void ingredientToNetwork(Ingredient ingredient, FriendlyByteBuf buffer) {
        Ingredient.CONTENTS_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, ingredient);
    }
    public static ItemStack readItem(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return ItemStack.EMPTY;
        }
        net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(buffer.readResourceLocation());
        int count = buffer.readVarInt();
        return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
    }
    public static void writeItem(FriendlyByteBuf buffer, ItemStack stack) {
        if (stack.isEmpty()) {
            buffer.writeBoolean(false);
            return;
        }
        buffer.writeBoolean(true);
        buffer.writeResourceLocation(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()));
        buffer.writeVarInt(stack.getCount());
    }
    public static net.minecraftforge.fluids.FluidStack readFluidStack(FriendlyByteBuf buffer) {
        net.minecraft.resources.ResourceLocation id = buffer.readResourceLocation();
        int amount = buffer.readVarInt();
        net.minecraft.world.level.material.Fluid fluid = net.minecraft.core.registries.BuiltInRegistries.FLUID.get(id);
        if (fluid == null) {
            return net.minecraftforge.fluids.FluidStack.EMPTY;
        }
        return new net.minecraftforge.fluids.FluidStack(fluid, amount);
    }
    public static void writeFluidStack(FriendlyByteBuf buffer, net.minecraftforge.fluids.FluidStack stack) {
        net.minecraft.resources.ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(stack.getFluid());
        buffer.writeResourceLocation(id == null ? net.minecraft.resources.ResourceLocation.parse("minecraft:empty") : id);
        buffer.writeVarInt(stack.getAmount());
    }
    public static ItemInteractionResult mapUse(InteractionResult result) {
        return switch (result) {
            case SUCCESS -> ItemInteractionResult.SUCCESS;
            case CONSUME, CONSUME_PARTIAL -> ItemInteractionResult.CONSUME;
            case FAIL -> ItemInteractionResult.FAIL;
            default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }
}
