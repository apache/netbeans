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
package org.netbeans.modules.profiler.heapwalk.details.jdk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 * @author Tomas Hurka
 */
@ServiceProvider(service=DetailsProvider.class)
public final class IoDetailsProvider extends DetailsProvider.Basic {
    
    private static final String FILE_MASK = "java.io.File+";                    // NOI18N
    private static final String ZIPFILE_MASK = "java.util.zip.ZipFile+";        // NOI18N
    private static final String RAF_MASK = "java.io.RandomAccessFile";          // NOI18N
    private static final String FIS_MASK = "java.io.FileInputStream";           // NOI18N
    private static final String FOS_MASK = "java.io.FileOutputStream";          // NOI18N
    private static final String FD_MASK = "java.io.FileDescriptor";             // NOI18N
    private static final String FD_RAF_CLASS = "java.io.RandomAccessFile";      // NOI18N
    private static final String FD_FIS_CLASS = "java.io.FileInputStream";       // NOI18N
    private static final String FD_FOS_CLASS = "java.io.FileOutputStream";      // NOI18N
    private static final String FCI_MASK = "sun.nio.ch.FileChannelImpl";        // NOI18N
    private static final String HEAPCHARBUFFER_MASK = "java.nio.HeapCharBuffer";// NOI18N
    
    private static final Object CACHE_LOCK = new Object();
    private static WeakHashMap<Heap,Map<Long,String>> CACHE;
    
    public IoDetailsProvider() {
        super(FILE_MASK, ZIPFILE_MASK, RAF_MASK, FIS_MASK, FOS_MASK, FD_MASK, FCI_MASK,
              HEAPCHARBUFFER_MASK);
    }
    
    public String getDetailsString(String className, Instance instance, Heap heap) {
        if (FILE_MASK.equals(className)) {                                      // File+
            return DetailsUtils.getInstanceFieldString(instance, "path", heap); // NOI18N
        } else if (ZIPFILE_MASK.equals(className)) {                            // ZipFile+
            return DetailsUtils.getInstanceFieldString(instance, "name", heap); // NOI18N
        } else if (RAF_MASK.equals(className)) {                                // RandomAccessFile
            return DetailsUtils.getInstanceFieldString(instance, "path", heap); // NOI18N
        } else if (FIS_MASK.equals(className)) {                                // FileInputStrea
            return DetailsUtils.getInstanceFieldString(instance, "path", heap); // NOI18N
        } else if (FOS_MASK.equals(className)) {                                // FileOutputStream
            return DetailsUtils.getInstanceFieldString(instance, "path", heap); // NOI18N
        } else if (FD_MASK.equals(className)) {                                 // FileDescriptor
            synchronized (CACHE_LOCK) {
                if (CACHE == null) {
                    CACHE = new WeakHashMap();
                }
                Map<Long,String> heapCache = CACHE.get(heap);
                if (heapCache == null) {
                    heapCache = computeFDCache(heap, instance.getJavaClass());
                    CACHE.put(heap, heapCache);
                }
                return heapCache.get(instance.getInstanceId());
            }
        } else if (FCI_MASK.equals(className)) {                                // FileChannelImpl
            return DetailsUtils.getInstanceFieldString(instance, "path", heap); // NOI18N
        } else if (HEAPCHARBUFFER_MASK.equals(className)) {
            int position = DetailsUtils.getIntFieldValue(instance, "position", -1); // NOI18N                                 // NOI18N
            int limit = DetailsUtils.getIntFieldValue(instance, "limit", -1);       // NOI18N                // NOI18N
            int offset = DetailsUtils.getIntFieldValue(instance, "offset", -1);       // NOI18N                // NOI18N
            return DetailsUtils.getPrimitiveArrayFieldString(instance, "hb", position + offset, limit - position, null, "...");
        }
        
        return null;
    }

    private Map<Long, String> computeFDCache(Heap heap, JavaClass fdClass) {
        Map<Long, String> cache = new HashMap<>();
        computeFDCacheForClass(heap, FD_RAF_CLASS, "fd", cache);                // NOI18N
        computeFDCacheForClass(heap, FD_FIS_CLASS, "fd", cache);                // NOi18N
        computeFDCacheForClass(heap, FD_FOS_CLASS, "fd", cache);                // NOI18N
        computeStdDescriptor(fdClass, "in", "Standard Input", cache);           // NOI18N
        computeStdDescriptor(fdClass, "out", "Standard Output", cache);         // NOi18N
        computeStdDescriptor(fdClass, "err", "Standard Error", cache);          // NOi18N
        return cache;
    }

    private void computeFDCacheForClass(Heap heap, String className, String fieldName, Map<Long, String> cache) {
        JavaClass rafClass = heap.getJavaClassByName(className);
        if (rafClass != null) {
            for (Instance raf : (List<Instance>)rafClass.getInstances()) {
                Instance fd = (Instance)raf.getValueOfField(fieldName);
                if (fd != null) {
                    String details = getDetailsString(className,raf,heap);
                    if (details != null) {
                        cache.put(fd.getInstanceId(), details);
                    }
                }
            }
        }
    }

    private void computeStdDescriptor(JavaClass fdClass, String field, String text, Map<Long, String> cache) {
        Instance stdFd = (Instance) fdClass.getValueOfStaticField(field);
        
        if (stdFd != null) {
            cache.put(stdFd.getInstanceId(), text);
        }
    }
    
}
