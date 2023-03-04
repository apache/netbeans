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
package org.netbeans.modules.editor.fold.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Simple layout, which fills the space vertically, then overflows to the next column.
 *
 * @author sdedic
 */
final class VerticalFlowLayout implements LayoutManager2 {

    private final Set<Component> components = new LinkedHashSet<Component>();
    private int hgap = 0;
    private int vgap = 0;

    public void setHGap(int hgap) {
        this.hgap = hgap;
    }

    public void setVGap(int vgap) {
        this.vgap = vgap;
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        this.components.add(comp);
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0;
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0;
    }

    @Override
    public void invalidateLayout(Container target) {
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        this.components.add(comp);
    }

    private Dimension computeDimension(Container parent, int type) {
        Insets insets = parent.getInsets();
        int x = insets.left;
        int y = insets.top;
        int columnWidth = 0;
        // int limitHeight = parent.getHeight() - insets.bottom;
        int maxY = 0;

        for (Component c : this.components) {
            if (c.isVisible()) {
                Dimension d;

                switch (type) {
                    case 0:
                        d = c.getPreferredSize();
                        break;
                    case 1:
                        d = c.getMinimumSize();
                        break;
                    default:
                        d = c.getMaximumSize();
                        break;
                }
                columnWidth = Math.max(columnWidth, d.width);
                /*
                if (limitHeight != 0 && y + d.height >= limitHeight) {
                    x += columnWidth + this.hgap;
                    y = insets.top;
                    columnWidth = d.width;
                }
                */
                y += d.height;
                maxY = Math.max(y, maxY);
                y += this.vgap;
            }
        }
        x += columnWidth;
        return new Dimension(x, maxY);
    }

    @Override
    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int x = insets.left;
        int y = insets.top;
        int columnWidth = 0;
        int limitHeight = parent.getHeight() - insets.bottom;
        for (Component c : this.components) {
            if (c.isVisible()) {
                Dimension d = c.getPreferredSize();
                columnWidth = Math.max(columnWidth, d.width);
                if (y + d.height >= limitHeight) {
                    x += columnWidth + this.hgap;
                    y = insets.top;
                }
                c.setBounds(x, y, d.width, d.height);
                y += d.height + this.vgap;
            }
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return computeDimension(parent, 1);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return computeDimension(parent, 1);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return computeDimension(target, 2);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        this.components.remove(comp);
    }
}
