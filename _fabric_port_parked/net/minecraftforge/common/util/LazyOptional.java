package net.minecraftforge.common.util;
import java.util.function.Supplier;
public class LazyOptional<T> {
    private static final LazyOptional<?> EMPTY = new LazyOptional<>(null);
    private Supplier<T> supplier;
    private T value;
    private boolean resolved;
    private boolean valid = true;
    private LazyOptional(Supplier<T> supplier) { this.supplier = supplier; }
    public static <T> LazyOptional<T> of(Supplier<T> supplier) { return new LazyOptional<>(supplier); }
    @SuppressWarnings("unchecked")
    public static <T> LazyOptional<T> empty() { return (LazyOptional<T>) EMPTY; }
    public boolean isPresent() { return valid && supplier != null; }
    public T orElse(T other) {
        T v = getValue();
        return v == null ? other : v;
    }
    public T orElseThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
        T v = getValue();
        if (v == null) throw exceptionSupplier.get();
        return v;
    }
    private T getValue() {
        if (!valid || supplier == null) return null;
        if (!resolved) {
            value = supplier.get();
            resolved = true;
        }
        return value;
    }
    @SuppressWarnings("unchecked")
    public <X> LazyOptional<X> cast() { return (LazyOptional<X>) this; }
    public void invalidate() { valid = false; value = null; }
    public void ifPresent(java.util.function.Consumer<? super T> consumer) {
        T v = getValue();
        if (v != null) consumer.accept(v);
    }
}
