/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.javascript.debugger;

import java.awt.Component;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.EngineComponentsProvider;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Provider of debugger GUI components needed for JavaScript Debugger,
 * minimized initially.
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="javascript-debuggerengine", types=EngineComponentsProvider.class)
public class EngineComponentsProviderImpl implements EngineComponentsProvider {
    
    private static final String PROPERTY_CLOSED_TC = "closedTopComponents"; // NOI18N
    private static final String PROPERTY_MINIMIZED_TC = "minimizedTopComponents"; // NOI18N
    private static final String PROPERTY_BASE_NAME = "javascript-debuggerengine.EngineComponentsProvider";   // NOI18N

    private static final String[] DBG_COMPONENTS_OPENED = {
        "localsView", "watchesView", "callstackView", "breakpointsView",
    };
    private static final String[] DBG_COMPONENTS_CLOSED = {
        "evaluatorPane", "resultsView", "sessionsView",
    };

    @Override
    public List<ComponentInfo> getComponents() {
        List<ComponentInfo> components = new ArrayList<ComponentInfo>(DBG_COMPONENTS_OPENED.length + DBG_COMPONENTS_CLOSED.length);
        for (String cid : DBG_COMPONENTS_OPENED) {
            components.add(EngineComponentsProvider.ComponentInfo.create(
                    cid, isOpened(cid, true), isMinimized(cid)));
        }
        for (String cid : DBG_COMPONENTS_CLOSED) {
            components.add(EngineComponentsProvider.ComponentInfo.create(
                    cid, isOpened(cid, false), isMinimized(cid)));
        }
        return components;
    }
    
    private static boolean isOpened(String cid, boolean open) {
        if (cid.equals("watchesView")) {
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
                getProperties(PROPERTY_MINIMIZED_TC).getBoolean(cid, true);
        boolean wasDeminim = !Properties.getDefault().getProperties(PROPERTY_BASE_NAME).
                getProperties(PROPERTY_MINIMIZED_TC).getBoolean(cid, true);
        boolean minimized = (wasMinimized || !wasDeminim);
        return minimized;
    }

    @Override
    public void willCloseNotify(List<ComponentInfo> components) {
        for (ComponentInfo ci : components) {
            Component c = ci.getComponent();
            if (c instanceof TopComponent) {
                TopComponent tc = (TopComponent) c;
                /*
                try {
                    Method pid = TopComponent.class.getDeclaredMethod("preferredID");
                    pid.setAccessible(true);
                    System.err.println("JS EngineComponentsProviderImpl.closing("+pid.invoke(tc)+") name = "+tc.getName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }*/
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
