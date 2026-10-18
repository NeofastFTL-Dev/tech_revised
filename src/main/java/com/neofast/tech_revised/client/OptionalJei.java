package com.neofast.tech_revised.client;

import net.minecraft.world.item.ItemStack;

public final class OptionalJei {
    private OptionalJei() {
    }

    public static void showRecipes(ItemStack stack, boolean uses) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        try {
            Class<?> plugin = Class.forName("com.neofast.tech_revised.integration.jei.TechRevisedJeiPlugin");
            plugin.getMethod("showItemRecipes", ItemStack.class, boolean.class).invoke(null, stack, uses);
        } catch (Throwable ignored) {
        }
    }
}
