package net.minecraftforge.network.simple;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
public class SimpleChannel {
    private final ResourceLocation name;
    private final Map<Class<?>, Integer> classToId = new HashMap<>();
    private final Map<Integer, Message<?>> messages = new HashMap<>();
    private boolean registeredPayload;
    public SimpleChannel(ResourceLocation name) { this.name = name; }
    public <MSG> void registerMessage(int index, Class<MSG> type, BiConsumer<MSG, FriendlyByteBuf> encoder,
                                      Function<FriendlyByteBuf, MSG> decoder,
                                      BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {
        classToId.put(type, index);
        messages.put(index, new Message<>(encoder, decoder, handler));
        ensurePayload();
    }
    private void ensurePayload() {
        if (registeredPayload) return;
        registeredPayload = true;
        PayloadTypeRegistry.playC2S().register(ChannelPayload.TYPE, ChannelPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ChannelPayload.TYPE, ChannelPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ChannelPayload.TYPE, (payload, context) -> {
            handle(payload, context.player(), false);
        });
        if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(ChannelPayload.TYPE, (payload, context) -> {
                handle(payload, null, true);
            });
        }
    }
    @SuppressWarnings("unchecked")
    private void handle(ChannelPayload payload, ServerPlayer sender, boolean client) {
        Message<Object> message = (Message<Object>) messages.get(payload.id());
        if (message == null) return;
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(payload.data()));
        Object packet = message.decoder.apply(buf);
        NetworkEvent.Context ctx = new NetworkEvent.Context(sender, client);
        message.handler.accept(packet, () -> ctx);
    }
    public <MSG> void sendToServer(MSG message) {
        ClientPlayNetworking.send(encode(message));
    }
    public <MSG> void send(PacketDistributor.TargetPoint target, MSG message) {
        if (target.player != null) {
            ServerPlayNetworking.send(target.player, encode(message));
        }
    }
    @SuppressWarnings("unchecked")
    private <MSG> ChannelPayload encode(MSG message) {
        Integer id = classToId.get(message.getClass());
        if (id == null) throw new IllegalArgumentException("Unregistered packet " + message.getClass());
        Message<MSG> registered = (Message<MSG>) messages.get(id);
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        registered.encoder.accept(message, buf);
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new ChannelPayload(id, data);
    }
    public record ChannelPayload(int id, byte[] data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ChannelPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("tech_revised", "channel"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ChannelPayload> CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeVarInt(payload.id);
                    buf.writeByteArray(payload.data);
                },
                buf -> new ChannelPayload(buf.readVarInt(), buf.readByteArray())
        );
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    private record Message<MSG>(BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder,
                                BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler) {}
}
