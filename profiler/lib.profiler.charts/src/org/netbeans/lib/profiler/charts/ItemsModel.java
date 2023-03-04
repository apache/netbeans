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
public interface ItemsModel {

    public int getItemsCount();

    public ChartItem getItem(int index);

    public void addItemsListener(ItemsListener listener);

    public void removeItemsListener(ItemsListener listener);


    public abstract static class Abstract implements ItemsModel {

        private List<ItemsListener> listeners;


        public void addItemsListener(ItemsListener listener) {
            if (listeners == null) listeners = new ArrayList();
            if (!listeners.contains(listener)) listeners.add(listener);
        }

        public void removeItemsListener(ItemsListener listener) {
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) listeners = null;
            }
        }


        protected void fireItemsAdded(List<ChartItem> addedItems) {
            if (listeners != null)
                for (ItemsListener listener : listeners)
                    listener.itemsAdded(addedItems);
        }

        protected void fireItemsRemoved(List<ChartItem> removedItems) {
            if (listeners != null)
                for (ItemsListener listener : listeners)
                    listener.itemsRemoved(removedItems);
        }

        protected void fireItemsChanged(List<ChartItemChange> itemChanges) {
            if (listeners != null)
                for (ItemsListener listener : listeners)
                    listener.itemsChanged(itemChanges);
        }

    }

}
