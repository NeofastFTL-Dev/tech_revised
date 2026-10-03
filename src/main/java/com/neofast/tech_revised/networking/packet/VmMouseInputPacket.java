package com.neofast.tech_revised.networking.packet;

import com.neofast.tech_revised.block.entity.custom.Windows7VmBlockEntity;
import com.neofast.tech_revised.integration.vm.VmCommandIntegration;
import com.neofast.tech_revised.integration.vm.VmLiveStreamManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VmMouseInputPacket {
    private static final int MIN_COORD = 0;
    private static final int MAX_COORD = 65535;
    private static final int MAX_WHEEL = 1920;

    private final BlockPos blockPos;
    private final int absoluteX;
    private final int absoluteY;
    private final int wheelDelta;
    private final int buttonMask;
    private final boolean completeClick;

    public VmMouseInputPacket(BlockPos blockPos, int absoluteX, int absoluteY, int buttonMask) {
        this(blockPos, absoluteX, absoluteY, 0, buttonMask, false);
    }

    public VmMouseInputPacket(BlockPos blockPos, int absoluteX, int absoluteY, int wheelDelta, int buttonMask) {
        this(blockPos, absoluteX, absoluteY, wheelDelta, buttonMask, false);
    }

    public VmMouseInputPacket(BlockPos blockPos, int absoluteX, int absoluteY, int wheelDelta, int buttonMask, boolean completeClick) {
        this.blockPos = blockPos;
        this.absoluteX = absoluteX;
        this.absoluteY = absoluteY;
        this.wheelDelta = wheelDelta;
        this.buttonMask = buttonMask;
        this.completeClick = completeClick;
    }

    public static VmMouseInputPacket click(BlockPos blockPos, int absoluteX, int absoluteY, int buttonMask) {
        return new VmMouseInputPacket(blockPos, absoluteX, absoluteY, 0, buttonMask, true);
    }

    public static void encode(VmMouseInputPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.blockPos);
        buffer.writeVarInt(packet.absoluteX);
        buffer.writeVarInt(packet.absoluteY);
        buffer.writeVarInt(packet.wheelDelta);
        buffer.writeVarInt(packet.buttonMask);
        buffer.writeBoolean(packet.completeClick);
    }

    public static VmMouseInputPacket decode(FriendlyByteBuf buffer) {
        BlockPos blockPos = buffer.readBlockPos();
        int absoluteX = buffer.readVarInt();
        int absoluteY = buffer.readVarInt();
        int wheelDelta = buffer.readVarInt();
        int buttonMask = buffer.readVarInt();
        boolean completeClick = buffer.readBoolean();
        return new VmMouseInputPacket(blockPos, absoluteX, absoluteY, wheelDelta, buttonMask, completeClick);
    }

    public static void handle(VmMouseInputPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            if (!player.hasPermissions(2)) {
                return;
            }

            if (player.distanceToSqr(
                    packet.blockPos.getX() + 0.5D,
                    packet.blockPos.getY() + 0.5D,
                    packet.blockPos.getZ() + 0.5D) > 256.0D) {
                return;
            }

            if (packet.absoluteX < MIN_COORD || packet.absoluteX > MAX_COORD
                    || packet.absoluteY < MIN_COORD || packet.absoluteY > MAX_COORD) {
                return;
            }

            if (packet.wheelDelta < -MAX_WHEEL || packet.wheelDelta > MAX_WHEEL) {
                return;
            }

            if ((packet.buttonMask & ~0x07) != 0) {
                return;
            }

            BlockEntity blockEntity = player.level().getBlockEntity(packet.blockPos);
            if (!(blockEntity instanceof Windows7VmBlockEntity vmBlockEntity)) {
                return;
            }

            boolean injected = VmLiveStreamManager.injectMouse(
                    player,
                    packet.blockPos,
                    packet.absoluteX,
                    packet.absoluteY,
                    packet.wheelDelta,
                    packet.buttonMask,
                    packet.completeClick);
            if (injected) {
                return;
            }

            if (packet.completeClick) {
                VmCommandIntegration.sendMouseClickAsync(
                        vmBlockEntity.getVmName(),
                        vmBlockEntity.getStartCommand(),
                        vmBlockEntity.getStopCommand(),
                        packet.absoluteX,
                        packet.absoluteY,
                        packet.buttonMask
                );
            } else {
                VmCommandIntegration.sendMouseEventAsync(
                        vmBlockEntity.getVmName(),
                        vmBlockEntity.getStartCommand(),
                        vmBlockEntity.getStopCommand(),
                        packet.absoluteX,
                        packet.absoluteY,
                        packet.wheelDelta,
                        packet.buttonMask
                );
            }
        });
        context.setPacketHandled(true);
    }
}
