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
 *
 * @author Jiri Sedlacek
 */
public class VerticalLayout implements LayoutManager {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int hgap;
    private int vgap;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public VerticalLayout() {
        this(10, 10);
    }

    public VerticalLayout(int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getHGap() {
        return hgap;
    }

    public int getVGap() {
        return vgap;
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void layoutContainer(Container container) {
        synchronized (container.getTreeLock()) {
            Insets insets = container.getInsets();

            int x = insets.left + hgap;
            int y = insets.top + vgap;
            int w = container.getSize().width - insets.left - insets.right - (2 * hgap);
            int h;

            for (int i = 0; i < container.getComponentCount(); i++) {
                Component component = container.getComponent(i);

                if (component.isVisible()) {
                    h = component.getPreferredSize().height;
                    component.setBounds(x, y, w, h);
                    y += (h + vgap);
                }
            }
        }
    }

    public Dimension minimumLayoutSize(Container container) {
        return preferredLayoutSize(container);
    }

    public Dimension preferredLayoutSize(Container container) {
        synchronized (container.getTreeLock()) {
            Insets insets = container.getInsets();

            int width = insets.left + insets.right + (2 * hgap);
            int height = insets.top + insets.bottom + vgap;

            for (int i = 0; i < container.getComponentCount(); i++) {
                Component component = container.getComponent(i);

                if (component.isVisible()) {
                    Dimension preferredDim = component.getPreferredSize();
                    width = Math.max(width, preferredDim.width + insets.left + insets.right + (2 * hgap));
                    height += (preferredDim.height + vgap);
                }
            }

            return new Dimension(width, height);
        }
    }

    public void removeLayoutComponent(Component comp) {
    }

    public String toString() {
        return getClass().getName() + "[hgap=" + hgap + ", vgap=" + vgap + "]"; // NOI18N
    }
}
