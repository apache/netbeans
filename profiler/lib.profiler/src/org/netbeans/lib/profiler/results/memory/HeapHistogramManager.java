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

package org.netbeans.lib.profiler.results.memory;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.lib.profiler.ProfilerEngineSettings;
import org.netbeans.lib.profiler.filters.GenericFilter;
import org.netbeans.lib.profiler.wireprotocol.HeapHistogramResponse;

/**
 *
 * @author Tomas Hurka
 */
public class HeapHistogramManager {

    private Map<Integer, String> classesIdMap = new HashMap<>(8000);
    private final ProfilerEngineSettings settings;

    public HeapHistogramManager(ProfilerEngineSettings settings) {
        this.settings = settings;
    }

    public HeapHistogram getHistogram(HeapHistogramResponse resp) {
        String[] newNames = resp.getNewNames();
        int[] newIds = resp.getNewids();

        for (int i = 0; i < newNames.length; i++) {
            classesIdMap.put(newIds[i], newNames[i]);
        }
        int ids[] = resp.getIds();
        long instances[] = resp.getInstances();
        long bytes[] = resp.getBytes();
        HeapHistogramImpl histogram = new HeapHistogramImpl(resp.getTime());
        GenericFilter classFilter = settings.getInstrumentationFilter();
        for (int i = 0; i < ids.length; i++) {
            String className = classesIdMap.get(ids[i]);
            
            if (classFilter.passes(className.replace('.', '/'))) { // NOI18N
                ClassInfoImpl ci = new ClassInfoImpl(className, instances[i], bytes[i]);
                histogram.addClassInfo(ci, false);
            }
        }
        return histogram;
    }

    class HeapHistogramImpl extends HeapHistogram {

        private Date time;
        private long totalHeapInstances;
        private long totalHeapBytes;
        private Set<ClassInfo> heap;
        private long totalPermInstances;
        private long totalPermBytes;
        private Set<ClassInfo> perm;

        HeapHistogramImpl(Date t) {
            time = t;
            heap = new HashSet<>(4096);
            perm = new HashSet<>();
        }

        void addClassInfo(ClassInfo ci, boolean permInfo) {
            if (permInfo) {
                perm.add(ci);
                totalPermInstances += ci.getInstancesCount();
                totalPermBytes += ci.getBytes();
            } else {
                heap.add(ci);
                totalHeapInstances += ci.getInstancesCount();
                totalHeapBytes += ci.getBytes();
            }
        }

        @Override
        public Date getTime() {
            return time;
        }

        @Override
        public long getTotalInstances() {
            return totalHeapInstances + totalPermInstances;
        }

        @Override
        public long getTotalBytes() {
            return totalHeapBytes + totalPermBytes;
        }

        @Override
        public Set<ClassInfo> getHeapHistogram() {
            return heap;
        }

        @Override
        public long getTotalHeapInstances() {
            return totalHeapInstances;
        }

        @Override
        public long getTotalHeapBytes() {
            return totalHeapBytes;
        }

        @Override
        public Set<ClassInfo> getPermGenHistogram() {
            return perm;
        }

        @Override
        public long getTotalPerGenInstances() {
            return totalPermInstances;
        }

        @Override
        public long getTotalPermGenHeapBytes() {
            return totalPermBytes;
        }
    }

    static class ClassInfoImpl extends HeapHistogram.ClassInfo {

        private String name;
        private long instances;
        private long bytes;

        ClassInfoImpl(String n, long i, long b) {
            name = n;
            instances = i;
            bytes = b;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getInstancesCount() {
            return instances;
        }

        @Override
        public long getBytes() {
            return bytes;
        }
    }
}
