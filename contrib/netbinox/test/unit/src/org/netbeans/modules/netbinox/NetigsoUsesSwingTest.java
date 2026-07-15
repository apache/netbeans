/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.netbinox;

import java.awt.GraphicsEnvironment;
import org.netbeans.core.startup.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;

/**
 * How does OSGi integration deals with dependency on swing?
 *
 * @author Jaroslav Tulach
 */
public class NetigsoUsesSwingTest extends SetupHid {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NetigsoUsesSwingTest.class);
    }

    private static Module m1;
    private static ModuleManager mgr;
    private File simpleModule;

    public NetigsoUsesSwingTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
//        NetigsoModuleFactory.clear();

        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "jars");
        jars.mkdirs();
        simpleModule = createTestJAR("uses-swing", null);
    }

    public void testCanReferenceJFrame() throws Exception {
        if (System.getProperty("netbeans.user") == null) {
            File ud = new File(getWorkDir(), "ud");
            ud.mkdirs();

            System.setProperty("netbeans.user", ud.getPath());


            ModuleSystem ms = Main.getModuleSystem();
            mgr = ms.getManager();
            mgr.mutexPrivileged().enterWriteAccess();
            try {
                m1 = mgr.create(simpleModule, null, false, false, false);
                mgr.enable(Collections.<Module>singleton(m1));
                Class<?> c = m1.getClassLoader().loadClass("org.barwing.Main");
                Runnable r = (Runnable)c.getDeclaredConstructor().newInstance();
                r.run();
            } finally {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        }

    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
}
