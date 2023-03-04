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
package org.netbeans.core.netigso;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import org.netbeans.MockEvents;
import org.netbeans.MockModuleInstaller;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import static org.netbeans.SetupHid.createTestJAR;

/**
 *
 * @author Jaroslav Tulach
 */
public class NetigsoDuplicatedClassForNameTest extends SetupHid {
    private File simpleModule;
    private File simpleModule2;
    private File dependsModule;
    public NetigsoDuplicatedClassForNameTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        Locale.setDefault(new Locale("te", "ST"));
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());

        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        simpleModule = createTestJAR("simple-module", null);
        
        String snd =
            "Manifest-Version: 1.0\n" +
"OpenIDE-Module: org.foo2/1\n" +
"OpenIDE-Module-Name: Second Foo test module\n" +
"OpenIDE-Module-Specification-Version: 1.2"
          + "";
        simpleModule2 = changeManifest(simpleModule, snd);
        
        dependsModule = createTestJAR("depends-on-simple-module", null, simpleModule);
    }
    
    public void testDuplicatedClassStillGetsLoaded() throws Exception {
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        HashSet<Module> all = null;
        try {
            Module m1 = mgr.create(simpleModule, null, false, false, false);
            Module m2 = mgr.create(simpleModule2, null, false, false, false);
            Module m3 = mgr.create(dependsModule, null, false, false, false);
            
            HashSet<Module> b = new HashSet<Module>(Arrays.asList(m1, m2, m3));
            mgr.enable(b);
            all = b;

            Class<?> clazz = m3.getClassLoader().loadClass("org.bar.SomethingReflective");
            
            Class<?> sprclass = m1.getClassLoader().loadClass("org.foo.Something");
            
            Thread.currentThread().setContextClassLoader(mgr.getClassLoader());
            Object res = clazz.getMethod("loadClass").invoke(null);

            assertEquals("forName loaded the right class", sprclass, res);
        } finally {
            if (all != null) {
                mgr.disable(all);
            }
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
    
}
