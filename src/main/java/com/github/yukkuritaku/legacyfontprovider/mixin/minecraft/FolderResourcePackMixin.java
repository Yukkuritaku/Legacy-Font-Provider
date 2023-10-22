package com.github.yukkuritaku.legacyfontprovider.mixin.minecraft;

import com.github.yukkuritaku.legacyfontprovider.ext.IResourcePackExt;
import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(FolderResourcePack.class)
public abstract class FolderResourcePackMixin extends AbstractResourcePack implements IResourcePackExt {

    public FolderResourcePackMixin(File file) {
        super(file);
    }

    @Unique
    public Set<String> legacyfontprovider$getResourceNamespaces(ResourcePackType type) {
        Set<String> set = Sets.newHashSet();
        File dir = new File(this.resourcePackFile, type.getDirectoryName());
        File[] listDirs = dir.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
        if (listDirs != null) {
            for(File listDir : listDirs) {
                String s = getRelativeName(dir, listDir);
                if (s.equals(s.toLowerCase(Locale.ROOT))) {
                    set.add(s.substring(0, s.length() - 1));
                } else {
                    this.logNameNotLowercase(s);
                }
            }
        }

        return set;
    }

    @Unique
    private void legacyfontprovider$add(File file, int maxDepth, String namespace, List<ResourceLocation> resourceLocations,
                                        String path, Predicate<String> predicate) {
        File[] afile = file.listFiles();
        if (afile != null) {
            for(File file1 : afile) {
                if (file1.isDirectory()) {
                    if (maxDepth > 0) {
                        this.legacyfontprovider$add(file1, maxDepth - 1, namespace, resourceLocations, path + file1.getName() + "/", predicate);
                    }
                } else if (!file1.getName().endsWith(".mcmeta") && predicate.test(file1.getName())) {
                    try {
                        resourceLocations.add(new ResourceLocation(namespace, path + file1.getName()));
                    } catch (Exception e) {
                        LogManager.getLogger().error(e.getMessage());
                    }
                }
            }
        }

    }

    @Override
    public Collection<ResourceLocation> legacyfontprovider$getAllResourceLocations(ResourcePackType type, String path, int maxDepth, Predicate<String> filter) {
        File file = new File(this.resourcePackFile, type.getDirectoryName());
        List<ResourceLocation> list = Lists.newArrayList();
        for (String namespace : this.legacyfontprovider$getResourceNamespaces(type)){
            this.legacyfontprovider$add(new File(new File(file, namespace), path), maxDepth, namespace, list, path + "/", filter);
        }
        return list;
    }
}
