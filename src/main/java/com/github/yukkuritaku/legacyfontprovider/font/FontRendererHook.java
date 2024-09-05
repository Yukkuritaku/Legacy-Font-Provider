package com.github.yukkuritaku.legacyfontprovider.font;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import com.github.yukkuritaku.legacyfontprovider.ext.MinecraftExt;

/**
 * a minecraft FontRenderer replacement class.
 * these methods are automatically called from transformers
 */
public class FontRendererHook {

    public static FontManager getFontManager() {
        return ((MinecraftExt) Minecraft.getMinecraft()).getFontManager();
    }

    public static FontProviderRenderer getFontProviderRenderer() {
        return ((MinecraftExt) Minecraft.getMinecraft()).getFontProviderRenderer();
    }

    public static int drawStringWithShadow(String text, float x, float y, int color) {
        return getFontProviderRenderer().drawStringWithShadow(text, x, y, color);
    }

    public static int drawString(String text, float x, float y, int color) {
        return getFontProviderRenderer().drawString(text, x, y, color);
    }

    public static int drawString(String text, float x, float y, int color, boolean dropShadow) {
        return getFontProviderRenderer().drawString(text, x, y, color, dropShadow);
    }

    public static void drawSplitString(String text, int x, int y, int wrapWidth, int color) {
        getFontProviderRenderer().drawSplitString(text, x, y, wrapWidth, color);
    }

    public static int getStringWidth(String text) {
        return getFontProviderRenderer().getStringWidth(text);
    }

    public static float getCharWidth(char text) {
        return getFontProviderRenderer().getCharWidth(text);
    }

    public static String trimStringToWidth(String text, int wrapWidth) {
        return getFontProviderRenderer().trimStringToWidth(text, wrapWidth);
    }

    public static String trimStringToWidth(String text, int wrapWidth, boolean reversed) {
        return getFontProviderRenderer().trimStringToWidth(text, wrapWidth, reversed);
    }

    public static int getWordWrappedHeight(String text, int wrapWidth) {
        return getFontProviderRenderer().getWordWrappedHeight(text, wrapWidth);
    }

    public static List<String> listFormattedStringToWidth(String text, int wrapWidth) {
        return getFontProviderRenderer().listFormattedStringToWidth(text, wrapWidth);
    }

    public static String wrapFormattedStringToWidth(String text, int wrapWidth) {
        return getFontProviderRenderer().wrapFormattedStringToWidth(text, wrapWidth);
    }

    public static boolean isSplashFontRenderer(FontRenderer fontRenderer) {
        try {
            Class<?> splashFontRenderer = Class.forName("cpw.mods.fml.client.SplashProgress$SplashFontRenderer");
            if (splashFontRenderer.isAssignableFrom(fontRenderer.getClass())) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
