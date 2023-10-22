package com.github.yukkuritaku.legacyfontprovider.ext;

import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.function.Predicate;

public interface IReloadableResourceManagerExt {

    Collection<ResourceLocation> legacyfontprovider$getAllResourceLocations(ResourcePackType type, String path, Predicate<String> filter);
}
