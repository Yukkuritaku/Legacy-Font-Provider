package com.github.yukkuritaku.legacyfontprovider.font;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.*;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers.GlyphProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class GlyphFont implements AutoCloseable {

    private static final Random RANDOM = new Random();

    private static final Glyph DEFAULT_ADVANCED = () -> 4.0f;

    private final TextureManager textureManager;
    private final ResourceLocation id;
    private BakedGlyph fallbackGlyph;
    private final List<GlyphProvider> glyphProviders = Lists.newArrayList();
    private final Char2ObjectMap<BakedGlyph> characters = new Char2ObjectOpenHashMap<>();
    private final Char2ObjectMap<Glyph> glyphs = new Char2ObjectOpenHashMap<>();
    private final Int2ObjectMap<CharList> glyphsByWidth = new Int2ObjectOpenHashMap<>();
    private final List<FontTexture> fontTextures = Lists.newArrayList();

    public GlyphFont(TextureManager textureManager, ResourceLocation id) {
        this.textureManager = textureManager;
        this.id = id;
    }

    public void setGlyphProviders(List<GlyphProvider> glyphProviderList) {
        this.glyphProviders.forEach(GlyphProvider::close);
        this.glyphProviders.clear();
        this.deleteTextures();
        this.fontTextures.clear();
        this.characters.clear();
        this.glyphs.clear();
        this.glyphsByWidth.clear();
        this.fallbackGlyph = this.createTexturedGlyph(DefaultGlyph.INSTANCE);
        Set<GlyphProvider> set = Sets.newHashSet();
        for (char c = 0; c < '\uffff'; ++c){
            for (GlyphProvider glyphProvider : glyphProviderList){
                Glyph glyph = c == ' ' ? DEFAULT_ADVANCED : glyphProvider.getGlyph(c);
                if (glyph != null){
                    set.add(glyphProvider);
                    if (glyph != DefaultGlyph.INSTANCE){
                        this.glyphsByWidth.computeIfAbsent(MathHelper.ceiling_float_int(glyph.getAdvance(false)),
                                width -> new CharArrayList()).add(c);
                    }
                    break;
                }
            }
        }
        glyphProviderList.stream().filter(set::contains).forEach(this.glyphProviders::add);

    }

    public void deleteTextures() {
        this.fontTextures.forEach(FontTexture::close);
    }

    private GlyphInfo getCharGlyph(char character) {
        for (GlyphProvider provider : this.glyphProviders) {
            GlyphInfo glyphInfo = provider.getGlyph(character);
            if (glyphInfo != null) {
                return glyphInfo;
            }
        }
        return DefaultGlyph.INSTANCE;
    }

    public Glyph findGlyph(char character){
        return this.glyphs.computeIfAbsent(character, c -> c == 32 ? DEFAULT_ADVANCED : this.getCharGlyph(c));
    }

    public BakedGlyph getGlyph(char character) {
        return this.characters.computeIfAbsent(character, chars -> chars == 32 ? EmptyGlyph.INSTANCE : this.createTexturedGlyph(this.getCharGlyph(chars)));
    }

    private BakedGlyph createTexturedGlyph(GlyphInfo glyphInfo) {
        for (FontTexture fontTexture : this.fontTextures) {
            BakedGlyph bakedGlyph = fontTexture.createBakedGlyph(glyphInfo);
            if (bakedGlyph != null) {
                return bakedGlyph;
            }
        }
        FontTexture fontTexture = new FontTexture(new ResourceLocation(this.id.getResourceDomain(), this.id.getResourcePath() + "/" + this.fontTextures.size()), glyphInfo.isColored());
        this.fontTextures.add(fontTexture);
        this.textureManager.loadTexture(fontTexture.getTextureLocation(), fontTexture);
        BakedGlyph bakedGlyph = fontTexture.createBakedGlyph(glyphInfo);
        return bakedGlyph == null ? this.fallbackGlyph : bakedGlyph;
    }

    public BakedGlyph obfuscate(Glyph glyph) {
        CharList charList = this.glyphsByWidth.get(MathHelper.ceiling_float_int(glyph.getAdvance(false)));
        return charList != null && !charList.isEmpty() ? this.getGlyph(charList.get(RANDOM.nextInt(charList.size()))) : this.fallbackGlyph;
    }

    @Override
    public void close() {
        this.deleteTextures();
    }
}
