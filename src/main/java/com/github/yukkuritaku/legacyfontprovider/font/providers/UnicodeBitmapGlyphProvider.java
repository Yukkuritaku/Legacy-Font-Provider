package com.github.yukkuritaku.legacyfontprovider.font.providers;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
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
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

public class UnicodeBitmapGlyphProvider implements GlyphProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private final IResourceManager resourceManager;
    private final byte[] sizes;
    private final String template;
    private final Map<ResourceLocation, BufferedImage> unicodeMap = Maps.newHashMap();

    public UnicodeBitmapGlyphProvider(IResourceManager resourceManager, byte[] sizes, String template) {
        this.resourceManager = resourceManager;
        this.sizes = sizes;
        this.template = template;
        label: for (int i = 0; i < 256; i++) {
            char c = (char) (i * 256);
            ResourceLocation resourceLocation = this.getTextureFor(c);
            try {
                IResource resource = this.resourceManager.getResource(resourceLocation);
                BufferedImage bufferedImage = TextureUtils.readBufferedImage(resource.getInputStream());
                if (bufferedImage.getWidth() == 256 && bufferedImage.getHeight() == 256) {
                    int a = 0;
                    while (true) {
                        if (a >= 256) {
                            continue label;
                        }
                        byte size = sizes[c + a];
                        if (size != 0 && rightShift(size) > and(size)) {
                            sizes[c + a] = 0;
                        }
                        ++a;
                    }
                }
            } catch (IOException ignored) {}
            Arrays.fill(sizes, c, c + 256, (byte) 0);
        }
    }

    private static int rightShift(byte b) {
        return b >> 4 & 15;
    }

    private static int and(byte b) {
        return (b & 15) + 1;
    }

    private ResourceLocation getTextureFor(char character) {
        ResourceLocation location = new ResourceLocation(
            String.format(this.template, String.format("%02x", character / 256)));
        return new ResourceLocation(location.getResourceDomain(), "textures/" + location.getResourcePath());
    }

    private BufferedImage loadTexture(ResourceLocation location) {
        try {
            IResource resource = this.resourceManager.getResource(location);
            return TextureUtils.readBufferedImage(resource.getInputStream());
        } catch (IOException e) {
            LOGGER.error("Couldn't load texture {}", location, e);
        }
        return null;
    }

    @Override
    public void close() {
        this.unicodeMap.values()
            .forEach(textures -> {
                try {
                    if (textures instanceof AutoCloseable) {
                        ((AutoCloseable) textures).close();
                    }
                } catch (Exception e) {

                }
            });
    }

    @Nullable
    @Override
    public GlyphInfo getGlyph(char character) {
        byte size = this.sizes[character];
        if (size != 0) {
            BufferedImage image = this.unicodeMap.computeIfAbsent(this.getTextureFor(character), this::loadTexture);
            if (image != null) {
                int i = rightShift(size);
                return new UnicodeBitmapGlyphInfo(
                    character % 16 * 16 + i,
                    (character & 255) / 16 * 16,
                    and(size) - i,
                    16,
                    image);
            }
        }
        return null;
    }

    static class UnicodeBitmapGlyphInfo implements GlyphInfo {

        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final BufferedImage texture;

        private UnicodeBitmapGlyphInfo(int x, int y, int width, int height, BufferedImage texture) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.texture = texture;
        }

        @Override
        public float getAdvance() {
            return (float) this.width / 2.0f + 1.0f;
        }

        @Override
        public float getBoldOffset() {
            return 0.5f;
        }

        @Override
        public float getShadowOffset() {
            return 0.5f;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> provider) {
            return provider.apply(new SheetGlyphInfo() {

                @Override
                public int getWidth() {
                    return UnicodeBitmapGlyphInfo.this.width;
                }

                @Override
                public int getHeight() {
                    return UnicodeBitmapGlyphInfo.this.height;
                }

                @Override
                public void uploadGlyph(int xOffset, int yOffset) {
                    TextureUtilAccessor.invokeUploadTextureImageSubImpl(
                        UnicodeBitmapGlyphInfo.this.texture.getSubimage(
                            UnicodeBitmapGlyphInfo.this.x,
                            UnicodeBitmapGlyphInfo.this.y,
                            UnicodeBitmapGlyphInfo.this.width,
                            UnicodeBitmapGlyphInfo.this.height),
                        xOffset,
                        yOffset,
                        false,
                        false);
                }

                // TODO Color Check
                @Override
                public boolean isColored() {
                    return true;
                }

                @Override
                public float getOversample() {
                    return 2.0f;
                }
            });
        }
    }

    public static class Factory implements GlyphProviderFactory {

        private final ResourceLocation sizes;
        private final String template;

        public Factory(ResourceLocation sizes, String template) {
            this.sizes = sizes;
            this.template = template;
        }

        public static Factory deserialize(JsonObject jsonObject) {
            return new Factory(
                new ResourceLocation(JsonUtil.getString(jsonObject, "sizes")),
                JsonUtil.getString(jsonObject, "template"));
        }

        @Override
        public GlyphProvider create(IResourceManager resourceManager) {
            try {
                IResource resource = Minecraft.getMinecraft()
                    .getResourceManager()
                    .getResource(this.sizes);
                byte[] size = new byte[65536];
                resource.getInputStream()
                    .read(size);
                return new UnicodeBitmapGlyphProvider(resourceManager, size, this.template);
            } catch (IOException e) {
                LOGGER.error("Cannot load {}, unicode glyphs will not render correctly", this.sizes);
                return null;
            }
        }
    }
}
