package org.acme.user.control;

public record FieldUpdate<T>(boolean present, T value) {

    public static <T> FieldUpdate<T> unset() {
        return new FieldUpdate<>(false, null);
    }

    public static <T> FieldUpdate<T> set(T value) {
        return new FieldUpdate<>(true, value);
    }
}
