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

import org.netbeans.lib.profiler.charts.ItemSelection;

/**
 *
 * @author Jiri Sedlacek
 */
public interface XYItemSelection extends ItemSelection {

    public int getValueIndex();

    public XYItem getItem();


    public static class Default extends ItemSelection.Default implements XYItemSelection {

        private int valueIndex;


        public Default(XYItem item, int valueIndex) {
            this(item, valueIndex, DISTANCE_UNKNOWN);
        }

        public Default(XYItem item, int valueIndex, int distance) {
            super(item, distance);
            this.valueIndex = valueIndex;
        }


        public XYItem getItem() {
            return (XYItem)super.getItem();
        }

        public int getValueIndex() {
            return valueIndex;
        }


        public boolean equals(Object o) {
            if (!super.equals(o)) return false;
            if (!(o instanceof XYItemSelection)) return false;

            XYItemSelection selection = (XYItemSelection)o;
            return selection.getValueIndex() == valueIndex;
        }

        public int hashCode() {
            return super.hashCode() + valueIndex;
        }

    }

}
