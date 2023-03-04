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
package org.netbeans.modules.debugger.ui;

import java.awt.Component;
import java.beans.DesignMode;
import java.beans.beancontext.BeanContextChildComponentProxy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.debugger.ui.EngineComponentsProvider.ComponentInfo;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * A helper class, which transforms the old providing of associated GUI components
 * via BeanContextChildComponentProxy to the new API {@link ComponentInfo}.
 * 
 * @author Martin Entlicher
 */
public class ComponentInfoFromBeanContext {
    
    private static final String PROPERTY_CLOSED_TC = "closedTopComponents"; // NOI18N
    private static final String PROPERTY_MINIMIZED_TC = "minimizedTopComponents"; // NOI18N

    public static Transform TRANSFORM; // Initialized from EngineComponentsProvider
    
    static List<ComponentInfo> transform(List<? extends BeanContextChildComponentProxy> componentProxies) {
        try {
            Class.forName(ComponentInfo.class.getName(), true, ComponentInfo.class.getClassLoader()); // Initializes TRANSFORM
        } catch (ClassNotFoundException cnfex) {}
        List<ComponentInfo> cinfos = new ArrayList<ComponentInfo>(componentProxies.size());
        for (final BeanContextChildComponentProxy cp : componentProxies) {
            final Component[] c = new Component[] { null };
            final boolean[] doOpen = new boolean[] { false };
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        c[0] = cp.getComponent();
                        doOpen[0] = (cp instanceof DesignMode) ? ((DesignMode) cp).isDesignTime() : true;
                    }
                });
                if (c[0] == null) {
                    //throw new NullPointerException("No component from "+cp);
                    continue;
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                continue;
            }
            //cs.add(c[0]);
            boolean open = doOpen[0];
            boolean minimized = false;
            if (c[0] instanceof TopComponent) {
                final TopComponent tc = (TopComponent) c[0];
                boolean wasClosed = Properties.getDefault().getProperties(DebuggerManagerListener.class.getName()).
                        getProperties(PROPERTY_CLOSED_TC).getBoolean(tc.getName(), false);
                boolean wasOpened = !Properties.getDefault().getProperties(DebuggerManagerListener.class.getName()).
                        getProperties(PROPERTY_CLOSED_TC).getBoolean(tc.getName(), true);
                open = (doOpen[0] && !wasClosed || !doOpen[0] && wasOpened);
                boolean wasMinimized = Properties.getDefault().getProperties(DebuggerManagerListener.class.getName()).
                        getProperties(PROPERTY_MINIMIZED_TC).getBoolean(tc.getName(), false);
                minimized = wasMinimized;
            }
            ComponentInfo ci = TRANSFORM.transform(c[0], open, minimized);
            cinfos.add(ci);
        }
        
        return cinfos;
    }

    static void closing(List<ComponentInfo> components) {
        for (ComponentInfo ci : components) {
            Component c = ci.getComponent();
            if (c instanceof TopComponent) {
                TopComponent tc = (TopComponent) c;
                /* To check which components we're closing:
                try {
                    Method pid = TopComponent.class.getDeclaredMethod("preferredID");
                    pid.setAccessible(true);
                    System.err.println("ComponentInfoFromBeanContext.closing("+pid.invoke(tc)+")");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }*/
                boolean isOpened = tc.isOpened();
                Properties.getDefault().getProperties(DebuggerManagerListener.class.getName()).
                        getProperties(PROPERTY_CLOSED_TC).setBoolean(tc.getName(), !isOpened);
                boolean isMinimized = WindowManager.getDefault().isTopComponentMinimized(tc);
                Properties.getDefault().getProperties(DebuggerManagerListener.class.getName()).
                        getProperties(PROPERTY_MINIMIZED_TC).setBoolean(tc.getName(), isMinimized);
            }
        }
        
    }
    
    public static interface Transform {
        
        ComponentInfo transform(Component c, boolean opened, boolean minimized);//BeanContextChildComponentProxy componentProxy);
        
    }
}
