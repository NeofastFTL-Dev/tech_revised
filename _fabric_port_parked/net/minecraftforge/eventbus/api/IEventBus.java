package net.minecraftforge.eventbus.api;
public interface IEventBus {
    default void register(Object listener) {}
    default <T> void addListener(java.util.function.Consumer<T> listener) {}
}
