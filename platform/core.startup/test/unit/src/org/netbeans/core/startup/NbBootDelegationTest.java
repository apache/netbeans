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

package org.netbeans.core.startup;

import java.io.File;
import java.util.Collections;
import org.netbeans.Module;
import org.netbeans.ModuleManager;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NbBootDelegationTest extends NbInstallerTestBase {

    public NbBootDelegationTest(String n) {
        super(n);
    }
    
    public void testNetBeansBootDelegation() throws Exception {
        ModuleManager mgr = Main.getModuleSystem().getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        System.setProperty("netbeans.bootdelegation", "javax.swing, javax.naming.*");
        try {
            Module m1 = mgr.create(new File(jars, "simple-module.jar"), null, false, false, false);
            mgr.enable(Collections.singleton(m1));
            final ClassLoader ldr = m1.getClassLoader();
            Class<?> jtree = Class.forName("javax.swing.JTree", true, ldr);
            assertNotNull("JTree found", jtree);
            String tableModel = "javax.swing.table.TableModel";
            try {
                Class<?> model = Class.forName(tableModel, true, ldr);
                fail("Model shall not be accessible: " + model);
            } catch (ClassNotFoundException ex) {
                // OK
                assertNotNull("The class exists on boot path", ClassLoader.getSystemClassLoader().loadClass(tableModel));
            }
            Class<?> list = Class.forName("java.util.ArrayList", true, ldr);
            assertNotNull("java packages are always accessible", list);
            Class<?> naming = Class.forName("javax.naming.event.EventContext", true, ldr);
            assertNotNull("naming is recursively visible", naming);
        } finally {
            System.getProperties().remove("netbeans.bootdelegation");
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }


}
