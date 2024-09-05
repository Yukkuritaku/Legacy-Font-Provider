package com.github.yukkuritaku.legacyfontprovider.ext;

import java.util.Collection;
import java.util.function.Predicate;

import net.minecraft.util.ResourceLocation;

import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;

public interface ResourcePackExt {

    Collection<ResourceLocation> legacyfontprovider$getAllResourceLocations(ResourcePackType type, String path,
        int maxDepth, Predicate<String> filter);
}
