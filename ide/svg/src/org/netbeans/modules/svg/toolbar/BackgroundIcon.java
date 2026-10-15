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
package org.netbeans.modules.svg.toolbar;

import org.netbeans.modules.svg.BackgroundMode;

import java.awt.*;
import javax.swing.*;
import org.netbeans.modules.svg.Utils;

/**
 *
 * @author Christian Lenz
 */
public class BackgroundIcon implements Icon {

    private final BackgroundMode bgMode;
    private final Color defaultColor;

    public BackgroundIcon(BackgroundMode bgMode, Color defaultColor) {
        this.bgMode = bgMode;
        this.defaultColor = defaultColor;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;

        int width = getIconWidth();
        int height = getIconHeight();

        switch (bgMode) {
            case BLACK -> {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(x, y, width, height);
            }
            case WHITE -> {
                g2d.setColor(Color.WHITE);
                g2d.fillRect(x, y, width, height);
            }
            case TRANSPARENT, DARK_TRANSPARENT ->
                Utils.drawSmallChestTilePattern(g2d, x, y, width, height, bgMode == BackgroundMode.DARK_TRANSPARENT);
            case DEFAULT -> {
                g2d.setColor(defaultColor);
                g2d.fillRect(x, y, width, height);
            }
        }
    }

    @Override
    public int getIconWidth() {
        return 16;
    }

    @Override
    public int getIconHeight() {
        return 16;
    }
}
