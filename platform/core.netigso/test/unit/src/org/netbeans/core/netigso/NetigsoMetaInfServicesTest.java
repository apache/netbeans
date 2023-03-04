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
import java.util.concurrent.Callable;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.openide.util.Lookup;

/**
 * Do we correctly handle META-INF/services in bundles?
 *
 * @author Jaroslav Tulach
 */
public class NetigsoMetaInfServicesTest extends SetupHid {
    private static Module m1;
    private static ModuleManager mgr;
    private int cnt;

    public NetigsoMetaInfServicesTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "space in path");
        jars.mkdirs();
        File simpleModule = createTestJAR("activate", null);
    }

    public void testActivation() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            Callable<?> fr = Lookup.getDefault().lookup(Callable.class);
            assertNull("No registration found yet", fr);

            
            File simpleModule = new File(jars, "activate.jar");
            m1 = mgr.create(simpleModule, null, false, false, false);
            mgr.enable(m1);

            Callable<?> registration = Lookup.getDefault().lookup(Callable.class);
            assertNotNull("Registration of Callable found", registration);
            Object s = registration.call();
            assertNotNull("Bundle started, its context provided", s);

            mgr.disable(m1);
            
            Callable<?> nr = Lookup.getDefault().lookup(Callable.class);
            assertNull("No registration found anymore", nr);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
}
