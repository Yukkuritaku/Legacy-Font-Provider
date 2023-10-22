package com.github.yukkuritaku.legacyfontprovider.font;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.BakedGlyph;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.GlyphInfo;
import com.github.yukkuritaku.legacyfontprovider.util.TextureUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)
public class FontTexture extends AbstractTexture implements AutoCloseable {

    private final ResourceLocation textureLocation;
    private final boolean colored;
    private final Entry entry;

    public FontTexture(ResourceLocation textureLocation, boolean colored) {
        this.textureLocation = textureLocation;
        LogManager.getLogger().info("FontTexture: {}", textureLocation.toString());
        this.colored = colored;
        this.entry = new Entry(0, 0, 256, 256);
        TextureUtil.allocateTexture(colored ? TextureUtil.PixelFormatGLCode.RGBA : TextureUtil.PixelFormatGLCode.INTENSITY,
                this.getGlTextureId(),
                256, 256);
        //TextureUtils.allocateTexture(this.colored ? TextureUtils.PixelFormat.RGBA : TextureUtils.PixelFormat.INTENSITY, this.getGlTextureId(), 256, 256);
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) {
    }

    @Override
    public void close() {
        this.deleteGlTexture();
    }

    public BakedGlyph createBakedGlyph(GlyphInfo glyphInfo) {
        if (glyphInfo.isColored() != this.colored) {
            return null;
        } else {
            Entry entry = this.entry.add(glyphInfo);
            if (entry != null) {
                GlStateManager.bindTexture(this.getGlTextureId());
                glyphInfo.uploadGlyph(entry.xOffset, entry.yOffset);
                return new BakedGlyph(this.textureLocation,
                        (entry.xOffset + 0.01f) / 256.0f,
                        (entry.xOffset - 0.01f + glyphInfo.getWidth()) / 256.0f,
                        (entry.yOffset + 0.01f) / 256.0f,
                        (entry.yOffset - 0.01f + glyphInfo.getHeight()) / 256.0f,
                        glyphInfo.getXStart(),
                        glyphInfo.getXEnd(),
                        glyphInfo.getYStart(),
                        glyphInfo.getYEnd());
            } else {
                return null;
            }
        }

    }

    static class Entry {
        final int xOffset;
        final int yOffset;
        final int width;
        final int height;
        Entry left;
        Entry right;
        private boolean occupied;

        private Entry(int xOffset, int yOffset, int width, int height) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.width = width;
            this.height = height;
        }

        @Nullable
        Entry add(GlyphInfo glyphInfo) {
            if (this.left != null && this.right != null) {
                Entry entry = this.left.add(glyphInfo);
                if (entry == null) {
                    entry = this.right.add(glyphInfo);
                }
                return entry;
            } else if (this.occupied) {
                return null;
            }
            int width = glyphInfo.getWidth();
            int height = glyphInfo.getHeight();
            if (width > this.width || height > this.height) {
                return null;
            } else if (width == this.width || height == this.height) {
                this.occupied = true;
                return this;
            } else {
                int renderWidth = this.width - width;
                int renderHeight = this.height - height;
                if (renderWidth > renderHeight) {
                    this.left = new Entry(this.xOffset, this.yOffset, width, this.height);
                    this.right = new Entry(this.xOffset + width + 1, this.yOffset, this.width - width - 1, this.height);
                } else {
                    this.left = new Entry(this.xOffset, this.yOffset, this.width, height);
                    this.right = new Entry(this.xOffset, this.yOffset + height + 1, this.width, this.height - height - 1);
                }
                return this.left.add(glyphInfo);
            }
        }
    }
}
