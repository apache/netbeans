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
package org.netbeans.modules.cnd.remote.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
public class HostMappingsAnalyzer {

    private final PlatformInfo secondPI;
    private final PlatformInfo firstPI;
    private Thread thread = null;
    private boolean cancelled = false;
    private final Object lock = new Object();

    public HostMappingsAnalyzer(ExecutionEnvironment remoteExecEnv) {
        this(remoteExecEnv, ExecutionEnvironmentFactory.getLocal());
    }

    private HostMappingsAnalyzer(ExecutionEnvironment secondEnv, ExecutionEnvironment firstEnv) {
        secondPI = PlatformInfo.getDefault(secondEnv);
        firstPI = PlatformInfo.getDefault(firstEnv);
    }
    
    /*package*/ void cancel() {
        synchronized (lock) {
            if (thread != null) {
                cancelled = true;
                thread.interrupt();
            }
        }
    }

    public Map<String, String> getMappings() {
        synchronized (lock) {
            cancelled = false;
            thread = Thread.currentThread();
        }
        try {      
            Map<String, String> mappingsFirst2Second = new HashMap<>();
            getMappingsImpl(mappingsFirst2Second);
            return mappingsFirst2Second;
        } finally {
            synchronized (lock) {
                thread = null;
            }            
        }
    }
    
    private void getMappingsImpl(Map<String, String> mappingsFirst2Second) {
        // all maps are host network name -> host local name
        Map<String, String> firstNetworkNames2Inner = populateMappingsList(firstPI, secondPI);
        if (isCancelled()) {
            return;
        }
        Map<String, String> secondNetworkNames2Inner = populateMappingsList(secondPI, firstPI);
        if (isCancelled()) {
            return;
        }

        if (firstNetworkNames2Inner.size() > 0 && secondNetworkNames2Inner.size() > 0) {
            for (Map.Entry<String, String> firstNetworkName : firstNetworkNames2Inner.entrySet()) {
                for (Map.Entry<String, String> secondNetworkName : secondNetworkNames2Inner.entrySet()) {
                    //TODO: investigate more complex cases
                    if (firstNetworkName.getKey().equals(secondNetworkName.getKey())) {
                        mappingsFirst2Second.put(firstNetworkName.getValue(), secondNetworkName.getValue());
                    }
                }
            }
        }

        for (HostMappingProvider provider : singularProviders) {
            if (isCancelled()) {
                return;
            }
            if (provider.isApplicable(secondPI, firstPI)) {
                Map<String, String> map = provider.findMappings(
                        secondPI.getExecutionEnvironment(), firstPI.getExecutionEnvironment());
                mappingsFirst2Second.putAll(map);
            }
            if (isCancelled()) {
                return;
            }
            if (provider.isApplicable(firstPI, secondPI)) {
                Map<String, String> map = provider.findMappings(
                        firstPI.getExecutionEnvironment(), secondPI.getExecutionEnvironment());
                mappingsFirst2Second.putAll(map);
            }
        }
    }

    private boolean isCancelled() {
        synchronized (lock) {
            return cancelled;
        }
    }

    // host is one we are searching on
    // other is host in which context we are interested in mappings
    private Map<String, String> populateMappingsList(PlatformInfo hostPlatformInfo, PlatformInfo otherPlatformInfo) {
        Map<String, String> map = new HashMap<>();
        for (HostMappingProvider prov : pairedProviders) {
            if (isCancelled()) {
                break;
            }            
            if (prov.isApplicable(hostPlatformInfo, otherPlatformInfo)) {
                map.putAll(prov.findMappings(
                        hostPlatformInfo.getExecutionEnvironment(), otherPlatformInfo.getExecutionEnvironment())  );
            }
        }
        return map;
    }

    private static final List<HostMappingProvider> pairedProviders;
    private static final List<HostMappingProvider> singularProviders;
    
    static {
        //providers
        pairedProviders = new ArrayList<>();
        singularProviders = new ArrayList<>();
        // TODO: should it be Lookup?
        pairedProviders.add(new HostMappingProviderWindows());
        pairedProviders.add(new HostMappingProviderSamba());
        
        // TODO: this kind of API is st...range 
        singularProviders.add(new HostMappingProviderSolaris());
        singularProviders.add(new HostMappingProviderLinux());
        singularProviders.add(new HostMappingProviderWindowsUnixNFS());
    }
}
