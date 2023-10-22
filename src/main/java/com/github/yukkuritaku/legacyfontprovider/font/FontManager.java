package com.github.yukkuritaku.legacyfontprovider.font;

import com.github.yukkuritaku.legacyfontprovider.ext.IReloadableResourceManagerExt;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers.DefaultGlyphProvider;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers.GlyphProvider;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.providers.GlyphProviderType;
import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.*;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SideOnly(Side.CLIENT)
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
            JsonObject deserialize = JsonUtils.gsonDeserialize(gson, IOUtils.toString(defaultJson, StandardCharsets.UTF_8), JsonObject.class);
            if (deserialize != null) {
                JsonArray jsonArray = JsonUtils.getJsonArray(deserialize, "providers");
                for (int i = jsonArray.size() - 1; i >= 0; --i) {
                    JsonObject object = JsonUtils.getJsonObject(jsonArray.get(i), "providers[" + i + "]");
                    try {
                        GlyphProviderType providerType = GlyphProviderType.byName(JsonUtils.getString(object, "type"));
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
            JsonObject obj = JsonUtils.gsonDeserialize(gson, IOUtils.toString(altJson, StandardCharsets.UTF_8), JsonObject.class);
            if (obj != null) {
                JsonArray jsonArray = JsonUtils.getJsonArray(obj, "providers");
                for (int i = jsonArray.size() - 1; i >= 0; --i) {
                    JsonObject object = JsonUtils.getJsonObject(jsonArray.get(i), "providers[" + i + "]");
                    try {
                        GlyphProviderType providerType = GlyphProviderType.byName(JsonUtils.getString(object, "type"));
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
                String path = location.getPath();
                ResourceLocation fontId = new ResourceLocation(location.getNamespace(), path.substring("font/".length(), path.length() - ".json".length()));
                List<GlyphProvider> providers = map.computeIfAbsent(fontId, r -> Lists.newArrayList(new DefaultGlyphProvider()));
                try {
                    for (IResource resource : resourceManager.getAllResources(location)){
                        try(InputStream stream = resource.getInputStream()) {
                            JsonArray jsonarray = JsonUtils.getJsonArray(JsonUtils.gsonDeserialize(gson, IOUtils.toString(stream, StandardCharsets.UTF_8), JsonObject.class), "providers");
                            for(int i = jsonarray.size() - 1; i >= 0; --i) {
                                JsonObject jsonobject = JsonUtils.getJsonObject(jsonarray.get(i), "providers[" + i + "]");
                                try {
                                    GlyphProviderType glyphProviderType = GlyphProviderType.byName(JsonUtils.getString(jsonobject, "type"));
                                    if (!this.forceUnicodeFont ||
                                            //glyphProviderType == GlyphProviderType.LEGACY_UNICODE ||
                                            !fontId.equals(DEFAULT_FONT_RENDERER_NAME)) {
                                        GlyphProvider glyphProvider = glyphProviderType.getFactory(jsonobject).create(resourceManager);
                                        if (glyphProvider != null) {
                                            providers.add(glyphProvider);
                                        }
                                    }
                                } catch (RuntimeException e) {
                                    LOGGER.warn("Unable to read definition '{}' in fonts.json in resourcepack: '{}': {}", fontId, resource.getResourcePackName(), e.getMessage());
                                }
                            }
                        } catch (RuntimeException e) {
                            LOGGER.warn("Unable to load font '{}' in fonts.json in resourcepack: '{}': {}", fontId, resource.getResourcePackName(), e.getMessage());
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
            ResourceLocation id = new ResourceLocation(location.getNamespace(), location.getPath().replace(".json", ""));
            this.fonts.computeIfAbsent(location, fontLocation -> new GlyphFont(this.textureManager, id)).setGlyphProviders(glyphProviders);
        });
        ProgressManager.pop(bar);
    }

    @Nullable
    public GlyphFont getGlyphFont(ResourceLocation id) {
        return this.fonts.computeIfAbsent(id, location -> {
            GlyphFont glyphFont = new GlyphFont(this.textureManager, location);
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
