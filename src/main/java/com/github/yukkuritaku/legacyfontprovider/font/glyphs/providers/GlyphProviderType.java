package com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers;

import com.github.yukkuritaku.legacyfontprovider.util.Util;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public enum GlyphProviderType {
    BITMAP("bitmap", TextureGlyphProvider.Factory::deserialize),
    //LEGACY_UNICODE("legacy_unicode", UnicodeTextureGlyphProvider.Factory::deserialize)
    ;


    private static final Map<String, GlyphProviderType> TYPES_BY_NAME = Util.make(Maps.newHashMap(), (p_211639_0_) -> {
        for(GlyphProviderType glyphProviderType : values()) {
            p_211639_0_.put(glyphProviderType.name, glyphProviderType);
        }
    });

    private final String name;
    private final Function<JsonObject, GlyphProviderFactory> factoryDeserializer;

    GlyphProviderType(String name, Function<JsonObject, GlyphProviderFactory> factoryDeserializer) {
        this.name = name;
        this.factoryDeserializer = factoryDeserializer;
    }


    public static GlyphProviderType byName(String typeIn) {
        GlyphProviderType glyphProviderType = TYPES_BY_NAME.get(typeIn);
        if (glyphProviderType == null) {
            throw new IllegalArgumentException("Invalid type: " + typeIn);
        } else {
            return glyphProviderType;
        }
    }

    public GlyphProviderFactory getFactory(JsonObject jsonIn) {
        return this.factoryDeserializer.apply(jsonIn);
    }
}
