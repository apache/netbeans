/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ant.debugger;

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

    final private Set<Component> components = new LinkedHashSet<Component>();
    
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
        return new Dimension(cols * getMaxCellWidth() + 2, height + 2);
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