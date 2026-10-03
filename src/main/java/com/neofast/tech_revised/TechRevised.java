
package com.neofast.tech_revised;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.block.entity.ModBlockEntities;
import com.neofast.tech_revised.fluid.ModFluids;
import com.neofast.tech_revised.item.ModCreativeModeTabs;
import com.neofast.tech_revised.item.ModItems;
import com.neofast.tech_revised.networking.ModNetworking;
import com.neofast.tech_revised.recipe.ModRecipes;
import com.neofast.tech_revised.screen.ModMenuTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TechRevised implements ModInitializer {
    public static final String MOD_ID = "tech_revised";
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModFluids.register(eventBus);
        ModBlocks.register(eventBus);
        ModItems.register(eventBus);
        ModCreativeModeTabs.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModMenuTypes.register(eventBus);
        ModRecipes.register(eventBus);
        ModNetworking.register();
        addOres();
        LOGGER.info("Tech Revised initialized on Fabric 1.21.1");
    }

    private static void addOres() {
        addOre("tin_ore_placed");
        addOre("nickel_ore_placed");
        addOre("graphite_ore_placed");
        addOre("chromite_ore_placed");
    }

    private static void addOre(String name) {
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MOD_ID, name))
        );
    }
}
