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

package org.netbeans.lib.profiler.charts.xy.synchronous;

import org.netbeans.lib.profiler.charts.Timeline;
import org.netbeans.lib.profiler.charts.ChartItemChange;
import org.netbeans.lib.profiler.charts.ItemsModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.lib.profiler.charts.ChartItem;

/**
 *
 * @author Jiri Sedlacek
 */
public class SynchronousXYItemsModel extends ItemsModel.Abstract {

    private final ArrayList<SynchronousXYItem> items = new ArrayList<>();
    private final Timeline timeline;


    // --- Constructor ---------------------------------------------------------

    public SynchronousXYItemsModel(Timeline timeline) {
        this.timeline = timeline;
    }

    public SynchronousXYItemsModel(Timeline timeline, SynchronousXYItem[] items) {
        this(timeline);

        if (items == null)
            throw new IllegalArgumentException("Items cannot be null"); // NOI18N
        if (items.length == 0)
            throw new IllegalArgumentException("Items cannot be empty"); // NOI18N

        addItems(items);
    }


    // --- Public interface ----------------------------------------------------

    public void addItems(SynchronousXYItem[] addedItems) {
        for (int i = 0; i < addedItems.length; i++) {
            addedItems[i].setTimeline(timeline);
            items.add(addedItems[i]);
        }
        
        fireItemsAdded(Arrays.asList((ChartItem[])addedItems));

        if (timeline.getTimestampsCount() > 0) valuesAdded();
    }

    public void removeItems(SynchronousXYItem[] removedItems) {
        for (SynchronousXYItem item : removedItems) items.remove(item);
        fireItemsRemoved(Arrays.asList((ChartItem[])removedItems));
    }


    public final void valuesAdded() {
        // Update values
        List<ChartItemChange> itemChanges = new ArrayList<>(items.size());
        for (SynchronousXYItem item : items) itemChanges.add(item.valuesChanged());
        fireItemsChanged(itemChanges);

        // Check timestamp
        int valueIndex = timeline.getTimestampsCount() - 1;
        long timestamp = timeline.getTimestamp(valueIndex);
        long previousTimestamp = valueIndex == 0 ? -1 :
                                 timeline.getTimestamp(valueIndex - 1);
        
        if (previousTimestamp != -1 && previousTimestamp >= timestamp)
// See #168544
//            throw new IllegalArgumentException(
//                           "ProfilerXYItemsModel: new timestamp " + timestamp + // NOI18N
//                           " not greater than previous " + previousTimestamp + // NOI18N
//                           ", skipping the values."); // NOI18N
            System.err.println("WARNING [" + SynchronousXYItemsModel.class.getName() + // NOI18N
                               "]: ProfilerXYItemsModel: new timestamp " + // NOI18N
                               timestamp + " not greater than previous " + // NOI18N
                               previousTimestamp + ", skipping the values."); // NOI18N
    }

    public final void valuesReset() {
        // Update values
        List<ChartItemChange> itemChanges = new ArrayList<>(items.size());
        for (SynchronousXYItem item : items) itemChanges.add(item.valuesChanged());
        fireItemsChanged(itemChanges);
    }


    public final Timeline getTimeline() {
        return timeline;
    }


    // --- AbstractItemsModel implementation -----------------------------------

    public final int getItemsCount() { return items.size(); }

    public final SynchronousXYItem getItem(int index) { return items.get(index); }

}
