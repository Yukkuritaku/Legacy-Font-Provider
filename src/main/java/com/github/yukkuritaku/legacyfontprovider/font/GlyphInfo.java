package com.github.yukkuritaku.legacyfontprovider.font;

import java.util.function.Function;

import com.github.yukkuritaku.legacyfontprovider.font.glyphs.BakedGlyph;
import com.github.yukkuritaku.legacyfontprovider.font.glyphs.EmptyGlyph;

public interface GlyphInfo {

    float getAdvance();

    default float getAdvance(boolean bold) {
        return this.getAdvance() + (bold ? this.getBoldOffset() : 0.0f);
    }

    default float getBoldOffset() {
        return 1.0f;
    }

    default float getShadowOffset() {
        return 1.0f;
    }

    BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> provider);

    interface SpaceGlyphInfo extends GlyphInfo {

        @Override
        default BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> provider) {
            return EmptyGlyph.INSTANCE;
        }
    }
}
