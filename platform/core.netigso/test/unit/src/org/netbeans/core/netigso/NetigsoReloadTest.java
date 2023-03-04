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

import java.util.logging.Level;
import org.netbeans.core.startup.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.openide.filesystems.FileUtil;
import org.osgi.framework.Bundle;

public class NetigsoReloadTest extends NetigsoHid {
    private static Module m1;
    private static ModuleManager mgr;
    private File withActivator;
    private File withoutA;
    private Logger LOG;

    public NetigsoReloadTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        
        LOG = Logger.getLogger("TEST." + getName());

        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "jars");
        jars.mkdirs();
        withActivator = createTestJAR("activate", null);
        withoutA = changeManifest(withActivator, "Manifest-Version: 1.0\n" +
                "Bundle-SymbolicName: org.activate\n" +
                "Import-Package: org.osgi.framework\n" +
                "Bundle-Version: 1.1\n");
    }

    public void testCanReloadAModule() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            m1 = mgr.create(withoutA, null, false, false, false);
            mgr.enable(m1);

        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        
        Bundle b = NetigsoServicesTest.findBundle("org.activate");
        assertEquals("version 1.1", "1.1.0", b.getVersion().toString());

        LOG.info("deleting old version and replacing the JAR");
        
        FileOutputStream os = new FileOutputStream(withoutA);
        FileInputStream is = new FileInputStream(withActivator);
        FileUtil.copy(is, os);
        is.close();
        os.close();
        
        LOG.log(Level.INFO, "jar {0} replaced, redeploying", withoutA);
        TestModuleDeployer.deployTestModule(withoutA);
        LOG.info("Deployed new module");
        
        Bundle newB = NetigsoServicesTest.findBundle("org.activate");
        assertEquals("new version 1.2", "1.2.0", newB.getVersion().toString());
        
        Class<?> main = m1.getClassLoader().loadClass("org.activate.Main");
        Object s = main.getField("start").get(null);
        assertNotNull("Bundle started, its context provided", s);

        mgr.mutexPrivileged().enterWriteAccess();
        try {
            mgr.disable(m1);
            Object e = main.getField("stop").get(null);
            assertNotNull("Bundle stopped, its context provided", e);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
}
