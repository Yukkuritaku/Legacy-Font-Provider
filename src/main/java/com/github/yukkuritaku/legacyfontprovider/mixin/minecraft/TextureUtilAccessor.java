package com.github.yukkuritaku.legacyfontprovider.mixin.minecraft;

import net.minecraft.client.renderer.texture.TextureUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.awt.image.BufferedImage;

@Mixin(TextureUtil.class)
public interface TextureUtilAccessor {

    @Invoker
    static void invokeUploadTextureImageSubImpl(BufferedImage p_110993_0_, int p_110993_1_, int p_110993_2_, boolean p_110993_3_, boolean p_110993_4_){
        throw new AssertionError();
    }

}
