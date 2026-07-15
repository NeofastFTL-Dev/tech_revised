package com.neofast.tech_revised.recipe;

import com.neofast.tech_revised.TechRevised;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TechRevised.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> INDUSTRIAL_STEEL_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "industrialsteel");

    public static final RegistryObject<RecipeSerializer<CrusherRecipe>> CRUSHER_SERIALIZER =
            SERIALIZERS.register("crusher", CrusherRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ElectricArcFurnaceRecipe>> ELECTRIC_ARC_FURNACE_SERIALIZER =
            SERIALIZERS.register("electric_arc_furnace", ElectricArcFurnaceRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ArgonOxygenDecarburizationConverterRecipe>> ARGON_OXYGEN_DECARBURIZATION_CONVERTER_SERIALIZER =
            SERIALIZERS.register("argon_oxygen_decarburization_converter",
                    ArgonOxygenDecarburizationConverterRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<CokeOvenRecipe>> COKE_OVEN_SERIALIZER =
            SERIALIZERS.register("coke_oven", CokeOvenRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<DrillingPlatformRecipe>> DRILLING_PLATFORM_SERIALIZER =
            SERIALIZERS.register("drilling_platform", DrillingPlatformRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<OxygenConverterRecipe>> OXYGEN_CONVERTER_SERIALIZER =
            SERIALIZERS.register("oxygen_converter", OxygenConverterRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<AlloyingRecipe>> ALLOYING_SERIALIZER =
            INDUSTRIAL_STEEL_SERIALIZERS.register("alloying", AlloyingRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<BatchingRecipe>> BATCHING_SERIALIZER =
            SERIALIZERS.register("batching", BatchingRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<MeltingRecipe>> MELTING_SERIALIZER =
            SERIALIZERS.register("melting", MeltingRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<FluidToItemRecipe>> EXTRUSION_SERIALIZER =
            SERIALIZERS.register("extrusion", FluidToItemRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ItemFluidToItemRecipe>> SIZING_SERIALIZER =
            SERIALIZERS.register("sizing", ItemFluidToItemRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<GenericIndustrialRecipe>> DRYING_SERIALIZER =
            SERIALIZERS.register("drying", () -> new GenericIndustrialRecipe.Serializer(() -> ModRecipes.DRYING_SERIALIZER.get(), DryingRecipeType.INSTANCE));

    public static final RegistryObject<RecipeSerializer<GenericIndustrialRecipe>> WINDING_SERIALIZER =
            SERIALIZERS.register("winding", () -> new GenericIndustrialRecipe.Serializer(() -> ModRecipes.WINDING_SERIALIZER.get(), WindingRecipeType.INSTANCE));

    public static final RegistryObject<RecipeSerializer<GenericIndustrialRecipe>> BUNDLING_SERIALIZER =
            SERIALIZERS.register("bundling", () -> new GenericIndustrialRecipe.Serializer(() -> ModRecipes.BUNDLING_SERIALIZER.get(), BundlingRecipeType.INSTANCE));

    public static final RegistryObject<RecipeSerializer<GenericIndustrialRecipe>> WEAVING_SERIALIZER =
            SERIALIZERS.register("weaving", () -> new GenericIndustrialRecipe.Serializer(() -> ModRecipes.WEAVING_SERIALIZER.get(), WeavingRecipeType.INSTANCE));

    public static final RegistryObject<RecipeSerializer<GenericIndustrialRecipe>> CHOPPING_SERIALIZER =
            SERIALIZERS.register("chopping", () -> new GenericIndustrialRecipe.Serializer(() -> ModRecipes.CHOPPING_SERIALIZER.get(), ChoppingRecipeType.INSTANCE));

    public static final RegistryObject<RecipeSerializer<PcbRecipe>> PCB_SERIALIZER =
            SERIALIZERS.register("pcb", () -> new PcbRecipe.Serializer(() -> ModRecipes.PCB_SERIALIZER.get(), PcbRecipeType.INSTANCE));

    public static final RegistryObject<RecipeSerializer<LaminationRecipe>> LAMINATION_SERIALIZER =
            SERIALIZERS.register("lamination", () -> new LaminationRecipe.Serializer(() -> ModRecipes.LAMINATION_SERIALIZER.get()));

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        INDUSTRIAL_STEEL_SERIALIZERS.register(eventBus);
    }
}

class DryingRecipeType implements net.minecraft.world.item.crafting.RecipeType<GenericIndustrialRecipe> {
    public static final DryingRecipeType INSTANCE = new DryingRecipeType();
    @Override public String toString() { return "tech_revised:drying"; }
}
class WindingRecipeType implements net.minecraft.world.item.crafting.RecipeType<GenericIndustrialRecipe> {
    public static final WindingRecipeType INSTANCE = new WindingRecipeType();
    @Override public String toString() { return "tech_revised:winding"; }
}
class BundlingRecipeType implements net.minecraft.world.item.crafting.RecipeType<GenericIndustrialRecipe> {
    public static final BundlingRecipeType INSTANCE = new BundlingRecipeType();
    @Override public String toString() { return "tech_revised:bundling"; }
}
class WeavingRecipeType implements net.minecraft.world.item.crafting.RecipeType<GenericIndustrialRecipe> {
    public static final WeavingRecipeType INSTANCE = new WeavingRecipeType();
    @Override public String toString() { return "tech_revised:weaving"; }
}
class ChoppingRecipeType implements net.minecraft.world.item.crafting.RecipeType<GenericIndustrialRecipe> {
    public static final ChoppingRecipeType INSTANCE = new ChoppingRecipeType();
    @Override public String toString() { return "tech_revised:chopping"; }
}

class PcbRecipeType implements net.minecraft.world.item.crafting.RecipeType<PcbRecipe> {
    public static final PcbRecipeType INSTANCE = new PcbRecipeType();
    @Override public String toString() { return "tech_revised:pcb"; }
}

class LaminationRecipeType implements net.minecraft.world.item.crafting.RecipeType<LaminationRecipe> {
    public static final LaminationRecipeType INSTANCE = new LaminationRecipeType();
    @Override public String toString() { return "tech_revised:lamination"; }
}
