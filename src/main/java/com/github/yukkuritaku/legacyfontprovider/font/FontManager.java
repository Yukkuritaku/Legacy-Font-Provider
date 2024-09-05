package com.github.yukkuritaku.legacyfontprovider.font;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.yukkuritaku.legacyfontprovider.ext.ResourceManagerExt;
import com.github.yukkuritaku.legacyfontprovider.font.providers.GlyphProvider;
import com.github.yukkuritaku.legacyfontprovider.font.providers.GlyphProviderType;
import com.github.yukkuritaku.legacyfontprovider.font.providers.MissingGlyphProvider;
import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;
import com.github.yukkuritaku.legacyfontprovider.util.JsonUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cpw.mods.fml.common.ProgressManager;

public class FontManager implements IResourceManagerReloadListener {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation DEFAULT_FONT_RENDERER_NAME = new ResourceLocation("default");
    public static final ResourceLocation GALACTIC_FONT_RENDERER_NAME = new ResourceLocation("alt");

    private final Map<ResourceLocation, FontProviderRenderer> fonts = Maps.newHashMap();
    private final TextureManager textureManager;
    private boolean forceUnicodeFont;

    public FontManager(TextureManager textureManager, boolean forceUnicodeFont) {
        this.textureManager = textureManager;
        this.forceUnicodeFont = forceUnicodeFont;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        ProgressManager.ProgressBar bar = ProgressManager.push("Legacy Font Provider", 3, true);
        Gson gson = new GsonBuilder().setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
        Map<ResourceLocation, List<GlyphProvider>> map = Maps.newHashMap();
        bar.step("Loading Default font");
        // Step 1: load default.json and alt.json in mod resource pack
        try (
            InputStream defaultJson = FontManager.class.getResourceAsStream("/assets/minecraft/font/default.json");
            InputStream altJson = FontManager.class.getResourceAsStream("/assets/minecraft/font/alt.json");) {
            // default
            this.loadFont(resourceManager, gson, map, new ResourceLocation("default"), defaultJson);
            // alt
            this.loadFont(resourceManager, gson, map, new ResourceLocation("alt"), altJson);

        } catch (IOException e) {
            LOGGER.error("IOException", e);
        }

        // Step 2: load from resource pack
        bar.step("Load font from resource pack");
        if (resourceManager instanceof ResourceManagerExt) {
            for (ResourceLocation location : ((ResourceManagerExt) resourceManager)
                .legacyfontprovider$getAllResourceLocations(
                    ResourcePackType.CLIENT_RESOURCES,
                    "font",
                    filter -> filter.endsWith(".json"))) {
                String path = location.getResourcePath();
                ResourceLocation fontId = new ResourceLocation(
                    location.getResourceDomain(),
                    path.substring("font/".length(), path.length() - ".json".length()));
                List<GlyphProvider> providers = map
                    .computeIfAbsent(fontId, r -> Lists.newArrayList(new MissingGlyphProvider()));
                try {
                    for (IResource resource : resourceManager.getAllResources(location)) {
                        try (InputStream stream = resource.getInputStream()) {
                            JsonArray jsonarray = JsonUtil.getJsonArray(
                                JsonUtil.gsonDeserialize(
                                    gson,
                                    IOUtils.toString(stream, StandardCharsets.UTF_8),
                                    JsonObject.class),
                                "providers");
                            for (int i = jsonarray.size() - 1; i >= 0; --i) {
                                JsonObject jsonobject = JsonUtil
                                    .getJsonObject(jsonarray.get(i), "providers[" + i + "]");
                                try {
                                    GlyphProviderType glyphProviderType = GlyphProviderType
                                        .byName(JsonUtil.getString(jsonobject, "type"));
                                    if (!this.forceUnicodeFont || glyphProviderType == GlyphProviderType.LEGACY_UNICODE
                                        || !fontId.equals(DEFAULT_FONT_RENDERER_NAME)) {
                                        GlyphProvider glyphProvider = glyphProviderType.getFactory(jsonobject)
                                            .create(resourceManager);
                                        if (glyphProvider != null) {
                                            providers.add(glyphProvider);
                                        }
                                    }
                                } catch (RuntimeException e) {
                                    LOGGER.warn(
                                        "Unable to read definition '{}' in fonts.json in resourcepack: '{}': {}",
                                        fontId,
                                        resource,
                                        e.getMessage());
                                }
                            }
                        } catch (RuntimeException e) {
                            LOGGER.warn(
                                "Unable to load font '{}' in fonts.json in resourcepack: '{}': {}",
                                fontId,
                                resource,
                                e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    LOGGER.warn("Unable to load font '{}' in fonts.json: {}", fontId, e.getMessage());
                }
            }
        }

        // Step 3: Apply fonts
        bar.step("Apply font.json to Glyph");
        Stream.concat(
            this.fonts.keySet()
                .stream(),
            map.keySet()
                .stream())
            .distinct()
            .forEach(location -> {
                List<GlyphProvider> glyphProviders = map.getOrDefault(location, Collections.emptyList());
                Collections.reverse(glyphProviders);
                ResourceLocation id = new ResourceLocation(
                    location.getResourceDomain(),
                    location.getResourcePath()
                        .replace(".json", ""));
                this.fonts
                    .computeIfAbsent(
                        location,
                        fontLocation -> new FontProviderRenderer(
                            this.textureManager,
                            new FontProviderSet(this.textureManager, id),
                            false))
                    .setGlyphProviders(glyphProviders);
            });
        ProgressManager.pop(bar);
    }

    private void loadFont(IResourceManager manager, Gson gson, Map<ResourceLocation, List<GlyphProvider>> map,
        ResourceLocation id, InputStream stream) throws IOException {
        List<GlyphProvider> glyphProviders = map
            .computeIfAbsent(id, loc -> Lists.newArrayList(new MissingGlyphProvider()));
        JsonObject deserialize = JsonUtil
            .gsonDeserialize(gson, IOUtils.toString(stream, StandardCharsets.UTF_8), JsonObject.class);
        if (deserialize != null) {
            JsonArray jsonArray = JsonUtil.getJsonArray(deserialize, "providers");
            for (int i = jsonArray.size() - 1; i >= 0; --i) {
                JsonObject object = JsonUtil.getJsonObject(jsonArray.get(i), "providers[" + i + "]");
                try {
                    GlyphProviderType providerType = GlyphProviderType.byName(JsonUtil.getString(object, "type"));
                    if (!this.forceUnicodeFont || providerType == GlyphProviderType.LEGACY_UNICODE
                        || !id.equals(DEFAULT_FONT_RENDERER_NAME)) {
                        GlyphProvider provider = providerType.getFactory(object)
                            .create(manager);
                        if (provider != null) {
                            glyphProviders.add(provider);
                        }
                    }
                } catch (RuntimeException e) {
                    LOGGER.warn("RuntimeException", e);
                }
            }
        }
    }

    public FontProviderRenderer getFontProvider(ResourceLocation id) {
        return this.fonts.computeIfAbsent(id, location -> {
            FontProviderRenderer fontProvider = new FontProviderRenderer(
                this.textureManager,
                new FontProviderSet(this.textureManager, location),
                false);// new FontRenderer(this.textureManager, new GlyphFont(this.textureManager, location));
            fontProvider.setGlyphProviders(Lists.newArrayList(new MissingGlyphProvider()));
            return fontProvider;
        });
    }

    public void setForceUnicodeFont(boolean forceUnicodeFont) {
        if (forceUnicodeFont != this.forceUnicodeFont) {
            this.forceUnicodeFont = forceUnicodeFont;
            this.onResourceManagerReload(
                Minecraft.getMinecraft()
                    .getResourceManager());
        }
    }
}
