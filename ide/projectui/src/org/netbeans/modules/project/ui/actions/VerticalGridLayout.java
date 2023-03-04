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
package org.netbeans.modules.project.ui.actions;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.LinkedHashSet;
import java.util.Set;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author mkozeny
 */
public class VerticalGridLayout implements LayoutManager2 {
    
    public VerticalGridLayout() {
        super();
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated != null) {
            Rectangle screenParams = Utilities.getUsableScreenBounds(activated.getGraphicsConfiguration());
            //half of the size is used, b/c sometimes it is submenu not visible at the top
            this.screenHeight = (int)(screenParams.height * 0.5);
        } else {
            this.screenHeight = (int)(WindowManager.getDefault().getMainWindow().getSize().height * 0.5);
        }
    }

    private final int screenHeight;

    private final Set<Component> components = new LinkedHashSet<Component>();
    
    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        this.components.add(comp);
    }

    /* these 3 methods need to be overridden properly */
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

    @Override
    public void layoutContainer(Container parent) {
        int x = 0;
        int y = 0;
        int cellWidth = getMaxCellWidth();
        for (Component c : this.components) {
            if (c.isVisible()) {
                Dimension d = c.getPreferredSize();
                if (y + d.height > parent.getHeight()) {
                    x += cellWidth;
                    y = 0;
                }
                c.setBounds(x + 1, y + 1, cellWidth, d.height);
                y += d.height;
            }
        }
    }

    /* these 3 methods need to be overridden properly */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(0, 0);
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return layoutSize(parent);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return layoutSize(target);
    }

    private Dimension layoutSize(Container target) {
        int cols = 1;
        int height;
        int componentCount = this.components.size();
        int itemsPerColumn = this.screenHeight / getMaxCellHeight();
        if (componentCount > itemsPerColumn) {
            cols = componentCount / itemsPerColumn;
            if (componentCount % itemsPerColumn != 0) {
                cols++;
            }
            height = itemsPerColumn * getMaxCellHeight();
        } else {
            height = getMenuItemsHeight();
        }
        return new Dimension(cols * getMaxCellWidth() + 2, height + 4);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        this.components.remove(comp);
    }

    private int getMaxCellHeight() {
        int cellHeight = 0;
        for (Component c : this.components) {
            if (c.getPreferredSize().height > cellHeight) {
                cellHeight = c.getPreferredSize().height;
            }
        }
        return cellHeight;
    }

    private int getMaxCellWidth() {
        int cellWidth = 0;
        for (Component c : this.components) {
            if (c.getPreferredSize().width > cellWidth) {
                cellWidth = c.getPreferredSize().width;
            }
        }
        return cellWidth;
    }
    
    private int getMenuItemsHeight() {
        int height = 0;
        for (Component c : this.components) {
            height += c.getPreferredSize().height;
        }
        return height;
    }
 }