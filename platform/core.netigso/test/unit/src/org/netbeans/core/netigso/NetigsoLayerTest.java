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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * How does OSGi integration deals with layer registration?
 *
 * @author Jaroslav Tulach
 */
public class NetigsoLayerTest extends SetupHid {
    private static Module m1;
    private static ModuleManager mgr;

    public NetigsoLayerTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();

        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "jars");
        jars.mkdirs();
        File simpleModule = createTestJAR("simple-module", null);
        File dependsOnSimpleModule = createTestJAR("depends-on-simple-module", null, simpleModule);

        if (System.getProperty("netbeans.user") == null) {
            File ud = new File(getWorkDir(), "ud");
            ud.mkdirs();

            System.setProperty("netbeans.user", ud.getPath());


            ModuleSystem ms = Main.getModuleSystem();
            mgr = ms.getManager();
            mgr.mutexPrivileged().enterWriteAccess();
            try {
                File j1 = new File(jars, "simple-module.jar");
                m1 = mgr.create(j1, null, false, false, false);
                mgr.enable(Collections.<Module>singleton(m1));
            } finally {
                mgr.mutexPrivileged().exitWriteAccess();
            }
        }

    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }
    public void testOSGiCanProvideLayer() throws Exception {
        FileObject fo;
        Module m2;
        try {
            mgr.mutexPrivileged().enterWriteAccess();
            String mfBar = "Bundle-SymbolicName: org.bar\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Import-Package: org.foo\n" +
                "Export-Package: org.bar\n" +
                "OpenIDE-Module-Layer: org/bar/layer.xml\n" +
                "\n\n";

            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            m2 = mgr.create(j2, null, false, false, false);
            mgr.enable(m2);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        try {
            mgr.mutexPrivileged().enterWriteAccess();
            fo = FileUtil.getConfigFile("TestFolder");
            assertNotNull("Folder found", fo);

            URL u = mgr.getClassLoader().getResource("org/bar/layer.xml");
            assertNotNull("System ClassLoader can load resources", u);
        } finally {
            mgr.disable(m2);
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }

    public void testOSGiCanProvideImpl() throws Exception {
        FileObject fo;
        Module m2;
        try {
            mgr.mutexPrivileged().enterWriteAccess();
            String mfBar = "Bundle-SymbolicName: org.kuk\n" +
                "Bundle-Version: 1.1.0\n" +
                "Bundle-ManifestVersion: 2\n" +
                "Import-Package: org.foo\n" +
                "OpenIDE-Module-Layer: org/bar/impl/layer.xml\n" +
                "\n\n";

            File j2 = changeManifest(new File(jars, "depends-on-simple-module.jar"), mfBar);
            m2 = mgr.create(j2, null, false, false, false);
            mgr.enable(m2);
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        try {
            mgr.mutexPrivileged().enterWriteAccess();
            fo = FileUtil.getConfigFile("TestImplFolder");
            assertNotNull("Folder found", fo);

            URL u = mgr.getClassLoader().getResource("org/bar/impl/layer.xml");
            assertNotNull("System ClassLoader can load resources", u);
        } finally {
            mgr.disable(m2);
            mgr.mutexPrivileged().exitWriteAccess();
        }
    }
}
