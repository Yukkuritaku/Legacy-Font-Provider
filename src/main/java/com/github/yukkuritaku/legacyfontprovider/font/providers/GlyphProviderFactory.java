package com.github.yukkuritaku.legacyfontprovider.font.providers;

import net.minecraft.client.resources.IResourceManager;

public interface GlyphProviderFactory {

    GlyphProvider create(IResourceManager resourceManager);
}
