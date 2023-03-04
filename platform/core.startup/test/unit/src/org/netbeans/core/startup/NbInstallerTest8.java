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

import org.netbeans.SetupHid;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestSuite;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest8 extends SetupHid {
    
    public NbInstallerTest8(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        // Turn on verbose logging while developing tests:
        System.setProperty("org.netbeans.core.modules", "0");
        // In case run standalone, need a work dir.
        if (System.getProperty("nbjunit.workdir") == null) {
            // Hope java.io.tmpdir is set...
            System.setProperty("nbjunit.workdir", System.getProperty("java.io.tmpdir"));
        }
        TestRunner.run(new NbTestSuite(NbInstallerTest8.class));
    }
    
    private File moduleJar;
    protected @Override void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.netbeans.core.modules.NbInstaller.noAutoDeps", "true");
        // leave NO_COMPAT_AUTO_TRANSITIVE_DEPS=false
        moduleJar = new File(jars, "look-for-myself.jar");
    }
    
    /** Test #28465: Lookup<ModuleInfo> should be ready soon, even while
     * modules are still loading. The ModuleInfo need not claim to be enabled
     * during this time, but it must exist.
     */
    public void testEarlyModuleInfoLookup() throws Exception {
        // Ought to load these modules:
        ModuleManager mgr = Main.getModuleSystem().getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m = mgr.get("lookformyself");
            assertNull(m);
            m = mgr.create(moduleJar, new ModuleHistory(moduleJar.getAbsolutePath()), false, false, false);
            assertEquals("look-for-myself.jar can be enabled", Collections.EMPTY_SET, m.getProblems());
            mgr.enable(m);
            Class<?> c = m.getClassLoader().loadClass("lookformyself.Loder");
            Method meth = c.getMethod("foundNow");
            assertTrue("ModuleInfo is found after startup", (Boolean) meth.invoke(null));
            Field f = c.getField("foundEarly");
            assertTrue("ModuleInfo is found during dataloader section initialization", (Boolean) f.get(null));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
}
