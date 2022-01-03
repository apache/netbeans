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
package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProviderFactory;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.discovery.api.DiscoveryProviderFactory.class)
public class DiscoveryProviderFactoryImpl extends DiscoveryProviderFactory {

    @Override
    public DiscoveryProvider createProvider(String providerID) {
        if (AnalyzeExecLog.EXEC_LOG_PROVIDER_ID.equals(providerID)) {
            return new AnalyzeExecLog();
        } else if (AnalyzeExecutable.EXECUTABLE_PROVIDER_ID.equals(providerID)) {
            return new AnalyzeExecutable();
        } else if (AnalyzeFolder.FOLDER_PROVIDER_ID.equals(providerID)) {
            return new AnalyzeFolder();
        } else if (AnalyzeMakeLog.MAKE_LOG_PROVIDER_ID.equals(providerID)) {
            return new AnalyzeMakeLog();
        }
        return null;
    }

    @Override
    public Collection<DiscoveryProvider> getAllProviders() {
        List<DiscoveryProvider> res = new ArrayList<DiscoveryProvider>(4);
        res.add(new AnalyzeExecLog());
        res.add(new AnalyzeExecutable());
        res.add(new AnalyzeFolder());
        res.add(new AnalyzeMakeLog());
        return res;
    }
}
