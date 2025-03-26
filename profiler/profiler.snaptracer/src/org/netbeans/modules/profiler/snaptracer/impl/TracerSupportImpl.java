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

package org.netbeans.modules.profiler.snaptracer.impl;

import org.netbeans.modules.profiler.snaptracer.TracerPackage;
import org.netbeans.modules.profiler.snaptracer.TracerPackageProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.profiler.snaptracer.Positionable;
import org.netbeans.modules.profiler.snaptracer.impl.packages.TestPackageProvider;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jiri Sedlacek
 */
public final class TracerSupportImpl {

    private static TracerSupportImpl INSTANCE;
    private static RequestProcessor PROCESSOR;

    private final Set<TracerPackageProvider> providers;


    public static synchronized TracerSupportImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TracerSupportImpl();
            PROCESSOR = new RequestProcessor("Tracer Processor", 5); // NOI18N
        }
        return INSTANCE;
    }


    public synchronized void registerPackageProvider(TracerPackageProvider provider) {
        providers.add(provider);
    }

    public synchronized void unregisterPackageProvider(TracerPackageProvider provider) {
        providers.remove(provider);
    }


    public synchronized boolean hasPackages(Object target) {
        for (TracerPackageProvider provider : providers)
            if (provider.getScope().isInstance(target))
                return true;
        return false;
    }

    public synchronized List<TracerPackage> getPackages(IdeSnapshot snapshot) {
        List<TracerPackage> packages = new ArrayList<>();
        for (TracerPackageProvider provider : providers)
            packages.addAll(Arrays.asList(provider.getPackages(snapshot)));
        packages.sort(Positionable.COMPARATOR);
        return packages;
    }
    
    
    public void perform(Runnable task) {
        PROCESSOR.post(task);
    }


    private TracerSupportImpl() {
        providers = new HashSet<>();
        registerPackageProvider(new TestPackageProvider());
    }

}
