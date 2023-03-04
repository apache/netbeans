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
package org.netbeans.modules.profiler.heapwalk.details.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Jiri Sedlacek
 */
public final class DetailsSupport {
    
    public static String getDetailsString(Instance instance, Heap heap) {
        // TODO [Tomas]: cache computed string per heap
        Collection<ProviderClassPair> pairs = getCompatibleProviders(instance.getJavaClass());
        for (ProviderClassPair pair : pairs) {
            String classKey = pair.classKey;
            if (pair.subclasses) classKey += "+";                               // NOI18N
            String string = pair.provider.getDetailsString(classKey, instance, heap);
            if (string != null) return string;
        }
        return null;
    }
    
    public static DetailsProvider.View getDetailsView(Instance instance, Heap heap) {
        Collection<ProviderClassPair> pairs = getCompatibleProviders(instance.getJavaClass());
        for (ProviderClassPair pair : pairs) {
            String classKey = pair.classKey;
            if (pair.subclasses) classKey += "+";                               // NOI18N
            DetailsProvider.View view = pair.provider.getDetailsView(classKey, instance, heap);
            if (view != null) return view;
        }
        return null;
    }
    
    
    private static final LinkedHashMap<String, List<ProviderClassPair>> PROVIDERS_CACHE =
            new LinkedHashMap<String, List<ProviderClassPair>>(10000) {
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > 5000;
                }
            };
    
    private static Lookup.Result<DetailsProvider> PROVIDERS;
    private static Collection<? extends DetailsProvider> getProviders() {
        if (PROVIDERS == null) {
            PROVIDERS = Lookup.getDefault().lookupResult(DetailsProvider.class);
            PROVIDERS.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent ev) { PROVIDERS_CACHE.clear(); }
            });
        }
        return PROVIDERS.allInstances();
    }
    
    private static List<ProviderClassPair> getCompatibleProviders(JavaClass cls) {
        String className = cls.getName();

        // Query the cache for already computed DetailsProviders
        List<ProviderClassPair> cachedPairs = PROVIDERS_CACHE.get(className);
        if (cachedPairs != null) return cachedPairs;
        
        // All registered className|DetailsProvider pairs
        List<ProviderClassPair> allPairs = new ArrayList<>();
        List<ProviderClassPair> simplePairs = new ArrayList<>();
        Collection<? extends DetailsProvider> providers = getProviders();
        for (DetailsProvider provider : providers) {
            String[] classes = provider.getSupportedClasses();
            if (classes != null && classes.length > 0)
                for (String classs : classes)
                    allPairs.add(new ProviderClassPair(provider, classs));
            else simplePairs.add(new ProviderClassPair(provider, null));
        }
        
        List<ProviderClassPair> pairs = new ArrayList<>();
        
        // Only compatible className|DetailsProvider pairs
        if (!allPairs.isEmpty()) {
            boolean superClass = false;
            while (cls != null) {
                String clsName = cls.getName();
                for (ProviderClassPair pair : allPairs)
                    if ((pair.subclasses || !superClass) &&
                        clsName.equals(pair.classKey))
                        pairs.add(pair);
                cls = cls.getSuperClass();
                superClass = true;
            }
        }
        
        // DetailsProviders without className definitions
        pairs.addAll(simplePairs);
        
        // Cache the computed DetailsProviders
        PROVIDERS_CACHE.put(className, pairs);
        
        return pairs;
    }
    
    
    private static class ProviderClassPair {
        
        final DetailsProvider provider;
        final String classKey;
        final boolean subclasses;
        
        ProviderClassPair(DetailsProvider provider, String classKey) {
            subclasses = classKey != null && classKey.endsWith("+");            // NOI18N
            this.provider = provider;
            this.classKey = !subclasses ? classKey :
                            classKey.substring(0, classKey.length() - 1);
        }
        
    }
    
}
