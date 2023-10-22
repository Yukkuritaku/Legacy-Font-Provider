package com.github.yukkuritaku.legacyfontprovider.font.glyphs;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
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

    public BakedGlyph(ResourceLocation textureLocation, float u0, float u1, float v0, float v1, float left, float right, float up, float down) {
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

    public void render(TextureManager textureManager, boolean italic, float x, float y, WorldRenderer builder, float red, float green, float blue, float alpha) {
        float left = x + this.left;
        float right = x + this.right;
        float upOffset = this.up - 3.0f;
        float downOffset = this.down - 3.0f;
        float up = y + upOffset;
        float down = y + downOffset;
        float m = italic ? 1.0f - 0.25f * upOffset : 0.0f;
        float n = italic ? 1.0f - 0.25f * downOffset : 0.0f;
        //GL11.glColor4f(red, green, blue, alpha);
        GL11.glTexCoord2f(this.u0, this.v0);
        GL11.glVertex3f(left + m, up, 0.0f);
        GL11.glTexCoord2f(this.u0, this.v1);
        GL11.glVertex3f(left + n, down, 0.0f);
        GL11.glTexCoord2f(this.u1, this.v1);
        GL11.glVertex3f(right + n, down, 0.0f);
        GL11.glTexCoord2f(this.u1, this.v0);
        GL11.glVertex3f(right + m, up, 0.0f);


        /*builder.pos(left + m, up, 0.0f).tex(this.u0, this.v0).color(red, green, blue, alpha).endVertex();
        builder.pos(left + n, down, 0.0f).tex(this.u0, this.v1).color(red, green, blue, alpha).endVertex();
        builder.pos(right + n, down, 0.0f).tex(this.u1, this.v1).color(red, green, blue, alpha).endVertex();
        builder.pos(right + m, up, 0.0f).tex(this.u1, this.v0).color(red, green, blue, alpha).endVertex();*/
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }
}
