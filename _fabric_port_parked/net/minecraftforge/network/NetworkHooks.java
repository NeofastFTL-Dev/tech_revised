package net.minecraftforge.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.common.extensions.IForgeMenuType;

import java.util.function.Consumer;

public class NetworkHooks {
    public static void openScreen(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        openScreen(player, provider, buf -> buf.writeBlockPos(pos));
    }

    public static void openScreen(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> extraData) {
        player.openMenu(new ExtendedScreenHandlerFactory<IForgeMenuType.ExtraData>() {
            @Override
            public IForgeMenuType.ExtraData getScreenOpeningData(ServerPlayer p) {
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
                extraData.accept(buf);
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                return new IForgeMenuType.ExtraData(bytes);
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                return provider.createMenu(syncId, inv, p);
            }
        });
    }
}
