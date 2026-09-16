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

import org.netbeans.core.startup.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;

/**
 * Is loading of libraries OK?
 *
 * @author Jaroslav Tulach
 */
public class NetbinoxLibraryTest extends SetupHid {
    private static Module m1;
    private static ModuleManager mgr;

    public NetbinoxLibraryTest(String name) {
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
            System.setProperty("activated.library", "does.not.exist");
            
            File simpleModule = new File(jars, "activate.jar");
            m1 = mgr.create(simpleModule, null, false, false, false);
            mgr.enable(m1);

            Class<?> main = m1.getClassLoader().loadClass("org.activate.Main");
            Object s = main.getField("start").get(null);
            assertNotNull("Bundle started, its context provided", s);

            String err = System.getProperty("activated.library");
            // the error is taken from ClassLoader.loadLibrary
            if (err == null || err.indexOf("in java.library.path") == -1) {
                fail("The make sure the ClassLoader.findLibrary returns null: " + err);
            }
            
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
