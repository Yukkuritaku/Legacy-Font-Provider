package com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers;

import net.minecraft.client.resources.IResourceManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public interface GlyphProviderFactory {

    @Nullable
    GlyphProvider create(IResourceManager resourceManager);
}
