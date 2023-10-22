package com.github.yukkuritaku.legacyfontprovider;

import com.github.yukkuritaku.legacyfontprovider.font.FontManager;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "legacyfontprovider",
        clientSideOnly = true,
        useMetadata=true)
public class LegacyFontProviderMod {
    private static final Logger LOGGER = LogManager.getLogger();

    private FontManager fontManager;
    private static LegacyFontProviderMod instance;

    public LegacyFontProviderMod(){
        instance = this;
    }

    public void setFontManager(FontManager fontManager) {
        if (this.fontManager != null){
            LOGGER.warn("can't set fontmanager because of already set");
            return;
        }
        this.fontManager = fontManager;
    }

    public FontManager getFontManager() {
        return fontManager;
    }

    public static LegacyFontProviderMod getInstance() {
        return instance;
    }
}
