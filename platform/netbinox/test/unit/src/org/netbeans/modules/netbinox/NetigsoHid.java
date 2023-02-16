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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.Events;
import org.netbeans.JarClassLoader;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.SetupHid;
import org.openide.filesystems.FileUtil;

/**
 * Basic infrastructure for testing OSGi functionality.
 *
 * @author Jaroslav Tulach
 */
public class NetigsoHid extends SetupHid {
    File simpleModule;

    public NetigsoHid(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "jars");
        jars.mkdirs();
        simpleModule = createTestJAR("simple-module", null);
        File dependsOnSimpleModule = createTestJAR("depends-on-simple-module", null, simpleModule);

        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();

        System.setProperty("netbeans.user", ud.getPath());
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }

    
    protected final File changeManifest(File orig, String manifest) throws IOException {
        return changeManifest(getWorkDir(), orig, manifest);
    }

    protected static File changeManifest(File dir, File orig, String manifest) throws IOException {
        return changeManifest(dir, orig.getName(), orig, manifest);
    }
    protected static File changeManifest(File dir, String newName, File orig, String manifest) throws IOException {
        File f = new File(dir, newName);
        Manifest mf = new Manifest(new ByteArrayInputStream(manifest.getBytes("utf-8")));
        mf.getMainAttributes().putValue("Manifest-Version", "1.0");
        JarOutputStream os = new JarOutputStream(new FileOutputStream(f), mf);
        JarFile jf = new JarFile(orig);
        Enumeration<JarEntry> en = jf.entries();
        InputStream is;
        while (en.hasMoreElements()) {
            JarEntry e = en.nextElement();
            if (e.getName().equals("META-INF/MANIFEST.MF")) {
                continue;
            }
            os.putNextEntry(e);
            is = jf.getInputStream(e);
            FileUtil.copy(is, os);
            is.close();
            os.closeEntry();
        }
        os.close();

        return f;
    }

}
