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

import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.NbBundle;


/**
 * An object collector that classifies the objects according to their type.
 * For each logged type, it counts the number of instances and sums their total
 * size.
 *
 * @param T the entry type, which can add additional properties
 * @author nenik
 */
public final class Distribution {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    /**
     *
     */
    public static class Entry {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private JavaClass type;
        private int count;
        private int size;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        /**
         * Creates an entry with given size and count=1
         * @param size the size this entry represent.
         */
        private Entry(JavaClass cls) {
            this.type = cls;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public final int getCount() {
            return count;
        }

        public final int getSize() {
            return size;
        }

        @Override
        public String toString() {
            return Utils.printClass(null, type.getName()) + ": " + getCount() + "/" + getSize() + "B"; // NOI18N
        }

        private void count(Instance in) {
            assert ((type == null) || type.equals(in.getJavaClass()));
            count++;
            size += in.getSize();
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Entry allEntry = new Entry(null);
    private HashMap<JavaClass, Entry> map = new HashMap<JavaClass, Entry>();
    private Set<Instance> counted = new HashSet<Instance>();

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     *
     */
    public Distribution() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Set<JavaClass> getClasses() {
        return Collections.unmodifiableSet(map.keySet());
    }

    public boolean isCounted(Instance in) {
        return counted.contains(in);
    }

    public Entry getResults(JavaClass cls) {
        return map.get(cls);
    }

    public void add(Instance in) {
        if (!counted.add(in)) {
            return;
        }

        JavaClass cls = in.getJavaClass();
        Entry en = map.get(cls);

        if (en == null) {
            map.put(cls, en = new Entry(cls));
        }

        en.count(in);
        allEntry.count(in);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(NbBundle.getMessage(Distribution.class, "FMT_DistEntry",
                allEntry.getCount(),
                allEntry.getSize()));

        for (JavaClass key : getClasses()) {
            result.append("  ").append(getResults(key)).append("\n"); // NOI18N
        }

        return result.toString();
    }
}
