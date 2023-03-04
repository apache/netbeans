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

package org.netbeans.modules.profiler.heapwalk.memorylint;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.openide.util.NbBundle;


/**
 *
 * @param T the entry type, which can add additional properties
 * @author nenik
 */
public final class Histogram<T extends Histogram.Entry> {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    /**
     *
     */
    public static class Entry<T extends Entry> {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private int count;
        private long size;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        /**
         * Creates an entry with given size and count=1
         * @param size the size this entry represent.
         */
        public Entry(long size) {
            this.count = 1;
            this.size = size;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public final int getCount() {
            return count;
        }

        public final long getSize() {
            return size;
        }

        @Override
        public String toString() {
            return "#:" + getCount() + "/" + getSize() + "B\n"; // NOI18N
        }

        /**
         * A callback for subclasses that should add the additional properties
         * from given source to this entry. The infrastructure takes care of
         * adding base properties. There's no need to call super.add().
         *
         * @param source The Entry to add to this
         */
        protected void add(T source) {
        }

        private void doAdd(T entry) {
            count += ((Entry)entry).count;
            size += ((Entry)entry).size;
            add(entry);
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private HashMap<String, T> map = new HashMap<String, T>();

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     *
     */
    public Histogram() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static final Comparator<Entry> sortByCount() {
        return new Comparator<Entry>() {
                public int compare(Entry o1, Entry o2) {
                    return o2.count - o1.count;
                }
            };
    }

    public static final Comparator<Entry> sortBySize() {
        return new Comparator<Entry>() {
                public int compare(Entry o1, Entry o2) {
                    return Long.compare(o2.size,o1.size);
                }
            };
    }

    public SortedMap<String, T> getResults(final Comparator<Entry> comparator) {
        SortedMap<String, T> sm = new TreeMap<String, T>(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    T t1 = map.get(o1);
                    T t2 = map.get(o2);
                    int delta = comparator.compare(t1, t2);

                    if (delta == 0) {
                        delta = o1.compareTo(o2);
                    }

                    return delta;
                }
            });
        sm.putAll(map);

        return sm;
    }

    public void add(String key, T entry) {
        T current = map.get(key);

        if (current != null) {
            ((Entry)current).doAdd(entry);
        } else {
            map.put(key, entry);
        }
    }

    public String toString(int treshold) {
        StringBuilder result = new StringBuilder();
        long totalSize = 0;
        int totalCount = 0;
        SortedMap<String, T> bySize = getResults(sortBySize());

        for (Map.Entry<String, T> entry : bySize.entrySet()) {
            long size = entry.getValue().getSize();

            if (size > treshold) {
                result.append(entry.getKey()).append(": ").append(entry.getValue()).append("<br>"); // NOI18N
            }

            totalSize += size;
            totalCount += entry.getValue().getCount();
        }

        result.append(NbBundle.getMessage(Histogram.class, "FMT_HistogramSum", totalCount, totalSize));

        return result.toString();
    }
}
