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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author Jiri Sedlacek
 */
public final class TimelinePanel extends JPanel {

    private final ChartPanel chartPanel;
    private final RowMouseHandler mouseHandler;


    // --- Constructor ---------------------------------------------------------
    
    public TimelinePanel(TimelineSupport support) {
        super(new BorderLayout());
        setOpaque(false);

        ProbesPanel probesPanel = new ProbesPanel(support);
        chartPanel = new ChartPanel(support.getChart(), support);

        add(probesPanel, BorderLayout.WEST);
        add(chartPanel, BorderLayout.CENTER);

        new ProbesWheelHandler(chartPanel, probesPanel).register();
        mouseHandler = new RowMouseHandler(support, probesPanel);
        mouseHandler.register();
    }


    // --- Public interface ----------------------------------------------------

    public void reset() {
        chartPanel.resetChart();
        resetSelection();
    }

    public void resetSelection() {
        chartPanel.resetSelection();
    }

    public void updateActions() {
        chartPanel.updateActions();
    }

    public Action zoomInAction() {
        return chartPanel.zoomInAction();
    }

    public Action zoomOutAction() {
        return chartPanel.zoomOutAction();
    }

    public Action toggleViewAction() {
        return chartPanel.toggleViewAction();
    }

    public AbstractButton mouseZoom() {
        return chartPanel.mouseZoom();
    }

    public AbstractButton mouseHScroll() {
        return chartPanel.mouseHScroll();
    }

    public AbstractButton mouseVScroll() {
        return chartPanel.mouseVScroll();
    }


    // --- Private implementation ----------------------------------------------

    private static class ProbesWheelHandler implements MouseWheelListener {

        private final ChartPanel chartPanel;
        private final ProbesPanel probesPanel;

        ProbesWheelHandler(ChartPanel chartPanel, ProbesPanel probesPanel) {
            this.chartPanel = chartPanel;
            this.probesPanel = probesPanel;
        }

        void register() {
            probesPanel.addMouseWheelListener(this);
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            chartPanel.vScroll(e);
        }

    }

    private static class RowMouseHandler extends MouseAdapter {

        private static final int RESIZE_RANGE = 3;

        private final TimelineSupport support;
        private final TimelineChart chart;
        private TimelineSelectionManager selection;
        private final ProbesPanel probesPanel;

        private int baseY;
        private int baseHeight;
        private TimelineChart.Row draggingRow;


        RowMouseHandler(TimelineSupport support, ProbesPanel probesPanel) {
            this.support = support;
            this.chart = support.getChart();
            this.selection = (TimelineSelectionManager)chart.getSelectionModel();
            this.probesPanel = probesPanel;
        }


        void register() {
            chart.addMouseListener(this);
            chart.addMouseMotionListener(this);
            probesPanel.getMouseTarget().addMouseListener(this);
            probesPanel.getMouseTarget().addMouseMotionListener(this);
        }


        public void mousePressed(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            updateRowState(e, SwingUtilities.isLeftMouseButton(e));
            chart.updateSelection(false, this);
            selection.setEnabled(draggingRow == null);
            updateCursor();
        }

        public void mouseReleased(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            chart.updateSelection(true, this);

            if (draggingRow == null && e.getSource() == chart)
                support.indexSelectionChanged(selection.getStartIndex(),
                                              selection.getEndIndex());
            
            updateRowState(e, false);
            updateCursor();

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    selection.setEnabled(true);
                }
            });
        }

        public void mouseMoved(MouseEvent e) {
            updateRowState(e, false);
            updateCursor();
        }

        public void mouseDragged(MouseEvent e){
            if (!SwingUtilities.isLeftMouseButton(e)) return;
            if (draggingRow != null) {
                boolean checkStep = (e.getModifiers() & Toolkit.getDefaultToolkit().
                                     getMenuShortcutKeyMask()) == 0;
                chart.setRowHeight(draggingRow.getIndex(), baseHeight + e.getY() - baseY, checkStep);
            }
        }


        private void updateRowState(MouseEvent e, boolean updateSelection) {
            baseY = e.getY();
            draggingRow = chart.getNearestRow(baseY, RESIZE_RANGE, true);
            if (draggingRow != null) {
                baseHeight = draggingRow.getHeight();
            }
        }

        private void updateCursor() {
            if (draggingRow != null) {
                Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                chart.setCursor(resizeCursor);
                probesPanel.setCursor(resizeCursor);
            } else {
                Cursor defaultCursor = Cursor.getDefaultCursor();
                chart.setCursor(defaultCursor);
                probesPanel.setCursor(defaultCursor);
            }
        }

    }

}
