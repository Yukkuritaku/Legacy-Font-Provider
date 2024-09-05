package com.github.yukkuritaku.legacyfontprovider.font;

import java.awt.image.BufferedImage;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.BakedGlyph;
import com.github.yukkuritaku.legacyfontprovider.mixin.normal.TextureUtilAccessor;

public enum SpecialGlyphInfo implements GlyphInfo {

    WHITE(() -> generate(5, 8, (x, y) -> -1)),
    MISSING(() -> {
        int i = 5;
        int j = 8;
        return generate(5, 8, (x, y) -> {
            boolean flag = x == 0 || x + 1 == 5 || y == 0 || y + 1 == 8;
            return flag ? -1 : 0;
        });
    });

    final BufferedImage image;

    private static BufferedImage generate(int width, int height, SpecialGlyphInfo.PixelProvider pixelProvider) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, pixelProvider.getColor(x, y));
            }
        }
        return image;
    }

    SpecialGlyphInfo(Supplier<BufferedImage> image) {
        this.image = image.get();
    }

    @Override
    public float getAdvance() {
        return this.image.getWidth() + 1.0f;
    }

    @Override
    public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> provider) {
        return provider.apply(new SheetGlyphInfo() {

            @Override
            public int getWidth() {
                return SpecialGlyphInfo.this.image.getWidth();
            }

            @Override
            public int getHeight() {
                return SpecialGlyphInfo.this.image.getHeight();
            }

            @Override
            public void uploadGlyph(int xOffset, int yOffset) {
                TextureUtilAccessor
                    .invokeUploadTextureImageSubImpl(SpecialGlyphInfo.this.image, xOffset, yOffset, false, false);
            }

            @Override
            public boolean isColored() {
                return true;
            }

            @Override
            public float getOversample() {
                return 1.0f;
            }
        });
    }

    @FunctionalInterface
    interface PixelProvider {

        int getColor(int x, int y);
    }
}
