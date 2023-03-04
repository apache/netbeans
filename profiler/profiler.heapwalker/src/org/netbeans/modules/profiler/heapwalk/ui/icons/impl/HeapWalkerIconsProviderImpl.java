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
package org.netbeans.modules.profiler.heapwalk.ui.icons.impl;

import org.netbeans.modules.profiler.heapwalk.ui.icons.HeapWalkerIcons;
import java.util.Map;
import org.netbeans.modules.profiler.spi.IconsProvider;

/**
 *
 * @author Jiri Sedlacek
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.profiler.spi.IconsProvider.class)
public final class HeapWalkerIconsProviderImpl extends IconsProvider.Basic {
    
    @Override
    protected final void initStaticImages(Map<String, String> cache) {
        cache.put(HeapWalkerIcons.CLASSES, "classes.png"); // NOI18N
        cache.put(HeapWalkerIcons.DATA, "data.png"); // NOI18N
        cache.put(HeapWalkerIcons.GC_ROOT, "gcRoot.png"); // NOI18N
        cache.put(HeapWalkerIcons.GC_ROOTS, "gcRoots.png"); // NOI18N
        cache.put(HeapWalkerIcons.INCOMING_REFERENCES, "incomingRef.png"); // NOI18N
        cache.put(HeapWalkerIcons.INSTANCES, "instances.png"); // NOI18N
        cache.put(HeapWalkerIcons.LOOP, "loop.png"); // NOI18N
        cache.put(HeapWalkerIcons.MEMORY_LINT, "memoryLint.png"); // NOI18N
        cache.put(HeapWalkerIcons.PROGRESS, "progress.png"); // NOI18N
        cache.put(HeapWalkerIcons.PROPERTIES, "properties.png"); // NOI18N
        cache.put(HeapWalkerIcons.RULES, "rules.png"); // NOI18N
        cache.put(HeapWalkerIcons.SAVED_OQL_QUERIES, "savedOQL.png"); // NOI18N
        cache.put(HeapWalkerIcons.STATIC, "static.png"); // NOI18N
        cache.put(HeapWalkerIcons.SYSTEM_INFO, "sysinfo.png"); // NOI18N
        cache.put(HeapWalkerIcons.WINDOW, "window.png"); // NOI18N
        cache.put(HeapWalkerIcons.BIGGEST_OBJECTS, "biggestObjects.png"); // NOI18N
        cache.put(HeapWalkerIcons.OQL_CONSOLE, "oqlConsole.png"); // NOI18N
    }
    
}
