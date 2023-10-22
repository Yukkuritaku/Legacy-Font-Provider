package com.github.yukkuritaku.legacyfontprovider.util;


import net.minecraft.util.EnumChatFormatting;

import javax.annotation.Nullable;
import java.util.Locale;

public class TextFormattingUtil {


    private static boolean isColor(EnumChatFormatting formatting){
        boolean color = false;
        switch (formatting){
            case BLACK:
            case LIGHT_PURPLE:
            case WHITE:
            case YELLOW:
            case RED:
            case AQUA:
            case GREEN:
            case BLUE:
            case DARK_BLUE:
            case DARK_GREEN:
            case DARK_AQUA:
            case DARK_RED:
            case DARK_PURPLE:
            case GOLD:
            case GRAY:
            case DARK_GRAY:
                color = true;
                break;
            case OBFUSCATED:
            case BOLD:
            case STRIKETHROUGH:
            case UNDERLINE:
            case RESET:
            case ITALIC:
                break;
        }
        return color;
    }

    @Nullable
    public static Integer getColor(EnumChatFormatting formatting){
        Integer color = 0;
        switch (formatting){
            case BLACK:
                break;
            case LIGHT_PURPLE:
                color = 16733695;
                break;
            case WHITE:
                color = 16777215;
                break;
            case YELLOW:
                color = 16777045;
                break;
            case RED:
                color = 16733525;
                break;
            case AQUA:
                color = 5636095;
                break;
            case GREEN:
                color = 5635925;
                break;
            case BLUE:
                color = 5592575;
                break;
            case DARK_BLUE:
                color = 170;
                break;
            case DARK_GREEN:
                color = 43520;
                break;
            case DARK_AQUA:
                color = 43690;
                break;
            case DARK_RED:
                color = 11141120;
                break;
            case DARK_PURPLE:
                color = 11141290;
                break;
            case GOLD:
                color = 16755200;
                break;
            case GRAY:
                color = 11184810;
                break;
            case DARK_GRAY:
                color = 5592405;
                break;
            case OBFUSCATED:
            case BOLD:
            case STRIKETHROUGH:
            case UNDERLINE:
            case RESET:
            case ITALIC:
                color = null;
                break;
        }
        return color;
    }

    @Nullable
    public static EnumChatFormatting fromFormattingCode(char formattingCodeIn) {
        char c0 = Character.toString(formattingCodeIn).toLowerCase(Locale.ROOT).charAt(0);
        for(EnumChatFormatting textformatting : EnumChatFormatting.values()) {
            if (textformatting.formattingCode == c0) {
                return textformatting;
            }
        }
        return null;
    }

    public static String getFormatString(String str) {
        StringBuilder stringbuilder = new StringBuilder();
        int i = -1;
        int length = str.length();
        while((i = str.indexOf(167, i + 1)) != -1) {
            if (i < length - 1) {
                EnumChatFormatting textformatting = fromFormattingCode(str.charAt(i + 1));
                if (textformatting != null) {
                    if (!textformatting.isFancyStyling()) {
                        stringbuilder.setLength(0);
                    }
                    if (textformatting != EnumChatFormatting.RESET) {
                        stringbuilder.append(textformatting);
                    }
                }
            }
        }
        return stringbuilder.toString();
    }
}
