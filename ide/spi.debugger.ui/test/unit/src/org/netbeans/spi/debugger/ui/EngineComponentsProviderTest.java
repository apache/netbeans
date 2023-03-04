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
package org.netbeans.spi.debugger.ui;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.api.debugger.DebuggerApiTestBase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.spi.debugger.ui.EngineComponentsProvider.ComponentInfo;
import org.openide.windows.TopComponent;

/**
 *
 * @author Martin
 */
public class EngineComponentsProviderTest extends DebuggerApiTestBase {
    
    public EngineComponentsProviderTest(String s) {
        super(s);
    }
    
    public static Test suite() {
        return EngineComponentsProviderTest.createTestSuite(EngineComponentsProviderTest.class);
    }

    public void testCreateTC() throws Throwable {
        ComponentInfo ciVar = EngineComponentsProvider.ComponentInfo.create("localsView");
        checkComponentInfo(ciVar, true, false, "org.netbeans.modules.debugger.ui.views.LocalsView");
        
        ComponentInfo ciWatch = EngineComponentsProvider.ComponentInfo.create("watchesView", false);
        checkComponentInfo(ciWatch, false, false, "org.netbeans.modules.debugger.ui.views.WatchesView");
        
        ComponentInfo ciCS = EngineComponentsProvider.ComponentInfo.create("callstackView", false, true);
        checkComponentInfo(ciCS, false, true, "org.netbeans.modules.debugger.ui.views.CallStackView");
    }
    
    private void checkComponentInfo(final ComponentInfo ci,
                                    boolean opened, boolean minimized,
                                    final String ID) throws Throwable {
        assertEquals("Opened", opened, ci.isOpened());
        assertEquals("Minimized", minimized, ci.isMinimized());
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    Component c = ci.getComponent();
                    assertTrue("Is TopComponent:", c instanceof TopComponent);
                    TopComponent tc = (TopComponent) c;
                    try {
                        Method pid = TopComponent.class.getDeclaredMethod("preferredID");
                        pid.setAccessible(true);
                        String tcId = (String) pid.invoke(tc);
                        //System.err.println("tcId = "+tcId);
                        assertEquals("TopComponent preferredID:", ID, tcId);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        } catch (InvocationTargetException itex) {
            Thread.dumpStack();
            throw itex.getTargetException();
        }
    }

    public static Test createTestSuite(Class<? extends TestCase> clazz) {
        NbModuleSuite.Configuration suiteConfiguration = NbModuleSuite.createConfiguration(clazz);
        suiteConfiguration = suiteConfiguration.gui(false);
        //suiteConfiguration = suiteConfiguration.reuseUserDir(false);
        return NbModuleSuite.create(suiteConfiguration);
    }

}
