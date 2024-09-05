package com.github.yukkuritaku.legacyfontprovider.font.glyphs;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

public class EmptyGlyph extends BakedGlyph {

    public static final EmptyGlyph INSTANCE = new EmptyGlyph();

    public EmptyGlyph() {
        super(new ResourceLocation(""), 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Override
    public void render(boolean italic, float x, float y, Tessellator tessellator, float red, float green, float blue,
        float alpha, int packedLight) {}

    @Nullable
    @Override
    public ResourceLocation getTextureLocation() {
        return null;
    }
}
