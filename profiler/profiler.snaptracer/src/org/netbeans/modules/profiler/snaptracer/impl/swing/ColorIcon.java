/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.profiler.snaptracer.impl.swing;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jiri Sedlacek
 */
public final class ColorIcon implements javax.swing.Icon {

    private static int WIDTH = 8;
    private static int HEIGHT = 8;
    private static Color BACKGROUND_COLOR = Color.WHITE;
    private static Color FOREGROUND_COLOR = Color.BLACK;

    public static final ColorIcon BOTTOM_SHADOW = new ColorIcon(null, true);

    private final boolean shadow;
    private final Color color;

    private static final Map<Color, ColorIcon> icons = new HashMap<>();


    private ColorIcon(Color color) {
        this(color, false);
    }

    private ColorIcon(Color color, boolean shadow) {
        this.color = color;
        this.shadow = shadow;
    }


    public static void setup(int width, int height, Color foreground, Color background) {
        WIDTH = width;
        HEIGHT = height;
        BACKGROUND_COLOR = background;
        FOREGROUND_COLOR = foreground;
        icons.clear();
    }

    public static ColorIcon fromColor(Color color) {
        ColorIcon icon = icons.get(color);
        if (icon == null) {
            icon = new ColorIcon(color);
            icons.put(color, icon);
        }
        return icon;
    }


    public int getIconWidth() {
        return WIDTH;
    }

    public int getIconHeight() {
        return HEIGHT;
    }

    public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
        if (shadow) {
            g.setColor(BACKGROUND_COLOR);
            g.drawLine(x, y + HEIGHT + 1, x + WIDTH - 1, y + HEIGHT + 1);
        } else {
            g.setColor(color);
            g.fillRect(x, y, WIDTH, HEIGHT);
            g.setColor(FOREGROUND_COLOR);
            g.drawRect(x, y, WIDTH - 1, HEIGHT - 1);
        }
    }
}
