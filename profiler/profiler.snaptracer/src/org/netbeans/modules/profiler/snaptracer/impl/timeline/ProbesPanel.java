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

package org.netbeans.modules.profiler.snaptracer.impl.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.netbeans.lib.profiler.charts.ChartConfigurationListener;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.snaptracer.TracerProbe;
import org.netbeans.modules.profiler.snaptracer.impl.icons.TracerIcons;
import org.netbeans.modules.profiler.snaptracer.impl.probes.ProbePresenter;
import org.netbeans.modules.profiler.snaptracer.impl.swing.HeaderButton;
import org.netbeans.modules.profiler.snaptracer.impl.swing.HeaderLabel;
import org.netbeans.modules.profiler.snaptracer.impl.swing.ScrollBar;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
final class ProbesPanel extends JPanel {
    
    private final ListPanel listPanel;
    private final JViewport viewport;
    private final HeaderButton increaseB;
    private final HeaderButton decreaseB;
    private final HeaderButton resetB;

    @NbBundle.Messages({
        "TOOLTIP_IncreaseRowsHeight=Increase rows height",
        "TOOLTIP_DecreaseRowsHeight=Decrease rows height",
        "TOOLTIP_ResetRowsHeight=Reset rows height",
        "LBL_Probes=Probes"
    })
    ProbesPanel(final TimelineSupport support) {
        final TimelineChart chart = support.getChart();

        listPanel = new ListPanel(new VerticalTimelineLayout(chart)) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = Utils.checkedInt(chart.getChartContext().getViewHeight());
                return d;
            }

            @Override
            protected void updateSelection() {
                int count = getComponentCount();
                for (int i = 0; i < count; i++)
                    ((ProbePresenter)getComponent(i)).setSelected(
                            chart.isRowSelected(chart.getRow(i)));
            }
        };

        viewport = new JViewport() {
            @Override
            public String getToolTipText(MouseEvent event) {
                Point p = event.getPoint();
                p.y += getViewPosition().y;
                return listPanel.getToolTipText(p);
            }
        };
        ToolTipManager.sharedInstance().registerComponent(viewport);
        viewport.setOpaque(true);
        viewport.setBackground(new Color(247, 247, 247));
        viewport.setView(listPanel);
        viewport.setViewPosition(new Point(0, 0));
        final ViewportUpdater updater = new ViewportUpdater(viewport);
        chart.addConfigurationListener(new ChartConfigurationListener.Adapter() {
            public void contentsWillBeUpdated(long offsetX, final long offsetY,
                                              double scaleX, double scaleY,
                                              long lastOffsetX, final long lastOffsetY,
                                              double lastScaleX, double lastScaleY) {
                if (lastOffsetY != offsetY)
                    SwingUtilities.invokeLater(updater.forPoint(new Point(
                            0, Utils.checkedInt(offsetY))));
            }
        });
        final JPanel bottomPanel = new JPanel(new GridLayout(1, 3));
        bottomPanel.setPreferredSize(new Dimension(100, new ScrollBar(JScrollBar.
                                     HORIZONTAL).getPreferredSize().height));
        bottomPanel.setOpaque(false);

        increaseB = new HeaderButton(null, Icons.getIcon(TracerIcons.INCREMENT)) {
            protected void performAction(ActionEvent e) {
                chart.increaseRowHeights((e.getModifiers() & Toolkit.getDefaultToolkit().
                                         getMenuShortcutKeyMask()) == 0);
            }
        };
        increaseB.setToolTipText(Bundle.TOOLTIP_IncreaseRowsHeight());
        bottomPanel.add(increaseB);

        decreaseB = new HeaderButton(null, Icons.getIcon(TracerIcons.DECREMENT)) {
            protected void performAction(ActionEvent e) {
                chart.decreaseRowHeights((e.getModifiers() & Toolkit.getDefaultToolkit().
                                         getMenuShortcutKeyMask()) == 0);
            }
        };
        decreaseB.setToolTipText(Bundle.TOOLTIP_DecreaseRowsHeight());
        bottomPanel.add(decreaseB);

        resetB = new HeaderButton(null, Icons.getIcon(TracerIcons.RESET)) {
            protected void performAction(ActionEvent e) {
                chart.resetRowHeights();
            }
        };
        resetB.setToolTipText(Bundle.TOOLTIP_ResetRowsHeight());
        bottomPanel.add(resetB);
        
        setOpaque(false);
        setLayout(new BorderLayout());
        add(new HeaderLabel(Bundle.LBL_Probes()), BorderLayout.NORTH);
        add(viewport, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        chart.addRowListener(new TimelineChart.RowListener() {
            public void rowsAdded(List<TimelineChart.Row> rows) {
                for (TimelineChart.Row row : rows) {
                    TracerProbe probe = support.getProbe(row);
                    listPanel.add(new ProbePresenter(probe, support.
                                  getDescriptor(probe)), row.getIndex());
                }
                listPanel.sync();
                revalidate();
                repaint();
                refreshButtons(true);
            }

            public void rowsRemoved(List<TimelineChart.Row> rows) {
                for (TimelineChart.Row row : rows)
                    listPanel.remove(row.getIndex());
                listPanel.sync();
                revalidate();
                repaint();
                refreshButtons(chart.hasRows());
            }

            public void rowsResized(List<TimelineChart.Row> rows) {
                listPanel.sync();
                revalidate();
                repaint();
            }
        });

        refreshButtons(chart.hasRows());
    }


    @Override
    public void setCursor(Cursor cursor) {
        viewport.setCursor(cursor);
    }

    Component getMouseTarget() {
        return viewport;
    }

    void updateSelection() {
        listPanel.updateSelection();
    }


    private void refreshButtons(boolean enabled) {
        increaseB.setEnabled(enabled);
        decreaseB.setEnabled(enabled);
        resetB.setEnabled(enabled);
    }


    private static class ViewportUpdater implements Runnable {

        private final JViewport viewport;
        private Point point;

        ViewportUpdater(JViewport viewport) { this.viewport = viewport; }

        Runnable forPoint(Point point) { this.point = point; return this; }

        public void run() { viewport.setViewPosition(point); }

    }


    private static class ListPanel extends JPanel {

        ListPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        protected void updateSelection() {
        }
        
        String getToolTipText(Point p) {
            String tooltip = null;
            Component c = getComponentAt(p);
            if (c instanceof JComponent)
                tooltip = (String)((JComponent)c).getClientProperty("ToolTipHelper"); // NOI18N
            return tooltip;
        }

        private void sync() {
            doLayout();
            repaint();
        }

    }

}
