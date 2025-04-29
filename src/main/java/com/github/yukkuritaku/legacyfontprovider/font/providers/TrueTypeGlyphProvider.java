package com.github.yukkuritaku.legacyfontprovider.font.providers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.github.yukkuritaku.legacyfontprovider.font.GlyphInfo;
import com.github.yukkuritaku.legacyfontprovider.font.SheetGlyphInfo;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.BakedGlyph;
import com.github.yukkuritaku.legacyfontprovider.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;

public class TrueTypeGlyphProvider implements GlyphProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private final STBTTFontinfo fontInfo;
    private final float oversample;
    private final CharSet chars = new CharArraySet();
    private final float shiftX;
    private final float shiftY;
    private final float scale;
    private final float ascent;

    protected TrueTypeGlyphProvider(STBTTFontinfo fontInfo, float pixelWidth, float pixelHeight, float width,
        float height, String skip) {
        this.fontInfo = fontInfo;
        this.oversample = pixelHeight;
        skip.chars()
            .forEach((p_211614_1_) -> this.chars.add((char) (p_211614_1_ & '\uffff')));
        this.shiftX = width * pixelHeight;
        this.shiftY = height * pixelHeight;
        this.scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, pixelWidth * pixelHeight);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ascent = stack.mallocInt(1);
            IntBuffer descent = stack.mallocInt(1);
            IntBuffer lineGap = stack.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, ascent, descent, lineGap);
            this.ascent = (float) ascent.get(0) * this.scale;
        }
    }

    @Nullable
    @Override
    public GlyphInfo getGlyph(char character) {
        if (this.chars.contains(character)) {
            return null;
        } else {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer ix0 = stack.mallocInt(1);
                IntBuffer ix1 = stack.mallocInt(1);
                IntBuffer iy0 = stack.mallocInt(1);
                IntBuffer iy1 = stack.mallocInt(1);
                int index = STBTruetype.stbtt_FindGlyphIndex(this.fontInfo, character);
                if (index != 0) {
                    STBTruetype.stbtt_GetGlyphBitmapBoxSubpixel(
                        this.fontInfo,
                        index,
                        this.scale,
                        this.scale,
                        this.shiftX,
                        this.shiftY,
                        ix0,
                        ix1,
                        iy0,
                        iy1);
                    int w = iy0.get(0) - ix0.get(0);
                    int h = iy1.get(0) - ix1.get(0);
                    if (w != 0 && h != 0) {
                        IntBuffer advanceWidth = stack.mallocInt(1);
                        IntBuffer leftSideBearing = stack.mallocInt(1);
                        STBTruetype.stbtt_GetGlyphHMetrics(this.fontInfo, index, advanceWidth, leftSideBearing);
                        return new TrueTypeGlpyhInfo(
                            ix0.get(0),
                            iy0.get(0),
                            -ix1.get(0),
                            -iy1.get(0),
                            (float) advanceWidth.get(0) * this.scale,
                            (float) leftSideBearing.get(0) * this.scale,
                            index);
                    }

                    return null;
                }

            }

            return null;
        }
    }

    class TrueTypeGlpyhInfo implements GlyphInfo {

        private final int width;
        private final int height;
        private final float bearingX;
        private final float bearingY;
        private final float advanceWidth;
        private final int glyphIndex;

        private TrueTypeGlpyhInfo(int p_i49751_2_, int p_i49751_3_, int p_i49751_4_, int p_i49751_5_, float p_i49751_6_,
            float p_i49751_7_, int glyphIndex) {
            this.width = p_i49751_3_ - p_i49751_2_;
            this.height = p_i49751_4_ - p_i49751_5_;
            this.advanceWidth = p_i49751_6_ / TrueTypeGlyphProvider.this.oversample;
            this.bearingX = (p_i49751_7_ + (float) p_i49751_2_ + TrueTypeGlyphProvider.this.shiftX)
                / TrueTypeGlyphProvider.this.oversample;
            this.bearingY = (TrueTypeGlyphProvider.this.ascent - (float) p_i49751_4_
                + TrueTypeGlyphProvider.this.shiftY) / TrueTypeGlyphProvider.this.oversample;
            this.glyphIndex = glyphIndex;
        }

        public float getAdvance() {
            return this.advanceWidth;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> provider) {
            return provider.apply(new SheetGlyphInfo() {

                @Override
                public int getWidth() {
                    return TrueTypeGlpyhInfo.this.width;
                }

                @Override
                public int getHeight() {
                    return TrueTypeGlpyhInfo.this.height;
                }

                @Override
                public void uploadGlyph(int xOffset, int yOffset) {
                    // TODO uploadGlyph for ttf support
                }

                // TODO Color check
                @Override
                public boolean isColored() {
                    return true;
                }

                @Override
                public float getOversample() {
                    return TrueTypeGlyphProvider.this.oversample;
                }

                public float getBearingX() {
                    return TrueTypeGlpyhInfo.this.bearingX;
                }

                public float getBearingY() {
                    return TrueTypeGlpyhInfo.this.bearingY;
                }
            });
        }
    }

    public static class Factory implements GlyphProviderFactory {

        private final ResourceLocation file;
        private final float size;
        private final float oversample;
        private final float shiftX;
        private final float shiftY;
        private final String chars;

        public Factory(ResourceLocation p_i49753_1_, float p_i49753_2_, float p_i49753_3_, float p_i49753_4_,
            float p_i49753_5_, String p_i49753_6_) {
            this.file = p_i49753_1_;
            this.size = p_i49753_2_;
            this.oversample = p_i49753_3_;
            this.shiftX = p_i49753_4_;
            this.shiftY = p_i49753_5_;
            this.chars = p_i49753_6_;
        }

        public static GlyphProviderFactory deserialize(JsonObject object) {
            float shift0 = 0.0F;
            float shift1 = 0.0F;
            if (object.has("shift")) {
                JsonArray shift = object.getAsJsonArray("shift");
                if (shift.size() != 2) {
                    throw new JsonParseException("Expected 2 elements in 'shift', found " + shift.size());
                }

                shift0 = JsonUtil.getFloat(shift.get(0), "shift[0]");
                shift1 = JsonUtil.getFloat(shift.get(1), "shift[1]");
            }

            StringBuilder builder = new StringBuilder();
            if (object.has("skip")) {
                JsonElement skip = object.get("skip");
                if (skip.isJsonArray()) {
                    JsonArray skipArray = JsonUtil.getJsonArray(skip, "skip");

                    for (int i = 0; i < skipArray.size(); ++i) {
                        builder.append(JsonUtil.getString(skipArray.get(i), "skip[" + i + "]"));
                    }
                } else {
                    builder.append(JsonUtil.getString(skip, "skip"));
                }
            }

            return new Factory(
                new ResourceLocation(JsonUtil.getString(object, "file")),
                JsonUtil.getFloat(object, "size", 11.0F),
                JsonUtil.getFloat(object, "oversample", 1.0F),
                shift0,
                shift1,
                builder.toString());
        }

        @Nullable
        public GlyphProvider create(IResourceManager manager) {
            try {
                IResource resource = manager.getResource(
                    new ResourceLocation(this.file.getResourceDomain(), "font/" + this.file.getResourcePath()));
                TrueTypeGlyphProvider.LOGGER.info("Loading font");
                ByteBuffer buffer = readToNativeBuffer(resource.getInputStream());
                buffer.flip();
                STBTTFontinfo fontInfo = STBTTFontinfo.create();
                TrueTypeGlyphProvider.LOGGER.info("Reading font");
                if (!STBTruetype.stbtt_InitFont(fontInfo, buffer)) {
                    throw new IOException("Invalid ttf");
                } else {
                    return new TrueTypeGlyphProvider(
                        fontInfo,
                        this.size,
                        this.oversample,
                        this.shiftX,
                        this.shiftY,
                        this.chars);
                }
            } catch (IOException e) {
                TrueTypeGlyphProvider.LOGGER.error("Couldn't load truetype font {}", this.file, e);
                return null;
            }
        }

        public static ByteBuffer readToNativeBuffer(InputStream stream) throws IOException {
            ByteBuffer bytebuffer;
            if (stream instanceof FileInputStream file) {
                FileChannel filechannel = file.getChannel();
                bytebuffer = MemoryUtil.memAlloc((int) filechannel.size() + 1);

                while (filechannel.read(bytebuffer) != -1) {}
            } else {
                bytebuffer = MemoryUtil.memAlloc(8192);
                ReadableByteChannel readablebytechannel = Channels.newChannel(stream);
                while (readablebytechannel.read(bytebuffer) != -1) {
                    if (bytebuffer.remaining() == 0) {
                        bytebuffer = MemoryUtil.memRealloc(bytebuffer, bytebuffer.capacity() * 2);
                    }
                }
            }

            return bytebuffer;
        }
    }

}
