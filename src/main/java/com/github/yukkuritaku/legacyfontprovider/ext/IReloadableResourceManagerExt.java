package com.github.yukkuritaku.legacyfontprovider.ext;

import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.function.Predicate;

@SideOnly(Side.CLIENT)
public interface IReloadableResourceManagerExt {

    Collection<ResourceLocation> legacyfontprovider$getAllResourceLocations(ResourcePackType type, String path, Predicate<String> filter);
}
