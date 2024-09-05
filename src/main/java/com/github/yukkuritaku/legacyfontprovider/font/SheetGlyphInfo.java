package com.github.yukkuritaku.legacyfontprovider.font;

public interface SheetGlyphInfo {

    int getWidth();

    int getHeight();

    void uploadGlyph(int xOffset, int yOffset);

    boolean isColored();

    float getOversample();

    default float getLeft() {
        return this.getBearingX();
    }

    default float getRight() {
        return this.getLeft() + (float) this.getWidth() / this.getOversample();
    }

    default float getTop() {
        return 7.0f - this.getBearingY();
    }

    default float getBottom() {
        return this.getTop() + (float) this.getHeight() / this.getOversample();
    }

    default float getBearingX() {
        return 0.0f;
    }

    default float getBearingY() {
        return 7.0f;
    }
}
