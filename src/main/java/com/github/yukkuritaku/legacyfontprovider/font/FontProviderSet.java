package com.github.yukkuritaku.legacyfontprovider.font;

import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.BakedGlyph;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.EmptyGlyph;
import com.github.yukkuritaku.legacyfontprovider.font.providers.GlyphProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class FontProviderSet implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();

    private static final GlyphInfo DEFAULT_ADVANCED = (GlyphInfo.SpaceGlyphInfo) () -> 4.0f;

    private final TextureManager textureManager;
    private final ResourceLocation name;
    private BakedGlyph missingGlyph;
    private BakedGlyph whiteGlyph;
    private final List<GlyphProvider> glyphProviders = Lists.newArrayList();
    private final Char2ObjectMap<BakedGlyph> glyphs = new Char2ObjectOpenHashMap<>();
    private final Char2ObjectMap<GlyphInfo> glyphInfos = new Char2ObjectOpenHashMap<>();
    private final Int2ObjectMap<CharList> glyphsByWidth = new Int2ObjectOpenHashMap<>();
    private final List<FontTexture> fontTextures = Lists.newArrayList();

    public FontProviderSet(TextureManager textureManager, ResourceLocation name) {
        this.textureManager = textureManager;
        this.name = name;
    }

    public void setGlyphProviders(List<GlyphProvider> glyphProviderList) {
        this.glyphProviders.forEach(GlyphProvider::close);
        this.glyphProviders.clear();
        this.deleteTextures();
        this.fontTextures.clear();
        this.glyphs.clear();
        this.glyphInfos.clear();
        this.glyphsByWidth.clear();
        this.missingGlyph = SpecialGlyphInfo.MISSING.bake(this::createBakedGlyph);
        this.whiteGlyph = SpecialGlyphInfo.WHITE.bake(this::createBakedGlyph);
        Set<GlyphProvider> set = Sets.newHashSet();
        for (char c = 0; c < '\uffff'; ++c) {
            for (GlyphProvider glyphProvider : glyphProviderList) {
                GlyphInfo glyph = c == ' ' ? DEFAULT_ADVANCED : glyphProvider.getGlyph(c);
                if (glyph != null) {
                    set.add(glyphProvider);
                    if (glyph != SpecialGlyphInfo.MISSING) {
                        this.glyphsByWidth
                            .computeIfAbsent(
                                MathHelper.ceiling_float_int(glyph.getAdvance(false)),
                                width -> new CharArrayList())
                            .add(c);
                    }
                    break;
                }
            }
        }
        glyphProviderList.stream()
            .filter(set::contains)
            .forEach(this.glyphProviders::add);
    }

    private GlyphInfo computeGlyphInfo(char character) {
        for (GlyphProvider provider : this.glyphProviders) {
            GlyphInfo glyphInfo = provider.getGlyph(character);
            if (glyphInfo != null) {
                return glyphInfo;
            }
        }
        return SpecialGlyphInfo.MISSING;
    }

    private BakedGlyph computeCharGlyph(char character) {
        for (GlyphProvider provider : this.glyphProviders) {
            GlyphInfo glyphInfo = provider.getGlyph(character);
            if (glyphInfo != null) {
                return glyphInfo.bake(this::createBakedGlyph);
            }
        }
        return this.missingGlyph;
    }

    public GlyphInfo findGlyph(char character) {
        return this.glyphInfos.computeIfAbsent(character, c -> c == 32 ? DEFAULT_ADVANCED : this.computeGlyphInfo(c));
    }

    public BakedGlyph getGlyph(char character) {
        return this.glyphs
            .computeIfAbsent(character, chars -> chars == 32 ? EmptyGlyph.INSTANCE : this.computeCharGlyph(chars));
    }

    private BakedGlyph createBakedGlyph(SheetGlyphInfo glyphInfo) {
        for (FontTexture fontTexture : this.fontTextures) {
            BakedGlyph bakedGlyph = fontTexture.createBakedGlyph(glyphInfo);
            if (bakedGlyph != null) {
                return bakedGlyph;
            }
        }
        FontTexture fontTexture = new FontTexture(
            new ResourceLocation(
                this.name.getResourceDomain(),
                this.name.getResourcePath() + "/" + this.fontTextures.size()),
            glyphInfo.isColored());
        this.fontTextures.add(fontTexture);
        this.textureManager.loadTexture(fontTexture.getTextureLocation(), fontTexture);
        BakedGlyph bakedGlyph = fontTexture.createBakedGlyph(glyphInfo);
        return bakedGlyph == null ? this.missingGlyph : bakedGlyph;
    }

    public BakedGlyph obfuscate(GlyphInfo glyph) {
        CharList charList = this.glyphsByWidth.get(MathHelper.ceiling_float_int(glyph.getAdvance(false)));
        return charList != null && !charList.isEmpty() ? this.getGlyph(charList.get(RANDOM.nextInt(charList.size())))
            : this.missingGlyph;
    }

    public ResourceLocation name() {
        return this.name;
    }

    public BakedGlyph whiteGlyph() {
        return this.whiteGlyph;
    }

    @Override
    public void close() {
        this.deleteTextures();
    }

    public void deleteTextures() {
        for (FontTexture fontTexture : this.fontTextures) {
            try {
                fontTexture.close();
            } catch (Exception e) {
                LOGGER.warn("Exception from font texture", e);
            }
        }
        this.fontTextures.clear();

    }
}
