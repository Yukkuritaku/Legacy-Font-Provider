package com.github.yukkuritaku.legacyfontprovider.util;

import net.minecraft.client.renderer.GlStateManager;

public class TextureUtil {

    public static void allocateTexture(PixelFormatGLCode pixelFormat, int glTextureId, int width, int height) {
        allocateTextureImpl(pixelFormat, glTextureId, 0, width, height);
    }

    public static void allocateTextureImpl(PixelFormatGLCode internalFormat, int glTextureId, int mipmapLevels, int width, int height) {
        synchronized (net.minecraftforge.fml.client.SplashProgress.class)
        {
            GlStateManager.deleteTexture(glTextureId);
            GlStateManager.bindTexture(glTextureId);
        }
        if (mipmapLevels >= 0) {
            GlStateManager.glTexParameteri(3553, 33085, mipmapLevels);
            GlStateManager.glTexParameteri(3553, 33082, 0);
            GlStateManager.glTexParameteri(3553, 33083, mipmapLevels);
            GlStateManager.glTexParameterf(3553, 34049, 0.0f);
        }

        for(int i = 0; i <= mipmapLevels; ++i) {
            GlStateManager.glTexImage2D(3553, i, internalFormat.getGlFormat(), width >> i, height >> i, 0, 6408, 5121, null);
        }

    }
    public enum PixelFormatGLCode {
        RGBA(6408),
        RGB(6407),
        LUMINANCE_ALPHA(6410),
        LUMINANCE(6409),
        INTENSITY(32841);

        private final int glConstant;

        private PixelFormatGLCode(int glFormatIn) {
            this.glConstant = glFormatIn;
        }

        int getGlFormat() {
            return this.glConstant;
        }
    }
}
