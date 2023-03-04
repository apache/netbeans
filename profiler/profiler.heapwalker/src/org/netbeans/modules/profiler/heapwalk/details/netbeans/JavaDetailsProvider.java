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
package org.netbeans.modules.profiler.heapwalk.details.netbeans;

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Hurka
 */
@ServiceProvider(service=DetailsProvider.class)
public class JavaDetailsProvider extends DetailsProvider.Basic {

    private static final String FO_INDEXABLE = "org.netbeans.modules.parsing.impl.indexing.FileObjectIndexable"; // NOI18N
    private static final String INDEXABLE = "org.netbeans.modules.parsing.spi.indexing.Indexable"; // NOI18N
    private static final String CLASSPATH_ENTRY = "org.netbeans.api.java.classpath.ClassPath$Entry";    // NOI18N
    
    long lastHeapId;
    String lastSeparator;
    
    public JavaDetailsProvider() {
        super(FO_INDEXABLE,INDEXABLE,CLASSPATH_ENTRY);
    }

    @Override
    public String getDetailsString(String className, Instance instance, Heap heap) {
        if (FO_INDEXABLE.equals(className))  {
            String root = DetailsUtils.getInstanceFieldString(instance, "root", heap); // NOI18N
            String relpath = DetailsUtils.getInstanceFieldString(instance, "relativePath", heap); // NOI18N
            if (root != null && relpath != null) {
                return root.concat(getFileSeparator(heap)).concat(relpath);    
            }
        } else if (INDEXABLE.equals(className)) {
            return DetailsUtils.getInstanceFieldString(instance, "delegate", heap); // NOI18N
        } else if (CLASSPATH_ENTRY.equals(className)) {
            return DetailsUtils.getInstanceFieldString(instance, "url", heap);  // NOI18N
        }
        return null;
    }
    
    private String getFileSeparator(Heap heap) {
        if (lastHeapId != System.identityHashCode(heap)) {
            lastSeparator = heap.getSystemProperties().getProperty("file.separator","/"); // NOI18N
        }
        return lastSeparator;
    }
}
