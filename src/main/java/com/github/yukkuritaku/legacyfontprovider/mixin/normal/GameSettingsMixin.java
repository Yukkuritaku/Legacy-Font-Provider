package com.github.yukkuritaku.legacyfontprovider.mixin.normal;

import net.minecraft.client.settings.GameSettings;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.yukkuritaku.legacyfontprovider.font.FontRendererHook;

@Mixin(GameSettings.class)
public class GameSettingsMixin {

    @Shadow
    public boolean forceUnicodeFont;

    @Inject(
        method = "setOptionValue",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;setUnicodeFlag(Z)V"))
    private void onSetOptionValue(GameSettings.Options p_74306_1_, int p_74306_2_, CallbackInfo ci) {
        FontRendererHook.getFontManager()
            .setForceUnicodeFont(this.forceUnicodeFont);
    }
}
