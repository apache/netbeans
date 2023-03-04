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
import org.netbeans.ModuleManagerTest;
import org.netbeans.MockEvents;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.Events;
import org.netbeans.Module;
import org.netbeans.ModuleManager;

/**
 * Checks that OpenIDE-Module-Hide-Classpath-Packages works.
 */
public class NbInstallerHideClasspathPackagesTest extends SetupHid {

    public NbInstallerHideClasspathPackagesTest(String n) {
        super(n);
    }

    public void testHideClasspathPackages() throws Exception {
        File m1j = new File(getWorkDir(), "m1.jar");
        Map<String,String> contents = new  HashMap<String,String>();
        contents.put("javax/net/SocketFactory.class", "ignored");
        contents.put("javax/swing/JPanel.class", "overrides");
        contents.put("javax/swing/text/Document.class", "overrides");
        contents.put("javax/naming/Context.class", "overrides");
        contents.put("javax/naming/spi/Resolver.class", "ignored");
        Map<String,String> mani = new HashMap<String,String>();
        mani.put("OpenIDE-Module", "m1");
        mani.put("OpenIDE-Module-Hide-Classpath-Packages", "javax.swing.**, javax.naming.*");
        createJar(m1j, contents, mani);
        File m2j = new File(getWorkDir(), "m2.jar");
        mani = new HashMap<String,String>();
        mani.put("OpenIDE-Module", "m2");
        mani.put("OpenIDE-Module-Module-Dependencies", "m1");
        // Just to check early attempts to load packages:
        mani.put("OpenIDE-Module-Layer", "m2/layer.xml");
        mani.put("OpenIDE-Module-Package-Dependencies", "javax.management[Descriptor]");
        createJar(m2j, Collections.singletonMap("m2/layer.xml", "<filesystem/>"), mani);
        File m3j = new File(getWorkDir(), "m3.jar");
        createJar(m3j, Collections.<String,String>emptyMap(), Collections.singletonMap("OpenIDE-Module", "m3"));
        Events ev = new MockEvents();
        NbInstaller inst = new NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(inst, ev);
        inst.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Module m1 = mgr.create(m1j, null, false, false, false);
            Module m2 = mgr.create(m2j, null, false, false, false);
            Module m3 = mgr.create(m3j, null, false, false, false);
            mgr.enable(new HashSet<Module>(Arrays.asList(m1, m2, m3)));
            ModuleManagerTest.assertDoesNotOverride(m1, "javax.net.SocketFactory");
            ModuleManagerTest.assertOverrides(m1, "javax.swing.JPanel");
            ModuleManagerTest.assertOverrides(m1, "javax.swing.text.Document");
            ModuleManagerTest.assertOverrides(m1, "javax.naming.Context");
            ModuleManagerTest.assertDoesNotOverride(m1, "javax.naming.spi.Resolver");
            ModuleManagerTest.assertDoesNotOverride(m2, "javax.net.SocketFactory");
            ModuleManagerTest.assertOverrides(m2, "javax.swing.JPanel");
            ModuleManagerTest.assertOverrides(m2, "javax.swing.text.Document");
            ModuleManagerTest.assertOverrides(m2, "javax.naming.Context");
            ModuleManagerTest.assertDoesNotOverride(m2, "javax.naming.spi.Resolver");
            ModuleManagerTest.assertDoesNotOverride(m3, "javax.net.SocketFactory");
            ModuleManagerTest.assertDoesNotOverride(m3, "javax.swing.JPanel");
            ModuleManagerTest.assertDoesNotOverride(m3, "javax.swing.text.Document");
            ModuleManagerTest.assertDoesNotOverride(m3, "javax.naming.Context");
            ModuleManagerTest.assertDoesNotOverride(m3, "javax.naming.spi.Resolver");
            // #159586: masked JRE classes should not be accessible from SCL either.
            ClassLoader scl = mgr.getClassLoader();
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.net.SocketFactory");
            ModuleManagerTest.assertOverrides(scl, "system class loader", "javax.swing.JPanel");
            ModuleManagerTest.assertOverrides(scl, "system class loader", "javax.swing.text.Document");
            ModuleManagerTest.assertOverrides(scl, "system class loader", "javax.naming.Context");
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.naming.spi.Resolver");
            mgr.disable(new HashSet<Module>(Arrays.asList(m1, m2, m3)));
            scl = mgr.getClassLoader();
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.net.SocketFactory");
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.swing.JPanel");
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.swing.text.Document");
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.naming.Context");
            ModuleManagerTest.assertDoesNotOverride(scl, "javax.naming.spi.Resolver");
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    @Override
    protected Level logLevel() {
        return Level.FINER;
    }

}
