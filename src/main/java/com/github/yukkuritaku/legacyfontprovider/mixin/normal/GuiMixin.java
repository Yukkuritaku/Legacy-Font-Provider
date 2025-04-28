package com.github.yukkuritaku.legacyfontprovider.mixin.normal;

import net.minecraft.client.gui.Gui;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(Gui.class)
public class GuiMixin {

    /*
     * @Inject(method = "drawTexturedModalRect", at = @At(value = "INVOKE", target =
     * "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V", ordinal = 0))
     * private void onDrawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height,
     * CallbackInfo ci){
     * Tessellator.instance.setColorRGBA_F(1.0f, 1.0f, 1.0f, 1.0f);
     * }
     * @Inject(method = "drawTexturedModelRectFromIcon", at = @At(value = "INVOKE", target =
     * "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V", ordinal = 0))
     * private void onDrawTexturedModelRectFromIcon(int x, int y, IIcon icon, int width, int height, CallbackInfo ci){
     * Tessellator.instance.setColorRGBA_F(1.0f, 1.0f, 1.0f, 1.0f);
     * }
     * @Inject(method = "func_146110_a", at = @At(value = "INVOKE", target =
     * "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V", ordinal = 0))
     * private static void onFunc_146110_a(int x, int y, float u, float v, int width, int height, float textureWidth,
     * float textureHeight, CallbackInfo ci){
     * Tessellator.instance.setColorRGBA_F(1.0f, 1.0f, 1.0f, 1.0f);
     * }
     * @Inject(method = "func_152125_a", at = @At(value = "INVOKE", target =
     * "Lnet/minecraft/client/renderer/Tessellator;addVertexWithUV(DDDDD)V", ordinal = 0))
     * private static void onFunc_152125_a(int x, int y, float u, float v, int uWidth, int vHeight, int width, int
     * height, float tileWidth, float tileHeight, CallbackInfo ci){
     * Tessellator.instance.setColorRGBA_F(1.0f, 1.0f, 1.0f, 1.0f);
     * }
     */
}
