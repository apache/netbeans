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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.Border;

/**
 *
 * @author Jiri Sedlacek
 */
public final class EnhancedLabelRenderer extends LabelRenderer {

    private static final EnhancedInsets EMPTY_INSETS = new EnhancedInsets();

    private EnhancedInsets marginInsets;
    private EnhancedInsets borderInsets;
    private Border border;
    private Color background;


    public void setMargin(Insets marginInsets) {
        if (marginInsets == null) this.marginInsets = EMPTY_INSETS;
        else this.marginInsets = new EnhancedInsets(marginInsets);
    }

    // Overridden for performance reasons.
    public void setBorder(Border border) {
        this.border = border;
        if (border == null) borderInsets = EMPTY_INSETS;
        else borderInsets = new EnhancedInsets(border.getBorderInsets(this));
    }

    // Overridden for performance reasons.
    public Border getBorder() {
        return border;
    }

    // Overridden for performance reasons.
    public void setBackground(Color background) {
        this.background = background;
    }

    // Overridden for performance reasons.
    public Color getBackground() {
        return background;
    }

    private EnhancedInsets getMarginInsets() {
        if (marginInsets == null) marginInsets = EMPTY_INSETS;
        return marginInsets;
    }

    private EnhancedInsets getBorderInsets() {
        if (borderInsets == null) borderInsets = EMPTY_INSETS;
        return borderInsets;
    }


    protected void prePaint(Graphics g, int x, int y) {
        if (background != null) {
            g.setColor(background);
            EnhancedInsets margin = getMarginInsets();
            Dimension size = getPreferredSize();
            g.fillRect(x - margin.left,
                       y - margin.top,
                       size.width + margin.width(),
                       size.height + margin.height());
        }
    }

    protected void postPaint(Graphics g, int x, int y) {
        if (border != null) {
            EnhancedInsets bi = getBorderInsets();
            EnhancedInsets margin = getMarginInsets();
            Dimension size = getPreferredSize();
            border.paintBorder(this, g,
                               x - margin.left - bi.left,
                               y - margin.top - bi.top,
                               size.width + margin.width() + bi.width(),
                               size.height + margin.height() + bi.height());
        }
    }


    private static class EnhancedInsets extends Insets {

        public EnhancedInsets() {
            this(0, 0, 0, 0);
        }
        
        public EnhancedInsets(Insets insets) {
            this(insets.top, insets.left, insets.bottom, insets.right);
        }

        public EnhancedInsets(int top, int left, int bottom, int right) {
            super(top, left, bottom, right);
        }


        public int width() {
            return left + right;
        }

        public int height() {
            return top + bottom;
        }

    }

}
