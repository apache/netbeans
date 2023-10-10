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
package org.netbeans.lib.profiler.ui.charts.xy;

import org.netbeans.lib.profiler.charts.ChartComponent;
import org.netbeans.lib.profiler.charts.ChartOverlay;
import org.netbeans.lib.profiler.charts.ChartSelectionListener;
import org.netbeans.lib.profiler.charts.ItemSelection;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.charts.ChartConfigurationListener;
import org.netbeans.lib.profiler.charts.swing.LongRect;
import org.netbeans.lib.profiler.charts.swing.Utils;

/**
 *
 * @author Jiri Sedlacek
 */
public class ProfilerXYSelectionOverlay extends ChartOverlay {

    private ChartComponent chart;

    private int selectionExtent;

    private final ConfigurationListener configurationListener;
    private final SelectionListener selectionListener;
    private final Set<Point> selectedValues;

    private Paint markPaint;
    private Paint oddPerfPaint;
    private Paint evenPerfPaint;

    private Stroke markStroke;
    private Stroke oddPerfStroke;
    private Stroke evenPerfStroke;


    public ProfilerXYSelectionOverlay() {
        configurationListener = new ConfigurationListener();
        selectionListener = new SelectionListener();
        selectedValues = new HashSet<>();
        initDefaultValues();
    }
    

    // --- Public API ----------------------------------------------------------

    public final void registerChart(ChartComponent chart) {
        unregisterListener();
        this.chart = chart;
        registerListener();
    }

    public final void unregisterChart(ChartComponent chart) {
        unregisterListener();
        this.chart = null;
    }


    // --- Private implementation ----------------------------------------------

    private void registerListener() {
        if (chart == null) return;
        chart.addConfigurationListener(configurationListener);
        chart.getSelectionModel().addSelectionListener(selectionListener);
    }

    private void unregisterListener() {
        if (chart == null) return;
        chart.removeConfigurationListener(configurationListener);
        chart.getSelectionModel().removeSelectionListener(selectionListener);
    }

    private void initDefaultValues() {
        markPaint = new Color(80, 80, 80);
        oddPerfPaint = Color.BLACK;
        evenPerfPaint = Color.WHITE;

        markStroke = new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        oddPerfStroke = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, new float[] { 1.0f, 3.0f }, 0);
        evenPerfStroke = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0, new float[] { 1.0f, 3.0f }, 2);

        selectionExtent = 3;
    }


    public void paint(Graphics g) {
        if (selectedValues.isEmpty()) return;

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHints(chart.getRenderingHints());

        Iterator<Point> it = selectedValues.iterator();
        boolean linePainted = false;

        while (it.hasNext()) {
            Point p = it.next();

            if (!linePainted) {
                g2.setPaint(evenPerfPaint);
                g2.setStroke(evenPerfStroke);
                g2.drawLine(p.x, 0, p.x, getHeight());
                g2.setPaint(oddPerfPaint);
                g2.setStroke(oddPerfStroke);
                g2.drawLine(p.x, 0, p.x, getHeight());

                g2.setPaint(markPaint);
                g2.setStroke(markStroke);

                linePainted = true;
            }

            g2.fillOval(p.x - selectionExtent + 1, p.y - selectionExtent + 1,
                        selectionExtent * 2 - 1, selectionExtent * 2 - 1);
        }

    }

    private void vLineBoundsChanged(Set<Point> oldSelection, Set<Point> newSelection) {
        Point oldSel = oldSelection.isEmpty() ? null : oldSelection.iterator().next();
        Point newSel = newSelection.isEmpty() ? null : newSelection.iterator().next();

        if (oldSel != null) repaint(oldSel.x - selectionExtent, 0,
                                             selectionExtent * 2, getHeight());
        if (newSel != null) repaint(newSel.x - selectionExtent, 0,
                                             selectionExtent * 2, getHeight());
    }

    private static void updateSelectedValues(Set<Point> selectedValues,
                                             List<ItemSelection> selectedItems,
                                             ChartComponent chart) {
        selectedValues.clear();
        for (ItemSelection sel : selectedItems) {
            ProfilerXYItemPainter painter = (ProfilerXYItemPainter)chart.getPaintersModel().getPainter(sel.getItem());
            LongRect bounds = painter.getSelectionBounds(sel, chart.getChartContext());
            selectedValues.add(new Point(Utils.checkedInt(bounds.x + (bounds.width >> 2) + 1),
                                         Utils.checkedInt(bounds.y + (bounds.height >> 2) + 1)));
        }
    }


    private class ConfigurationListener extends ChartConfigurationListener.Adapter {
        public void contentsUpdated(long offsetX, long offsetY,
                                    double scaleX, double scaleY,
                                    long lastOffsetX, long lastOffsetY,
                                    double lastScaleX, double lastScaleY,
                                    int shiftX, int shiftY) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Set<Point> oldSelectedValues = new HashSet<>(selectedValues);
                        updateSelectedValues(selectedValues, chart.getSelectionModel().getHighlightedItems(), chart);
                        vLineBoundsChanged(oldSelectedValues, selectedValues);
                    }
                });
        }
    }

    private class SelectionListener implements ChartSelectionListener {

        public void selectionModeChanged(int newMode, int oldMode) {}

        public void selectionBoundsChanged(Rectangle newBounds, Rectangle oldBounds) {}

        public void selectedItemsChanged(List<ItemSelection> currentItems,
              List<ItemSelection> addedItems, List<ItemSelection> removedItems) {}

        public void highlightedItemsChanged(List<ItemSelection> currentItems,
              List<ItemSelection> addedItems, List<ItemSelection> removedItems) {
            Set<Point> oldSelectedValues = new HashSet<>(selectedValues);
            updateSelectedValues(selectedValues, currentItems, chart);
            vLineBoundsChanged(oldSelectedValues, selectedValues);
        }

    }

}
