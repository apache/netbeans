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
package org.netbeans.modules.web.webkit.tooling.networkmonitor;

import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.spi.NetworkMonitorFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=NetworkMonitorFactory.class)
public class NetworkMonitorFactoryImpl implements NetworkMonitorFactory {

    @Override
    public Lookup createNetworkMonitor(WebKitDebugging webkit, Lookup projectContext) {
        NetworkMonitor monitor = NetworkMonitor.createNetworkMonitor(projectContext);
        webkit.getNetwork().addListener(monitor);
        return Lookups.fixed(webkit, monitor);
    }

    @Override
    public void stopNetworkMonitor(Lookup session) {
        WebKitDebugging webkit = session.lookup(WebKitDebugging.class);
        assert webkit != null;
        NetworkMonitor monitor = session.lookup(NetworkMonitor.class);
        assert monitor != null;
        if (monitor == null || webkit == null) {
            return;
        }
        webkit.getNetwork().removeListener(monitor);
        monitor.close();
    }

}
