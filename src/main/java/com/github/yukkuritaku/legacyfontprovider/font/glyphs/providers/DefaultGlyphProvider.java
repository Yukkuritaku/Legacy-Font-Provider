package com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.DefaultGlyph;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.GlyphInfo;

public class DefaultGlyphProvider implements GlyphProvider{

    @Override
    public GlyphInfo getGlyph(char character) {
        return DefaultGlyph.INSTANCE;
    }
}
