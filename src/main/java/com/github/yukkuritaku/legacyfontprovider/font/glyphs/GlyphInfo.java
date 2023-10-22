package com.github.yukkuritaku.legacyfontprovider.font.glyphs;

public interface GlyphInfo extends Glyph{

    int getWidth();
    int getHeight();
    void uploadGlyph(int xOffset, int yOffset);
    boolean isColored();
    float getOversample();

    default float getXStart(){
        return this.getBearingX();
    }

    default float getXEnd(){
        return this.getXStart() + this.getWidth() / this.getOversample();
    }

    default float getYStart(){
        return this.getBearingY();
    }

    default float getYEnd(){
        return this.getYStart() + this.getHeight() / this.getOversample();
    }

    default float getBearingY(){
        return 3.0f;
    }

}
