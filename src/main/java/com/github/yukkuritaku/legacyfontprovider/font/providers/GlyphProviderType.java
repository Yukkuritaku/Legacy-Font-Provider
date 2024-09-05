package com.github.yukkuritaku.legacyfontprovider.font.providers;

import java.util.Map;
import java.util.function.Function;

import com.github.yukkuritaku.legacyfontprovider.util.Util;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

public enum GlyphProviderType {

    BITMAP("bitmap", BitmapGlyphProvider.Factory::deserialize),
    LEGACY_UNICODE("legacy_unicode", UnicodeBitmapGlyphProvider.Factory::deserialize);

    private static final Map<String, GlyphProviderType> TYPES_BY_NAME = Util.make(Maps.newHashMap(), map -> {
        GlyphProviderType[] types = values();
        for (GlyphProviderType type : types) {
            map.put(type.name, type);
        }
    });
    private final String name;
    private final Function<JsonObject, GlyphProviderFactory> factoryDeserializer;

    GlyphProviderType(String name, Function<JsonObject, GlyphProviderFactory> deserializer) {
        this.name = name;
        this.factoryDeserializer = deserializer;
    }

    public static GlyphProviderType byName(String name) {
        GlyphProviderType type = TYPES_BY_NAME.get(name);
        if (type == null) {
            throw new IllegalArgumentException("Invalid type: " + name);
        } else {
            return type;
        }
    }

    public GlyphProviderFactory getFactory(JsonObject obj) {
        return this.factoryDeserializer.apply(obj);
    }
}
