package com.github.yukkuritaku.legacyfontprovider.font;

import java.util.Arrays;
import java.util.function.IntFunction;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class CodepointMap<T> {

    private static final int BLOCK_BITS = 8;
    private static final int BLOCK_SIZE = 256;
    private static final int IN_BLOCK_MASK = 0xFF;
    private static final int MAX_BLOCK = 4351;
    private static final int BLOCK_COUNT = 4352;
    private final T[] empty;
    private final T[][] blockMap;
    private final IntFunction<T[]> blockConstructor;

    public CodepointMap(IntFunction<T[]> blockConstructor, IntFunction<T[][]> blockMapConstructor) {
        this.empty = blockConstructor.apply(BLOCK_SIZE);
        this.blockMap = blockMapConstructor.apply(BLOCK_COUNT);
        Arrays.fill(this.blockMap, this.empty);
        this.blockConstructor = blockConstructor;
    }

    public void clear() {
        Arrays.fill(this.blockMap, this.empty);
    }

    @Nullable
    public T get(int index) {
        int i = index >> 8;
        int j = index & IN_BLOCK_MASK;
        return this.blockMap[i][j];
    }

    @Nullable
    public T put(int index, T value) {
        int i = index >> BLOCK_BITS;
        int j = index & IN_BLOCK_MASK;
        T[] map = this.blockMap[i];
        if (map == this.empty) {
            map = this.blockConstructor.apply(BLOCK_SIZE);
            this.blockMap[i] = map;
            map[j] = value;
            return null;
        } else {
            T t = map[j];
            map[j] = value;
            return t;
        }
    }

    public T computeIfAbsent(int index, IntFunction<T> valueIfAbsentGetter) {
        int i = index >> BLOCK_BITS;
        int j = index & IN_BLOCK_MASK;
        T[] map = this.blockMap[i];
        T t = map[j];
        if (t != null) {
            return t;
        } else {
            if (map == this.empty) {
                map = this.blockConstructor.apply(BLOCK_SIZE);
                this.blockMap[i] = map;
            }

            T value = valueIfAbsentGetter.apply(index);
            map[j] = value;
            return value;
        }
    }

    @Nullable
    public T remove(int index) {
        int i = index >> BLOCK_BITS;
        int j = index & IN_BLOCK_MASK;
        T[] at = this.blockMap[i];
        if (at == this.empty) {
            return null;
        } else {
            T t = at[j];
            at[j] = null;
            return t;
        }
    }

    public void forEach(CodepointMap.Output<T> output) {
        for (int i = 0; i < this.blockMap.length; i++) {
            T[] at = this.blockMap[i];
            if (at != this.empty) {
                for (int j = 0; j < at.length; j++) {
                    T t = at[j];
                    if (t != null) {
                        int k = i << BLOCK_BITS | j;
                        output.accept(k, t);
                    }
                }
            }
        }
    }

    public IntSet keySet() {
        IntOpenHashSet intopenhashset = new IntOpenHashSet();
        this.forEach((p_285165_, p_285389_) -> intopenhashset.add(p_285165_));
        return intopenhashset;
    }

    @FunctionalInterface
    public interface Output<T> {

        void accept(int index, T object);
    }

}
