package com.github.yukkuritaku.legacyfontprovider.mixin.normal;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.github.yukkuritaku.legacyfontprovider.ext.ResourceManagerExt;
import com.github.yukkuritaku.legacyfontprovider.ext.ResourcePackExt;
import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;
import com.google.common.collect.Lists;

@Mixin(FallbackResourceManager.class)
public class FallbackResourceManagerMixin implements ResourceManagerExt {

    @Shadow
    @Final
    protected List resourcePacks;

    @Override
    public Collection<ResourceLocation> legacyfontprovider$getAllResourceLocations(ResourcePackType type, String path,
        Predicate<String> filter) {
        List list = Lists.newArrayList();
        for (Object resourcePack : this.resourcePacks) {
            if (resourcePack instanceof ResourcePackExt) {
                list.addAll(
                    ((ResourcePackExt) resourcePack)
                        .legacyfontprovider$getAllResourceLocations(type, path, Integer.MAX_VALUE, filter));
            }
        }
        list.sort(
            (Comparator.comparing(ResourceLocation::getResourceDomain)
                .thenComparing(ResourceLocation::getResourcePath)));
        return list;
    }

}
