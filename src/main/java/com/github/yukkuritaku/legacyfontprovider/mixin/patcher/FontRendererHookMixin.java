package com.github.yukkuritaku.legacyfontprovider.mixin.patcher;

import club.sk1er.patcher.hooks.FontRendererHook;
import club.sk1er.patcher.mixins.accessors.FontRendererAccessor;
import club.sk1er.patcher.util.enhancement.text.EnhancedFontRenderer;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.BakedGlyph;
import com.github.yukkuritaku.legacyfontprovider.util.FontUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Deque;

//TODO Patcher Support
@Mixin(value = FontRendererHook.class, remap = false)
public abstract class FontRendererHookMixin {

    @Shadow(remap = false)
    @Final
    private FontRenderer fontRenderer;

    @Shadow(remap = false)
    @Final
    private FontRendererAccessor fontRendererAccessor;

    @Shadow(remap = false)
    @Final
    private Minecraft mc;

    @Shadow(remap = false)
    protected abstract void startDrawing();

    @Shadow(remap = false)
    protected abstract void endDrawing();

    @Shadow(remap = false) public int glTextureId;

    @Shadow(remap = false) public static boolean forceRefresh;

    @Shadow(remap = false) public abstract void create();

    @Shadow(remap = false)
    public static String clearColorReset(String text) {
        throw new AssertionError();
    }

    @Shadow(remap = false) @Final private EnhancedFontRenderer enhancedFontRenderer;

    @Shadow(remap = false) protected abstract float getCharWidthFloat(char c);

    @Shadow(remap = false) protected abstract float getBoldOffset(int index);
    @Shadow(remap = false) protected abstract void deleteTextureId();

    @Shadow(remap = false) public abstract float renderChar(char ch, boolean italic);

    @Unique
    private void legacyfontprovider$adjustOrAppend(
            Deque<FontUtil.RenderPair> style, float posX, float effectiveWidth, float lastRed, float lastGreen, float lastBlue, float lastAlpha
    ) {
        FontUtil.RenderPair lastStart = style.peekLast();
        if (lastStart != null
                && lastStart.red == lastRed
                && lastStart.green == lastGreen
                && lastStart.blue == lastBlue
                && lastStart.alpha == lastAlpha
                && lastStart.posX + lastStart.width >= posX - 1.0F) {
            lastStart.width = posX + effectiveWidth - lastStart.posX;
        } else {
            style.add(new FontUtil.RenderPair(posX, effectiveWidth, lastRed, lastGreen, lastBlue, lastAlpha));
        }
    }

    @Unique
    public void legacyfontprovider$render(BakedGlyph bakedGlyph, boolean bold, boolean italic, float boldOffset, float x, float y, float r, float g, float b, float a) {
        bakedGlyph.render(this.fontRendererAccessor.getRenderEngine(), italic, x, y, null, r, g, b, a);
        if (bold) {
            bakedGlyph.render(this.fontRendererAccessor.getRenderEngine(), italic, x + boldOffset, y, null, r, g, b, a);
        }
    }

}
