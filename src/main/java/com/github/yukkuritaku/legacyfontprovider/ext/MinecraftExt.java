package com.github.yukkuritaku.legacyfontprovider.ext;

import com.github.yukkuritaku.legacyfontprovider.font.FontManager;
import com.github.yukkuritaku.legacyfontprovider.font.FontProviderRenderer;

public interface MinecraftExt {

    FontManager getFontManager();

    FontProviderRenderer getFontProviderRenderer();

    FontProviderRenderer getFishyFontProviderRenderer();
}
