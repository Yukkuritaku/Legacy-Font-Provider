package com.github.yukkuritaku.legacyfontprovider.font.glyphs;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EmptyGlyph extends BakedGlyph {

    public static final EmptyGlyph INSTANCE = new EmptyGlyph();

    public EmptyGlyph() {
        super(new ResourceLocation(""), 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public void render(TextureManager textureManager, boolean italic, float x, float y, BufferBuilder builder, float red, float green, float blue, float alpha) {}

    @Override
    public ResourceLocation getTextureLocation() {
        return null;
    }
}
