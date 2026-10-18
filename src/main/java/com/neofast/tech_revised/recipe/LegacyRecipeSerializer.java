
package com.neofast.tech_revised.recipe;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import java.util.stream.Stream;
public abstract class LegacyRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    public abstract T fromJson(JsonObject json);
    public abstract T fromNetwork(RegistryFriendlyByteBuf buffer);
    public abstract void toNetwork(FriendlyByteBuf buffer, T recipe);
    @Override
    public MapCodec<T> codec() {
        return new MapCodec<>() {
            @Override public <T1> Stream<T1> keys(DynamicOps<T1> ops) { return Stream.empty(); }
            @Override public <T1> DataResult<T> decode(DynamicOps<T1> ops, MapLike<T1> input) {
                JsonObject json = ops.convertTo(JsonOps.INSTANCE, ops.createMap(input.entries())).getAsJsonObject();
                try {
                    return DataResult.success(fromJson(json));
                } catch (Exception e) {
                    return DataResult.error(e::getMessage);
                }
            }
            @Override public <T1> RecordBuilder<T1> encode(T input, DynamicOps<T1> ops, RecordBuilder<T1> prefix) {
                return prefix;
            }
        };
    }
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return StreamCodec.of((buf, recipe) -> toNetwork(buf, recipe), this::fromNetwork);
    }
}
