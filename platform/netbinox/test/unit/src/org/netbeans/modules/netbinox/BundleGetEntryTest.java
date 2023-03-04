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

package org.netbeans.modules.netbinox;

import java.net.URL;
import org.netbeans.core.startup.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.netbeans.core.netigso.NetigsoServicesTest;
import org.openide.util.Lookup;
import org.openide.util.Mutex.ExceptionAction;
import org.osgi.framework.Bundle;

/**
 * Can we read resources from NetBeans modules?
 * This time from public packages...
 *
 * @author Jaroslav Tulach
 */
public class BundleGetEntryTest extends SetupHid {
    private static Module m1;
    private static ModuleManager mgr;
    private File jar;
    private File simpleModule;

    public BundleGetEntryTest(String name) {
        super(name);
    }

    protected String moduleManifest() {
        return "OpenIDE-Module: can.read.metainf\n"
           + "OpenIDE-Public-Packages: org.activate\n"
           + "\n"
           + "\n";
    }

    protected final @Override void setUp() throws Exception {
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        simpleModule = createTestJAR("activate", null);
        String mf = moduleManifest();
       jar = NetigsoHid.changeManifest(getWorkDir(), simpleModule, mf);
    }


    public void testActivation() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutex().writeAccess(new ExceptionAction<Void>() {
            @Override
            public Void run() throws Exception {
                Callable<?> fr = Lookup.getDefault().lookup(Callable.class);
                assertNull("No registration found yet", fr);


                m1 = mgr.create(jar, null, false, false, false);
                mgr.enable(m1);

                Module m2 = mgr.create(simpleModule, null, false, false, false);
                mgr.enable(m2);

                
                Bundle bundle = NetigsoServicesTest.findBundle("can.read.metainf");
                assertNotNull("Bundle found", bundle);
                URL res = bundle.getEntry("org/activate/entry.txt");
                assertNotNull("Entry found", res);
                byte[] arr = new byte[1000];
                int len = res.openStream().read(arr);
                String s = new String(arr, 0, len);
                assertEquals("Ahoj", s.substring(0,4));

                mgr.disable(m1);

                return null;
            }
        });
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
}
