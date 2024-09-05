package com.github.yukkuritaku.legacyfontprovider.font.providers;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.yukkuritaku.legacyfontprovider.font.GlyphInfo;
import com.github.yukkuritaku.legacyfontprovider.font.SheetGlyphInfo;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.BakedGlyph;
import com.github.yukkuritaku.legacyfontprovider.mixin.normal.TextureUtilAccessor;
import com.github.yukkuritaku.legacyfontprovider.util.JsonUtil;
import com.github.yukkuritaku.legacyfontprovider.util.TextureUtils;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;

public class BitmapGlyphProvider implements GlyphProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private final BufferedImage texture;
    private final Char2ObjectMap<GlyphInfo> glyphInfos;

    public BitmapGlyphProvider(BufferedImage texture, Char2ObjectMap<GlyphInfo> glyphInfos) {
        this.texture = texture;
        this.glyphInfos = glyphInfos;
    }

    @Override
    public void close() {
        if (this.texture instanceof AutoCloseable) {
            try {
                ((AutoCloseable) this.texture).close();
            } catch (Exception e) {

            }
        }
    }

    @Nullable
    public GlyphInfo getGlyph(char character) {
        return this.glyphInfos.get(character);
    }

    static final class BitmapGlyphInfo implements GlyphInfo {

        private final float oversample;
        private final BufferedImage texture;
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final int advance;
        private final int ascent;

        private BitmapGlyphInfo(float oversample, BufferedImage texture, int x, int y, int width, int height,
            int advance, int ascent) {
            this.oversample = oversample;
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.advance = advance;
            this.ascent = ascent;
        }

        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> provider) {
            return provider.apply(new SheetGlyphInfo() {

                @Override
                public int getWidth() {
                    return BitmapGlyphInfo.this.width;
                }

                @Override
                public int getHeight() {
                    return BitmapGlyphInfo.this.height;
                }

                @Override
                public void uploadGlyph(int xOffset, int yOffset) {
                    TextureUtilAccessor.invokeUploadTextureImageSubImpl(
                        BitmapGlyphInfo.this.texture.getSubimage(
                            BitmapGlyphInfo.this.x,
                            BitmapGlyphInfo.this.y,
                            BitmapGlyphInfo.this.width,
                            BitmapGlyphInfo.this.height),
                        xOffset,
                        yOffset,
                        false,
                        false);
                }

                // TODO Color check
                @Override
                public boolean isColored() {
                    return true;
                }

                @Override
                public float getOversample() {
                    return BitmapGlyphInfo.this.oversample;
                }

                @Override
                public float getBearingY() {
                    return BitmapGlyphInfo.this.ascent;
                }
            });
        }
    }

    public static class Factory implements GlyphProviderFactory {

        private final ResourceLocation file;
        private final List<String> chars;
        private final int height;
        private final int ascent;

        public Factory(ResourceLocation textureLocation, int height, int ascent, List<String> chars) {
            this.file = new ResourceLocation(
                textureLocation.getResourceDomain(),
                "textures/" + textureLocation.getResourcePath());
            this.chars = chars;
            this.height = height;
            this.ascent = ascent;
        }

        public static Factory deserialize(JsonObject jsonObject) {
            int height = JsonUtil.getInt(jsonObject, "height", 8);
            int ascent = JsonUtil.getInt(jsonObject, "ascent");
            if (ascent > height) {
                throw new JsonParseException("Ascent " + ascent + " higher than height " + height);
            } else {
                List<String> list = Lists.newArrayList();
                JsonArray array = JsonUtil.getJsonArray(jsonObject, "chars");
                for (int size = 0; size < array.size(); size++) {
                    String chars = JsonUtil.getString(array.get(size), "chars[" + size + "]");
                    if (size > 0) {
                        int length = chars.length();
                        int arrayLength = list.get(0)
                            .length();
                        if (length != arrayLength) {
                            throw new JsonParseException(
                                "Elements of chars have to be the same length (found: " + length
                                    + ", expected: "
                                    + arrayLength
                                    + "), pad with space or \\u0000");
                        }
                    }
                    list.add(chars);
                }
                if (!list.isEmpty() && !list.get(0)
                    .isEmpty()) {
                    return new Factory(
                        new ResourceLocation(JsonUtil.getString(jsonObject, "file")),
                        height,
                        ascent,
                        list);
                } else {
                    throw new JsonParseException("Expected to find data in chars, found none.");
                }
            }
        }

        @Override
        public GlyphProvider create(IResourceManager resourceManager) {
            try {
                IResource resource = resourceManager.getResource(this.file);
                BufferedImage bufferedImage = TextureUtils.readBufferedImage(resource.getInputStream());
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();
                int charWidth = width / this.chars.get(0)
                    .length();
                int charHeight = height / this.chars.size();
                float scale = (float) this.height / charHeight;
                Char2ObjectMap<GlyphInfo> map = new Char2ObjectOpenHashMap<>();
                for (int y = 0; y < this.chars.size(); ++y) {
                    String character = this.chars.get(y);
                    for (int length = 0; length < character.length(); ++length) {
                        char charAt = character.charAt(length);
                        if (charAt != 0 && charAt != ' ') {
                            int x = this.getCharacterWidth(bufferedImage, charWidth, charHeight, length, y);
                            map.put(
                                charAt,
                                new BitmapGlyphInfo(
                                    scale,
                                    bufferedImage,
                                    length * charWidth,
                                    y * charHeight,
                                    charWidth,
                                    charHeight,
                                    (int) ((0.5 + (x * scale)) + 1),
                                    this.ascent));
                        }
                    }
                }
                return new BitmapGlyphProvider(bufferedImage, map);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        private int getCharacterWidth(BufferedImage bufferedImage, int charWidth, int charHeight, int column, int row) {
            int width;
            for (width = charWidth - 1; width >= 0; --width) {
                int x = column * charWidth + width;
                for (int height = 0; height < charHeight; ++height) {
                    int y = row * charHeight + height;
                    if (isLuminanceOrAlpha(bufferedImage, x, y)) {
                        return width + 1;
                    }
                }
            }
            return width + 1;
        }

        private boolean isLuminanceOrAlpha(BufferedImage image, int x, int y) {
            int color = image.getRGB(x, y);
            int alpha = color & 0xFF000000;
            // extract each color component
            int red = (color >>> 16) & 0xFF;
            int green = (color >>> 8) & 0xFF;
            int blue = (color) & 0xFF;

            // calc luminance in range 0.0 to 1.0; using SRGB luminance constants
            float luminance = (red * 0.2126f + green * 0.7152f + blue * 0.0722f) / 255;

            return luminance >= 1f || alpha == 0xFF000000;
        }
    }
}
