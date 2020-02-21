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
package org.netbeans.modules.cnd.makeproject.api.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.util.Lookup;

public class LogicalViewNodeProviders {

    private static final LogicalViewNodeProviders instance = new LogicalViewNodeProviders();
    private final List<LogicalViewNodeProvider> providers = new ArrayList<>(0);

    private LogicalViewNodeProviders(){
    }
    
    public static LogicalViewNodeProviders getInstance() {
        return instance;
    }

    public List<LogicalViewNodeProvider> getProviders() {
        List<LogicalViewNodeProvider> actualProviders;
        synchronized (providers) {
            actualProviders = new ArrayList<>(providers);
        }
        Collection<? extends LogicalViewNodeProvider> mwc = Lookup.getDefault().lookupAll(LogicalViewNodeProvider.class);
        for (LogicalViewNodeProvider lvnp : mwc) {
            if (lvnp != null) {
                actualProviders.add(lvnp);
            }
        }
        return actualProviders;
    }

    public LogicalViewNodeProvider[] getProvidersAsArray() {
        List<LogicalViewNodeProvider> cn = getProviders();
        return cn.toArray(new LogicalViewNodeProvider[cn.size()]);
    }

    public void addProvider(LogicalViewNodeProvider provider) {
        synchronized (providers) {
            providers.add(provider);
        }
    }

    public void removeProvider(LogicalViewNodeProvider provider) {
        synchronized (providers) {
            providers.remove(provider);
        }
    }
}
