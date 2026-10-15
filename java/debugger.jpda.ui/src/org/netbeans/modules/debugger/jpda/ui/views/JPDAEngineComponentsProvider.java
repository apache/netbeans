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

package org.netbeans.modules.debugger.jpda.ui.views;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.EngineComponentsProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession", types=EngineComponentsProvider.class)
public class JPDAEngineComponentsProvider implements EngineComponentsProvider {

    private static final String PROPERTY_CLOSED_TC = "closedTopComponents"; // NOI18N
    private static final String PROPERTY_MINIMIZED_TC = "minimizedTopComponents"; // NOI18N
    private static final String PROPERTY_BASE_NAME = "netbeans-JPDASession.EngineComponentsProvider"; // NOI18N

    private static final String[] DBG_COMPONENTS_OPENED = {
        "localsView", "watchesView", "breakpointsView", "debuggingView", "evaluatorPane"         // NOI18N
    };
    private static final String[] DBG_COMPONENTS_CLOSED = {
        "callstackView", "resultsView", "sessionsView",        // NOI18N
        "sources", "threadsView", "classes"                                     // NOI18N
    };

    @Override
    public List<ComponentInfo> getComponents() {
        List<ComponentInfo> components = new ArrayList<>(DBG_COMPONENTS_OPENED.length + DBG_COMPONENTS_CLOSED.length);
        for (String cid : DBG_COMPONENTS_OPENED) {
            components.add(EngineComponentsProvider.ComponentInfo.create(
                    cid, isOpened(cid, true), isMinimized(cid)));
        }
        for (String cid : DBG_COMPONENTS_CLOSED) {
            if ("classes".equals(cid) && !checkTCExist(cid)) {
                // Ignore classes component that might not be present.
                continue;
            }
            components.add(EngineComponentsProvider.ComponentInfo.create(
                    cid, isOpened(cid, false), isMinimized(cid)));
        }
        return components;
    }
    
    private static boolean checkTCExist(String name) {
        String role = WindowManager.getDefault().getRole();
        FileObject wsRoot = FileUtil.getConfigRoot().getFileObject("Windows2");
        if (wsRoot == null) {
            return false;
        }
        if (role != null) {
            wsRoot = wsRoot.getFileObject("Roles/"+role);
            if (wsRoot == null) {
               return false;
            }
        }
        return wsRoot.getFileObject("Components/"+name+".settings") != null;
    }

    private static boolean isOpened(String cid, boolean open) {
        if (cid.equals("watchesView")) {    // NOI18N
            Preferences preferences = NbPreferences.forModule(ContextProvider.class).node("variables_view"); // NOI18N
            open = !preferences.getBoolean("show_watches", true); // NOI18N
        }
        boolean wasClosed = Properties.getDefault().getProperties(PROPERTY_BASE_NAME).
                getProperties(PROPERTY_CLOSED_TC).getBoolean(cid, false);
        boolean wasOpened = !Properties.getDefault().getProperties(PROPERTY_BASE_NAME).
                getProperties(PROPERTY_CLOSED_TC).getBoolean(cid, true);
        open = (open && !wasClosed || !open && wasOpened);
        return open;
    }
    
    private static boolean isMinimized(String cid) {
        boolean wasMinimized = Properties.getDefault().getProperties(PROPERTY_BASE_NAME).
                getProperties(PROPERTY_MINIMIZED_TC).getBoolean(cid, false);
        boolean wasDeminim = !Properties.getDefault().getProperties(PROPERTY_BASE_NAME).
                getProperties(PROPERTY_MINIMIZED_TC).getBoolean(cid, false);
        boolean minimized = (wasMinimized || !wasDeminim);
        return minimized;
    }

    @Override
    public void willCloseNotify(List<ComponentInfo> components) {
        for (ComponentInfo ci : components) {
            Component c = ci.getComponent();
            if (c instanceof TopComponent) {
                TopComponent tc = (TopComponent) c;
                boolean isOpened = tc.isOpened();
                String tcId = WindowManager.getDefault().findTopComponentID(tc);
                Properties.getDefault().getProperties(PROPERTY_BASE_NAME).
                        getProperties(PROPERTY_CLOSED_TC).setBoolean(tcId, !isOpened);
                boolean isMinimized = WindowManager.getDefault().isTopComponentMinimized(tc);
                Properties.getDefault().getProperties(PROPERTY_BASE_NAME).
                        getProperties(PROPERTY_MINIMIZED_TC).setBoolean(tcId, isMinimized);
            }
        }
    }
    
}
