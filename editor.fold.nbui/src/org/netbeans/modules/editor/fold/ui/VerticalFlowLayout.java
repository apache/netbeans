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

    final private Set<Component> components = new LinkedHashSet<Component>();
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
