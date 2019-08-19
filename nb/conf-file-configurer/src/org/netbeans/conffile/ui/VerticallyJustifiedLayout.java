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
package org.netbeans.conffile.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * Just a layout manager arranges components in a vertical column giving them
 * all the same height.
 *
 * @author Tim Boudreau
 */
final class VerticallyJustifiedLayout implements LayoutManager {

    private final int gap;
    private final int vpad;

    VerticallyJustifiedLayout(int gap, int vpad) {
        this.gap = gap;
        this.vpad = vpad;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        // do nothing
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        // do nothing
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Dimension maxes = new Dimension();
        Component[] comps = parent.getComponents();
        for (Component c : comps) {
            Dimension d = c.getPreferredSize();
            maxes.width = Math.max(d.width, maxes.width);
            maxes.height = Math.max(d.height, maxes.height);
        }
        maxes.height *= comps.length;
        Insets ins = parent.getInsets();
        maxes.width += ins.left + ins.right;
        maxes.height += ins.top + ins.bottom + vpad;
        maxes.height += Math.max(0, comps.length - 1) * gap;
        return maxes;
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    @Override
    public void layoutContainer(Container parent) {
        Dimension maxes = new Dimension();
        Component[] comps = parent.getComponents();
        // justify
        for (Component c : comps) {
            Dimension d = c.getPreferredSize();
            maxes.width = Math.max(d.width, maxes.width);
            maxes.height = Math.max(d.height, maxes.height);
        }
        // position
        Insets ins = parent.getInsets();
        int left = ins.left;
        int top = ins.top;
        for (int i = 0; i < comps.length; i++) {
            Component comp = comps[i];
            comp.setBounds(left, top, maxes.width, maxes.height);
            top += maxes.height + gap;
        }
    }

}
