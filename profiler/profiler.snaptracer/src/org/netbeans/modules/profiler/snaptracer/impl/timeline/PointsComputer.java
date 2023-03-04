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

import java.awt.Rectangle;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.charts.xy.XYItem;
import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYChartContext;

/**
 *
 * @author Jiri Sedlacek
 */
final class PointsComputer {

    private static final int INDEXES_STEP = 1000;

    private int[] arr1;
    private int[] arr2;
    private final int[] count = new int[1];
    private final int[][] ret = new int[2][];


    PointsComputer() {}


    void reset() {
        arr1 = null;
        arr2 = null;
    }

    private int[] arr1(int size) {
        if (arr1 == null || arr1.length < size)
            arr1 = newArr(size + INDEXES_STEP, true);
        return arr1;
    }
    
    private int[] arr2(int size) {
        if (arr2 == null || arr2.length < size)
            arr2 = newArr(size + INDEXES_STEP, false);
        return arr2;
    }

    private int[] newArr(int size, boolean arr1) {
        int[] arr = new int[size];
        if (arr1) ret[0] = arr;
        return arr;
    }


    int[][] getVisible(Rectangle dirtyArea, int valuesCount,
                       SynchronousXYChartContext context,
                       int extraFactor, int extraTrailing) {

        if (context.getViewWidth() == 0) return null;
        
        int[][] visibleBounds = context.getVisibleBounds(dirtyArea);

        int firstFirst = visibleBounds[0][0];
        int firstIndex = firstFirst;
        if (firstIndex == -1) firstIndex = visibleBounds[0][1];
        if (firstIndex == -1) return null;
        // firstIndex - 2: workaround for polyline joins
        if (firstFirst != -1) firstIndex = Math.max(firstIndex - 2, 0);

        int lastFirst = visibleBounds[1][0];
        int lastIndex = lastFirst;
        if (lastIndex == -1) lastIndex = visibleBounds[1][1];
        if (lastIndex == -1) lastIndex = valuesCount - 1;
        // lastIndex + 2: workaround for polyline joins
        if (lastFirst != -1) lastIndex = Math.min(lastIndex + 2, valuesCount - 1);

        int itemsStep = (int)Math.ceil(valuesCount / context.getViewWidth());
        if (itemsStep == 0) itemsStep = 1;

        int visibleCount = lastIndex - firstIndex + 1;

        if (itemsStep > 1) {
            int firstMod = firstIndex % itemsStep;
            firstIndex -= firstMod;
            int lastMod = lastIndex % itemsStep;
            lastIndex = lastIndex - lastMod + itemsStep;
            visibleCount = (lastIndex - firstIndex) / itemsStep + 1;
            lastIndex = Math.min(lastIndex, valuesCount - 1);
        }

        int visibleCountEx = extraFactor == 1 ? visibleCount :
                            (visibleCount - 1) * extraFactor + 2;

        count[0] = visibleCountEx + extraTrailing;
        int[] visibleIndexes = arr1(count[0]);

        for (int i = 0; i < visibleCountEx; i++) {
            int index = firstIndex + (i / extraFactor) * itemsStep;
            if (index > lastIndex) index = lastIndex;
            visibleIndexes[i] = index;
        }
        
        for (int i = visibleCountEx; i < visibleIndexes.length; i++)
            visibleIndexes[i] = -1;

        ret[1] = count;
        return ret;
    }

    int getZeroY(SynchronousXYChartContext context) {
        int zeroY = Utils.checkedInt(context.getViewY(context.getDataOffsetY()));
        zeroY = Math.max(Utils.checkedInt(context.getViewportOffsetY()), zeroY);
        zeroY = Math.min(Utils.checkedInt(context.getViewportOffsetY() +
                                          context.getViewportHeight()), zeroY);
        return zeroY;
    }

    int[][] createPoints(int[] indexes, int itemsCount, XYItem item,
                         double dataFactor, SynchronousXYChartContext context) {

        int[] xPoints = indexes;
        int[] yPoints = arr2(itemsCount);

        for (int i = 0; i < itemsCount; i++) {
            int dataIndex = xPoints[i];
            if (dataIndex != -1) {
                xPoints[i] = Utils.checkedInt(Math.ceil(
                             context.getViewX(item.getXValue(dataIndex))));
                yPoints[i] = Utils.checkedInt(Math.ceil(
                             context.getViewY(item.getYValue(dataIndex) *
                             dataFactor)));
            }
        }

        ret[1] = yPoints;
        return ret;
    }

}
