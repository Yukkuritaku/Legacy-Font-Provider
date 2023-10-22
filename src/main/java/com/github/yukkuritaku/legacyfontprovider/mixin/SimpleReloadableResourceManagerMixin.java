package com.github.yukkuritaku.legacyfontprovider.mixin;

import com.github.yukkuritaku.legacyfontprovider.ext.IReloadableResourceManagerExt;
import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;
import java.util.function.Predicate;

@Mixin(SimpleReloadableResourceManager.class)
public class SimpleReloadableResourceManagerMixin implements IReloadableResourceManagerExt {
    @Shadow @Final private Map<String, FallbackResourceManager> domainResourceManagers;

    @Override
    public Collection<ResourceLocation> legacyfontprovider$getAllResourceLocations(ResourcePackType type, String path, Predicate<String> filter) {
        Set<ResourceLocation> locations = Sets.newHashSet();
        for (FallbackResourceManager resourceManager : this.domainResourceManagers.values()){
            locations.addAll(((IReloadableResourceManagerExt)resourceManager).legacyfontprovider$getAllResourceLocations(type, path, filter));
        }
        List<ResourceLocation> resourceLocations = Lists.newArrayList(locations);
        Collections.sort(resourceLocations);
        return resourceLocations;
    }
}
