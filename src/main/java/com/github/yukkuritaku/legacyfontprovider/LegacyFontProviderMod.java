package com.github.yukkuritaku.legacyfontprovider;

import com.github.yukkuritaku.legacyfontprovider.font.FontManager;
import cpw.mods.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MODID,
        version = Tags.VERSION,
        name = Tags.MODNAME,
        dependencies = "required-after:spongemixins",
        acceptedMinecraftVersions = "[1.7.10]"
)
public class LegacyFontProviderMod {

    private static final Logger LOG = LogManager.getLogger(Tags.MODID);

    @Mod.Instance
    private static LegacyFontProviderMod instance;

    public static LegacyFontProviderMod getInstance() {
        return instance;
    }

    private FontManager fontManager;

    public void setFontManager(FontManager fontManager) {
        if (this.fontManager != null) {
            LOG.warn("can't set fontmanager because of already set");
            return;
        }
        this.fontManager = fontManager;
    }

    public FontManager getFontManager() {
        return fontManager;
    }

    public static void debug(String message) {
        LOG.debug(message);
    }

    public static void info(String message) {
        LOG.info(message);
    }

    public static void warn(String message) {
        LOG.warn(message);
    }

    public static void error(String message) {
        LOG.error(message);
    }
}