package com.github.yukkuritaku.legacyfontprovider.font.providers;

import javax.annotation.Nullable;

import com.github.yukkuritaku.legacyfontprovider.font.GlyphInfo;
import com.github.yukkuritaku.legacyfontprovider.font.SpecialGlyphInfo;

public class MissingGlyphProvider implements GlyphProvider {

    @Nullable
    @Override
    public GlyphInfo getGlyph(char character) {
        return SpecialGlyphInfo.MISSING;
    }
}
