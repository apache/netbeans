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

package org.netbeans.lib.profiler.charts.xy;

import org.netbeans.lib.profiler.charts.ChartItemChange;
import org.netbeans.lib.profiler.charts.swing.LongRect;

/**
 *
 * @author Jiri Sedlacek
 */
public interface XYItemChange extends ChartItemChange {

    public XYItem getItem();

    public int[] getValuesIndexes();

    public LongRect getOldValuesBounds();

    public LongRect getNewValuesBounds();

    public LongRect getDirtyValuesBounds();


    public static class Default extends ChartItemChange.Default implements XYItemChange {

        private final int[] valuesIndexes;
        private final LongRect oldBounds;
        private final LongRect newBounds;
        private final LongRect dirtyBounds;


        public Default(XYItem item, int[] valuesIndexes, LongRect oldBounds,
                       LongRect newBounds, LongRect dirtyBounds) {
            super(item);
            this.valuesIndexes = valuesIndexes;
            this.oldBounds = oldBounds;
            this.newBounds = newBounds;
            this.dirtyBounds = dirtyBounds;
        }


        public XYItem getItem() { return (XYItem)super.getItem(); }

        public int[] getValuesIndexes() { return valuesIndexes; }

        public LongRect getOldValuesBounds() { return oldBounds; }

        public LongRect getNewValuesBounds() { return newBounds; }

        public LongRect getDirtyValuesBounds() { return dirtyBounds; }

    }

}
