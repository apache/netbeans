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

import org.netbeans.core.startup.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.osgi.framework.Bundle;

/**
 * Can not started bundle depend on autoload?
 *
 * @author Jaroslav Tulach
 */
public class NetigsoHighStartLevelDependsOnAutoloadTest extends SetupHid {
    private static Module m1;
    private static ModuleManager mgr;
    private static File activate;
    private File simpleModule;

    public NetigsoHighStartLevelDependsOnAutoloadTest(String name) {
        super(name);
    }
    
    protected boolean autoload() {
        return false;
    }

    protected @Override void setUp() throws Exception {
        if (System.getProperty("netbeans.user") != null) {
            return;
        }
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        simpleModule = createTestJAR("simple-module", null);
        activate = createTestJAR("depends-on-simple-module", null, simpleModule);
        String mf = "Bundle-SymbolicName: org.snd.module\n" +
            "Bundle-Version: 33.0.3\n" +
            "Bundle-ManifestVersion: 2\n" +
            "Import-Package: org.foo\n" +
            "Export-Package: org.bar\n" +
            "\n\n";
        activate = NetigsoHid.changeManifest(getWorkDir(), activate, mf);
        
    }
    public void testNotStartedBundleDependsOnAutoload() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            m1 = mgr.createBundle(activate, null, false, false, false, 4);
            Module m2 = mgr.create(simpleModule, null, false, autoload(), false);
            
            mgr.enable(m1);
            Bundle bundle = NetigsoServicesTest.findBundle(m1.getCodeNameBase());

            Class<?> main = bundle.loadClass("org.bar.SomethingElse");
            assertNotNull("Class loaded", main);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
}
