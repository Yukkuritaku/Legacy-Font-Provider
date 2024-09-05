package com.github.yukkuritaku.legacyfontprovider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;

@Mod(
    modid = LegacyFontProviderMod.MOD_ID,
    version = Tags.VERSION,
    name = "LegacyFontProvider",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required:unicodefix@[1.3,);")
public class LegacyFontProviderMod {

    public static final String MOD_ID = "legacyfontprovider";
    public static final Logger LOG = LogManager.getLogger(MOD_ID);
}
