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
import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel;
import org.netbeans.modules.profiler.snaptracer.impl.swing.ColorIcon;
import org.netbeans.modules.profiler.snaptracer.impl.swing.LabelRenderer;
import org.netbeans.modules.profiler.snaptracer.impl.swing.LegendFont;

/**
 *
 * @author Jiri Sedlacek
 */
final class TimelineUnitsOverlay extends ChartOverlay {

    private final TimelineChart chart;
    private final LabelRenderer painter;

    private Model model;


    TimelineUnitsOverlay(TimelineChart chart) {
        this.chart = chart;

        painter = new LabelRenderer();
        painter.setFont(new LegendFont());

        int size = painter.getFont().getSize() - 3;
        ColorIcon.setup(size, size,
                LegendFont.FOREGROUND_COLOR, LegendFont.BACKGROUND_COLOR);
    }


    void setupModel(Model model) {
        this.model = model;
    }


    private boolean hasValues() {
        return ((SynchronousXYItemsModel)chart.getItemsModel()).getTimeline().
                getTimestampsCount() > 0;
    }

    private void setupPainter(String text, Color color) {
        painter.setText(text);
        painter.setIcon(color == null ? null : ColorIcon.fromColor(color));
    }


    public void paint(Graphics g) {
        if (model == null || !hasValues()) return;

        int w = getWidth();
        model.prefetch();
        int rowsCount = chart.getRowsCount();

        for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
            TimelineChart.Row row = chart.getRow(rowIndex);
            ChartContext rowContext = row.getContext();

            int y = Utils.checkedInt(rowContext.getViewportOffsetY());
            int h = rowContext.getViewportHeight();

            Color[] colors = model.getColors(row);

            int xx = w - 2;
            int yy = y;
            for (int itemIndex = colors.length - 1; itemIndex >= 0; itemIndex--) {
                setupPainter(model.getMaxUnits(row)[itemIndex], colors[itemIndex]);
                xx -= painter.getPreferredSize().width;
                paint(g, xx, yy);
                xx -= 10;
            }

            xx = w - 2;
            yy = -1;
            for (int itemIndex = colors.length - 1; itemIndex >= 0; itemIndex--) {
                setupPainter(model.getMinUnits(row)[itemIndex], colors[itemIndex]);
                Dimension pd = painter.getPreferredSize();
                xx -= pd.width;
                if (yy == -1) yy = y + h - pd.height - 1;
                paint(g, xx, yy);
                xx -= 10;
            }
        }
    }

    private void paint(Graphics g, int x, int y) {
        painter.setLocation(x, y + 1);
        painter.setForeground(LegendFont.BACKGROUND_COLOR);
        painter.paint(g);

        painter.setLocation(x, y);
        painter.setForeground(LegendFont.FOREGROUND_COLOR);
        if (painter.getIcon() != null)
            painter.setIcon(ColorIcon.BOTTOM_SHADOW);
        painter.paint(g);
    }


    // --- Peformance tweaks ---------------------------------------------------

    public void invalidate() {}

    public void update(Graphics g) {}


    // --- Model definition ----------------------------------------------------

    static interface Model {

        public void prefetch();
        public Color[]  getColors(TimelineChart.Row row);
        public String[] getMinUnits(TimelineChart.Row row);
        public String[] getMaxUnits(TimelineChart.Row row);

    }

}
