package com.github.yukkuritaku.legacyfontprovider.mixin;

import com.github.yukkuritaku.legacyfontprovider.ext.IReloadableResourceManagerExt;
import com.github.yukkuritaku.legacyfontprovider.ext.IResourcePackExt;
import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;
import com.google.common.collect.Lists;
import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Mixin(FallbackResourceManager.class)
public class FallbackResourceManagerMixin implements IReloadableResourceManagerExt {

    @Shadow @Final protected List<IResourcePack> resourcePacks;

    @Override
    public Collection<ResourceLocation> legacyfontprovider$getAllResourceLocations(ResourcePackType type, String path, Predicate<String> filter) {
        List<ResourceLocation> list = Lists.newArrayList();
        for (IResourcePack resourcePack : this.resourcePacks){
            if (resourcePack instanceof IResourcePackExt){
                list.addAll(((IResourcePackExt)resourcePack).legacyfontprovider$getAllResourceLocations(type, path, Integer.MAX_VALUE, filter));
            }
        }
        list.sort((Comparator.comparing(ResourceLocation::getResourceDomain).thenComparing(ResourceLocation::getResourcePath)));
        return list;
    }
}
