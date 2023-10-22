package com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.GlyphInfo;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
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
import java.util.Arrays;
import java.util.Map;

// Unused
@SideOnly(Side.CLIENT)
public class UnicodeTextureGlyphProvider implements GlyphProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final IResourceManager resourceManager;
    private final byte[] sizes;
    private final String template;
    private final Map<ResourceLocation, BufferedImage> unicodeMap = Maps.newHashMap();

    public UnicodeTextureGlyphProvider(IResourceManager resourceManager, byte[] sizes, String template) {
        this.resourceManager = resourceManager;
        this.sizes = sizes;
        this.template = template;
        label:
        for (int i = 0; i < 256; i++) {
            char c = (char) (i * 256);
            ResourceLocation resourceLocation = this.getTexture(c);
            try(IResource resource = this.resourceManager.getResource(resourceLocation)) {
                BufferedImage bufferedImage = TextureUtil.readBufferedImage(resource.getInputStream());
                if (bufferedImage.getWidth() == 256 && bufferedImage.getHeight() == 256){
                    int a = 0;
                    while (true){
                        if (a >= 256){
                            continue label;
                        }
                        byte size = sizes[c + a];
                        if (size != 0 && unpackWidth(size) > func_212454_b(size)) {
                            sizes[c + a] = 0;
                        }
                        ++a;
                    }
                }
            }catch (IOException ignored){}
            Arrays.fill(sizes, c, c + 256, (byte) 0);
        }
    }


    private static int unpackWidth(byte b) {
        return b >> 4 & 15;
    }

    private static int func_212454_b(byte b) {
        return (b & 15) + 1;
    }

    @Override
    public void close() {
        //this.unicodeMap.values().forEach(NativeImage::close);
    }

    @Override
    public @Nullable GlyphInfo getGlyph(char character) {
        byte size = sizes[character];
        if (size != 0){
            BufferedImage image = unicodeMap.computeIfAbsent(this.getTexture(character), this::loadTexture);
            if (image != null){
                int i = unpackWidth(size);
                return new TextureGlyphInfo(
                        character % 16 * 16 + i,
                        (character & 255) / 16 * 16,
                        func_212454_b(size) - i, 16,
                        image);
            }
        }
        return null;
    }

    private BufferedImage loadTexture(ResourceLocation location){
        try(IResource resource = this.resourceManager.getResource(location)) {
            return TextureUtil.readBufferedImage(resource.getInputStream());
        } catch (IOException e) {
            LOGGER.error("Couldn't load texture {}", location, e);
        }
        return null;
    }

    private ResourceLocation getTexture(char c) {
        ResourceLocation resourceLocation = new ResourceLocation(String.format(this.template, String.format("%02x", c / 256)));
        return new ResourceLocation(resourceLocation.getNamespace(), "textures/" + resourceLocation.getPath());
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements GlyphProviderFactory{
        private final ResourceLocation sizes;
        private final String template;

        public Factory(ResourceLocation sizes, String template) {
            this.sizes = sizes;
            this.template = template;
        }
        public static Factory deserialize(JsonObject jsonObject){
            return new Factory(new ResourceLocation(JsonUtils.getString(jsonObject, "sizes")), JsonUtils.getString(jsonObject, "template"));
        }

        @Override
        public @Nullable GlyphProvider create(IResourceManager resourceManager) {
            try(IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(this.sizes)) {
                byte[] size = new byte[65536];
                resource.getInputStream().read(size);
                return new UnicodeTextureGlyphProvider(resourceManager, size, this.template);
            }catch (IOException e){
                LOGGER.error("Cannot load {}, unicode glyphs will not render correctly", this.sizes);
                return null;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    static final class TextureGlyphInfo implements GlyphInfo{

        private final int width;
        private final int height;
        private final int unpackSkipPixels;
        private final int unpackSkipRows;
        private final BufferedImage texture;

        private TextureGlyphInfo(int width, int height, int unpackSkipPixels, int unpackSkipRows, BufferedImage texture){
            this.width = width;
            this.height = height;
            this.unpackSkipPixels = unpackSkipPixels;
            this.unpackSkipRows = unpackSkipRows;
            this.texture = texture;
        }

        @Override
        public float getAdvance() {
            return (float) this.width / 2 + 1;
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
        public void uploadGlyph(int xOffset, int yOffset) {
            //TextureUtil.uploadTextureImageSubImpl(subImage, xOffset, yOffset, false, false);
            //this.texture.uploadTextureSub(0, xOffset, yOffset, this.unpackSkipPixels, this.unpackSkipRows, this.width, this.height, false);
        }

        @Override
        public float getShadowOffset() {
            return 0.5f;
        }

        @Override
        public float getBoldOffset() {
            return 0.5f;
        }

        //TODO Color check?
        @Override
        public boolean isColored() {
            return true;
        }

        @Override
        public float getOversample() {
            return 2.0f;
        }
    }
}
