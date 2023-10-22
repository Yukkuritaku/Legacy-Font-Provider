package com.github.yukkuritaku.legacyfontprovider.ext;

import com.github.yukkuritaku.legacyfontprovider.font.GlyphFont;
import org.jetbrains.annotations.Nullable;

public interface GlyphFontExt {
    void legacyfontprovider$setGlyphFont(GlyphFont font);
    @Nullable
    GlyphFont legacyfontprovider$getGlyphFont();
}
