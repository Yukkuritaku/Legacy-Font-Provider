package com.github.yukkuritaku.legacyfontprovider.mixin.minecraft;

import com.github.yukkuritaku.legacyfontprovider.LegacyFontProviderMod;
import com.github.yukkuritaku.legacyfontprovider.ext.GlyphFontExt;
import com.github.yukkuritaku.legacyfontprovider.font.FontManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    private IReloadableResourceManager mcResourceManager;

    @Shadow public FontRenderer fontRenderer;

    @Shadow public TextureManager renderEngine;

    @Shadow public FontRenderer standardGalacticFontRenderer;

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/MusicTicker;<init>(Lnet/minecraft/client/Minecraft;)V"))
    private void onInit$MusicTicker(CallbackInfo ci) {
        LegacyFontProviderMod.getInstance().setFontManager(new FontManager(this.renderEngine, Minecraft.getMinecraft().gameSettings.forceUnicodeFont));
        this.mcResourceManager.registerReloadListener(LegacyFontProviderMod.getInstance().getFontManager());
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;setUnicodeFlag(Z)V"))
    private void onInit$setUnicodeFlag(CallbackInfo ci) {
        ((GlyphFontExt) this.fontRenderer).legacyfontprovider$setGlyphFont(LegacyFontProviderMod.getInstance().getFontManager().getGlyphFont(FontManager.DEFAULT_FONT_RENDERER_NAME));
    }

    @Inject(method = "startGame",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/IReloadableResourceManager;registerReloadListener(Lnet/minecraft/client/resources/IResourceManagerReloadListener;)V",
                    ordinal = 3))
    private void onInit$(CallbackInfo ci) {
        ((GlyphFontExt) this.standardGalacticFontRenderer).legacyfontprovider$setGlyphFont(LegacyFontProviderMod.getInstance().getFontManager().getGlyphFont(FontManager.GALACTIC_FONT_RENDERER_NAME));
    }


}
