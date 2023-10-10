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

import org.netbeans.modules.profiler.snaptracer.impl.timeline.items.ValueItemDescriptor;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.lib.profiler.charts.Timeline;
import org.netbeans.lib.profiler.charts.xy.synchronous.SynchronousXYItem;
import org.netbeans.modules.profiler.snaptracer.ProbeItemDescriptor;

/**
 * All methods must be invoked from the EDT.
 *
 * @author Jiri Sedlacek
 */
final class TimelineModel implements Timeline {

    private static final int STORAGE_BUFFER_STEP = 50;

    private int bufferStep;

    private int valuesCount;
    private long[] timestamps;
    private long[][] values;

    private final List<TimelineXYItem> items = new ArrayList<>();


    TimelineModel() {
        this(STORAGE_BUFFER_STEP);
    }
    
    private TimelineModel(int bufferStep) {
        this.bufferStep = bufferStep;
        reset();
    }


    int getItemsCount() {
        return items.size();
    }


    TimelineXYItem[] createItems(ProbeItemDescriptor[] itemDescriptors) {
        int itemsCount = values == null ? 0 : values.length;
        int addedItemsCount = itemDescriptors.length;
        TimelineXYItem[] itemsArr = new TimelineXYItem[addedItemsCount];

        for (int i = 0; i < addedItemsCount; i++) {
            if (itemDescriptors[i] instanceof ValueItemDescriptor) {
                ValueItemDescriptor d = (ValueItemDescriptor)itemDescriptors[i];
                itemsArr[i] = new TimelineXYItem(d.getName(), d.getMinValue(),
                                                 d.getMaxValue(), itemsCount + i) {
                    public long getYValue(int valueIndex) {
                        return values[getIndex()][valueIndex];
                    }
                };
            } else {
                // Reserved for non-value items
            }
            items.add(itemsArr[i]);
        }

        addItemsImpl(addedItemsCount);

        return itemsArr;
    }

    void removeItems(SynchronousXYItem[] removed) {
        removeItemsImpl(removed.length);

        int firstRemovedIndex = ((TimelineXYItem)removed[0]).getIndex();
        for (SynchronousXYItem item : removed)
            items.remove(firstRemovedIndex);
        for (int i = firstRemovedIndex; i < items.size(); i++)
            items.get(i).setIndex(i);
    }


    void addValues(long timestamp, long[] newValues) {
        updateStorage();
        
        // Check last timestamp whether greater than the new one
        long lastTimestamp = valuesCount == 0 ? -1 : timestamps[valuesCount - 1];
        // Silently increase timestamp, JVM was busy - timer out of sync
        if (lastTimestamp >= timestamp) timestamp = lastTimestamp + 1;

        timestamps[valuesCount] = timestamp;
        for (int i = 0; i < values.length; i++)
            values[i][valuesCount] = newValues[i];

        valuesCount++;
    }
    

    void reset() {
        valuesCount = 0;

        timestamps = null;
        if (values != null) {
            if (values.length == 0) {
                values = null;
            } else {
                for (int i = 0; i < values.length; i++)
                    values[i] = new long[0];
            }
        }
    }


    private void addItemsImpl(int addedItemsCount) {
        int newItemsCount = (values == null ? 0 : values.length) + addedItemsCount;
        values = new long[newItemsCount][];
        reset();
    }

    private void removeItemsImpl(int removedItemsCount) {
        values = new long[values.length - removedItemsCount][];
        reset();
    }


    public int getTimestampsCount() {
        return valuesCount;
    }

    public long getTimestamp(int index) {
        return timestamps[index];
    }


    private void updateStorage() {
        if (timestamps == null) {
            timestamps = new long[bufferStep];
            for (int i = 0; i < values.length; i++)
                values[i] = new long[bufferStep];
        } else if (valuesCount == timestamps.length) {
            timestamps = extendArray(timestamps, bufferStep);
            for (int i = 0; i < values.length; i++)
                values[i] = extendArray(values[i], bufferStep);
        }
    }

    private static long[] extendArray(long[] array, int extraLength) {
        int originalLength = array.length;
        long[] newArray = new long[originalLength + extraLength];
        System.arraycopy(array, 0, newArray, 0, originalLength);
        return newArray;
    }

}
