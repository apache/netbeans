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
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.core.startup.layers.ArchiveURLMapper;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seplatform.platformdefinition.JavaPlatformProviderImpl;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.netbeans.modules.masterfs.MasterURLMapper;

// XXX needs to test listening as well
import org.openide.util.test.MockLookup;

/**
 * @author  David Konecny
 */
public class JavadocForBinaryQueryLibraryImplTest extends NbTestCase {
    
    public JavadocForBinaryQueryLibraryImplTest(java.lang.String testName) {
        super(testName);
        MockLookup.setInstances(
                TestLibraryProviderImpl.getDefault(),
                new JavaPlatformProviderImpl(),
                new ArchiveURLMapper(),
                new JavadocForBinaryQueryLibraryImpl(),
                new MasterURLMapper());
    }
    
    private String getBase() throws Exception {
        File dir = getWorkDir();
        if (Utilities.isWindows()) {
            dir = new File(dir.getCanonicalPath());
        }
        return dir.toString();
    }
    
    protected @Override void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath()); 
        super.setUp();
        clearWorkDir();        
    }
    
    private void setupLibraries() throws Exception {
        File dir = new File(getBase());
        
        // create library1:
        String libPath = dir.toString() + "/library1";
        File library = createJar(new File(libPath), "library1.jar", new String[]{"Main.class"});
        File javadoc = new File(libPath+"/javadoc1");
        javadoc.mkdir();
        registerLibrary("library1", library, javadoc);
        
        // create library2:
        libPath = dir.toString() + "/library2";
        library = createJar(new File(libPath), "library2.jar", new String[]{"Main.class"});
        javadoc = createJar(new File(libPath), "library2javadoc.jar", new String[]{"index.html"});
        registerLibrary("library2", library, javadoc);
        
        // create library3:
        libPath = dir.toString() + "/library3";
        library = new File(libPath+"/library3");
        library.mkdirs();
        javadoc = new File(libPath+"/javadoc3");
        javadoc.mkdirs();
        registerLibrary("library3", library, javadoc);
        
        // refresh FS
        FileUtil.toFileObject(dir).getFileSystem().refresh(false);
    }
    
    private File createJar(File folder, String name, String resources[]) throws Exception {
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
    
    private void registerLibrary(final String libName, final File cp, final File javadoc) throws Exception {
        LibraryTestUtils.registerLibrary(libName, cp, null, javadoc);
    }
    
    public void testQuery() throws Exception {
        setupLibraries();
        
        // library1: test that folder with javadoc is found for the jar
        File f = new File(getBase()+"/library1/library1.jar");
        URL u = Utilities.toURI(f).normalize().toURL();
        u = FileUtil.getArchiveRoot(u);
        URL urls[] = JavadocForBinaryQuery.findJavadoc(u).getRoots();
        assertEquals(1, urls.length);
        String base = Utilities.toURI(new File(getBase())).toString();
        assertEquals(base+"library1/javadoc1/", urls[0].toExternalForm());
        
        // library2: test that jar with javadoc is found for the class from library jar
        f = new File(getBase()+"/library2/library2.jar");
        String us = Utilities.toURI(f).normalize().toString();
        us = "jar:" + us + "!/";
        u = new URL(us);
        urls = JavadocForBinaryQuery.findJavadoc(u).getRoots();
        assertEquals(1, urls.length);
        assertEquals("jar:"+base+"library2/library2javadoc.jar!/", urls[0].toExternalForm());
        
        // library2: test that folder with javadoc is found for the classpath root from the library
        f = new File(getBase()+"/library3/library3");
        u = Utilities.toURI(f).normalize().toURL();
        urls = JavadocForBinaryQuery.findJavadoc(u).getRoots();
        assertEquals(1, urls.length);
        assertEquals(base+"library3/javadoc3/", urls[0].toExternalForm());
    }
    
}
