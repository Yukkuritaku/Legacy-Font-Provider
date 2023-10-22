package com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.DefaultGlyph;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.GlyphInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public class DefaultGlyphProvider implements GlyphProvider{

    @Override
    public @Nullable GlyphInfo getGlyph(char character) {
        return DefaultGlyph.INSTANCE;
    }
}
