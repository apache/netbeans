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
package org.netbeans.modules.java.j2seplatform.libraries;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.netbeans.modules.project.libraries.DefaultLibraryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author sdedic
 */
public class LibraryTestUtils {
    public static void registerLibrary(final String libName, final File cp, final File src, File javadoc) throws Exception {
        DefaultLibraryImplementation lib;
        lib = new DefaultLibraryImplementation("j2se", new String[]{"classpath", "src", "javadoc"});
        lib.setName(libName);
        List<URL> l = new ArrayList<URL>();
        URL u = Utilities.toURI(cp).toURL();
        if (cp.getPath().endsWith(".jar")) {
            u = FileUtil.getArchiveRoot(u);
        }
        l.add(u);
        lib.setContent("classpath", l);
        if (src != null) {
            l = new ArrayList<URL>();
            u = Utilities.toURI(src).toURL();
            if (src.getPath().endsWith(".jar")) {
                u = FileUtil.getArchiveRoot(u);
            }
            l.add(u);
            lib.setContent("src", l);
        }
        if (javadoc != null) {
            l = new ArrayList<URL>();
            u = Utilities.toURI(javadoc).toURL();
            if (javadoc.getPath().endsWith(".jar")) {
                u = FileUtil.getArchiveRoot(u);
            }
            l.add(u);
            lib.setContent("javadoc", l);
        }
        TestLibraryProviderImpl prov = TestLibraryProviderImpl.getDefault();
        prov.addLibrary(lib);
    }


    public static File createJar(File folder, String name, String resources[]) throws Exception {
        folder.mkdirs();
        File f = new File(folder,name);
        if (!f.exists()) {
            f.createNewFile();
        }
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(f));
        for (int i = 0; i < resources.length; i++) {
            jos.putNextEntry(new ZipEntry(resources[i]));
        }
        jos.close();
        return f;
    }
}
