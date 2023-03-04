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

package org.netbeans.lib.profiler.charts;

/**
 *
 * @author Jiri Sedlacek
 */
public interface ItemSelection {

    public static final int DISTANCE_UNKNOWN = Integer.MAX_VALUE;


    public ChartItem getItem();

    public int getDistance();


    public static class Default implements ItemSelection {

        private ChartItem item;
        private int distance;


        public Default(ChartItem item) {
            this(item, DISTANCE_UNKNOWN);
        }

        public Default(ChartItem item, int distance) {
            this.item = item;
            this.distance = distance;
        }


        public ChartItem getItem() {
            return item;
        }

        public int getDistance() {
            return distance;
        }


        public boolean equals(Object o) {
            if (!(o instanceof ItemSelection)) return false;
            ItemSelection selection = (ItemSelection)o;
            return selection.getItem() == item;
        }

        public int hashCode() {
            return item.hashCode();
        }

    }

}
