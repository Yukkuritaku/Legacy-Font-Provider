package com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.GlyphInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public interface GlyphProvider extends AutoCloseable{

    default void close(){

    }
    @Nullable
    default GlyphInfo getGlyph(char character){
        return null;
    }
}
