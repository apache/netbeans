/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.acp.ui.chat;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.swing.ImageIcon;

final class AcpChatImage {

    private static final int MAX_ENCODED_LENGTH = 12 * 1024 * 1024;
    private static final long MAX_PIXELS = 16_000_000;
    private static final int DISPLAY_WIDTH = 400;
    private static final int DISPLAY_HEIGHT = 300;

    private AcpChatImage() {
    }

    static ImageIcon decode(String data, String mimeType) throws IOException {
        String format = switch (mimeType == null ? "image/png" : mimeType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpeg";
            case "image/gif" -> "gif";
            default -> throw new IOException("Unsupported image type");
        };
        if (data == null || data.isEmpty() || data.length() > MAX_ENCODED_LENGTH) {
            throw new IOException("Invalid image length");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(data);
        } catch (IllegalArgumentException ex) {
            throw new IOException("Invalid base64 image", ex);
        }
        try (var input = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IOException("Unknown image format");
            }
            ImageReader reader = readers.next();
            try {
                if (!format.equalsIgnoreCase(reader.getFormatName())) {
                    throw new IOException("Image format does not match its MIME type");
                }
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_PIXELS) {
                    throw new IOException("Invalid image dimensions");
                }
                BufferedImage decoded = reader.read(0);
                return new ImageIcon(scale(decoded));
            } finally {
                reader.dispose();
            }
        }
    }

    private static BufferedImage scale(BufferedImage image) {
        double ratio = Math.min((double) DISPLAY_WIDTH / image.getWidth(), (double) DISPLAY_HEIGHT / image.getHeight());
        if (ratio >= 1) {
            return image;
        }
        int width = Math.max(1, (int) (image.getWidth() * ratio));
        int height = Math.max(1, (int) (image.getHeight() * ratio));
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = resized.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(image, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return resized;
    }
}
