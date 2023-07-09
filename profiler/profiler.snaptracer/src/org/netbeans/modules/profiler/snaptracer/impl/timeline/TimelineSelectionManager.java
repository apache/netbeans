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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.charts.ChartComponent;
import org.netbeans.lib.profiler.charts.ChartConfigurationListener;
import org.netbeans.lib.profiler.charts.ChartContext;
import org.netbeans.lib.profiler.charts.ChartItem;
import org.netbeans.lib.profiler.charts.ChartSelectionListener;
import org.netbeans.lib.profiler.charts.ChartSelectionModel;
import org.netbeans.lib.profiler.charts.ItemPainter;
import org.netbeans.lib.profiler.charts.ItemSelection;
import org.netbeans.lib.profiler.charts.ItemsModel;
import org.netbeans.lib.profiler.charts.PaintersModel;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.charts.xy.XYItem;
import org.netbeans.lib.profiler.charts.xy.XYItemSelection;

/**
 *
 * @author Jiri Sedlacek
 */
class TimelineSelectionManager implements ChartSelectionModel {

    private ChartComponent chart;

    private ChartListener chartListener;
    private MouseListener mouseListener;

    private int selectionMode;
    private int moveMode;
    private int dragMode;

    private int hoverMode;
    private int hoverDistanceLimit;

    private int mouseX;
    private int mouseY;
    private boolean inChart;

    private Rectangle selectionBounds;

    private List<ItemSelection> highlightedSelection;
    private List<ItemSelection> selectedSelection;

    private boolean mousePanningBackup;

    private boolean enabled = true;

    private List<ChartSelectionListener> selectionListeners;


    public TimelineSelectionManager() {
        mouseX = -1;
        mouseY = -1;
        inChart = false;

        chartListener = new ChartListener();
        mouseListener = new MouseListener();

        setMoveMode(SELECTION_NONE);
        setDragMode(SELECTION_NONE);
        setSelectionMode(moveMode);

        setHoverMode(HOVER_NONE);
        setHoverDistanceLimit(HOVER_DISTANCE_LIMIT_NONE);
    }


    // --- Internal API --------------------------------------------------------

    void registerChart(ChartComponent chart) {
        unregisterListener();
        this.chart = chart;
        registerListener();
    }

    void unregisterChart(ChartComponent chart) {
        unregisterListener();
        this.chart = null;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    // --- Selection mode ------------------------------------------------------

    public final void setMoveMode(int mode) {
        moveMode = mode;
        if (selectionMode != moveMode) setSelectionMode(moveMode);
    }

    public final int getMoveMode() {
        return moveMode;
    }

    public final void setDragMode(int mode) {
        dragMode = mode;
    }

    public final int getDragMode() {
        return dragMode;
    }

    public final int getSelectionMode() {
        return selectionMode;
    }

    public final void setHoverMode(int mode) {
        hoverMode = mode;
        updateHighlightedItems();
    }

    public final int getHoverMode() {
        return hoverMode;
    }

    public final void setHoverDistanceLimit(int limit) {
        hoverDistanceLimit = limit;
    }

    public final int getHoverDistanceLimit() {
        return hoverDistanceLimit;
    }


    // --- Selection bounds ----------------------------------------------------

    private int startIndex = -1;
    private int endIndex = -1;

    public final void setSelectionBounds(int x, int y, int w, int h) {
        setSelectionBounds(new Rectangle(x, y, w, h));
    }

    public void selectAll() {
        Rectangle oldSelectionBounds = this.selectionBounds == null ? null :
                                       new Rectangle(this.selectionBounds);

        selectionBounds = new Rectangle(new Point(0, 0), chart.getSize());
        startIndex = 0;
        ChartItem item = chart.getItemsModel().getItem(0);
        endIndex = ((XYItem)item).getValuesCount() - 1;

        fireSelectionBoundsChanged(this.selectionBounds, oldSelectionBounds);
    }

    public final void setSelectionBounds(Rectangle selectionBounds) {
        if (selectionBounds == null && this.selectionBounds == null) return;

        normalizeBounds(selectionBounds);

        if (this.selectionBounds != null && this.selectionBounds.equals(selectionBounds) ||
            selectionBounds != null && selectionBounds.equals(this.selectionBounds)) return;

        Rectangle oldSelectionBounds = this.selectionBounds == null ? null :
                                       new Rectangle(this.selectionBounds);

        if (selectionBounds == null) this.selectionBounds = null;
        else if (this.selectionBounds == null) this.selectionBounds = new Rectangle(selectionBounds);
        else this.selectionBounds.setBounds(selectionBounds);

        fireSelectionBoundsChanged(this.selectionBounds, oldSelectionBounds);
    }

    public final Rectangle getSelectionBounds() {
        return selectionBounds == null ? new Rectangle() : new Rectangle(selectionBounds);
    }

    public int getStartIndex() { return startIndex; }
    
    public int getEndIndex() { return endIndex; }


    private void normalizeBounds(Rectangle bounds) {
        if (bounds == null) return;
        
        ItemSelection sel = getClosestSelection(bounds.x, bounds.y);
        if (!(sel instanceof XYItemSelection)) return;

//      #262588 : Do not change selection view bounds
//        ChartContext context = chart.getChartContext();

        XYItemSelection xySel = (XYItemSelection)sel;
//        XYItem item = xySel.getItem();
        startIndex = xySel.getValueIndex();
        endIndex = startIndex;
//        long valX = item.getXValue(startIndex);
//        bounds.x = Utils.checkedInt(context.getViewX(valX));

        if (bounds.width == 0) return;

        sel = getClosestSelection(bounds.x + bounds.width, bounds.y + bounds.height);
        if (!(sel instanceof XYItemSelection)) return;

        xySel = (XYItemSelection)sel;
//        item = xySel.getItem();
        endIndex = xySel.getValueIndex();
//        long valX = item.getXValue(endIndex);
//        bounds.width = Utils.checkedInt(context.getViewX(valX)) - bounds.x;
    }


    // --- Highlighted items ---------------------------------------------------

    public final void setHighlightedItems(List<ItemSelection> items) {
        if (highlightedSelection == null) {
            if (items.isEmpty()) return;
            highlightedSelection = new ArrayList(items);
            fireHighlightedItemsChanged(items, items, Collections.EMPTY_LIST);
        } else {
            List<ItemSelection> addedItems = new ArrayList<>();
            List<ItemSelection> removedItems = new ArrayList<>();

            for (ItemSelection item : items)
                if (!highlightedSelection.contains(item)) addedItems.add(item);

            for (ItemSelection item : highlightedSelection)
                if (!items.contains(item)) removedItems.add(item);

            if (addedItems.isEmpty() && removedItems.isEmpty()) return;

            highlightedSelection = new ArrayList(items);
            fireHighlightedItemsChanged(items, addedItems, removedItems);
        }
    }

    public final List<ItemSelection> getHighlightedItems() {
        return highlightedSelection == null ? Collections.EMPTY_LIST :
                                              new ArrayList(highlightedSelection);
    }


    // --- Selected items ------------------------------------------------------

    public final void setSelectedItems(List<ItemSelection> items) {
        if (selectedSelection == null) {
            if (items.isEmpty()) return;
            selectedSelection = new ArrayList(items);
            fireSelectedItemsChanged(items, items, Collections.EMPTY_LIST);
        } else {
            List<ItemSelection> addedItems = new ArrayList<>();
            List<ItemSelection> removedItems = new ArrayList<>();

            for (ItemSelection item : items)
                if (!selectedSelection.contains(item)) addedItems.add(item);

            for (ItemSelection item : selectedSelection)
                if (!items.contains(item)) removedItems.add(item);

            if (addedItems.isEmpty() && removedItems.isEmpty()) return;

            selectedSelection = new ArrayList(items);
            fireSelectedItemsChanged(items, addedItems, removedItems);
        }
    }

    public final List<ItemSelection> getSelectedItems() {
        return selectedSelection == null ? Collections.EMPTY_LIST :
                                           new ArrayList(selectedSelection);
    }


    // --- Selection listeners -------------------------------------------------

    public final void addSelectionListener(ChartSelectionListener listener) {
        if (selectionListeners == null) selectionListeners = new ArrayList();
        selectionListeners.add(listener);
    }

    public final void removeSelectionListener(ChartSelectionListener listener) {
        if (selectionListeners == null) return;
        selectionListeners.remove(listener);
    }


    // --- Private implementation ----------------------------------------------

    private void setSelectionMode(int selectionMode) {
        if (this.selectionMode == selectionMode) return;
        int oldSelectionMode = this.selectionMode;
        this.selectionMode = selectionMode;
        fireSelectionModeChanged(this.selectionMode, oldSelectionMode);
    }


    private void registerListener() {
        if (chart == null) return;
        chart.addMouseListener(mouseListener);
        chart.addMouseMotionListener(mouseListener);
        chart.addConfigurationListener(chartListener);
    }

    private void unregisterListener() {
        if (chart == null) return;
        chart.removeMouseListener(mouseListener);
        chart.removeMouseMotionListener(mouseListener);
        chart.removeConfigurationListener(chartListener);
    }


    private void updateHighlightedItems() {
        final int x = mouseX;
        final int y = mouseY;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (hoverMode == HOVER_NONE || !inChart) {
                    setHighlightedItems(Collections.EMPTY_LIST);
                    return;
                }

                ItemsModel itemsModel = chart.getItemsModel();
                PaintersModel paintersModel = chart.getPaintersModel();

                int itemsCount = itemsModel.getItemsCount();
                List<ItemSelection> closestSelection = new ArrayList<>(itemsCount);

                for (int i = 0; i < itemsCount; i++) {
                    ChartItem item = itemsModel.getItem(i);
                    ItemPainter painter = paintersModel.getPainter(item);
                    ItemSelection selection = painter.getClosestSelection(item, x, y,
                                                            chart.getChartContext());

                    if (selection != null) {
                        int distance = selection.getDistance();
                        if (hoverMode == HOVER_EACH_NEAREST || closestSelection.isEmpty()) {
                            if (hoverDistanceLimit == HOVER_DISTANCE_LIMIT_NONE ||
                                distance <= hoverDistanceLimit)
                                closestSelection.add(selection);
                        } else {
                            if (closestSelection.get(0).getDistance() > distance) {
                                if (hoverDistanceLimit == HOVER_DISTANCE_LIMIT_NONE ||
                                    distance <= hoverDistanceLimit)
                                    closestSelection.set(0, selection);
                            }
                        }
                    }
                }

                setHighlightedItems(closestSelection);
            }
        });
    }

    private ItemSelection getClosestSelection(int x, int y) {
        ItemsModel itemsModel = chart.getItemsModel();
        int itemsCount = itemsModel.getItemsCount();
        if (itemsCount == 0) return null;

        PaintersModel paintersModel = chart.getPaintersModel();
        int itemIndex = 0;
        while (itemIndex < itemsCount) {
            ChartItem item = itemsModel.getItem(itemIndex);
            ItemPainter painter = paintersModel.getPainter(item);
            if (!(painter instanceof TimelineIconPainter))
                return painter.getClosestSelection(item, x, y, chart.getChartContext());
            itemIndex++;
        }

        ChartItem item = itemsModel.getItem(0);
        ItemPainter painter = paintersModel.getPainter(item);
        return painter.getClosestSelection(item, x, y, chart.getChartContext());
    }


    private void fireSelectionModeChanged(int newMode, int oldMode) {
        if (selectionListeners == null) return;
        for (ChartSelectionListener listener : selectionListeners)
            listener.selectionModeChanged(newMode, oldMode);
    }

    private void fireSelectionBoundsChanged(Rectangle newBounds, Rectangle oldBounds) {
        if (selectionListeners == null) return;
        for (ChartSelectionListener listener : selectionListeners)
            listener.selectionBoundsChanged(newBounds, oldBounds);
    }

    private void fireHighlightedItemsChanged(List<ItemSelection> currentItems,
            List<ItemSelection> addedItems, List<ItemSelection> removedItems) {
        if (selectionListeners == null) return;
        for (ChartSelectionListener listener : selectionListeners)
            listener.highlightedItemsChanged(currentItems, addedItems, removedItems);
    }

    private void fireSelectedItemsChanged(List<ItemSelection> currentItems,
            List<ItemSelection> addedItems, List<ItemSelection> removedItems) {
        if (selectionListeners == null) return;
        for (ChartSelectionListener listener : selectionListeners)
            listener.selectedItemsChanged(currentItems, addedItems, removedItems);
    }


    private class ChartListener extends ChartConfigurationListener.Adapter {

        public void contentsWillBeUpdated(long offsetX, long offsetY,
                                double scaleX, double scaleY,
                                long lastOffsetX, long lastOffsetY,
                                double lastScaleX, double lastScaleY) {
            updateHighlightedItems();
        }

    }


    private class MouseListener extends MouseAdapter implements MouseMotionListener {

        public void mousePressed(final MouseEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (!enabled || !SwingUtilities.isLeftMouseButton(e)) return;
                    mousePanningBackup = chart.isMousePanningEnabled();

                    setSelectionMode(dragMode);
                    if (selectionMode != SELECTION_NONE) {
                        chart.disableMousePanning();
                        setSelectionBounds(null); // Clears previous selection
                        setSelectionBounds(e.getX(), e.getY(), 0, 0);
                    }
                }
            });
            
        }

        public void mouseReleased(final MouseEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (!enabled || !SwingUtilities.isLeftMouseButton(e)) return;
                    // Clear previous selection
        //            setSelectionBounds(null);

                    setSelectionMode(moveMode);
                    if (selectionMode == SELECTION_NONE)
                        chart.setMousePanningEnabled(mousePanningBackup);

                    // Refresh selection if needed
        //            if (selectionMode != SELECTION_NONE)
        //                setSelectionBounds(e.getX(), e.getY(), 0, 0);
                }
            });
            
        }

        public void mouseClicked(final MouseEvent e) {
            if (!enabled || !SwingUtilities.isLeftMouseButton(e)) return;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (selectionMode != SELECTION_NONE)
                        setSelectionBounds(e.getX(), e.getY(), 0, 0);
                }
            });
            
        }

        public void mouseEntered(final MouseEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    inChart = true;
                    mouseX = e.getX();
                    mouseY = e.getY();
                }
            });
            
        }

        public void mouseExited(final MouseEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    inChart = false;

        //            if (selectionMode == SELECTION_LINE_V ||
        //                selectionMode == SELECTION_LINE_H ||
        //                selectionMode == SELECTION_CROSS) {
        //                setSelectionBounds(null);
        //            }

                    updateHighlightedItems();
                }
            });
            
        }

        public void mouseDragged(final MouseEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (!enabled || !SwingUtilities.isLeftMouseButton(e)) return;
                    if (selectionMode == SELECTION_RECT) {
                        setSelectionBounds(selectionBounds.x, selectionBounds.y,
                                e.getX() - selectionBounds.x, e.getY() - selectionBounds.y);
                    }
                }
            });

        }

        public void mouseMoved(final MouseEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    mouseX = e.getX();
                    mouseY = e.getY();

                    if (selectionMode == SELECTION_NONE) setSelectionBounds(null);
        //            else setSelectionBounds(mouseX, mouseY, 0, 0);

                    updateHighlightedItems();
                }
            });
            
        }

    }

}
