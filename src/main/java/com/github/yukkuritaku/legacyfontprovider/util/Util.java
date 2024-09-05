package com.github.yukkuritaku.legacyfontprovider.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Util {

    public static <T> T make(Supplier<T> p_199748_0_) {
        return p_199748_0_.get();
    }

    public static <T> T make(T p_200696_0_, Consumer<T> p_200696_1_) {
        p_200696_1_.accept(p_200696_0_);
        return p_200696_0_;
    }
}
