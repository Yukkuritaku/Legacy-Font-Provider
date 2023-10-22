package com.github.yukkuritaku.legacyfontprovider.font.glyphs;

import com.github.yukkuritaku.legacyfontprovider.mixin.minecraft.TextureUtilAccessor;
import com.github.yukkuritaku.legacyfontprovider.util.Util;
import net.minecraft.client.renderer.texture.TextureUtil;

import java.awt.image.BufferedImage;

public enum DefaultGlyph implements GlyphInfo {
    INSTANCE;

    private static final BufferedImage DEFAULT_IMAGE = Util.make(new BufferedImage(5, 8, BufferedImage.TYPE_INT_ARGB), nativeImage -> {
        for (int y = 0; y < 8; y++){
            for (int x = 0; x < 5; x++){
                boolean flag = x == 0 || x + 1 == 5 || y == 0 || y + 1 == 8;
                nativeImage.setRGB(x, y, flag ? -1 : 0);
            }
        }
    });


    @Override
    public float getAdvance() {
        return 6.0f;
    }

    @Override
    public int getWidth() {
        return 5;
    }

    @Override
    public int getHeight() {
        return 8;
    }

    @Override
    public void uploadGlyph(int xOffset, int yOffset) {
        TextureUtilAccessor.invokeUploadTextureImageSubImpl(DEFAULT_IMAGE, xOffset, yOffset, false, false);
    }

    @Override
    public boolean isColored() {
        return true;
    }

    @Override
    public float getOversample() {
        return 1.0f;
    }
}
