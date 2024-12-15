package com.github.yukkuritaku.legacyfontprovider.mixin.normal;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiEnchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.yukkuritaku.legacyfontprovider.font.FontRendererHook;

@Mixin(GuiEnchantment.class)
public class GuiEnchantmentMixin {

    @Redirect(
        method = "drawGuiContainerBackgroundLayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;drawSplitString(Ljava/lang/String;IIII)V"))
    private void onDrawGuiContainerBackgroundLayer_drawSplitString(FontRenderer instance, String text, int x, int y,
        int wrapWidth, int textColor) {
        FontRendererHook.getFishyFontProviderRenderer()
            .drawSplitString(text, x, y, wrapWidth, textColor);
    }
}
