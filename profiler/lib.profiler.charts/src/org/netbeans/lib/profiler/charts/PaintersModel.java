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

package org.netbeans.lib.profiler.charts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jiri Sedlacek
 */
public interface PaintersModel {

    public ItemPainter getPainter(ChartItem item);

    public void addPaintersListener(PaintersListener listener);

    public void removePaintersListener(PaintersListener listener);


    public abstract static class Abstract implements PaintersModel {

        private List<PaintersListener> listeners;


        public void addPaintersListener(PaintersListener listener) {
            if (listeners == null) listeners = new ArrayList();
            if (!listeners.contains(listener)) listeners.add(listener);
        }

        public void removePaintersListener(PaintersListener listener) {
            if (listeners != null) {
                listeners.remove(listener);
                if (listeners.isEmpty()) listeners = null;
            }
        }


        protected void firePaintersChanged() {
            if (listeners != null)
                for (PaintersListener listener : listeners)
                    listener.paintersChanged();
        }

        protected void firePaintersChanged(List<ItemPainter> changedPainters) {
            if (listeners != null)
                for (PaintersListener listener : listeners)
                    listener.paintersChanged(changedPainters);
        }

    }


    public static class Default extends Abstract {

        private final Map<ChartItem, ItemPainter> painters;


        public Default() {
            painters = new HashMap<>();
        }

        public Default(ChartItem[] items, ItemPainter[] painters) {
            this();

            if (items == null)
                throw new IllegalArgumentException("Items cannot be null"); // NOI18N
            if (painters == null)
                throw new IllegalArgumentException("Painters cannot be null"); // NOI18N
            if (items.length != painters.length)
                throw new IllegalArgumentException("Items don't match painters"); // NOI18N

            addPainters(items, painters);
        }


        public void addPainters(ChartItem[] addedItems, ItemPainter[] addedPainters) {
            for (int i = 0; i < addedItems.length; i++)
                painters.put(addedItems[i], addedPainters[i]);
        }

        public void removePainters(ChartItem[] removedItems) {
            for (int i = 0; i < removedItems.length; i++)
                painters.remove(removedItems[i]);
        }


        public ItemPainter getPainter(ChartItem item) {
            return painters.get(item);
        }

    }

}
