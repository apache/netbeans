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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import org.netbeans.lib.profiler.charts.ChartContext;
import org.netbeans.lib.profiler.charts.ChartOverlay;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItem;
import org.netbeans.modules.profiler.snaptracer.impl.swing.ColorIcon;
import org.netbeans.modules.profiler.snaptracer.impl.swing.LabelRenderer;
import org.netbeans.modules.profiler.snaptracer.impl.swing.LegendFont;

/**
 *
 * @author Jiri Sedlacek
 */
final class TimelineLegendOverlay extends ChartOverlay {

    private final TimelineChart chart;
    private final LabelRenderer painter;


    TimelineLegendOverlay(TimelineChart chart) {
        this.chart = chart;

        painter = new LabelRenderer();
        painter.setFont(new LegendFont());

        int size = painter.getFont().getSize() - 3;
        ColorIcon.setup(size, size,
                LegendFont.FOREGROUND_COLOR, LegendFont.BACKGROUND_COLOR);
    }


    private void setupPainter(String text, Color color) {
        painter.setText(text);
        painter.setIcon(ColorIcon.fromColor(color));
    }


    public void paint(Graphics g) {
        int rowsCount = chart.getRowsCount();
        for (int i = 0; i < rowsCount; i++) {
            TimelineChart.Row row = chart.getRow(i);
            ChartContext rowContext = row.getContext();
            SynchronousXYItem[] rowItems = row.getItems();

            int x = 3;
            int y = -1;

            for (SynchronousXYItem rowItem : rowItems) {
                TimelineXYPainter itemPainter =
                        (TimelineXYPainter)chart.getPaintersModel().getPainter(rowItem);
                if (itemPainter.isPainting()) {
                    setupPainter(rowItem.getName(), itemPainter.getDefiningColor());
                    Dimension pd = painter.getPreferredSize();
                    if (y == -1)
                        y = Utils.checkedInt(rowContext.getViewportOffsetY()) +
                            rowContext.getViewportHeight() - pd.height - 1;
                    paint(g, x, y);
                    x += pd.width + 10;
                }
            }
        }
    }

    private void paint(Graphics g, int x, int y) {
        painter.setLocation(x, y + 1);
        painter.setForeground(LegendFont.BACKGROUND_COLOR);
        painter.paint(g);

        painter.setLocation(x, y);
        painter.setForeground(LegendFont.FOREGROUND_COLOR);
        painter.setIcon(ColorIcon.BOTTOM_SHADOW);
        painter.paint(g);
    }

    // --- Peformance tweaks ---------------------------------------------------

    public void invalidate() {}

    public void update(Graphics g) {}

}
