package com.github.yukkuritaku.legacyfontprovider.font.providers;

import javax.annotation.Nullable;

import com.github.yukkuritaku.legacyfontprovider.font.GlyphInfo;

public interface GlyphProvider extends AutoCloseable {

    float BASELINE = 7.0F;

    @Override
    default void close() {}

    @Nullable
    default GlyphInfo getGlyph(char character) {
        return null;
    }
}
