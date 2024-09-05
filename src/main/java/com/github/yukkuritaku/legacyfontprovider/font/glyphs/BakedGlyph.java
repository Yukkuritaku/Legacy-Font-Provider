package com.github.yukkuritaku.legacyfontprovider.font.glyphs;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

public class BakedGlyph {

    private final ResourceLocation textureLocation;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private final float left;
    private final float right;
    private final float up;
    private final float down;

    public BakedGlyph(ResourceLocation textureLocation, float u0, float u1, float v0, float v1, float left, float right,
        float up, float down) {
        this.textureLocation = textureLocation;
        this.u0 = u0;
        this.u1 = u1;
        this.v0 = v0;
        this.v1 = v1;
        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
    }

    public void render(boolean italic, float x, float y, Tessellator tessellator, float red, float green, float blue,
        float alpha, int packedLight) {
        float left = x + this.left;
        float right = x + this.right;
        float up = y + this.up;
        float down = y + this.down;
        float upOffset = italic ? 1.0F - 0.25F * this.up : 0.0F;
        float downOffset = italic ? 1.0F - 0.25F * this.down : 0.0F;
        tessellator.setColorRGBA_F(red, green, blue, alpha);
        tessellator.addVertexWithUV(left + upOffset, up, 0.0, this.u0, this.v0);
        tessellator.addVertexWithUV(left + downOffset, down, 0.0, this.u0, this.v1);
        tessellator.addVertexWithUV(right + downOffset, down, 0.0, this.u1, this.v1);
        tessellator.addVertexWithUV(right + upOffset, up, 0.0, this.u1, this.v0);
    }

    @Nullable
    public ResourceLocation getTextureLocation() {
        return this.textureLocation;
    }
}
