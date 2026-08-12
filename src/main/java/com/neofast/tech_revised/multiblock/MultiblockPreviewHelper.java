package com.neofast.tech_revised.multiblock;

import com.neofast.tech_revised.networking.ModNetworking;
import com.neofast.tech_revised.networking.packet.ShowMultiblockPreviewPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

/**
 * Shared entry point for showing in-world multiblock structure previews
 * (GregTech-style hologram) when a player shift-right-clicks a controller.
 */
public final class MultiblockPreviewHelper {
    private MultiblockPreviewHelper() {
    }

    /**
     * Sends a preview packet to the given player.
     *
     * @param player        target player (must be a ServerPlayer for the packet to send)
     * @param controllerPos world position of the controller block
     * @param front         horizontal facing of the controller
     * @param blueprintId   one of: eaf, coke_oven, drilling_platform, oxygen_converter, aod_converter
     */
    public static void showPreview(Player player, BlockPos controllerPos, Direction front, String blueprintId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        ModNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> serverPlayer),
                new ShowMultiblockPreviewPacket(controllerPos, front, blueprintId)
        );
    }
}
