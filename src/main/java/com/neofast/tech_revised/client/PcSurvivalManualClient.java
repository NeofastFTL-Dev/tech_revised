package com.neofast.tech_revised.client;

import net.minecraft.client.Minecraft;

public final class PcSurvivalManualClient {
    private PcSurvivalManualClient() {
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new PcSurvivalManualScreen());
    }
}
