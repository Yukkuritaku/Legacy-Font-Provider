package com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers;

import net.minecraft.client.resources.IResourceManager;

public interface GlyphProviderFactory {

    GlyphProvider create(IResourceManager resourceManager);
}
