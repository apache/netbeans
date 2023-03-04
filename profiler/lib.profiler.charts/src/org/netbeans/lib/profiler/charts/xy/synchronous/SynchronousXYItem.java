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

package org.netbeans.lib.profiler.charts.xy.synchronous;

import org.netbeans.lib.profiler.charts.ChartItemListener;
import org.netbeans.lib.profiler.charts.swing.LongRect;
import org.netbeans.lib.profiler.charts.xy.XYItem;
import org.netbeans.lib.profiler.charts.xy.XYItemChange;
import org.netbeans.lib.profiler.charts.Timeline;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class SynchronousXYItem implements XYItem {

    private final String name;

    private Timeline timeline;

    private int lastIndex;
    
    private final LongRect bounds;
    private long initialMinY;
    private long initialMaxY;

    private LongRect initialBounds;

    private long minY;
    private long maxY;


    // --- Constructor ---------------------------------------------------------

    public SynchronousXYItem(String name) {
        this(name, Long.MAX_VALUE);
    }

    public SynchronousXYItem(String name, long initialMinY) {
        this(name, initialMinY, Long.MIN_VALUE);
    }

    public SynchronousXYItem(String name, long initialMinY, long initialMaxY) {
        this.name = name;
        this.initialMinY = initialMinY;
        this.initialMaxY = initialMaxY;
        minY = Long.MAX_VALUE;
        maxY = Long.MIN_VALUE;
        bounds = new LongRect();
        initialBounds = new LongRect();
        lastIndex = -1;
    }


    // --- Item telemetry ------------------------------------------------------

    public String getName() { return name; }

    public void setInitialBounds(LongRect initialBounds) { this.initialBounds = initialBounds; }

    public LongRect getInitialBounds() { return initialBounds; }

    public XYItemChange valuesChanged() {

        int index = timeline.getTimestampsCount() - 1;
        XYItemChange change = null;

        if (lastIndex == index) { // No change

            LongRect b = new LongRect(bounds);
            change = new XYItemChange.Default(this, new int[] { -1 }, b, b, b);

        } else if (index > -1) { // New item(s)

            // Save oldBounds, setup dirtyBounds
            LongRect oldBounds = new LongRect(bounds);
            LongRect dirtyBounds = new LongRect();

            boolean initBounds = lastIndex == -1;
            int dirtyIndex = lastIndex == -1 ? 0 : lastIndex;

            // Process other values
            for (int i = dirtyIndex; i <= index; i++) {

                long timestamp = timeline.getTimestamp(i);
                long value = getYValue(i);

                // Update item minY/maxY
                minY = Math.min(value, minY);
                maxY = Math.max(value, maxY);

                // Process item bounds
                if (initBounds) {
                    // Initialize item bounds
                    bounds.x = timestamp;
                    bounds.y = Math.min(value, initialMinY);
                    bounds.width = 0;
                    bounds.height = Math.max(value, initialMaxY) - bounds.y;
                    initBounds = false;
                } else {
                    // Update item bounds
                    LongRect.add(bounds, timestamp, value);
                }

                // Process dirty bounds
                if (i == dirtyIndex) {
                    // Setup dirty bounds
                    dirtyBounds.x = timestamp;
                    dirtyBounds.y = value;
                    dirtyBounds.width = timeline.getTimestamp(index) - dirtyBounds.x;
                } else {
                    // Update dirty y/height
                    long dirtyY = dirtyBounds.y;
                    dirtyBounds.y = Math.min(dirtyY, value);
                    dirtyBounds.height = Math.max(dirtyY, value) - dirtyBounds.y;
                }

            }

            // Return ItemChange
            int indexesCount = index - lastIndex;
            int[] indexes = new int[indexesCount];
            for (int i = 0; i < indexesCount; i++) indexes[i] = lastIndex + 1 + i;
            change = new XYItemChange.Default(this, indexes, oldBounds,
                                              new LongRect(bounds), dirtyBounds);

        } else { // Reset

            minY = Long.MAX_VALUE;
            maxY = Long.MIN_VALUE;

            // Save oldBounds
            LongRect oldBounds = new LongRect(bounds);
            LongRect.set(bounds, 0, 0, 0, 0);

            // Return ItemChange
            change = new XYItemChange.Default(this, new int[] { -1 }, oldBounds,
                                            new LongRect(bounds), oldBounds);

        }
        
        lastIndex = index;
        return change;
        
    }

    public int getValuesCount() { return timeline.getTimestampsCount(); }

    public long getXValue(int index) { return timeline.getTimestamp(index); }

    public abstract long getYValue(int index);

    public long getMinYValue() { return minY; }

    public long getMaxYValue() { return maxY; }
    
    public LongRect getBounds() {
        if (getValuesCount() == 0) return initialBounds;
        else return bounds;
    }


    // --- ChartItem implementation (ChartItemListener not supported) ----------

    public void addItemListener(ChartItemListener listener) {}

    public void removeItemListener(ChartItemListener listener) {}


    // --- Internal interface --------------------------------------------------

    void setTimeline(Timeline timeline) { this.timeline = timeline; }

}
