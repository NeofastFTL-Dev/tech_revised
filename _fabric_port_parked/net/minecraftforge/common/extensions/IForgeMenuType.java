package net.minecraftforge.common.extensions;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;

public class IForgeMenuType {
    public record ExtraData(byte[] bytes) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ExtraData> CODEC = StreamCodec.of(
                (buf, data) -> buf.writeByteArray(data.bytes),
                buf -> new ExtraData(buf.readByteArray())
        );
    }

    public static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        return new ExtendedScreenHandlerType<>(
                (syncId, inv, data) -> {
                    FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data.bytes()));
                    return factory.create(syncId, inv, buf);
                },
                ExtraData.CODEC
        );
    }
}
