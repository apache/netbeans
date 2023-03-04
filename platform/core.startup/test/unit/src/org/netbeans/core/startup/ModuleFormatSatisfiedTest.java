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
import org.netbeans.MockEvents;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.Module;
import org.netbeans.ModuleManager;

/** Checks whether a modules are provided with ModuleFormat1 token.
 *
 * @author Jaroslav Tulach
 */
public class ModuleFormatSatisfiedTest extends SetupHid {
    private File moduleJarFile;

    public ModuleFormatSatisfiedTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.netbeans.core.modules.NbInstaller.noAutoDeps", "true");
        
        Manifest man = new Manifest ();
        man.getMainAttributes ().putValue ("Manifest-Version", "1.0");
        man.getMainAttributes ().putValue ("OpenIDE-Module", "org.test.FormatDependency/1");
        String req = "org.openide.modules.ModuleFormat1, org.openide.modules.ModuleFormat2";
        man.getMainAttributes ().putValue ("OpenIDE-Module-Requires", req);
        
        clearWorkDir();
        moduleJarFile = new File(getWorkDir(), "ModuleFormatTest.jar");
        JarOutputStream os = new JarOutputStream(new FileOutputStream(moduleJarFile), man);
        os.putNextEntry (new JarEntry ("empty/test.txt"));
        os.close ();
    }
    
    public void testTryToInstallTheModule () throws Exception {
        final MockEvents ev = new MockEvents();
        NbInstaller installer = new NbInstaller(ev);
        ModuleManager mgr = new ModuleManager(installer, ev);
        installer.registerManager(mgr);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            addOpenideModules(mgr);
            Module m1 = mgr.create(moduleJarFile, null, false, false, false);
            assertEquals(Collections.EMPTY_SET, m1.getProblems());
            mgr.enable(m1);
            mgr.disable(m1);
            mgr.delete(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    static void addOpenideModules (ModuleManager mgr) throws Exception {
        ClassLoader l = SetupHid.class.getClassLoader();
        String openide =
"Manifest-Version: 1.0\n" +
"OpenIDE-Module: org.openide.modules\n" +
"OpenIDE-Module-Localizing-Bundle: org/openide/modules/Bundle.properties\n" +
"Specification-Title: NetBeans\n" +
"OpenIDE-Module-Specification-Version: 6.2\n" +
"\n" +
"Name: /org/openide/modules/\n" +
"Package-Title: org.openide.modules\n";
               
        Manifest mani = new Manifest (new java.io.ByteArrayInputStream (openide.getBytes ()));
        mgr.enable(mgr.createFixed(mani, null, l));
     }
    
}
