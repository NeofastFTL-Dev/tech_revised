package net.minecraftforge.network;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Supplier;
public class PacketDistributor {
    public static final PacketDistributor PLAYER = new PacketDistributor();
    public TargetPoint with(Supplier<ServerPlayer> player) { return new TargetPoint(player.get()); }
    public static final class TargetPoint {
        public final ServerPlayer player;
        public TargetPoint(ServerPlayer player) { this.player = player; }
    }
}
