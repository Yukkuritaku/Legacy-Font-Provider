package com.github.yukkuritaku.legacyfontprovider.mixin;

import com.github.yukkuritaku.legacyfontprovider.ext.IResourcePackExt;
import com.github.yukkuritaku.legacyfontprovider.resources.ResourcePackType;
import com.google.common.collect.Lists;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Mixin(FileResourcePack.class)
public abstract class FileResourcePackMixin implements IResourcePackExt {
    @Shadow
    public abstract ZipFile getResourcePackZipFile() throws IOException;

    @Override
    public Collection<ResourceLocation> legacyfontprovider$getAllResourceLocations(ResourcePackType type, String path, int maxDepth, Predicate<String> filter) {
        ZipFile zipFile;
        try {
            zipFile = this.getResourcePackZipFile();
        }catch (IOException e){
            return Collections.emptySet();
        }
        Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
        List<ResourceLocation> list = Lists.newArrayList();
        String fileType = type.getDirectoryName();
        while (enumeration.hasMoreElements()){
            ZipEntry entry = enumeration.nextElement();
            if (!entry.isDirectory() && entry.getName().startsWith(fileType)){
                String fileName = entry.getName().substring(fileType.length());
                if (!fileName.endsWith(".mcmeta")){
                    int i = fileName.indexOf(47);
                    if (i >= 0){
                        String s = fileName.substring(i + 1);
                        if (s.startsWith(path + "/")){
                            String[] strings = s.substring(path.length() + 2).split("/");
                            if (strings.length >= maxDepth + 1 && filter.test(s)){
                                String namespace = fileName.substring(0, i);
                                list.add(new ResourceLocation(namespace, s));
                            }
                        }
                    }
                }
            }
        }
        return list;
    }
}
