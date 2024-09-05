package com.github.yukkuritaku.legacyfontprovider.mixin.normal;

import java.util.List;

import net.minecraft.client.gui.FontRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.yukkuritaku.legacyfontprovider.font.FontRendererHook;

@Mixin(FontRenderer.class)
public class FontRendererMixin {

    @Inject(method = "drawStringWithShadow", at = @At("HEAD"), cancellable = true)
    private void onDrawStringWithShadow(String text, int x, int y, int color, CallbackInfoReturnable<Integer> cir) {
        if (!FontRendererHook.isSplashFontRenderer((FontRenderer) (Object) this))
            cir.setReturnValue(FontRendererHook.drawStringWithShadow(text, x, y, color));
    }

    @Inject(method = "drawString(Ljava/lang/String;III)I", at = @At("HEAD"), cancellable = true)
    private void onDrawString(String text, int x, int y, int color, CallbackInfoReturnable<Integer> cir) {
        if (!FontRendererHook.isSplashFontRenderer((FontRenderer) (Object) this))
            cir.setReturnValue(FontRendererHook.drawString(text, x, y, color));
    }

    @Inject(method = "drawString(Ljava/lang/String;IIIZ)I", at = @At("HEAD"), cancellable = true)
    private void onDrawString(String text, int x, int y, int color, boolean dropShadow,
        CallbackInfoReturnable<Integer> cir) {
        if (!FontRendererHook.isSplashFontRenderer((FontRenderer) (Object) this))
            cir.setReturnValue(FontRendererHook.drawString(text, x, y, color, dropShadow));
    }

    @Inject(method = "getStringWidth", at = @At("HEAD"), cancellable = true)
    private void onGetStringWidth(String text, CallbackInfoReturnable<Integer> cir) {
        if (!FontRendererHook.isSplashFontRenderer((FontRenderer) (Object) this))
            cir.setReturnValue(FontRendererHook.getStringWidth(text));
    }

    @Inject(method = "getCharWidth", at = @At("HEAD"), cancellable = true)
    private void onGetCharWidth(char p_78263_1_, CallbackInfoReturnable<Integer> cir) {
        if (!FontRendererHook.isSplashFontRenderer((FontRenderer) (Object) this))
            cir.setReturnValue((int) FontRendererHook.getCharWidth(p_78263_1_));
    }

    @Inject(method = "trimStringToWidth(Ljava/lang/String;I)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void onTrimToStringWidth(String text, int wrapWidth, CallbackInfoReturnable<String> cir) {
        if (!FontRendererHook.isSplashFontRenderer((FontRenderer) (Object) this))
            cir.setReturnValue(FontRendererHook.trimStringToWidth(text, wrapWidth));
    }

    @Inject(method = "trimStringToWidth(Ljava/lang/String;IZ)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void onTrimToStringWidth(String p_78262_1_, int p_78262_2_, boolean p_78262_3_,
        CallbackInfoReturnable<String> cir) {
        if (!FontRendererHook.isSplashFontRenderer((FontRenderer) (Object) this))
            cir.setReturnValue(FontRendererHook.trimStringToWidth(p_78262_1_, p_78262_2_, p_78262_3_));
    }

    @Inject(method = "drawSplitString", at = @At("HEAD"), cancellable = true)
    private void onDrawSplitString(String str, int x, int y, int wrapWidth, int textColor, CallbackInfo ci) {
        if (!FontRendererHook.isSplashFontRenderer((FontRenderer) (Object) this)) {
            FontRendererHook.drawSplitString(str, x, y, wrapWidth, textColor);
            ci.cancel();
        }
    }

    @Inject(method = "splitStringWidth", at = @At("HEAD"), cancellable = true)
    private void onSplitStringWidth(String p_78267_1_, int p_78267_2_, CallbackInfoReturnable<Integer> cir) {
        if (!FontRendererHook.isSplashFontRenderer((FontRenderer) (Object) this))
            cir.setReturnValue(FontRendererHook.getWordWrappedHeight(p_78267_1_, p_78267_2_));
    }

    @Inject(method = "listFormattedStringToWidth", at = @At("HEAD"), cancellable = true)
    private void onListFormattedStringToWidth(String p_78267_1_, int p_78267_2_,
        CallbackInfoReturnable<List<String>> cir) {
        if (!FontRendererHook.isSplashFontRenderer((FontRenderer) (Object) this))
            cir.setReturnValue(FontRendererHook.listFormattedStringToWidth(p_78267_1_, p_78267_2_));
    }

}
