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
package org.netbeans.modules.svg;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 *
 * @author Christian Lenz
 */
public class Utils {

    /**
     * Creates a pre-rendered chest like tile pattern as a BufferedImage.
     *
     * @param tileSize The size of each tile in the pattern.
     * @param transparent Whether to add a transparent overlay to the pattern.
     * @return The generated chest like pattern as a BufferedImage.
     */
    public static BufferedImage createChestTilePattern(int tileSize, boolean transparent) {
        int patternSize = tileSize * 2;
        BufferedImage image = new BufferedImage(patternSize, patternSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        drawSmallChestTilePattern(g2d, 0, 0, patternSize, patternSize, transparent);

        return image;
    }

    /**
     * Draws a repeating chest like tiled pattern within the specified visible
     * rectangle.
     *
     * @param g The graphics object used for drawing.
     * @param visibleRect The visible area to draw the pattern.
     * @param tileSize The size of each tile in the pattern.
     * @param transparent Whether to add a transparent overlay to the pattern.
     */
    public static void drawChestTilePattern(Graphics g, Rectangle visibleRect, int tileSize, boolean transparent) {
        BufferedImage backgroundImage = createChestTilePattern(tileSize, transparent);
        int imgWidth = backgroundImage.getWidth();
        int imgHeight = backgroundImage.getHeight();

        for (int y = visibleRect.y; y < visibleRect.y + visibleRect.height; y += imgHeight) {
            for (int x = visibleRect.x; x < visibleRect.x + visibleRect.width; x += imgWidth) {
                g.drawImage(backgroundImage, x, y, null);
            }
        }
    }

    /**
     * Draws a small chest like tile pattern on a specified area.
     *
     * @param g2d The graphics object used for drawing.
     * @param x The x-coordinate of the top-left corner.
     * @param y The y-coordinate of the top-left corner.
     * @param width The width of the area to draw the pattern.
     * @param height The height of the area to draw the pattern.
     * @param transparentOverlay Whether to add a transparent overlay on top of
     * the pattern.
     */
    public static void drawSmallChestTilePattern(Graphics2D g2d, int x, int y, int width, int height, boolean transparentOverlay) {
        int tileSize = width / 2;

        try {
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    if ((row + col) % 2 == 0) {
                        g2d.setColor(Color.LIGHT_GRAY);
                    } else {
                        g2d.setColor(Color.WHITE);
                    }

                    g2d.fillRect(x + col * tileSize, y + row * tileSize, tileSize, tileSize);
                }
            }

            if (transparentOverlay) {
                g2d.setColor(new Color(0, 0, 0, 128)); // Semi-transparent black overlay (50% opacity)
                g2d.fillRect(x, y, width, height);
            }
        } finally {
            g2d.dispose();
        }
    }
}
