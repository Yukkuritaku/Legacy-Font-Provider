package com.github.yukkuritaku.legacyfontprovider.mixin.normal;

import java.awt.image.BufferedImage;

import net.minecraft.client.renderer.texture.TextureUtil;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TextureUtil.class)
public interface TextureUtilAccessor {

    @Invoker
    static void invokeUploadTextureImageSubImpl(BufferedImage image, int xOffset, int yOffset, boolean blur,
        boolean clamp) {
        throw new AssertionError();
    }

}
