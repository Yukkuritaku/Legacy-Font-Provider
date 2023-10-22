package com.github.yukkuritaku.legacyfontprovider.font;

import com.github.yukkuritaku.legacyfontprovider.ext.IReloadableResourceManagerExt;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers.DefaultGlyphProvider;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers.GlyphProvider;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers.GlyphProviderType;
import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;
import com.github.yukkuritaku.legacyfontprovider.util.JsonUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import cpw.mods.fml.common.ProgressManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class FontManager implements IResourceManagerReloadListener{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation DEFAULT_FONT_RENDERER_NAME = new ResourceLocation("default");
    public static final ResourceLocation GALACTIC_FONT_RENDERER_NAME = new ResourceLocation("alt");

    private final Map<ResourceLocation, GlyphFont> fonts = Maps.newHashMap();
    private final TextureManager textureManager;
    private boolean forceUnicodeFont;

    public FontManager(TextureManager textureManager, boolean forceUnicodeFont) {
        this.textureManager = textureManager;
        this.forceUnicodeFont = forceUnicodeFont;
    }

    protected static String getRelativeName(File file, File file2) {
        return file.toURI().relativize(file2.toURI()).getPath();
    }

    public static String lastStr(String regex, String inputStr) {
        String[] split = inputStr.split(regex);
        return split[split.length - 1];
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        ProgressManager.ProgressBar bar = ProgressManager.push("Legacy Font Provider", 3, true);
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        Map<ResourceLocation, List<GlyphProvider>> map = Maps.newHashMap();
        bar.step("Loading Default font");
        // Round 1: load default.json and alt.json in mod resource pack
        try(InputStream defaultJson = FontManager.class.getResourceAsStream("/assets/minecraft/font/default.json");
            InputStream altJson = FontManager.class.getResourceAsStream("/assets/minecraft/font/alt.json");
        ) {
            ResourceLocation idLocation = new ResourceLocation("default");
            List<GlyphProvider> glyphProviders = map.computeIfAbsent(idLocation,
                    loc -> Lists.newArrayList(new DefaultGlyphProvider()));
            JsonObject deserialize = JsonUtil.gsonDeserialize(gson, IOUtils.toString(defaultJson, StandardCharsets.UTF_8), JsonObject.class);
            if (deserialize != null) {
                JsonArray jsonArray = JsonUtil.getJsonArray(deserialize, "providers");
                for (int i = jsonArray.size() - 1; i >= 0; --i) {
                    JsonObject object = JsonUtil.getJsonObject(jsonArray.get(i), "providers[" + i + "]");
                    try {
                        GlyphProviderType providerType = GlyphProviderType.byName(JsonUtil.getString(object, "type"));
                        if (!this.forceUnicodeFont ||
                                //providerType == GlyphProviderType.LEGACY_UNICODE ||
                                !idLocation.equals(DEFAULT_FONT_RENDERER_NAME)) {
                            GlyphProvider provider = providerType.getFactory(object).create(resourceManager);
                            if (provider != null) {
                                glyphProviders.add(provider);
                            }
                        }
                    } catch (RuntimeException e) {
                        LOGGER.warn("RuntimeException", e);
                    }
                }
            }
            ResourceLocation alt = new ResourceLocation("alt");
            List<GlyphProvider> providers = map.computeIfAbsent(alt,
                    loc -> Lists.newArrayList(new DefaultGlyphProvider()));
            JsonObject obj = JsonUtil.gsonDeserialize(gson, IOUtils.toString(altJson, StandardCharsets.UTF_8), JsonObject.class);
            if (obj != null) {
                JsonArray jsonArray = JsonUtil.getJsonArray(obj, "providers");
                for (int i = jsonArray.size() - 1; i >= 0; --i) {
                    JsonObject object = JsonUtil.getJsonObject(jsonArray.get(i), "providers[" + i + "]");
                    try {
                        GlyphProviderType providerType = GlyphProviderType.byName(JsonUtil.getString(object, "type"));
                        if (!this.forceUnicodeFont ||
                                //providerType == GlyphProviderType.LEGACY_UNICODE ||
                                !alt.equals(DEFAULT_FONT_RENDERER_NAME)) {
                            GlyphProvider provider = providerType.getFactory(object).create(resourceManager);
                            if (provider != null) {
                                providers.add(provider);
                            }
                        }
                    } catch (RuntimeException e) {
                        LOGGER.warn("RuntimeException", e);
                    }
                }
            }
        }catch (IOException e){
            LOGGER.error("IOException", e);
        }

        bar.step("Load font from resource pack");
        if (resourceManager instanceof IReloadableResourceManagerExt) {
            for (ResourceLocation location : ((IReloadableResourceManagerExt) resourceManager)
                    .legacyfontprovider$getAllResourceLocations(ResourcePackType.CLIENT_RESOURCES, "font", filter -> filter.endsWith(".json"))){
                String path = location.getResourcePath();
                ResourceLocation fontId = new ResourceLocation(location.getResourceDomain(), path.substring("font/".length(), path.length() - ".json".length()));
                List<GlyphProvider> providers = map.computeIfAbsent(fontId, r -> Lists.newArrayList(new DefaultGlyphProvider()));
                try {
                    for (Object obj : resourceManager.getAllResources(location)){
                        IResource resource = (IResource) obj;
                        try(InputStream stream = resource.getInputStream()) {
                            JsonArray jsonarray = JsonUtil.getJsonArray(JsonUtil.gsonDeserialize(gson, IOUtils.toString(stream, StandardCharsets.UTF_8), JsonObject.class), "providers");
                            for(int i = jsonarray.size() - 1; i >= 0; --i) {
                                JsonObject jsonobject = JsonUtil.getJsonObject(jsonarray.get(i), "providers[" + i + "]");
                                try {
                                    GlyphProviderType glyphProviderType = GlyphProviderType.byName(JsonUtil.getString(jsonobject, "type"));
                                    if (!this.forceUnicodeFont ||
                                            //glyphProviderType == GlyphProviderType.LEGACY_UNICODE ||
                                            !fontId.equals(DEFAULT_FONT_RENDERER_NAME)) {
                                        GlyphProvider glyphProvider = glyphProviderType.getFactory(jsonobject).create(resourceManager);
                                        if (glyphProvider != null) {
                                            providers.add(glyphProvider);
                                        }
                                    }
                                } catch (RuntimeException e) {
                                    LOGGER.warn("Unable to read definition '{}' in fonts.json in resourcepack: '{}': {}", fontId, resource, e.getMessage());
                                }
                            }
                        } catch (RuntimeException e) {
                            LOGGER.warn("Unable to load font '{}' in fonts.json in resourcepack: '{}': {}", fontId, resource, e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    LOGGER.warn("Unable to load font '{}' in fonts.json: {}", fontId, e.getMessage());
                }
            }
        }
        bar.step("Apply font.json to Glyph");
        Stream.concat(this.fonts.keySet().stream(), map.keySet().stream()).distinct().forEach(location -> {
            List<GlyphProvider> glyphProviders = map.getOrDefault(location, Collections.emptyList());
            Collections.reverse(glyphProviders);
            ResourceLocation id = new ResourceLocation(location.getResourceDomain(), location.getResourcePath().replace(".json", ""));
            this.fonts.computeIfAbsent(location, fontLocation -> new GlyphFont(this.textureManager, id)).setGlyphProviders(glyphProviders);
        });
        ProgressManager.pop(bar);
    }

    @Nullable
    public static <T> T gsonDeserialize(Gson gsonIn, Reader readerIn, Class<T> adapter, boolean lenient) {
        try {
            JsonReader jsonReader = new JsonReader(readerIn);
            jsonReader.setLenient(lenient);
            return gsonIn.getAdapter(adapter).read(jsonReader);
        } catch (IOException var5) {
            throw new JsonParseException(var5);
        }
    }

    @Nullable
    public static <T> T gsonDeserialize(Gson gsonIn, String json, Class<T> adapter, boolean lenient) {
        return gsonDeserialize(gsonIn, new StringReader(json), adapter, lenient);
    }


    @Nullable
    public static <T> T gsonDeserialize(Gson gsonIn, String json, Class<T> adapter) {
        return gsonDeserialize(gsonIn, json, adapter, false);
    }

    public GlyphFont getGlyphFont(ResourceLocation id) {
        return this.fonts.computeIfAbsent(id, location -> {
            GlyphFont glyphFont = new GlyphFont(this.textureManager, location);//new FontRenderer(this.textureManager, new GlyphFont(this.textureManager, location));
            glyphFont.setGlyphProviders(Lists.newArrayList(new DefaultGlyphProvider()));
            return glyphFont;
        });
    }

    public void setForceUnicodeFont(boolean forceUnicodeFont) {
        if (forceUnicodeFont != this.forceUnicodeFont) {
            this.forceUnicodeFont = forceUnicodeFont;
            this.onResourceManagerReload(Minecraft.getMinecraft().getResourceManager());
        }
    }
}
