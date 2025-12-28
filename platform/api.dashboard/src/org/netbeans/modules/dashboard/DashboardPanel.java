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
package org.netbeans.modules.dashboard;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.netbeans.spi.dashboard.DashboardDisplayer;

/**
 *
 */
final class DashboardPanel extends JPanel {

    private static final int WIDGET_WIDTH = 240;
    private static final int WIDGET_HEIGHT = 260;
    private static final int WIDGET_GAP = 20;
    private static final int MAX_COLUMNS = 3;

    private final List<WidgetPanel> widgetPanels;
    private final GridLayout layout;

    private boolean showing;

    DashboardPanel(DashboardDisplayer displayer,
            List<DashboardDisplayer.WidgetReference> widgetRefs) {
        widgetPanels = new ArrayList<>(widgetRefs.size());
        layout = new GridLayout(0, MAX_COLUMNS, WIDGET_GAP, WIDGET_GAP);
        setLayout(layout);
        setOpaque(false);
        for (var ref : widgetRefs) {
            WidgetPanel w = WidgetPanel.create(displayer, ref);
            w.setPreferredSize(new Dimension(WIDGET_WIDTH, WIDGET_HEIGHT));
            add(w);
            widgetPanels.add(w);
        }
        addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
            @Override
            public void ancestorResized(HierarchyEvent e) {
                reflowGrid();
            }

        });
    }

    void notifyShowing() {
        if (!showing) {
            showing = true;
            widgetPanels.forEach(WidgetPanel::notifyShowing);
        }
    }

    void notifyHidden() {
        if (showing) {
            showing = false;
            widgetPanels.forEach(WidgetPanel::notifyHidden);
        }
    }

    private void reflowGrid() {
        Container parent = SwingUtilities.getAncestorOfClass(JViewport.class, this);
        if (parent == null) {
            parent = getParent();
        }
        if (parent instanceof JComponent jparent) {
            int currentColumns = layout.getColumns();
            int currentWidth = jparent.getVisibleRect().width;
            int widgetSpace = WIDGET_WIDTH + (2 * WIDGET_GAP);
            int requiredColumns;
            if (currentWidth > 1) {
                requiredColumns = Math.min(MAX_COLUMNS,
                        Math.max(1, currentWidth / widgetSpace));
            } else {
                requiredColumns = MAX_COLUMNS;
            }
            if (requiredColumns != currentColumns) {
                layout.setColumns(requiredColumns);
                revalidate();
            }
        }

    }

}
