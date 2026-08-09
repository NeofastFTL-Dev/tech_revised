package com.neofast.tech_revised.integration.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class MultiblockLayoutJeiRecipe {
    private final Component machineName;
    private final ItemStack controller;
    private final List<ItemStack> requiredParts;
    private final Component dimensions;
    private final List<Component> notes;
    private final MultiblockStructureBlueprint blueprint;

    public MultiblockLayoutJeiRecipe(Component machineName,
                                     ItemStack controller,
                                     List<ItemStack> requiredParts,
                                     Component dimensions,
                                     List<Component> notes,
                                     MultiblockStructureBlueprint blueprint) {
        this.machineName = machineName;
        this.controller = controller.copy();
        this.requiredParts = requiredParts.stream()
                .map(ItemStack::copy)
                .collect(Collectors.toUnmodifiableList());
        this.dimensions = dimensions;
        this.notes = List.copyOf(notes);
        this.blueprint = blueprint;
    }

    public Component getMachineName() {
        return machineName;
    }

    public ItemStack getController() {
        return controller.copy();
    }

    public List<ItemStack> getRequiredParts() {
        // Prefer live blueprint materials (accurate counts) when available.
        if (blueprint != null && !blueprint.getMaterials().isEmpty()) {
            return blueprint.getMaterials();
        }
        return requiredParts;
    }

    public Component getDimensions() {
        return dimensions;
    }

    public List<Component> getNotes() {
        return notes;
    }

    public MultiblockStructureBlueprint getBlueprint() {
        return blueprint;
    }
}
