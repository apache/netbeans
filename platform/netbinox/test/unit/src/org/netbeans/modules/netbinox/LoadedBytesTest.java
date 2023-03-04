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

import org.netbeans.core.startup.*;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;

/**
 * Verify content is not loaded when enumerating covered packages.
 *
 * @author Jaroslav Tulach
 */
public class LoadedBytesTest extends SetupHid {
    private static Module m1;
    private static ModuleManager mgr;
    int counter;
    boolean used;
    private File file;


    public LoadedBytesTest(String name) {
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
        File simpleModule = createTestJAR("simple-module", null);
        
        String mf = "Manifest-Version: 1.0\n" + 
            "Bundle-SymbolicName: org.foo\n" +
            "Bundle-Version: 1.2\n\n";
        
        file = NetigsoHid.changeManifest(getWorkDir(), simpleModule, mf);
        NetbinoxFactory.LOG.addHandler(new CountingHandler());
    }

    public void testEnumeratingEntriesDoesNotLoadContent() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        mgr = ms.getManager();
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            m1 = mgr.create(file, null, false, false, false);
            mgr.enable(m1);
            assertTrue("Is enabled: ", m1.isEnabled());

            assertEquals("No bytes loaded", 0, counter);
            
            Class<?> main = m1.getClassLoader().loadClass("org.foo.Something");
            assertNotNull("Class can be loaded", main);
            mgr.disable(m1);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        assertTrue("The CountingHandler was active", used);
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
    private class CountingHandler extends Handler {
        public CountingHandler() {
            setLevel(Level.ALL);
        }

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().startsWith("opening") && record.getMessage().contains("because")) {
                used = true;
            }
            if (record.getMessage().startsWith("Loaded") && record.getMessage().contains("bytes")) {
                String name = (String) record.getParameters()[0];
                if (name.endsWith(".class")) {
                    counter += (Integer)record.getParameters()[1];
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
    
}
