
package com.neofast.tech_revised;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.client.ModItemColorHandlers;
import com.neofast.tech_revised.client.MultiblockPreviewRenderer;
import com.neofast.tech_revised.fluid.BaseFluidType;
import com.neofast.tech_revised.fluid.ModFluids;
import com.neofast.tech_revised.item.ModItems;
import com.neofast.tech_revised.screen.CrusherScreen;
import com.neofast.tech_revised.screen.ElectricArcFurnaceControllerScreen;
import com.neofast.tech_revised.screen.ElectricArcFurnaceInputBusScreen;
import com.neofast.tech_revised.screen.ElectricArcFurnaceOutputBusScreen;
import com.neofast.tech_revised.screen.GenericIndustrialMachineScreen;
import com.neofast.tech_revised.screen.ModMenuTypes;
import com.neofast.tech_revised.screen.MultiInputIndustrialMachineScreen;
import com.neofast.tech_revised.screen.MultiblockControllerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.concurrent.atomic.AtomicReference;

public class TechRevisedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(ModMenuTypes.CRUSHER_MENU.get(), CrusherScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_ARC_FURNACE_CONTROLLER_MENU.get(), ElectricArcFurnaceControllerScreen::new);
        MenuScreens.register(ModMenuTypes.MULTIBLOCK_CONTROLLER_MENU.get(), MultiblockControllerScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_ARC_FURNACE_INPUT_BUS_MENU.get(), ElectricArcFurnaceInputBusScreen::new);
        MenuScreens.register(ModMenuTypes.ELECTRIC_ARC_FURNACE_OUTPUT_BUS_MENU.get(), ElectricArcFurnaceOutputBusScreen::new);
        MenuScreens.register(ModMenuTypes.GENERIC_INDUSTRIAL_MACHINE_MENU.get(), GenericIndustrialMachineScreen::new);
        MenuScreens.register(ModMenuTypes.MULTI_INPUT_INDUSTRIAL_MACHINE_MENU.get(), MultiInputIndustrialMachineScreen::new);

        registerFluid(ModFluids.HEAVY_CRUDE_OIL.get(), ModFluids.FLOWING_HEAVY_CRUDE_OIL.get(), ModFluids.HEAVY_CRUDE_OIL_TYPE.get());
        registerFluid(ModFluids.CREOSOTE.get(), ModFluids.FLOWING_CREOSOTE.get(), ModFluids.CREOSOTE_TYPE.get());
        registerFluid(ModFluids.OXYGEN.get(), ModFluids.FLOWING_OXYGEN.get(), ModFluids.OXYGEN_TYPE.get());
        registerFluid(ModFluids.HYDROGEN.get(), ModFluids.FLOWING_HYDROGEN.get(), ModFluids.HYDROGEN_TYPE.get());
        registerFluid(ModFluids.NAPHTHA.get(), ModFluids.FLOWING_NAPHTHA.get(), ModFluids.NAPHTHA_TYPE.get());
        registerFluid(ModFluids.ETHYLENE.get(), ModFluids.FLOWING_ETHYLENE.get(), ModFluids.ETHYLENE_TYPE.get());
        registerFluid(ModFluids.CHLORINE.get(), ModFluids.FLOWING_CHLORINE.get(), ModFluids.CHLORINE_TYPE.get());
        registerFluid(ModFluids.VINYL_CHLORIDE.get(), ModFluids.FLOWING_VINYL_CHLORIDE.get(), ModFluids.VINYL_CHLORIDE_TYPE.get());
        registerFluid(ModFluids.LIQUID_GLASS.get(), ModFluids.FLOWING_LIQUID_GLASS.get(), ModFluids.LIQUID_GLASS_TYPE.get());
        registerFluid(ModFluids.SODIUM_CARBONATE_SOLUTION.get(), ModFluids.FLOWING_SODIUM_CARBONATE_SOLUTION.get(), ModFluids.SODIUM_CARBONATE_SOLUTION_TYPE.get());
        registerFluid(ModFluids.CUPRIC_CHLORIDE_SOLUTION.get(), ModFluids.FLOWING_CUPRIC_CHLORIDE_SOLUTION.get(), ModFluids.CUPRIC_CHLORIDE_SOLUTION_TYPE.get());
        registerFluid(ModFluids.SODIUM_HYDROXIDE_SOLUTION.get(), ModFluids.FLOWING_SODIUM_HYDROXIDE_SOLUTION.get(), ModFluids.SODIUM_HYDROXIDE_SOLUTION_TYPE.get());

        ModBlocks.getTintedBlockColors().forEach((block, color) ->
                ColorProviderRegistry.BLOCK.register((state, level, pos, tintIndex) -> tintIndex == 0 ? color : 0xFFFFFFFF, block.get()));
        ModItems.getTintedItemColors().forEach((item, color) ->
                ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 0 ? color : 0xFFFFFFFF, item.get()));

        MultiblockPreviewRenderer.register();
    }

    private static void registerFluid(FlowingFluid still, FlowingFluid flowing, FluidType type) {
        BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), still, flowing);
        AtomicReference<IClientFluidTypeExtensions> ext = new AtomicReference<>();
        type.initializeClient(ext::set);
        IClientFluidTypeExtensions client = ext.get();
        if (client != null) {
            FluidRenderHandlerRegistry.INSTANCE.register(still, flowing,
                    new SimpleFluidRenderHandler(client.getStillTexture(), client.getFlowingTexture(), client.getTintColor()));
        }
    }
}
