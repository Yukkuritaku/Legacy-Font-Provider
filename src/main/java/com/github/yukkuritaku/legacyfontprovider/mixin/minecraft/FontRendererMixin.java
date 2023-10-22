package com.github.yukkuritaku.legacyfontprovider.mixin.minecraft;

import com.github.yukkuritaku.legacyfontprovider.LegacyFontProviderMod;
import com.github.yukkuritaku.legacyfontprovider.ext.GlyphFontExt;
import com.github.yukkuritaku.legacyfontprovider.font.GlyphFont;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.BakedGlyph;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.DefaultGlyph;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.Glyph;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontRenderer.class)
public abstract class FontRendererMixin implements GlyphFontExt, AutoCloseable {

    @Unique
    private GlyphFont legacyfontprovider$glyphFont;

    @Shadow
    @Final
    private TextureManager renderEngine;
    @Shadow
    protected float posX;
    @Shadow
    protected float posY;

    @Shadow
    private float red;
    @Shadow
    private float blue;
    @Shadow
    private float green;
    @Shadow
    private float alpha;
    @Shadow
    private boolean unicodeFlag;

    @Shadow
    protected abstract float renderDefaultChar(int ch, boolean italic);

    @Shadow
    protected abstract float renderUnicodeChar(char ch, boolean italic);

    @Shadow
    protected int[] charWidth;
    @Shadow
    protected byte[] glyphWidth;


    @Inject(method = "renderCharAtPos", at = @At("HEAD"), cancellable = true)
    private void onRenderChar(int p_78278_1_, char ch, boolean italic, CallbackInfoReturnable<Float> cir) {
        if (legacyfontprovider$glyphFont != null) {
            BakedGlyph glyph = legacyfontprovider$glyphFont.getGlyph(ch);
            Glyph fontGlyph = legacyfontprovider$glyphFont.findGlyph(ch);
            if (glyph.getTextureLocation() != null) {
                if (!fontGlyph.equals(DefaultGlyph.INSTANCE)) {
                    //GlStateManager.enableBlend();

                    this.renderEngine.bindTexture(glyph.getTextureLocation());
                    GL11.glBegin(7);
                    glyph.render(this.renderEngine, italic, this.posX, this.posY, null, this.red, this.blue, this.green, this.alpha);
                    GL11.glEnd();
                    //GlStateManager.disableBlend();
                    cir.setReturnValue(fontGlyph.getAdvance());
                }
            }
        } else {
            int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000"
                    .indexOf(ch);
            float f;
            if (ch == 160) {
                cir.setReturnValue(4.0f);
            } else if (ch == ' ') {
                cir.setReturnValue(4.0f);
            } else {
                if (i != -1 && !this.unicodeFlag) {
                    f = this.renderDefaultChar(i, italic);
                    cir.setReturnValue(f);
                } else {
                    f = this.renderUnicodeChar(ch, italic);
                    cir.setReturnValue(this.renderUnicodeChar(ch, italic));
                }
            }
        }
    }

    @Inject(method = "getCharWidth", at = @At("HEAD"), cancellable = true)
    private void onGetCharWidth(char character, CallbackInfoReturnable<Integer> cir) {
        if (legacyfontprovider$glyphFont != null) {
            Glyph fontGlyph = legacyfontprovider$glyphFont.findGlyph(character);
            if (!fontGlyph.equals(DefaultGlyph.INSTANCE)) {
                cir.setReturnValue(character == 167 ? 0 : MathHelper.ceiling_float_int(legacyfontprovider$glyphFont.findGlyph(character).getAdvance(false)));
            }
        }else {
            if (character == 160) {
                cir.setReturnValue(4);
            } else if (character == 167) {
                cir.setReturnValue(-1);
            } else if (character == ' ') {
                cir.setReturnValue(4);
            } else {
                int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(character);
                if (character > 0 && i != -1 && !this.unicodeFlag) {
                    cir.setReturnValue(this.charWidth[i]);
                } else if (this.glyphWidth[character] != 0) {
                    int j = this.glyphWidth[character] & 255;
                    int k = j >>> 4;
                    int l = j & 15;
                    ++l;
                    cir.setReturnValue((l - k) / 2 + 1);
                } else {
                    cir.setReturnValue(0);
                }
            }
        }
    }

    @Inject(method = "setUnicodeFlag", at = @At("HEAD"))
    private void onSetUnicode(boolean unicodeFlagIn, CallbackInfo ci) {
        if (LegacyFontProviderMod.getInstance().getFontManager() != null)
            LegacyFontProviderMod.getInstance().getFontManager().setForceUnicodeFont(unicodeFlagIn);
    }

    @Override
    public void legacyfontprovider$setGlyphFont(GlyphFont font) {
        legacyfontprovider$glyphFont = font;
    }

    @Override
    public void close() {
        if (legacyfontprovider$glyphFont != null)
            legacyfontprovider$glyphFont.close();
    }
}
