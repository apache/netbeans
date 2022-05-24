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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.charts.ChartConfigurationListener;
import org.netbeans.lib.profiler.charts.ChartSelectionModel;
import org.netbeans.lib.profiler.charts.Timeline;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemsModel;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.snaptracer.impl.icons.TracerIcons;
import org.netbeans.modules.profiler.snaptracer.impl.options.TracerOptions;
import org.netbeans.modules.profiler.snaptracer.impl.swing.ScrollBar;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ACTION_ZoomIn_name=Zoom in",
    "ACTION_ZoomOut_name=Zoom out",
    "ACTION_FixedScale_name=Fixed scale",
    "ACTION_ScaleToFit_name=Scale to fit",
    "ACTION_WheelZooms_name=Mouse wheel zooms",
    "ACTION_WheelHScrolls_name=Mouse wheel scrolls horizontally",
    "ACTION_WheelVScrolls_name=Mouse wheel scrolls vertically"
})
final class ChartPanel extends JPanel {

    private static final Icon ZOOM_IN_ICON = Icons.getIcon(GeneralIcons.ZOOM_IN);
    private static final Icon ZOOM_OUT_ICON = Icons.getIcon(GeneralIcons.ZOOM_OUT);
    private static final Icon FIXED_SCALE_ICON = Icons.getIcon(GeneralIcons.ZOOM);
    private static final Icon SCALE_TO_FIT_ICON = Icons.getIcon(GeneralIcons.SCALE_TO_FIT);
    private static final Icon ZMWHEEL_ICON = Icons.getIcon(TracerIcons.MOUSE_WHEEL_ZOOM);
    private static final Icon HMWHEEL_ICON = Icons.getIcon(TracerIcons.MOUSE_WHEEL_HORIZONTAL);
    private static final Icon VMWHEEL_ICON = Icons.getIcon(TracerIcons.MOUSE_WHEEL_VERTICAL);


    private final TimelineChart chart;

    private ZoomInAction zoomInAction;
    private ZoomOutAction zoomOutAction;
    private ToggleViewAction toggleViewAction;

    private final JScrollBar hScrollBar;
    private final JScrollBar vScrollBar;
    private final MouseWheelListener defaultWheelHandler;
    private AbstractButton mouseZoom;
    private AbstractButton mouseHScroll;
    private AbstractButton mouseVScroll;


    ChartPanel(TimelineChart chart, TimelineSupport support) {
        this.chart = chart;

        boolean speed = Utils.forceSpeed();

        chart.setBackground(!UIUtils.isDarkResultsBackground() ? Color.WHITE : new Color(70, 70, 70));
        if (speed && TracerOptions.getInstance().isRowsSelectionEnabled())
            chart.addPreDecorator(new RowBackgroundDecorator(chart));
        chart.addPreDecorator(new RowBoundsDecorator(chart));
        chart.addPostDecorator(new RowForegroundDecorator(chart,
                TracerOptions.getInstance().isRowsDecorationEnabled(),
                speed ? false : TracerOptions.getInstance().isRowsSelectionEnabled()));

        TimelineSelectionOverlay selectionOverlay = new TimelineSelectionOverlay();
        chart.addOverlayComponent(selectionOverlay);
        selectionOverlay.registerChart(support);

        XChartSelectionOverlay xOverlay = new XChartSelectionOverlay(support);
        xOverlay.setLineMode(false, false, false, false);
        chart.addOverlayComponent(xOverlay);
        xOverlay.registerChart(chart);

        ChartSelectionModel selectionModel = chart.getSelectionModel();
        if (selectionModel != null) {
            selectionModel.setHoverMode(ChartSelectionModel.HOVER_EACH_NEAREST);
            selectionModel.setMoveMode(ChartSelectionModel.SELECTION_LINE_V);
            selectionModel.setDragMode(ChartSelectionModel.SELECTION_RECT);
        }
        
        TimelineAxis axis = new TimelineAxis(chart, support);
        
        hScrollBar = new ScrollBar(JScrollBar.HORIZONTAL);
        hScrollBar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (hScrollBar.getValueIsAdjusting())
                    ChartPanel.this.chart.updateSelection(false, hScrollBar);
                else
                    ChartPanel.this.chart.updateSelection(true, hScrollBar);
            }
        });
        chart.attachHorizontalScrollBar(hScrollBar);

        vScrollBar = new ScrollBar(JScrollBar.VERTICAL) {
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize();
                dim.height = 1;
                return dim;
            }
        };
        vScrollBar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (vScrollBar.getValueIsAdjusting())
                    ChartPanel.this.chart.updateSelection(false, vScrollBar);
                else
                    ChartPanel.this.chart.updateSelection(true, vScrollBar);
            }
        });
        chart.attachVerticalScrollBar(vScrollBar);
        
        defaultWheelHandler = chart.getMouseWheelListeners()[0];

        chart.addConfigurationListener(new VisibleBoundsListener());

        setOpaque(false);
        setLayout(new BorderLayout());
        add(axis, BorderLayout.NORTH);
        add(chart, BorderLayout.CENTER);
        add(vScrollBar, BorderLayout.EAST);
        add(hScrollBar, BorderLayout.SOUTH);

        resetChart();
    }


    // --- Internal interface --------------------------------------------------

    void resetChart() {
        chart.setScale(0.02, 1);
        chart.setOffset(0, 0);
    }

    boolean resetSelection() {
        chart.getSelectionModel().setSelectionBounds(null);
        return false;
    }


    void updateActions() {
        if (zoomInAction != null) zoomInAction.updateAction();
        if (zoomOutAction != null) zoomOutAction.updateAction();
        if (toggleViewAction != null) toggleViewAction.updateAction();
    }

    Action zoomInAction() {
        if (zoomInAction == null) zoomInAction = new ZoomInAction();
        return zoomInAction;
    }

    Action zoomOutAction() {
        if (zoomOutAction == null) zoomOutAction = new ZoomOutAction();
        return zoomOutAction;
    }

    Action toggleViewAction() {
        if (toggleViewAction == null) {
            toggleViewAction = new ToggleViewAction();
            if (TracerOptions.getInstance().getZoomMode().equals(TracerOptions.
                    SCALE_TO_FIT)) toggleViewAction.actionPerformed(null);
        }
        return toggleViewAction;
    }


    AbstractButton mouseZoom() {
        if (mouseZoom == null) {
            mouseZoom = new OneWayToggleButton(ZMWHEEL_ICON, Bundle.ACTION_WheelZooms_name()) {
                protected void performAction() { mouseZoomImpl(); }
            };
            if (TracerOptions.getInstance().getMouseWheelAction().equals(
                    TracerOptions.MOUSE_WHEEL_ZOOMS)) {
                mouseZoom.setSelected(true);
                mouseZoomImpl();
            }
        }
        return mouseZoom;
    }

    AbstractButton mouseHScroll() {
        if (mouseHScroll == null) {
            mouseHScroll = new OneWayToggleButton(HMWHEEL_ICON, Bundle.ACTION_WheelHScrolls_name()) {
                protected void performAction() { mouseHScrollImpl(); }
            };
            if (TracerOptions.getInstance().getMouseWheelAction().equals(
                    TracerOptions.MOUSE_WHEEL_HSCROLLS)) {
                mouseHScroll.setSelected(true);
                mouseHScrollImpl();
            }
        }
        return mouseHScroll;
    }

    AbstractButton mouseVScroll() {
        if (mouseVScroll == null) {
            mouseVScroll = new OneWayToggleButton(VMWHEEL_ICON, Bundle.ACTION_WheelVScrolls_name()) {
                protected void performAction() { mouseVScrollImpl(); }
            };
            if (TracerOptions.getInstance().getMouseWheelAction().equals(
                    TracerOptions.MOUSE_WHEEL_VSCROLLS)) {
                mouseVScroll.setSelected(true);
                mouseVScrollImpl();
            }
        }
        return mouseVScroll;
    }


    // --- Mouse wheel handling ------------------------------------------------

    void vScroll(MouseWheelEvent e) {
        scroll(vScrollBar, e);
    }

    private void mouseZoomImpl() {
        clearWheelHandlers();
        chart.setMouseZoomingEnabled(true);
        chart.addMouseWheelListener(defaultWheelHandler);
    }

    private void mouseHScrollImpl() {
        chart.setMouseZoomingEnabled(false);
        clearWheelHandlers();
        setWheelScrollHandler(hScrollBar);
    }

    private void mouseVScrollImpl() {
        chart.setMouseZoomingEnabled(false);
        clearWheelHandlers();
        setWheelScrollHandler(vScrollBar);
    }

    private void clearWheelHandlers() {
        MouseWheelListener[] handlers = chart.getMouseWheelListeners();
        for (MouseWheelListener handler : handlers)
            chart.removeMouseWheelListener(handler);
    }

    private void setWheelScrollHandler(final JScrollBar scrollBar) {
        chart.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                scroll(scrollBar, e);
            }
        });
    }

    private static void scroll(JScrollBar scrollBar, MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int unitsToScroll = e.getUnitsToScroll();
            int direction = unitsToScroll < 0 ? -1 : 1;
            if (unitsToScroll != 0) {
                int increment = scrollBar.getUnitIncrement(direction);
                int oldValue = scrollBar.getValue();
                int newValue = oldValue + increment * unitsToScroll;
                newValue = Math.max(Math.min(newValue, scrollBar.getMaximum() -
                        scrollBar.getVisibleAmount()), scrollBar.getMinimum());
                if (oldValue != newValue) scrollBar.setValue(newValue);
            }
        }
    }


    // --- Actions support -----------------------------------------------------

    private class ZoomInAction extends AbstractAction {

        private static final int ONE_SECOND_WIDTH_THRESHOLD = 200;

        public ZoomInAction() {
            super();

            putValue(SHORT_DESCRIPTION, Bundle.ACTION_ZoomIn_name());
            putValue(SMALL_ICON, ZOOM_IN_ICON);

            updateAction();
        }

        public void actionPerformed(ActionEvent e) {
            boolean followsWidth = chart.currentlyFollowingDataWidth();
            chart.zoom(getWidth() / 2, getHeight() / 2, 2d);
            if (followsWidth) chart.setOffset(chart.maxOffsetX(), chart.getOffsetY());

            chart.repaintDirty();
        }

        private void updateAction() {
            Timeline timeline = ((SynchronousXYItemsModel)chart.getItemsModel()).getTimeline();
            setEnabled(timeline.getTimestampsCount() > 1 && !chart.fitsWidth() &&
                       chart.viewWidth(1000) < ONE_SECOND_WIDTH_THRESHOLD);
        }

    }

    private class ZoomOutAction extends AbstractAction {

        private static final float USED_CHART_WIDTH_THRESHOLD = 0.33f;

        public ZoomOutAction() {
            super();

            putValue(SHORT_DESCRIPTION, Bundle.ACTION_ZoomOut_name());
            putValue(SMALL_ICON, ZOOM_OUT_ICON);

            updateAction();
        }

        public void actionPerformed(ActionEvent e) {
            boolean followsWidth = chart.currentlyFollowingDataWidth();
            chart.zoom(getWidth() / 2, getHeight() / 2, 0.5d);
            if (followsWidth) chart.setOffset(chart.maxOffsetX(), chart.getOffsetY());

            chart.repaintDirty();
        }

        private void updateAction() {
            Timeline timeline = ((SynchronousXYItemsModel)chart.getItemsModel()).getTimeline();
            setEnabled(timeline.getTimestampsCount() > 0 && !chart.fitsWidth() &&
                       chart.getContentsWidth() > getWidth() * USED_CHART_WIDTH_THRESHOLD);
        }

    }

    private class ToggleViewAction extends AbstractAction {

        private long origOffsetX  = -1;
        private double origScaleX = -1;

        public ToggleViewAction() {
            super();
            updateAction();
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isMiddleMouseButton(e))
                        actionPerformed(null);
                }
            });
        }

        public void actionPerformed(ActionEvent e) {
            boolean fitsWidth = chart.fitsWidth();

            if (!fitsWidth) {
                origOffsetX = chart.getOffsetX();
                if (chart.tracksDataWidth() && origOffsetX == chart.maxOffsetX())
                    origOffsetX = Long.MAX_VALUE;
                origScaleX  = chart.getScaleX();
            }

            chart.setFitsWidth(!fitsWidth);

            if (fitsWidth && origOffsetX != -1 && origScaleX != -1) {
                chart.setScale(origScaleX, chart.getScaleY());
                chart.setOffset(origOffsetX, chart.getOffsetY());
            }

            updateAction();
            if (zoomInAction != null) zoomInAction.updateAction();
            if (zoomOutAction != null) zoomOutAction.updateAction();

            chart.repaintDirty();

        }

        private void updateAction() {
            boolean fitsWidth = chart.fitsWidth();
            Icon icon = fitsWidth ? FIXED_SCALE_ICON : SCALE_TO_FIT_ICON;
            String name = fitsWidth ? Bundle.ACTION_FixedScale_name() : Bundle.ACTION_ScaleToFit_name();
            putValue(SHORT_DESCRIPTION, name);
            putValue(SMALL_ICON, icon);
        }

    }


    private abstract static class OneWayToggleButton extends JToggleButton {

        private boolean action;


        public OneWayToggleButton(Icon icon, String toolTip) {
            super(icon);
            setToolTipText(toolTip);
        }

        protected void processMouseEvent(MouseEvent e) {
            if (isSelected()) {
                e.consume();
                action = false;
            } else {
                action = true;
            }
            super.processMouseEvent(e);
        }

        protected void processKeyEvent(KeyEvent e) {
            if (isSelected()) {
                e.consume();
                action = false;
            } else {
                action = true;
            }
            super.processKeyEvent(e);
        }

        protected final void fireActionPerformed(ActionEvent e) {
            if (action) performAction();
        }

        protected abstract void performAction();

    }


    // --- ChartConfigurationListener implementation ---------------------------

    private class VisibleBoundsListener extends ChartConfigurationListener.Adapter {

        public void dataBoundsChanged(long dataOffsetX, long dataOffsetY,
                                      long dataWidth, long dataHeight,
                                      long oldDataOffsetX, long oldDataOffsetY,
                                      long oldDataWidth, long oldDataHeight) {

            if (zoomInAction != null) zoomInAction.updateAction();
            if (zoomOutAction != null) zoomOutAction.updateAction();
        }

        public void scaleChanged(double oldScaleX, double oldScaleY,
                                 double newScaleX, double newScaleY) {

            if (zoomInAction != null) zoomInAction.updateAction();
            if (zoomOutAction != null) zoomOutAction.updateAction();
        }
    }

}
