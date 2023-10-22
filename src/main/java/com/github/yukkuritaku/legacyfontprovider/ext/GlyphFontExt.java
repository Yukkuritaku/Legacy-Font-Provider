package com.github.yukkuritaku.legacyfontprovider.ext;

import com.github.yukkuritaku.legacyfontprovider.font.GlyphFont;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface GlyphFontExt {
    void legacyfontprovider$setGlyphFont(GlyphFont font);
}
