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
