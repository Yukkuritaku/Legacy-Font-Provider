package com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.GlyphInfo;
import com.github.yukkuritaku.legacyfontprovider.mixin.TextureUtilAccessor;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
public class TextureGlyphProvider implements GlyphProvider {

    private final Char2ObjectMap<TextureGlyphInfo> glyphInfos;

    private static final Logger LOGGER = LogManager.getLogger();

    public TextureGlyphProvider(Char2ObjectMap<TextureGlyphInfo> glyphInfos) {
        this.glyphInfos = glyphInfos;
    }

    @Override
    public void close() {
        GlyphProvider.super.close();
    }

    @Override
    public @Nullable GlyphInfo getGlyph(char character) {
        return this.glyphInfos.get(character);
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements GlyphProviderFactory {

        private final ResourceLocation file;
        private final List<String> chars;
        private final int height;
        private final int ascent;

        public Factory(ResourceLocation textureLocation, int height, int ascent, List<String> chars) {
            this.file = new ResourceLocation(textureLocation.getResourceDomain(), "textures/" + textureLocation.getResourcePath());
            this.chars = chars;
            this.height = height;
            this.ascent = ascent;
        }

        public static Factory deserialize(JsonObject jsonObject) {
            int height = JsonUtils.getInt(jsonObject, "height", 8);
            int ascent = JsonUtils.getInt(jsonObject, "ascent");
            if (ascent > height) {
                throw new JsonParseException("Ascent " + ascent + " higher than height " + height);
            } else {
                List<String> list = Lists.newArrayList();
                JsonArray array = JsonUtils.getJsonArray(jsonObject, "chars");
                for (int size = 0; size < array.size(); size++) {
                    String chars = JsonUtils.getString(array.get(size), "chars[" + size + "]");
                    if (size > 0) {
                        int length = chars.length();
                        int arrayLength = list.get(0).length();
                        if (length != arrayLength) {
                            throw new JsonParseException("Elements of chars have to be the same length (found: " + length + ", expected: " + arrayLength + "), pad with space or \\u0000");
                        }
                    }
                    list.add(chars);
                }
                if (!list.isEmpty() && !list.get(0).isEmpty()) {
                    return new Factory(new ResourceLocation(JsonUtils.getString(jsonObject, "file")), height, ascent, list);
                } else {
                    throw new JsonParseException("Expected to find data in chars, found none.");
                }
            }
        }

        @Override
        public @Nullable GlyphProvider create(IResourceManager resourceManager) {
            try {
                IResource resource = resourceManager.getResource(this.file);
                BufferedImage bufferedImage = TextureUtil.readBufferedImage(resource.getInputStream());
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();
                int charWidth = width / this.chars.get(0).length();
                int charHeight = height / this.chars.size();
                float scale = (float) this.height / charHeight;
                Char2ObjectMap<TextureGlyphInfo> map = new Char2ObjectOpenHashMap<>();
                for (int chars = 0; chars < this.chars.size(); ++chars) {
                    String s = this.chars.get(chars);
                    for (int length = 0; length < s.length(); ++length) {
                        char charAt = s.charAt(length);
                        if (charAt != 0 && charAt != ' ') {
                            int x = this.getCharacterWidth(bufferedImage, charWidth, charHeight, length, chars);
                            map.put(charAt, new TextureGlyphInfo(scale, bufferedImage,
                                    length * charWidth,
                                    chars * charHeight,
                                    charWidth,
                                    charHeight,
                                    (int) ((0.5 + (x * scale)) + 1),
                                    this.ascent
                            ));
                        }
                    }
                }
                return new TextureGlyphProvider(map);
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

    @SideOnly(Side.CLIENT)
    static final class TextureGlyphInfo implements GlyphInfo {

        private final float scale;
        private final BufferedImage texture;
        private final int unpackSkipPixels;
        private final int unpackSkipRows;
        private final int width;
        private final int height;
        private final int advanceWidth;
        private final int ascent;

        private TextureGlyphInfo(float scale, BufferedImage texture, int unpackSkipPixels, int unpackSkipRows, int width, int height, int advanceWidth, int ascent) {
            this.scale = scale;
            this.texture = texture;
            this.unpackSkipPixels = unpackSkipPixels;
            this.unpackSkipRows = unpackSkipRows;
            this.width = width;
            this.height = height;
            this.advanceWidth = advanceWidth;
            this.ascent = ascent;
        }

        @Override
        public float getAdvance() {
            return (float) this.advanceWidth;
        }

        @Override
        public int getWidth() {
            return this.width;
        }

        @Override
        public int getHeight() {
            return this.height;
        }

        @Override
        public float getBearingY() {
            return GlyphInfo.super.getBearingY() + 7.0f - (float) this.ascent;
        }

        @Override
        public void uploadGlyph(int xOffset, int yOffset) {
            TextureUtilAccessor.invokeUploadTextureImageSubImpl(texture.getSubimage(this.unpackSkipPixels, this.unpackSkipRows,
                    this.width, this.height),
                    xOffset, yOffset, false, false);
        }

        //TODO Color check?
        @Override
        public boolean isColored() {
            //return this.texture.getFormat().getPixelSize() > 1;
            return true;
        }

        @Override
        public float getOversample() {
            return 1.0f / this.scale;
        }
    }

}
