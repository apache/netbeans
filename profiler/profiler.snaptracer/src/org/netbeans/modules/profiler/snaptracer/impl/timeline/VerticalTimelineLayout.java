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

package org.netbeans.modules.profiler.snaptracer.impl.timeline;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import org.netbeans.lib.profiler.charts.ChartContext;
import org.netbeans.lib.profiler.charts.swing.Utils;

/**
 *
 * @author Jiri Sedlacek
 */
final class VerticalTimelineLayout implements LayoutManager2 {

    private final TimelineChart chart;


    // --- Constructor ---------------------------------------------------------

    VerticalTimelineLayout(TimelineChart chart) {
        this.chart = chart;
    }


    // --- Public API ----------------------------------------------------------

    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    public Dimension maximumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, Utils.checkedInt(chart.getChartContext().getViewHeight()));

        for (int i = 0; i < parent.getComponentCount(); i++)
            dim.width = Math.max(dim.width, parent.getComponent(i).
                                     getPreferredSize().width);

        return dim;
    }

    public void layoutContainer(Container parent) {
        int width = parent.getWidth();
        for (int i = 0; i < parent.getComponentCount(); i++) {
                ChartContext context = chart.getRow(i).getContext();
                parent.getComponent(i).setBounds(0, Utils.checkedInt(context.getViewportOffsetY() + chart.getOffsetY()),
                                                 width, context.getViewportHeight());
        }
    }


    // --- Implicit implementation ---------------------------------------------

    public void addLayoutComponent(Component comp, Object constraints) {}

    public void addLayoutComponent(String name, Component comp) {}

    public void removeLayoutComponent(Component comp) {}

    public float getLayoutAlignmentX(Container target) { return 0.5f; }

    public float getLayoutAlignmentY(Container target) { return 0.5f; }

    public void invalidateLayout(Container target) {}

}
