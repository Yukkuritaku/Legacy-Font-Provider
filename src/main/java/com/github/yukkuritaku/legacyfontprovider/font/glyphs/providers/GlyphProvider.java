package com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.GlyphInfo;

public interface GlyphProvider extends AutoCloseable{

    default void close(){

    }
    default GlyphInfo getGlyph(char character){
        return null;
    }
}
