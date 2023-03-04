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
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import org.netbeans.lib.profiler.charts.ChartContext;
import org.netbeans.lib.profiler.charts.ChartDecorator;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 */
final class RowForegroundDecorator implements ChartDecorator {

    private static final Color SELECTED_FILTER = new Color(0, 0, 200, 25);
    private static final float[] FRACTIONS = new float[] { 0.0f, 0.49f, 0.51f, 1.0f };
    private static final Color[] COLORS = !UIUtils.isDarkResultsBackground() ?
        new Color[] { new Color(250, 251, 252, 120),
                      new Color(237, 240, 242, 120),
                      new Color(229, 233, 236, 125),
                      new Color(215, 221, 226, 130) } :
        new Color[] { new Color(050, 051, 052, 110),
                      new Color(037, 040, 042, 110),
                      new Color(29, 033, 036, 115),
                      new Color(015, 021, 026, 120) };

    private final TimelineChart chart;
    private final boolean gradient;
    private final boolean selection;


    RowForegroundDecorator(TimelineChart chart, boolean gradient, boolean selection) {
        this.chart = chart;
        this.gradient = gradient;
        this.selection = selection;
    }


    public void paint(Graphics2D g, Rectangle dirtyArea, ChartContext context) {
        if (gradient || selection) {
            int rowsCount = chart.getRowsCount();
            for (int i = 0; i < rowsCount; i++) {
                TimelineChart.Row row = chart.getRow(i);
                ChartContext rowContext = row.getContext();

                int y = Utils.checkedInt(rowContext.getViewportOffsetY());
                int h = Utils.checkedInt(rowContext.getViewportHeight() - 1);

                if (gradient) {
                    g.setPaint(new LinearGradientPaint(0, y, 0, y + h, FRACTIONS, COLORS));
                    g.fillRect(0, y, chart.getWidth(), h);
                }

                if (selection && chart.isRowSelected(row)) {
                    g.setColor(SELECTED_FILTER);
                    g.fillRect(0, y, chart.getWidth(), h);
                }
            }
        }
    }

}
