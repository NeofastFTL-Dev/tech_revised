package com.neofast.tech_revised.client;

import com.neofast.tech_revised.block.ModBlocks;
import com.neofast.tech_revised.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

public final class PcSurvivalManualPages {
    public record Chapter(String id, String category, String layoutId, List<ItemStack> icons) {
        public String title() {
            return Component.translatable("screen.tech_revised.manual.chapter." + id + ".title").getString();
        }

        public String body() {
            return Component.translatable("screen.tech_revised.manual.chapter." + id + ".body").getString();
        }
    }

    public static final List<String> CATEGORY_ORDER = List.of(
            "start", "mining", "metals", "chemicals", "boards", "silicon", "pc", "peripherals", "office"
    );

    public static final List<String> CHAPTER_IDS = List.of(
            "the_goal",
            "factory_rules",
            "configurator",
            "first_tools",
            "ore_crushing",
            "mining_budget",
            "solid_fuel_generator",
            "coke_oven",
            "steel",
            "stainless",
            "crude_oil",
            "plastics",
            "copper_wire",
            "fiberglass",
            "bare_pcbs",
            "bare_pcbs_2",
            "silicon_ics",
            "ic_packages",
            "ram",
            "motherboard",
            "cpu",
            "gpu",
            "psu",
            "hdd",
            "ssd",
            "case",
            "tower_assembly",
            "monitor",
            "keyboard",
            "mouse",
            "hdmi_cable",
            "power_cable",
            "speakers",
            "desk_chair",
            "workstation"
    );

    private PcSurvivalManualPages() {
    }

    public static int size() {
        return CHAPTER_IDS.size();
    }

    public static Chapter get(int index) {
        String id = CHAPTER_IDS.get(index);
        return new Chapter(id, categoryOf(id), layoutOf(id), iconsOf(id));
    }

    public static String categoryTitle(String category) {
        return Component.translatable("screen.tech_revised.manual.category." + category).getString();
    }

    private static String categoryOf(String id) {
        return switch (id) {
            case "the_goal", "factory_rules", "configurator", "first_tools" -> "start";
            case "ore_crushing", "mining_budget", "solid_fuel_generator" -> "mining";
            case "coke_oven", "steel", "stainless" -> "metals";
            case "crude_oil", "plastics", "copper_wire" -> "chemicals";
            case "fiberglass", "bare_pcbs", "bare_pcbs_2" -> "boards";
            case "silicon_ics", "ic_packages", "ram" -> "silicon";
            case "motherboard", "cpu", "gpu", "psu", "hdd", "ssd", "case", "tower_assembly" -> "pc";
            case "monitor", "keyboard", "mouse", "hdmi_cable", "power_cable", "speakers" -> "peripherals";
            default -> "office";
        };
    }

    private static String layoutOf(String id) {
        return switch (id) {
            case "coke_oven" -> "coke_oven";
            case "ore_crushing" -> "crusher";
            case "steel" -> "eaf";
            case "stainless" -> "aod_converter";
            case "crude_oil" -> "drilling_platform";
            case "fiberglass" -> "industrial_batching_mixer";
            case "bare_pcbs" -> "design_engineering_station";
            case "bare_pcbs_2" -> "pick_and_place_robot";
            case "workstation" -> "workstation";
            default -> null;
        };
    }

    private static List<ItemStack> iconsOf(String id) {
        return switch (id) {
            case "the_goal" -> stacks(ModBlocks.TESTED_MONITOR.get(), ModBlocks.TESTED_DESKTOP_TOWER.get(), ModItems.PC_SURVIVAL_MANUAL.get());
            case "factory_rules" -> stacks(ModBlocks.TRANSPORT_FRAME.get(), ModBlocks.ELECTRIC_ARC_FURNACE_ENERGY_INPUT_HATCH.get(), ModItems.CONFIGURATOR.get());
            case "configurator" -> stacks(ModItems.CONFIGURATOR.get(), ModBlocks.TRANSPORT_FRAME.get());
            case "first_tools" -> stacks(ModBlocks.CRUSHER.get(), ModBlocks.SOLID_FUEL_GENERATOR.get(), ModItems.CONFIGURATOR.get());
            case "ore_crushing" -> stacks(ModBlocks.CRUSHER.get(), ModItems.GRAPHITE_DUST.get(), ModItems.CHROMITE_DUST.get());
            case "mining_budget" -> stacks(ModBlocks.CRUSHER.get());
            case "solid_fuel_generator" -> stacks(ModBlocks.SOLID_FUEL_GENERATOR.get(), ModItems.COAL_COKE.get());
            case "coke_oven" -> stacks(ModBlocks.COKE_OVEN_CONTROLLER.get(), ModItems.COAL_COKE.get());
            case "steel" -> stacks(ModBlocks.BLAST_FURNACE_CORE.get(), ModBlocks.ELECTRIC_ARC_FURNACE_CONTROLLER.get());
            case "stainless" -> stacks(ModBlocks.ARGON_OXYGEN_DECARBURIZATION_CONVERTER_CONTROLLER.get(), ModBlocks.OXYGEN_CONVERTER_CONTROLLER.get());
            case "crude_oil" -> stacks(ModBlocks.DRILLING_PLATFORM_CONTROLLER.get());
            case "plastics" -> stacks(ModBlocks.INDUSTRIAL_BATCHING_MIXER.get());
            case "copper_wire" -> stacks(ModItems.CONFIGURATOR.get());
            case "fiberglass" -> stacks(ModBlocks.INDUSTRIAL_BATCHING_MIXER.get(), ModBlocks.REFRACTORY_MELTING_FURNACE.get(), ModBlocks.INDUSTRIAL_TEXTILE_WEAVING_LOOM.get());
            case "bare_pcbs" -> stacks(ModBlocks.DESIGN_ENGINEERING_STATION.get(), ModBlocks.INDUSTRIAL_SHEAR.get(), ModBlocks.HYDRAULIC_CLADDING_PRESS.get());
            case "bare_pcbs_2" -> stacks(ModBlocks.CNC_ROUTER.get(), ModBlocks.PICK_AND_PLACE_ROBOT.get(), ModBlocks.AOI_VERIFICATION_STATION.get());
            case "silicon_ics" -> stacks(ModBlocks.DESIGN_ENGINEERING_STATION.get());
            case "workstation" -> stacks(ModBlocks.TESTED_MONITOR.get(), ModBlocks.TESTED_KEYBOARD.get(), ModBlocks.TESTED_DESK.get());
            case "desk_chair" -> stacks(ModBlocks.TESTED_DESK.get(), ModBlocks.TESTED_CHAIR_SEAT.get(), ModBlocks.TESTED_CHAIR_BACKREST.get());
            case "monitor" -> stacks(ModBlocks.TESTED_MONITOR.get());
            case "keyboard" -> stacks(ModBlocks.TESTED_KEYBOARD.get());
            case "mouse" -> stacks(ModBlocks.TESTED_MOUSE.get());
            case "hdmi_cable" -> stacks(ModBlocks.TESTED_HDMI_CABLE.get());
            case "power_cable" -> stacks(ModBlocks.TESTED_POWER_CABLE.get());
            case "speakers" -> stacks(ModBlocks.TESTED_SPEAKER.get());
            case "tower_assembly" -> stacks(ModBlocks.TESTED_DESKTOP_TOWER.get());
            default -> List.of();
        };
    }

    private static List<ItemStack> stacks(ItemLike... items) {
        List<ItemStack> list = new ArrayList<>();
        for (ItemLike item : items) {
            list.add(new ItemStack(item));
        }
        return list;
    }
}
