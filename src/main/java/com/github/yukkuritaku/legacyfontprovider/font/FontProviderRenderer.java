package com.github.yukkuritaku.legacyfontprovider.font;

import java.util.*;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.BakedGlyph;
import com.github.yukkuritaku.legacyfontprovider.font.providers.GlyphProvider;
import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;

public class FontProviderRenderer implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger();
    public int FONT_HEIGHT = 9;
    public Random fontRandom = new Random();
    private final TextureManager textureManager;
    private final FontProviderSet fontProvider;
    private boolean bidiFlag;
    private boolean fishy;

    public FontProviderRenderer(TextureManager textureManager, FontProviderSet fontProvider, boolean fishy) {
        this.textureManager = textureManager;
        this.fontProvider = fontProvider;
        this.fishy = fishy;
    }

    private static int getChatFormattingColor(EnumChatFormatting chatFormatting) {
        return switch (chatFormatting) {
            case BLACK -> 0;
            case DARK_BLUE -> 170;
            case DARK_GREEN -> 43520;
            case DARK_AQUA -> 43690;
            case DARK_RED -> 11141120;
            case DARK_PURPLE -> 11141290;
            case GOLD -> 16755200;
            case GRAY -> 11184810;
            case DARK_GRAY -> 5592405;
            case BLUE -> 5592575;
            case GREEN -> 5635925;
            case AQUA -> 5636095;
            case RED -> 16733525;
            case LIGHT_PURPLE -> 16733695;
            case YELLOW -> 16777045;
            case WHITE -> 16777215;
            //
            default -> throw new IllegalStateException("Not a color!");
        };
    }

    @Nullable
    public static EnumChatFormatting fromFormattingCode(char character) {
        char c = Character.toString(character)
            .toLowerCase(Locale.ROOT)
            .charAt(0);
        EnumChatFormatting[] values = EnumChatFormatting.values();
        for (EnumChatFormatting chatFormatting : values) {
            if (chatFormatting.formattingCode == c) {
                return chatFormatting;
            }
        }

        return null;
    }

    public static String getFormatString(String string) {
        StringBuilder builder = new StringBuilder();
        int index = -1;
        int length = string.length();

        while ((index = string.indexOf(167, index + 1)) != -1) {
            if (index < length - 1) {
                EnumChatFormatting formatting = fromFormattingCode(string.charAt(index + 1));
                if (formatting != null) {
                    if (!formatting.isFancyStyling()) {
                        builder.setLength(0);
                    }

                    if (formatting != EnumChatFormatting.RESET) {
                        builder.append(formatting);
                    }
                }
            }
        }

        return builder.toString();
    }

    public void setGlyphProviders(List<GlyphProvider> glyphProviders) {
        this.fontProvider.setGlyphProviders(glyphProviders);
    }

    @Override
    public void close() {
        this.fontProvider.close();
    }

    public int drawStringWithShadow(String text, float x, float y, int color) {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        return this.renderString(text, x, y, color, true);
    }

    public int drawString(String text, float x, float y, int color) {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        return this.renderString(text, x, y, color, false);
    }

    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        return this.renderString(text, x, y, color, dropShadow);
    }

    private String bidiReorder(String text) {
        try {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(text), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        } catch (ArabicShapingException var3) {
            return text;
        }
    }

    private int renderString(String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) {
            return 0;
        } else {
            if (this.bidiFlag) {
                text = this.bidiReorder(text);
            }

            if ((color & -67108864) == 0) {
                color |= -16777216;
            }

            if (dropShadow) {
                this.renderStringAtPos(text, x, y, color, true);
            }

            x = this.renderStringAtPos(text, x, y, color, false);
            return (int) x + (dropShadow ? 1 : 0);
        }
    }

    private float renderStringAtPos(String text, float x, float y, int color, boolean dropShadow) {
        GL11.glPushMatrix();
        float shadowColor = dropShadow ? 0.25F : 1.0F;
        float defR = (float) (color >> 16 & 255) / 255.0F * shadowColor;
        float defG = (float) (color >> 8 & 255) / 255.0F * shadowColor;
        float defB = (float) (color & 255) / 255.0F * shadowColor;
        float r = defR;
        float g = defG;
        float b = defB;
        float a = (float) (color >> 24 & 255) / 255.0F;
        ResourceLocation oldLocation = null;
        Tessellator.instance.startDrawingQuads();
        boolean obfuscated = false;
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikethrough = false;
        List<Entry> entries = Lists.newArrayList();

        for (int index = 0; index < text.length(); ++index) {
            char character = text.charAt(index);
            if (character == 167 && index + 1 < text.length()) {
                EnumChatFormatting formatting = fromFormattingCode(text.charAt(index + 1));
                if (formatting != null) {
                    if (!formatting.isFancyStyling()) {
                        obfuscated = false;
                        bold = false;
                        strikethrough = false;
                        underline = false;
                        italic = false;
                        r = defR;
                        g = defG;
                        b = defB;
                    }

                    if (formatting.isColor()) {
                        int chatColor = getChatFormattingColor(formatting);
                        r = (float) (chatColor >> 16 & 255) / 255.0F * shadowColor;
                        g = (float) (chatColor >> 8 & 255) / 255.0F * shadowColor;
                        b = (float) (chatColor & 255) / 255.0F * shadowColor;
                    } else if (formatting == EnumChatFormatting.OBFUSCATED) {
                        obfuscated = true;
                    } else if (formatting == EnumChatFormatting.BOLD) {
                        bold = true;
                    } else if (formatting == EnumChatFormatting.STRIKETHROUGH) {
                        strikethrough = true;
                    } else if (formatting == EnumChatFormatting.UNDERLINE) {
                        underline = true;
                    } else if (formatting == EnumChatFormatting.ITALIC) {
                        italic = true;
                    }
                }

                ++index;
            } else {
                GlyphInfo glyph = this.fontProvider.findGlyph(character);
                BakedGlyph bakedGlyph = obfuscated && character != ' ' ? this.fontProvider.obfuscate(glyph)
                    : this.fontProvider.getGlyph(character);
                ResourceLocation textureLocation = bakedGlyph.getTextureLocation();
                float offset;
                float renderingOffset;
                if (textureLocation != null) {
                    if (oldLocation != textureLocation) {
                        Tessellator.instance.draw();
                        this.textureManager.bindTexture(textureLocation);
                        Tessellator.instance.startDrawingQuads();
                        oldLocation = textureLocation;
                    }

                    offset = bold ? glyph.getBoldOffset() : 0.0F;
                    renderingOffset = dropShadow ? glyph.getShadowOffset() : 0.0F;
                    this.renderGlyph(
                        bakedGlyph,
                        bold,
                        italic,
                        offset,
                        x + renderingOffset,
                        y + renderingOffset,
                        r,
                        g,
                        b,
                        a);
                }

                offset = glyph.getAdvance(bold);
                renderingOffset = dropShadow ? 1.0F : 0.0F;
                if (strikethrough) {
                    entries.add(
                        new Entry(
                            x + renderingOffset - 1.0F,
                            y + renderingOffset + (float) this.FONT_HEIGHT / 2.0F,
                            x + renderingOffset + offset,
                            y + renderingOffset + (float) this.FONT_HEIGHT / 2.0F - 1.0F,
                            r,
                            g,
                            b,
                            a));
                }

                if (underline) {
                    entries.add(
                        new Entry(
                            x + renderingOffset - 1.0F,
                            y + renderingOffset + (float) this.FONT_HEIGHT,
                            x + renderingOffset + offset,
                            y + renderingOffset + (float) this.FONT_HEIGHT - 1.0F,
                            r,
                            g,
                            b,
                            a));
                }

                x += offset;
            }
        }

        Tessellator.instance.draw();
        if (!entries.isEmpty()) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            Tessellator.instance.startDrawingQuads();
            for (Entry entry : entries) {
                entry.pipe(Tessellator.instance);
            }

            Tessellator.instance.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
        GL11.glPopMatrix();
        return x;
    }

    private void renderGlyph(BakedGlyph glyph, boolean dropShadow, boolean italic, float offset, float x, float y,
        float r, float g, float b, float a) {
        glyph.render(italic, x, y, Tessellator.instance, r, g, b, a, 0);
        if (dropShadow) {
            glyph.render(italic, x + offset, y, Tessellator.instance, r, g, b, a, 0);
        }

    }

    public int getStringWidth(String text) {
        if (text == null) {
            return 0;
        } else {
            float advanceOffset = 0.0F;
            boolean bold = false;

            for (int index = 0; index < text.length(); ++index) {
                char character = text.charAt(index);
                if (character == 167 && index < text.length() - 1) {
                    ++index;
                    EnumChatFormatting chatFormatting = fromFormattingCode(text.charAt(index));
                    if (chatFormatting == EnumChatFormatting.BOLD) {
                        bold = true;
                    } else if (chatFormatting != null && !chatFormatting.isFancyStyling()) {
                        bold = false;
                    }
                } else {
                    advanceOffset += this.fontProvider.findGlyph(character)
                        .getAdvance(bold);
                }
            }

            return MathHelper.ceiling_double_int(advanceOffset);
        }
    }

    public float getCharWidth(char character) {
        return character == 167 ? 0.0F
            : (float) MathHelper.ceiling_double_int(
                this.fontProvider.findGlyph(character)
                    .getAdvance(false));
    }

    public String trimStringToWidth(String text, int wrapWidth) {
        return this.trimStringToWidth(text, wrapWidth, false);
    }

    public String trimStringToWidth(String text, int wrapWidth, boolean reversed) {
        StringBuilder builder = new StringBuilder();
        float charaterWidth = 0.0F;
        int length = reversed ? text.length() - 1 : 0;
        int i = reversed ? -1 : 1;
        boolean format = false;
        boolean bold = false;

        for (int textLength = length; textLength >= 0 && textLength < text.length()
            && charaterWidth < (float) wrapWidth; textLength += i) {
            char character = text.charAt(textLength);
            if (format) {
                format = false;
                EnumChatFormatting chatFormatting = fromFormattingCode(character);
                if (chatFormatting == EnumChatFormatting.BOLD) {
                    bold = true;
                } else if (chatFormatting != null && !chatFormatting.isFancyStyling()) {
                    bold = false;
                }
            } else if (character == 167) {
                format = true;
            } else {
                charaterWidth += this.getCharWidth(character);
                if (bold) {
                    ++charaterWidth;
                }
            }

            if (charaterWidth > (float) wrapWidth) {
                break;
            }

            if (reversed) {
                builder.insert(0, character);
            } else {
                builder.append(character);
            }
        }

        return builder.toString();
    }

    private String trimStringNewline(String text) {
        while (text != null && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    public void drawSplitString(String text, int x, int y, int wrapWidth, int textColor) {
        text = this.trimStringNewline(text);
        this.renderSplitString(text, x, y, wrapWidth, textColor);
    }

    private void renderSplitString(String text, int x, int y, int wrapWidth, int textColor) {
        List<String> list = this.listFormattedStringToWidth(text, wrapWidth);

        for (Iterator<String> itr = list.iterator(); itr.hasNext(); y += this.FONT_HEIGHT) {
            String fmtString = itr.next();
            float renderingX = (float) x;
            if (this.bidiFlag) {
                int strWidth = this.getStringWidth(this.bidiReorder(fmtString));
                renderingX += (float) (wrapWidth - strWidth);
            }

            this.renderString(fmtString, renderingX, (float) y, textColor, false);
        }

    }

    public int getWordWrappedHeight(String text, int wrapWidth) {
        return this.FONT_HEIGHT * this.listFormattedStringToWidth(text, wrapWidth)
            .size();
    }

    public void setBidiFlag(boolean flag) {
        this.bidiFlag = flag;
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return Arrays.asList(
            this.wrapFormattedStringToWidth(str, wrapWidth)
                .split("\n"));
    }

    public String wrapFormattedStringToWidth(String str, int wrapWidth) {
        String wrappedFormatString;
        String subStr;
        for (wrappedFormatString = ""; !str.isEmpty(); wrappedFormatString = wrappedFormatString + subStr + "\n") {
            int sizeWidth = this.sizeStringToWidth(str, wrapWidth);
            if (str.length() <= sizeWidth) {
                return wrappedFormatString + str;
            }

            subStr = str.substring(0, sizeWidth);
            char c = str.charAt(sizeWidth);
            boolean emptyOrNewLine = c == ' ' || c == '\n';
            str = getFormatString(subStr) + str.substring(sizeWidth + (emptyOrNewLine ? 1 : 0));
        }

        return wrappedFormatString;
    }

    private int sizeStringToWidth(String str, int wrapWidth) {
        int maxWidth = Math.max(1, wrapWidth);
        int length = str.length();
        float charaterWidth = 0.0F;
        int index = 0;
        int newLineIndex = -1;
        boolean bold = false;

        for (boolean flag = true; index < length; ++index) {
            char character = str.charAt(index);
            switch (character) {
                case '\n':
                    --index;
                    break;
                case ' ':
                    newLineIndex = index;
                default:
                    if (charaterWidth != 0.0F) {
                        flag = false;
                    }

                    charaterWidth += this.getCharWidth(character);
                    if (bold) {
                        ++charaterWidth;
                    }
                    break;
                case '§':
                    if (index < length - 1) {
                        ++index;
                        EnumChatFormatting chatFormatting = fromFormattingCode(str.charAt(index));
                        if (chatFormatting == EnumChatFormatting.BOLD) {
                            bold = true;
                        } else if (chatFormatting != null && !chatFormatting.isFancyStyling()) {
                            bold = false;
                        }
                    }
            }

            if (character == '\n') {
                ++index;
                newLineIndex = index;
                break;
            }

            if (charaterWidth > (float) maxWidth) {
                if (flag) {
                    ++index;
                }
                break;
            }
        }

        return index != length && newLineIndex != -1 && newLineIndex < index ? newLineIndex : index;
    }

    public boolean getBidiFlag() {
        return this.bidiFlag;
    }

    static class Entry {

        protected final float x1;
        protected final float y1;
        protected final float x2;
        protected final float y2;
        protected final float red;
        protected final float green;
        protected final float blue;
        protected final float alpha;

        private Entry(float x1, float y1, float x2, float y2, float red, float green, float blue, float alpha) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        public void pipe(Tessellator tessellator) {
            tessellator.setColorRGBA_F(this.red, this.green, this.blue, this.alpha);
            tessellator.addVertex(this.x1, this.y1, 0.0);
            tessellator.addVertex(this.x2, this.y1, 0.0);
            tessellator.addVertex(this.x2, this.y2, 0.0);
            tessellator.addVertex(this.x1, this.y2, 0.0);
        }
    }
}
