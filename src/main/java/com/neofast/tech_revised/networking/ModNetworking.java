package com.neofast.tech_revised.networking;

import com.neofast.tech_revised.TechRevised;
import com.neofast.tech_revised.networking.packet.ShowMultiblockPreviewPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TechRevised.MOD_ID, "messages"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    private ModNetworking() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                ShowMultiblockPreviewPacket.class,
                ShowMultiblockPreviewPacket::encode,
                ShowMultiblockPreviewPacket::decode,
                ShowMultiblockPreviewPacket::handle
        );
    }
}
