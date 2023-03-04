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

import java.lang.ref.WeakReference;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.webkit.debugging.api.network.Network;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

public class NetworkMonitor implements Network.Listener {

    private static WeakReference<NetworkMonitor> lastNetworkMonitor = new WeakReference<>(null);
    
    private final Model model;
    private final Project project;
    private volatile NetworkMonitorTopComponent component;
    private volatile boolean debuggingSession;

    private NetworkMonitor(Lookup projectContext, NetworkMonitorTopComponent comp, boolean debuggingSession) {
        this.component = comp;
        this.model = new Model(projectContext);
        this.debuggingSession = debuggingSession;
        project = projectContext.lookup(Project.class);
        lastNetworkMonitor = new WeakReference<>(this);
    }

    boolean isConnected() {
        return debuggingSession;
    }

    void open() {
        final boolean show = NetworkMonitorTopComponent.canReopenNetworkComponent();
        if (show) {
            // active model if NetworkMonitor is going to be shown:
            model.activate();
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (component == null) {
                    component = new NetworkMonitorTopComponent(model, isConnected());
                    if (show) {
                        component.open();
                        component.requestActive();
                    }
                } else {
                    component.setModel(model, isConnected());
                }
            }
        });
    }

    private void resetComponent() {
        this.component = null;
    }

    public static NetworkMonitor createNetworkMonitor(Lookup projectContext) {
        NetworkMonitorTopComponent component = findNetworkMonitorTC();
        // reuse TopComponent if it is open; but always create a new model for new monitoring session
        NetworkMonitor nm = new NetworkMonitor(projectContext, component, true);
        nm.open();
        return nm;
    }

    public static void reopenNetworkMonitor() {
        NetworkMonitorTopComponent component = findNetworkMonitorTC();
        if (component != null) {
            component.requestActive();
        } else {
            NetworkMonitor nm = lastNetworkMonitor.get();
            if (nm != null) {
                // reuse model from last user NetworkMonitor but create a new UI:
                nm.resetComponent();
            } else {
                // open blank NetworkMonitor:
                nm = new NetworkMonitor(Lookup.EMPTY, null, false);
            }
            nm.open();
        }
    }

    private static NetworkMonitorTopComponent findNetworkMonitorTC() {
        for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
            if (tc instanceof NetworkMonitorTopComponent) {
                return (NetworkMonitorTopComponent)tc;
            }
        }
        return null;
    }

    public void close() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                NetworkMonitorTopComponent cmp = component;
                if (cmp != null && cmp.isOpened()) {
                    cmp.close();
                    // reopen automatically NetworkMonitor next time:
                    NetworkMonitorTopComponent.setReopenNetworkComponent(true);
                }
            }
        });
        debuggingSession = false;
    }

    // Implementation of Network.Listener

    @Override
    public void networkRequest(Network.Request request) {
        model.add(request);
        DependentFileQueryImpl.networkRequest(project, request);
    }

    @Override
    public void webSocketRequest(Network.WebSocketRequest request) {
        model.add(request);
    }

}
