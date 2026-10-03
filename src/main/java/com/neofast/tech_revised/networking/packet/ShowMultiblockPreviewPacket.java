package com.neofast.tech_revised.networking.packet;

import com.neofast.tech_revised.client.MultiblockPreviewRenderer;
import com.neofast.tech_revised.multiblock.MultiblockStructureBlueprint;
import com.neofast.tech_revised.multiblock.MachineLayout;
import com.neofast.tech_revised.multiblock.MachineLayouts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → client: show a translucent multiblock structure preview at a controller.
 */
public class ShowMultiblockPreviewPacket {
    private final BlockPos controllerPos;
    private final Direction front;
    private final String blueprintId;

    public ShowMultiblockPreviewPacket(BlockPos controllerPos, Direction front, String blueprintId) {
        this.controllerPos = controllerPos;
        this.front = front;
        this.blueprintId = blueprintId;
    }

    public static void encode(ShowMultiblockPreviewPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.controllerPos);
        buf.writeEnum(packet.front);
        buf.writeUtf(packet.blueprintId);
    }

    public static ShowMultiblockPreviewPacket decode(FriendlyByteBuf buf) {
        return new ShowMultiblockPreviewPacket(buf.readBlockPos(), buf.readEnum(Direction.class), buf.readUtf());
    }

    public static void handle(ShowMultiblockPreviewPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MultiblockStructureBlueprint blueprint = resolveBlueprint(packet.blueprintId);
            if (blueprint != null) {
                MultiblockPreviewRenderer.showPreview(packet.controllerPos, packet.front, blueprint);
            }
        }));
        ctx.get().setPacketHandled(true);
    }

    private static MultiblockStructureBlueprint resolveBlueprint(String id) {
        MachineLayout layout = MachineLayouts.get(id);
        return layout == null ? null : layout.toBlueprint();
    }
}
