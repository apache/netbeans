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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.Box;

/**
 *
 * @author Jiri Sedlacek
 */
public final class HorizontalLayout implements LayoutManager {

    private final boolean proportionalHeight;
    private final int hGap;


    public HorizontalLayout(boolean proportionalHeight) {
        this(proportionalHeight, 0);
    }

    public HorizontalLayout(boolean proportionalHeight, int hGap) {
        this.proportionalHeight = proportionalHeight;
        this.hGap = hGap;
    }


    public void layoutContainer(final Container parent) {
        final Insets insets = parent.getInsets();
        int posX = insets.left;
        final int posY = insets.top;
        final int height = parent.getHeight() - insets.top - insets.bottom;

        for (Component comp : parent.getComponents()) {
            if (comp.isVisible()) {
                Dimension pref = comp.getPreferredSize();
                if (proportionalHeight) {
                    int h = Math.min(pref.height, height);
                    int o = (height - h) / 2;
                    comp.setBounds(posX, posY + o, pref.width, h);
                } else {
                    comp.setBounds(posX, posY, pref.width, height);
                }
                posX += hGap;
                posX += pref.width;
            }
        }
    }

    public Dimension minimumLayoutSize(final Container parent) {
        final Insets insets = parent.getInsets();
        final Dimension d = new Dimension(insets.left + insets.right,
                                          insets.top + insets.bottom);
        int maxHeight = 0;
        int visibleCount = 0;

        for (Component comp : parent.getComponents()) {
            if (comp.isVisible() && !(comp instanceof Box.Filler)) {
                final Dimension size = comp.getPreferredSize();
                maxHeight = Math.max(maxHeight, size.height);
                d.width += size.width;
                visibleCount++;
            }
        }

        d.width += (visibleCount - 1) * hGap;
        d.height += maxHeight;

        return d;
    }

    public Dimension preferredLayoutSize(final Container parent) {
        final Insets insets = parent.getInsets();
        final Dimension d = new Dimension(insets.left + insets.right,
                                          insets.top + insets.bottom);
        int maxHeight = 0;
        int visibleCount = 0;

        for (Component comp : parent.getComponents()) {
            if (comp.isVisible()) {
                final Dimension size = comp.getPreferredSize();
                maxHeight = Math.max(maxHeight, size.height);
                d.width += size.width;
                visibleCount++;
            }
        }

        d.width += (visibleCount - 1) * hGap;
        d.height += maxHeight;

        return d;
    }


    public void addLayoutComponent(final String name, final Component comp) {}

    public void removeLayoutComponent(final Component comp) {}

}
