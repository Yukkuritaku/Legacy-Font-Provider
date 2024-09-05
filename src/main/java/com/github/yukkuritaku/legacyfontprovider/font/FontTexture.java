package com.github.yukkuritaku.legacyfontprovider.font;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.BakedGlyph;

public class FontTexture extends AbstractTexture implements AutoCloseable {

    private final ResourceLocation textureLocation;
    private final boolean colored;
    private final Node node;

    public FontTexture(ResourceLocation textureLocation, boolean colored) {
        this.textureLocation = textureLocation;
        this.colored = colored;
        this.node = new Node(0, 0, 256, 256);
        TextureUtil.allocateTexture(this.getGlTextureId(), 256, 256);

    }

    private void bindTexture() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.getGlTextureId());
    }

    @Override
    public void close() {
        this.deleteGlTexture();
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) {}

    @Nullable
    public BakedGlyph createBakedGlyph(SheetGlyphInfo glyphInfo) {
        if (glyphInfo.isColored() != this.colored) {
            return null;
        } else {
            Node node = this.node.insert(glyphInfo);
            if (node != null) {
                this.bindTexture();
                glyphInfo.uploadGlyph(node.x, node.y);
                return new BakedGlyph(
                    this.textureLocation,
                    ((float) node.x + 0.01F) / 256.0F,
                    ((float) node.x - 0.01F + (float) glyphInfo.getWidth()) / 256.0F,
                    ((float) node.y + 0.01F) / 256.0F,
                    ((float) node.y - 0.01F + (float) glyphInfo.getHeight()) / 256.0F,
                    glyphInfo.getLeft(),
                    glyphInfo.getRight(),
                    glyphInfo.getTop(),
                    glyphInfo.getBottom());
            } else {
                return null;
            }
        }
    }

    public ResourceLocation getTextureLocation() {
        return this.textureLocation;
    }

    static class Node {

        final int x;
        final int y;
        private final int width;
        private final int height;
        @Nullable
        private FontTexture.Node left;
        @Nullable
        private FontTexture.Node right;
        private boolean occupied;

        Node(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Nullable
        FontTexture.Node insert(SheetGlyphInfo glyphInfo) {
            if (this.left != null && this.right != null) {
                FontTexture.Node node = this.left.insert(glyphInfo);
                if (node == null) {
                    node = this.right.insert(glyphInfo);
                }

                return node;
            } else if (this.occupied) {
                return null;
            } else {
                int i = glyphInfo.getWidth();
                int j = glyphInfo.getHeight();
                if (i > this.width || j > this.height) {
                    return null;
                } else if (i == this.width && j == this.height) {
                    this.occupied = true;
                    return this;
                } else {
                    int k = this.width - i;
                    int l = this.height - j;
                    if (k > l) {
                        this.left = new FontTexture.Node(this.x, this.y, i, this.height);
                        this.right = new FontTexture.Node(this.x + i + 1, this.y, this.width - i - 1, this.height);
                    } else {
                        this.left = new FontTexture.Node(this.x, this.y, this.width, j);
                        this.right = new FontTexture.Node(this.x, this.y + j + 1, this.width, this.height - j - 1);
                    }

                    return this.left.insert(glyphInfo);
                }
            }
        }
    }
}
