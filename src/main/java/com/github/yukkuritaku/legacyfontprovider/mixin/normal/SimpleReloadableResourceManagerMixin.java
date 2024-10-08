package com.github.yukkuritaku.legacyfontprovider.mixin.normal;

import java.util.*;
import java.util.function.Predicate;

import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.github.yukkuritaku.legacyfontprovider.ext.ResourceManagerExt;
import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Mixin(SimpleReloadableResourceManager.class)

public class SimpleReloadableResourceManagerMixin implements ResourceManagerExt {

    @Shadow
    @Final
    private Map domainResourceManagers;

    @Override
    public Collection<ResourceLocation> legacyfontprovider$getAllResourceLocations(ResourcePackType type, String path,
        Predicate<String> filter) {
        Set locations = Sets.newHashSet();
        for (Object resourceManager : this.domainResourceManagers.values()) {
            locations.addAll(
                ((ResourceManagerExt) resourceManager).legacyfontprovider$getAllResourceLocations(type, path, filter));
        }
        List resourceLocations = Lists.newArrayList(locations);
        resourceLocations.sort(
            (Comparator.comparing(ResourceLocation::getResourceDomain)
                .thenComparing(ResourceLocation::getResourcePath)));
        return resourceLocations;
    }
}
