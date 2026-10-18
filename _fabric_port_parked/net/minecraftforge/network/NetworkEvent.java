package net.minecraftforge.network;
import net.minecraft.server.level.ServerPlayer;
public class NetworkEvent {
    public static class Context {
        private final ServerPlayer sender;
        private final boolean client;
        public Context(ServerPlayer sender, boolean client) {
            this.sender = sender;
            this.client = client;
        }
        public ServerPlayer getSender() { return sender; }
        public boolean isClient() { return client; }
        public void enqueueWork(Runnable runnable) {
            if (sender != null && sender.getServer() != null) {
                sender.getServer().execute(runnable);
            } else {
                runnable.run();
            }
        }
        public void setPacketHandled(boolean handled) {}
    }
}
