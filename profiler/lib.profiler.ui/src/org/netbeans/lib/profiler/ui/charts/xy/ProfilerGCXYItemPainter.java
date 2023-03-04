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

package org.netbeans.lib.profiler.ui.charts.xy;

import org.netbeans.lib.profiler.charts.ChartContext;
import org.netbeans.lib.profiler.charts.ChartItem;
import org.netbeans.lib.profiler.charts.ChartItemChange;
import org.netbeans.lib.profiler.charts.ItemSelection;
import org.netbeans.lib.profiler.charts.swing.LongRect;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.charts.xy.XYItem;
import org.netbeans.lib.profiler.charts.xy.XYItemChange;
import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChartContext;
import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItemPainter;

/**
 *
 * @author Jiri Sedlacek
 */
public class ProfilerGCXYItemPainter extends SynchronousXYItemPainter {

    // --- Constructor ---------------------------------------------------------

    public static ProfilerGCXYItemPainter painter(Color fillColor) {
        
        return new ProfilerGCXYItemPainter(fillColor);
    }


    ProfilerGCXYItemPainter(Color fillColor) {
        super(0, null, fillColor, TYPE_ABSOLUTE, 0);
    }


    // --- ItemPainter implementation ------------------------------------------

    public LongRect getItemBounds(ChartItem item, ChartContext context) {
//        if (!(item instanceof ProfilerGCXYItem))
//            throw new UnsupportedOperationException("Unsupported item: " + item); // NOI18N

        // TODO: should return real bounds (== empty bounds for no gc)

        LongRect viewBounds = super.getItemBounds(item, context);
        viewBounds.y = 0;
        viewBounds.height = context.getViewportHeight();
        return viewBounds;
    }

    public LongRect getDirtyBounds(ChartItemChange itemChange, ChartContext context) {
//        if (!(itemChange instanceof XYItemChange))
//            throw new UnsupportedOperationException("Unsupported itemChange: " + itemChange);
        
        XYItemChange change = (XYItemChange)itemChange;
        ProfilerGCXYItem item = (ProfilerGCXYItem)change.getItem();
        
        int[] indexes = change.getValuesIndexes();

        if (indexes.length == 1 && indexes[0] == -1) {
            // Data reset
            LongRect dirtyBounds = change.getDirtyValuesBounds();
            
            long x = (long)context.getViewX(dirtyBounds.x);
            long y = (long)context.getViewY(context.getDataOffsetY() +
                                            context.getDataHeight());
            long width = (long)context.getViewWidth(dirtyBounds.width);
            long height = context.getViewportHeight();

            return new LongRect(x, y, width, height);
        } else {
            // New data
            int index = indexes[0];
            int lastIndex = indexes[indexes.length - 1];

            long dataStart = -1;
            long dataEnd   = -1;

            while (index <= lastIndex) {
                long[] gcEnds = item.getGCEnds(index);
                if (gcEnds.length > 0) {
                    dataEnd = gcEnds[gcEnds.length - 1];
                    if (dataStart == -1) {
                        long[] gcStarts = item.getGCStarts(index);
                        dataStart = gcStarts[0];
                    }
                }
                index++;
            }

            if (dataStart == -1) return new LongRect();
            if (dataEnd == -1) dataEnd = item.getXValue(item.getValuesCount() - 1);

            long x = (long)context.getViewX(dataStart);
            long y = (long)context.getViewY(context.getDataOffsetY() +
                                            context.getDataHeight());
            long width = (long)context.getViewWidth(dataEnd - dataStart);
            width = Math.max(width, 1);
            long height = context.getViewportHeight();

            return new LongRect(x, y, width, height);

        }
    }


    public double getItemView(double dataY, XYItem item, ChartContext context) {
        return 0;
    }

    public double getItemValue(double viewY, XYItem item, ChartContext context) {
        return 0;
    }

    public double getItemValueScale(XYItem item, ChartContext context) {
        return -1;
    }


    // --- Private implementation ----------------------------------------------

    
    protected void paint(XYItem item, List<ItemSelection> highlighted,
                       List<ItemSelection> selected, Graphics2D g,
                       Rectangle dirtyArea, SynchronousXYChartContext context) {
//        if (!(item instanceof ProfilerGCXYItem))
//            throw new UnsupportedOperationException("Unsupported item: " + item); // NOI18N

        int valuesCount = item.getValuesCount();
        if (valuesCount < 2) return;
        if (context.getViewWidth() == 0 || context.getViewHeight() == 0) return;

        int[][] visibleBounds = context.getVisibleBounds(dirtyArea);

        int firstFirst = visibleBounds[0][0];
        int index = firstFirst;
        if (index == -1) index = visibleBounds[0][1];
        if (index == -1) return;

        int lastFirst = visibleBounds[1][0];
        int lastIndex = lastFirst;
        if (lastIndex == -1) lastIndex = visibleBounds[1][1];
        if (lastIndex == -1) lastIndex = valuesCount - 1;
        if (lastFirst != -1 && lastIndex < valuesCount - 1) lastIndex += 1;

        int itemsStep = (int)Math.ceil((double)valuesCount / (double)context.getViewWidth());
        if (itemsStep == 0) itemsStep = 1;

        int visibleCount = lastIndex - index + 1;

        if (itemsStep > 1) {
            int firstMod = index % itemsStep;
            index -= firstMod;
            int lastMod = lastIndex % itemsStep;
            lastIndex = lastIndex - lastMod + itemsStep;
            visibleCount = (lastIndex - index) / itemsStep + 1;
            lastIndex = Math.min(lastIndex, valuesCount - 1);
        }

        ProfilerGCXYItem xyItem = (ProfilerGCXYItem)item;

        g.setColor(fillColor);

        int startY = (int)context.getViewY(context.getDataOffsetY() +
                                           context.getDataHeight());
        int height = context.getViewportHeight();

        for (int iter = 0; iter < visibleCount; iter++) {
            long[] gcStarts = xyItem.getGCStarts(index);
            if (gcStarts.length > 0) {
                long[] gcEnds = xyItem.getGCEnds(index);
                for (int i = 0; i < gcStarts.length; i++) {
                    int itemStart = Utils.checkedInt(
                                          context.getViewX(gcStarts[i]));
                    long gcEnd = gcEnds[i];
                    if (gcEnd == -1)
                        gcEnd =  item.getXValue(item.getValuesCount() - 1);
                    int itemLength = Utils.checkedInt(
                                           context.getViewWidth(
                                           gcEnd - gcStarts[i]));

                    g.fillRect(itemStart, startY, Math.max(itemLength, 1), height);
                }
            }
            
            index = Math.min(index + itemsStep, lastIndex);
        }
        
    }

}
