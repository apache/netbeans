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
import org.netbeans.lib.profiler.charts.ItemSelection;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.lib.profiler.charts.ChartContext;
import org.netbeans.lib.profiler.charts.ChartItem;
import org.netbeans.lib.profiler.charts.ChartItemChange;
import org.netbeans.lib.profiler.charts.swing.LongRect;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.charts.xy.XYItem;
import org.netbeans.lib.profiler.charts.xy.XYItemChange;
import org.netbeans.lib.profiler.charts.xy.XYItemSelection;
import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChartContext;
import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItem;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.snaptracer.impl.IdeSnapshot;
import org.netbeans.modules.profiler.snaptracer.impl.icons.TracerIcons;

/**
 *
 * @author Jiri Sedlacek
 */
class TimelineIconPainter extends TimelineXYPainter {

    private static final Icon ICON = Icons.getIcon(TracerIcons.GENERIC_ACTION);

    private static final int ICON_EXTENT = 8;

    protected final Color color;
    protected final IdeSnapshot snapshot;


    // --- Constructor ---------------------------------------------------------

    TimelineIconPainter(Color color, IdeSnapshot snapshot) {
        super(ICON_EXTENT, true, 1);
        this.color = color;
        this.snapshot = snapshot;
    }


    // --- Abstract interface --------------------------------------------------

    protected void paint(XYItem item, List<ItemSelection> highlighted,
                         List<ItemSelection> selected, Graphics2D g,
                         Rectangle dirtyArea, SynchronousXYChartContext
                         context) {

        if (context.getViewWidth() == 0) return;
        
        int[][] visibleBounds = context.getVisibleBounds(dirtyArea);

        int firstFirst = visibleBounds[0][0];
        int firstIndex = firstFirst;
        if (firstIndex == -1) firstIndex = visibleBounds[0][1];
        if (firstIndex == -1) return;

        int minX = dirtyArea.x - ICON_EXTENT;
        while (context.getViewX(item.getXValue(firstIndex)) > minX && firstIndex > 0) firstIndex--;

        int endIndex = item.getValuesCount() - 1;
        int lastFirst = visibleBounds[1][0];
        int lastIndex = lastFirst;
        if (lastIndex == -1) lastIndex = visibleBounds[1][1];
        if (lastIndex == -1) lastIndex = endIndex;

        int maxX = dirtyArea.x + dirtyArea.width + ICON_EXTENT;
        while (context.getViewX(item.getXValue(lastIndex)) < maxX && lastIndex < endIndex) lastIndex++;

        g.setColor(color);

        for (int index = firstIndex; index <= lastIndex; index++) {
            long dataY = item.getYValue(index);
            if (dataY == 0) continue;

            long dataX = item.getXValue(index);
            int  viewX = Utils.checkedInt(context.getViewX(dataX));
            Icon icon = snapshot.getLogInfoForValue(dataY).getIcon();
            if (icon == null) icon = ICON;
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            icon.paintIcon(null, g, viewX - iconWidth / 2, (context.getViewportHeight() - iconHeight) / 2);
        }
    }

    protected Color getDefiningColor() { return color; }
    

    // --- ItemPainter implementation ------------------------------------------

    public LongRect getItemBounds(ChartItem item) {
        XYItem xyItem = (XYItem)item;
        return getDataBounds(xyItem.getBounds());
    }

    public LongRect getItemBounds(ChartItem item, ChartContext context) {
        XYItem xyItem = (XYItem)item;
        return getViewBounds(xyItem.getBounds(), context);
    }


    public boolean isBoundsChange(ChartItemChange itemChange) {
        XYItemChange change = (XYItemChange)itemChange;
        return !LongRect.equals(change.getOldValuesBounds(),
                                change.getNewValuesBounds());
    }

    public boolean isAppearanceChange(ChartItemChange itemChange) {
        XYItemChange change = (XYItemChange)itemChange;
        LongRect dirtyBounds = change.getDirtyValuesBounds();
        return dirtyBounds.width != 0 && dirtyBounds.height != 0;
    }

    public LongRect getDirtyBounds(ChartItemChange itemChange, ChartContext context) {
        XYItemChange change = (XYItemChange)itemChange;
        return getViewBounds(change.getDirtyValuesBounds(), context);
    }


    public boolean supportsHovering(ChartItem item) {
        return true;
    }

    public boolean supportsSelecting(ChartItem item) {
        return true;
    }

    public LongRect getSelectionBounds(ItemSelection selection, ChartContext context) {

        XYItemSelection sel = (XYItemSelection)selection;
        XYItem item  = sel.getItem();
        int selectedValueIndex = sel.getValueIndex();

        if (selectedValueIndex == -1 ||
            selectedValueIndex >= item.getValuesCount())
            // This happens on reset - bounds of the selection are unknown, let's clear whole area
            return new LongRect(0, 0, context.getViewportWidth(),
                                context.getViewportHeight());
        else
            return getViewBounds(item, selectedValueIndex, context);
    }

    public XYItemSelection getClosestSelection(ChartItem item, int viewX,
                                               int viewY, ChartContext context) {

        SynchronousXYChartContext contx = (SynchronousXYChartContext)context;
        SynchronousXYItem xyItem = (SynchronousXYItem)item;

        int nearestTimestampIndex = contx.getNearestTimestampIndex(viewX, viewY);
        if (nearestTimestampIndex == -1) return new XYItemSelection.Default(xyItem,
                nearestTimestampIndex, ItemSelection.DISTANCE_UNKNOWN);

        int minX = viewX - ICON_EXTENT;
        int maxX = viewX + ICON_EXTENT;
        int itemX = Utils.checkedInt(contx.getViewX(xyItem.getXValue(nearestTimestampIndex)));
        if (itemX > maxX || itemX < minX) return new XYItemSelection.Default(xyItem,
                nearestTimestampIndex, ItemSelection.DISTANCE_UNKNOWN);

        int closest = -1;
        int index = nearestTimestampIndex;
        while (index < xyItem.getValuesCount()) {
            if (Utils.checkedInt(contx.getViewX(xyItem.getXValue(index))) > maxX) break;
            if (xyItem.getYValue(index) != 0) closest = index;
            index++;
        }

        if (closest != -1) return new XYItemSelection.Default(xyItem, closest,
                ItemSelection.DISTANCE_UNKNOWN);

        index = nearestTimestampIndex - 1;
        while (index >= 0) {
            if (Utils.checkedInt(contx.getViewX(xyItem.getXValue(index))) < minX) break;
            if (xyItem.getYValue(index) != 0) closest = index;
            index--;
        }

        if (closest != -1) return new XYItemSelection.Default(xyItem, closest,
                ItemSelection.DISTANCE_UNKNOWN);

        return new XYItemSelection.Default(xyItem, nearestTimestampIndex,
                ItemSelection.DISTANCE_UNKNOWN);
    }


    // --- XYItemPainter implementation ----------------------------------------

    public double getItemView(double dataY, XYItem item, ChartContext context) {
        return -1;
    }

    public double getItemValue(double viewY, XYItem item, ChartContext context) {
        return -1;
    }

    public double getItemValueScale(XYItem item, ChartContext context) {
        return -1;
    }


    // --- Private implementation ----------------------------------------------

    private LongRect getDataBounds(LongRect itemBounds) {

        LongRect bounds = new LongRect(itemBounds);
        bounds.y = 0;
        bounds.height = 1000;
        return bounds;
    }

    private LongRect getViewBounds(LongRect itemBounds, ChartContext context) {

        boolean isData = itemBounds.height != 0;

        LongRect viewBounds = context.getViewRect(itemBounds);

        if (isData) {
            viewBounds.y = Utils.checkedInt(context.getViewY(context.getDataHeight() / 2));
            viewBounds.height = 0;
            LongRect.addBorder(viewBounds, ICON_EXTENT);
        } else {
            LongRect.clear(viewBounds);
        }
        
        return viewBounds;
    }

    private LongRect getViewBounds(XYItem item, int valueIndex, ChartContext context) {
        long xValue = item.getXValue(valueIndex);
        LongRect viewBounds = new LongRect(Utils.checkedInt(context.getViewX(xValue)),
                                           Utils.checkedInt(context.getViewY(context.
                                           getDataHeight() / 2)), 0, 0);

        if (item.getYValue(valueIndex) != 0) LongRect.addBorder(viewBounds, ICON_EXTENT);

        return viewBounds;
    }

}
