package com.github.yukkuritaku.legacyfontprovider.ext;

import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.function.Predicate;

public interface IResourcePackExt {

    Collection<ResourceLocation> legacyfontprovider$getAllResourceLocations(ResourcePackType type, String path, int maxDepth, Predicate<String> filter);

}
