package com.github.yukkuritaku.legacyfontprovider.util;


import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class TextureUtils {

    public static void allocateTexture(PixelFormatGLCode pixelFormat, int glTextureId, int width, int height) {
        allocateTextureImpl(pixelFormat, glTextureId, 0, width, height);
    }

    public static void allocateTextureImpl(PixelFormatGLCode internalFormat, int glTextureId, int mipmapLevels, int width, int height) {
        synchronized (cpw.mods.fml.client.SplashProgress.class)
        {
            GL11.glDeleteTextures(glTextureId);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTextureId);
        }
        if (mipmapLevels >= 0) {
            GL11.glTexParameteri(3553, 33085, mipmapLevels);
            GL11.glTexParameteri(3553, 33082, 0);
            GL11.glTexParameteri(3553, 33083, mipmapLevels);
            GL11.glTexParameterf(3553, 34049, 0.0f);
        }

        for(int i = 0; i <= mipmapLevels; ++i) {
            GL11.glTexImage2D(3553, i, internalFormat.getGlFormat(), width >> i, height >> i, 0, 6408, 5121, (ByteBuffer) null);
        }
    }


    public static BufferedImage readBufferedImage(InputStream imageStream) throws IOException {
        BufferedImage bufferedimage;
        try {
            bufferedimage = ImageIO.read(imageStream);
        } finally {
            IOUtils.closeQuietly(imageStream);
        }

        return bufferedimage;
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
