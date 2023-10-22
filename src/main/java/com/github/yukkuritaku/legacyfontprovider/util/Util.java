package com.github.yukkuritaku.legacyfontprovider.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Util {


    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <T> T make(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }
}
