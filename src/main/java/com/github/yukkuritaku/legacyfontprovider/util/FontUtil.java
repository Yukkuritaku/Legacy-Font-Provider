package com.github.yukkuritaku.legacyfontprovider.util;

import net.minecraft.client.renderer.WorldRenderer;

public class FontUtil {

    public static class RenderPair {
        public final float red;
        public final float green;
        public final float blue;
        public final float alpha;
        public float posX;
        public float width;

        public RenderPair(float posX, float width, float red, float green, float blue, float alpha) {
            this.posX = posX;
            this.width = width;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }
    }

    public static class Entry {
        protected final float x1;
        protected final float y1;
        protected final float x2;
        protected final float y2;
        protected final float red;
        protected final float green;
        protected final float blue;
        protected final float alpha;

        public Entry(float x1, float y1, float x2, float y2, float red, float green, float blue, float alpha) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        public void pipe(WorldRenderer buffer) {
            buffer.pos(this.x1, this.y1, 0.0).color(this.red, this.green, this.blue, this.alpha).endVertex();
            buffer.pos(this.x2, this.y1, 0.0).color(this.red, this.green, this.blue, this.alpha).endVertex();
            buffer.pos(this.x2, this.y2, 0.0).color(this.red, this.green, this.blue, this.alpha).endVertex();
            buffer.pos(this.x1, this.y2, 0.0).color(this.red, this.green, this.blue, this.alpha).endVertex();
        }
    }
}
