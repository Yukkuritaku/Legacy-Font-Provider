package com.github.yukkuritaku.legacyfontprovider.plugin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

import java.util.Arrays;
import java.util.List;

import static com.github.yukkuritaku.legacyfontprovider.plugin.TargetedMod.VANILLA;


public enum Mixin {

    //
    // IMPORTANT: Do not make any references to any mod from this file. This file is loaded quite early on and if
    // you refer to other mods you load them as well. The consequence is: You can't inject any previously loaded classes!
    // Exception: Tags.java, as long as it is used for Strings only!
    //

    // Replace with your own mixins:
    ItemEditableBookMixin("minecraft.ItemEditableBookMixin", Side.BOTH, VANILLA),
    FallbackResourceManagerMixin("minecraft.FallbackResourceManagerMixin", Side.CLIENT, VANILLA),
    FileResourcePackMixin("minecraft.FileResourcePackMixin", Side.CLIENT, VANILLA),
    FolderResourcePackMixin("minecraft.FolderResourcePackMixin", Side.CLIENT, VANILLA),
    FontRendererMixin("minecraft.FontRendererMixin", Side.CLIENT, VANILLA),
    MinecraftMixin("minecraft.MinecraftMixin", Side.CLIENT, VANILLA),
    SimpleReloadableResourceManagerMixin("minecraft.SimpleReloadableResourceManagerMixin", Side.CLIENT, VANILLA),


    ;

    public final String mixinClass;
    public final List<TargetedMod> targetedMods;
    private final Side side;

    Mixin(String mixinClass, Side side, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.targetedMods = Arrays.asList(targetedMods);
        this.side = side;
    }

    Mixin(String mixinClass, TargetedMod... targetedMods) {
        this.mixinClass = mixinClass;
        this.targetedMods = Arrays.asList(targetedMods);
        this.side = Side.BOTH;
    }

    public boolean shouldLoad(List<TargetedMod> loadedMods) {
        return (side == Side.BOTH
                || side == Side.SERVER && FMLLaunchHandler.side().isServer()
                || side == Side.CLIENT && FMLLaunchHandler.side().isClient())
                && loadedMods.containsAll(targetedMods);
    }
}

enum Side {
    BOTH,
    CLIENT,
    SERVER
}