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

package org.netbeans.libs.javafx;

import javafx.embed.swing.JFXPanel;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class IsFXEnabledTest extends NbTestCase {

    public IsFXEnabledTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.emptyConfiguration().addTest(IsFXEnabledTest.class).
            gui(false).
            suite();
    }
    
    public void testIsFxModuleEnabled() throws Exception {
        try {
            JFXPanel p = new JFXPanel();
        } catch (RuntimeException | LinkageError err) {
            return;
        }
        for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (mi.getCodeNameBase().equals("org.netbeans.libs.javafx")) {
                assertTrue("Enabled", mi.isEnabled());
                Class app = mi.getClassLoader().loadClass("javafx.application.Application");
                assertNotNull("FX class loaded OK", app);
                return;
            }
        }
        fail("libs.javafx not found!");
    }
}
