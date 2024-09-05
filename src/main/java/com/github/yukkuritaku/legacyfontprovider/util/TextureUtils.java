package com.github.yukkuritaku.legacyfontprovider.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

public class TextureUtils {

    public static BufferedImage readBufferedImage(InputStream imageStream) throws IOException {
        BufferedImage bufferedimage;
        try {
            bufferedimage = ImageIO.read(imageStream);
        } finally {
            IOUtils.closeQuietly(imageStream);
        }

        return bufferedimage;
    }
}
