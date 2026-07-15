package com.neofast.tech_revised.fluid;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, TechRevised.MOD_ID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, TechRevised.MOD_ID);

    private static final ResourceLocation HEAVY_CRUDE_STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation HEAVY_CRUDE_FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");
    private static final ResourceLocation CREOSOTE_STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation CREOSOTE_FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");
    private static final ResourceLocation OXYGEN_STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation OXYGEN_FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");
    private static final ResourceLocation HYDROGEN_STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation HYDROGEN_FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");
    private static final ResourceLocation LIQUID_GLASS_STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation LIQUID_GLASS_FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");
    private static final ResourceLocation SODIUM_CARBONATE_STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation SODIUM_CARBONATE_FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");
    private static final ResourceLocation CUPRIC_CHLORIDE_STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation CUPRIC_CHLORIDE_FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");
    private static final ResourceLocation SODIUM_HYDROXIDE_STILL_TEXTURE =
            new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation SODIUM_HYDROXIDE_FLOWING_TEXTURE =
            new ResourceLocation("minecraft", "block/water_flow");

    public static final RegistryObject<FluidType> HEAVY_CRUDE_OIL_TYPE = FLUID_TYPES.register("heavy_crude_oil_type",
            () -> new BaseFluidType(HEAVY_CRUDE_STILL_TEXTURE, HEAVY_CRUDE_FLOWING_TEXTURE, 0xFF2C1F17,
                    FluidType.Properties.create()
                            .density(3200)
                            .viscosity(8000)
                            .temperature(350)
                            .descriptionId("fluid.tech_revised.heavy_crude_oil")));

    public static final RegistryObject<FlowingFluid> HEAVY_CRUDE_OIL = FLUIDS.register("heavy_crude_oil",
            () -> new ForgeFlowingFluid.Source(HeavyCrudeOilPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_HEAVY_CRUDE_OIL = FLUIDS.register("flowing_heavy_crude_oil",
            () -> new ForgeFlowingFluid.Flowing(HeavyCrudeOilPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FluidType> CREOSOTE_TYPE = FLUID_TYPES.register("creosote_type",
            () -> new BaseFluidType(CREOSOTE_STILL_TEXTURE, CREOSOTE_FLOWING_TEXTURE, 0xFF4B2B16,
                    FluidType.Properties.create()
                            .density(1800)
                            .viscosity(5000)
                            .temperature(420)
                            .descriptionId("fluid.tech_revised.creosote")));

    public static final RegistryObject<FlowingFluid> CREOSOTE = FLUIDS.register("creosote",
            () -> new ForgeFlowingFluid.Source(CreosotePropertiesHolder.PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_CREOSOTE = FLUIDS.register("flowing_creosote",
            () -> new ForgeFlowingFluid.Flowing(CreosotePropertiesHolder.PROPERTIES));

    public static final RegistryObject<FluidType> OXYGEN_TYPE = FLUID_TYPES.register("oxygen_type",
            () -> new BaseFluidType(OXYGEN_STILL_TEXTURE, OXYGEN_FLOWING_TEXTURE, 0xFF83D8FF,
                    FluidType.Properties.create()
                            .density(300)
                            .viscosity(600)
                            .temperature(295)
                            .descriptionId("fluid.tech_revised.oxygen")));

    public static final RegistryObject<FlowingFluid> OXYGEN = FLUIDS.register("oxygen",
            () -> new ForgeFlowingFluid.Source(OxygenPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_OXYGEN = FLUIDS.register("flowing_oxygen",
            () -> new ForgeFlowingFluid.Flowing(OxygenPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FluidType> HYDROGEN_TYPE = FLUID_TYPES.register("hydrogen_type",
            () -> new BaseFluidType(HYDROGEN_STILL_TEXTURE, HYDROGEN_FLOWING_TEXTURE, 0xFFEEF9FF,
                    FluidType.Properties.create()
                            .density(100)
                            .viscosity(400)
                            .temperature(295)
                            .descriptionId("fluid.tech_revised.hydrogen")));

    public static final RegistryObject<FlowingFluid> HYDROGEN = FLUIDS.register("hydrogen",
            () -> new ForgeFlowingFluid.Source(HydrogenPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_HYDROGEN = FLUIDS.register("flowing_hydrogen",
            () -> new ForgeFlowingFluid.Flowing(HydrogenPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FluidType> LIQUID_GLASS_TYPE = FLUID_TYPES.register("liquid_glass_type",
            () -> new BaseFluidType(LIQUID_GLASS_STILL_TEXTURE, LIQUID_GLASS_FLOWING_TEXTURE, 0xFFFFE57F,
                    FluidType.Properties.create()
                            .density(2500)
                            .viscosity(10000)
                            .temperature(1800)
                            .descriptionId("fluid.tech_revised.liquid_glass")));

    public static final RegistryObject<FlowingFluid> LIQUID_GLASS = FLUIDS.register("liquid_glass",
            () -> new ForgeFlowingFluid.Source(LiquidGlassPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_LIQUID_GLASS = FLUIDS.register("flowing_liquid_glass",
            () -> new ForgeFlowingFluid.Flowing(LiquidGlassPropertiesHolder.PROPERTIES));

    public static final RegistryObject<FluidType> SODIUM_CARBONATE_SOLUTION_TYPE = FLUID_TYPES.register("sodium_carbonate_solution_type",
            () -> new BaseFluidType(SODIUM_CARBONATE_STILL_TEXTURE, SODIUM_CARBONATE_FLOWING_TEXTURE, 0xFF7FBFFF,
                    FluidType.Properties.create()
                            .density(1100)
                            .viscosity(1000)
                            .descriptionId("fluid.tech_revised.sodium_carbonate_solution")));

    public static final RegistryObject<FlowingFluid> SODIUM_CARBONATE_SOLUTION = FLUIDS.register("sodium_carbonate_solution",
            () -> new ForgeFlowingFluid.Source(SodiumCarbonatePropertiesHolder.PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_SODIUM_CARBONATE_SOLUTION = FLUIDS.register("flowing_sodium_carbonate_solution",
            () -> new ForgeFlowingFluid.Flowing(SodiumCarbonatePropertiesHolder.PROPERTIES));

    public static final RegistryObject<FluidType> CUPRIC_CHLORIDE_SOLUTION_TYPE = FLUID_TYPES.register("cupric_chloride_solution_type",
            () -> new BaseFluidType(CUPRIC_CHLORIDE_STILL_TEXTURE, CUPRIC_CHLORIDE_FLOWING_TEXTURE, 0xFF007F3F,
                    FluidType.Properties.create()
                            .density(1200)
                            .viscosity(1200)
                            .descriptionId("fluid.tech_revised.cupric_chloride_solution")));

    public static final RegistryObject<FlowingFluid> CUPRIC_CHLORIDE_SOLUTION = FLUIDS.register("cupric_chloride_solution",
            () -> new ForgeFlowingFluid.Source(CupricChloridePropertiesHolder.PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_CUPRIC_CHLORIDE_SOLUTION = FLUIDS.register("flowing_cupric_chloride_solution",
            () -> new ForgeFlowingFluid.Flowing(CupricChloridePropertiesHolder.PROPERTIES));

    public static final RegistryObject<FluidType> SODIUM_HYDROXIDE_SOLUTION_TYPE = FLUID_TYPES.register("sodium_hydroxide_solution_type",
            () -> new BaseFluidType(SODIUM_HYDROXIDE_STILL_TEXTURE, SODIUM_HYDROXIDE_FLOWING_TEXTURE, 0xFFE0E0E0,
                    FluidType.Properties.create()
                            .density(1150)
                            .viscosity(1100)
                            .descriptionId("fluid.tech_revised.sodium_hydroxide_solution")));

    public static final RegistryObject<FlowingFluid> SODIUM_HYDROXIDE_SOLUTION = FLUIDS.register("sodium_hydroxide_solution",
            () -> new ForgeFlowingFluid.Source(SodiumHydroxidePropertiesHolder.PROPERTIES));

    public static final RegistryObject<FlowingFluid> FLOWING_SODIUM_HYDROXIDE_SOLUTION = FLUIDS.register("flowing_sodium_hydroxide_solution",
            () -> new ForgeFlowingFluid.Flowing(SodiumHydroxidePropertiesHolder.PROPERTIES));

    private static final class HeavyCrudeOilPropertiesHolder {
        private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
                HEAVY_CRUDE_OIL_TYPE, HEAVY_CRUDE_OIL, FLOWING_HEAVY_CRUDE_OIL)
                .bucket(ModItems.HEAVY_CRUDE_OIL_BUCKET)
                .block(ModBlocks.HEAVY_CRUDE_OIL_BLOCK)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(30);
    }

    private static final class CreosotePropertiesHolder {
        private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
                CREOSOTE_TYPE, CREOSOTE, FLOWING_CREOSOTE)
                .bucket(ModItems.CREOSOTE_BUCKET)
                .block(ModBlocks.CREOSOTE_BLOCK)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(20);
    }

    private static final class OxygenPropertiesHolder {
        private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
                OXYGEN_TYPE, OXYGEN, FLOWING_OXYGEN)
                .bucket(ModItems.OXYGEN_BUCKET)
                .block(ModBlocks.OXYGEN_BLOCK)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(10);
    }

    private static final class HydrogenPropertiesHolder {
        private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
                HYDROGEN_TYPE, HYDROGEN, FLOWING_HYDROGEN)
                .bucket(ModItems.HYDROGEN_BUCKET)
                .block(ModBlocks.HYDROGEN_BLOCK)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(10);
    }

    private static final class LiquidGlassPropertiesHolder {
        private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
                LIQUID_GLASS_TYPE, LIQUID_GLASS, FLOWING_LIQUID_GLASS)
                .bucket(ModItems.LIQUID_GLASS_BUCKET)
                .block(ModBlocks.LIQUID_GLASS_BLOCK)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(40);
    }

    private static final class SodiumCarbonatePropertiesHolder {
        private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
                SODIUM_CARBONATE_SOLUTION_TYPE, SODIUM_CARBONATE_SOLUTION, FLOWING_SODIUM_CARBONATE_SOLUTION)
                .bucket(ModItems.SODIUM_CARBONATE_SOLUTION_BUCKET)
                .block(ModBlocks.SODIUM_CARBONATE_SOLUTION_BLOCK)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(10);
    }

    private static final class CupricChloridePropertiesHolder {
        private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
                CUPRIC_CHLORIDE_SOLUTION_TYPE, CUPRIC_CHLORIDE_SOLUTION, FLOWING_CUPRIC_CHLORIDE_SOLUTION)
                .bucket(ModItems.CUPRIC_CHLORIDE_SOLUTION_BUCKET)
                .block(ModBlocks.CUPRIC_CHLORIDE_SOLUTION_BLOCK)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(10);
    }

    private static final class SodiumHydroxidePropertiesHolder {
        private static final ForgeFlowingFluid.Properties PROPERTIES = new ForgeFlowingFluid.Properties(
                SODIUM_HYDROXIDE_SOLUTION_TYPE, SODIUM_HYDROXIDE_SOLUTION, FLOWING_SODIUM_HYDROXIDE_SOLUTION)
                .bucket(ModItems.SODIUM_HYDROXIDE_SOLUTION_BUCKET)
                .block(ModBlocks.SODIUM_HYDROXIDE_SOLUTION_BLOCK)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .tickRate(10);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }
}
