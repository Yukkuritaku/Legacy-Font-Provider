package com.github.yukkuritaku.legacyfontprovider.mixin.normal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.client.settings.GameSettings;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.yukkuritaku.legacyfontprovider.ext.MinecraftExt;
import com.github.yukkuritaku.legacyfontprovider.font.FontManager;
import com.github.yukkuritaku.legacyfontprovider.font.FontProviderRenderer;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftExt {

    @Shadow
    public abstract TextureManager getTextureManager();

    // isUnicode
    @Shadow
    public abstract boolean func_152349_b();

    @Shadow
    public GameSettings gameSettings;
    @Shadow
    private LanguageManager mcLanguageManager;
    @Shadow
    private IReloadableResourceManager mcResourceManager;
    @Shadow
    @Final
    private IMetadataSerializer metadataSerializer_;

    @Unique
    private FontManager legacyfontprovider$fontManager;
    @Unique
    private FontProviderRenderer legacyfontprovider$fontProviderRenderer;
    @Unique
    private FontProviderRenderer legacyfontprovider$fishyFontProviderRenderer;

    @Inject(
        method = "startGame",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;<init>(Lnet/minecraft/client/settings/GameSettings;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/TextureManager;Z)V",
            ordinal = 0))

    private void onStartGame_normal_font(CallbackInfo ci) {
        this.legacyfontprovider$fontManager = new FontManager(this.getTextureManager(), this.func_152349_b());
        this.mcResourceManager.registerReloadListener(this.legacyfontprovider$fontManager);
        this.legacyfontprovider$fontProviderRenderer = this.legacyfontprovider$fontManager
            .getFontProvider(FontManager.DEFAULT_FONT_RENDERER_NAME);
        if (this.gameSettings.language != null) {
            this.legacyfontprovider$fontProviderRenderer
                .setBidiFlag(this.mcLanguageManager.isCurrentLanguageBidirectional());
        }
    }

    @Inject(
        method = "startGame",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/FontRenderer;<init>(Lnet/minecraft/client/settings/GameSettings;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/TextureManager;Z)V",
            ordinal = 1))

    private void onStartGame_enchantment_font(CallbackInfo ci) {
        this.legacyfontprovider$fishyFontProviderRenderer = this.legacyfontprovider$fontManager
            .getFontProvider(FontManager.GALACTIC_FONT_RENDERER_NAME);
    }

    @Override
    public FontManager getFontManager() {
        return this.legacyfontprovider$fontManager;
    }

    @Override
    public FontProviderRenderer getFontProviderRenderer() {
        return this.legacyfontprovider$fontProviderRenderer;
    }

    @Override
    public FontProviderRenderer getFishyFontProviderRenderer() {
        return this.legacyfontprovider$fishyFontProviderRenderer;
    }
}
