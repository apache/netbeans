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

package org.netbeans.lib.profiler.ui.components;

import java.awt.*;


/**
 * @author Jiri Sedlacek
 */
public class ColorIcon implements javax.swing.Icon {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Color borderColor = Color.BLACK;
    private Color color = Color.BLACK;
    private int height = 5;
    private int width = 5;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of ColorIcon */
    public ColorIcon() {
    }

    public ColorIcon(Color color) {
        this();
        setColor(color);
    }

    public ColorIcon(Color color, int width, int height) {
        this(color);
        setIconWidth(width);
        setIconHeight(height);
    }

    public ColorIcon(Color color, Color borderColor, int width, int height) {
        this(color, width, height);
        setBorderColor(borderColor);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setIconHeight(int height) {
        this.height = height;
    }

    public int getIconHeight() {
        return height;
    }

    public void setIconWidth(int width) {
        this.width = width;
    }

    public int getIconWidth() {
        return width;
    }

    public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
        if (color != null) {
            g.setColor(color);
            g.fillRect(x, y, width, height);
        }

        if (borderColor != null) {
            g.setColor(borderColor);
            g.drawRect(x, y, width - 1, height - 1);
        }
    }
}
