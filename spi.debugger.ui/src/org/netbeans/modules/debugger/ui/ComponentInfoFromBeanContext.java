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
