package com.github.yukkuritaku.legacyfontprovider.mixin.normal.compat.ezstorage;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.zerofall.ezstorage.gui.GuiStorageCore;

@Pseudo
@Mixin(GuiStorageCore.class)
public class GuiStorageCoreMixin {

    @Inject(
        method = "drawGuiContainerForegroundLayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/TextureManager;bindTexture(Lnet/minecraft/util/ResourceLocation;)V"))
    private void onDrawGuiContainerForegroundLayer(int mouseX, int mouseY, CallbackInfo ci) {
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
