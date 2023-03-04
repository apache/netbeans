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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jiri Sedlacek
 */
public interface ChartItem {
    
    public void addItemListener(ChartItemListener listener);
    
    public void removeItemListener(ChartItemListener listener);


    public abstract static class Abstract implements ChartItem {


        private List<ChartItemListener> listeners;


        public void addItemListener(ChartItemListener listener) {
            if (listeners == null) listeners = new ArrayList();
            if (!listeners.contains(listener)) listeners.add(listener);
        }

        public void removeItemListener(ChartItemListener listener) {
            if (listeners != null) listeners.remove(listener);
        }


        protected void fireItemChanged(ChartItemChange itemChange) {
            if (listeners != null)
                for (ChartItemListener listener : listeners)
                    listener.chartItemChanged(itemChange);
        }

    }

}
