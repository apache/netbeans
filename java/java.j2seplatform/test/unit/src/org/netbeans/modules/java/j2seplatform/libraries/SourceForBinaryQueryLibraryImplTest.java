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

package org.netbeans.modules.java.j2seplatform.libraries;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.core.startup.layers.ArchiveURLMapper;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seplatform.platformdefinition.JavaPlatformProviderImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;
import org.netbeans.modules.masterfs.MasterURLMapper;
import org.openide.util.test.MockLookup;

/**
 * J2SELibrarySourceForBinaryQuery test
 *
 */
public class SourceForBinaryQueryLibraryImplTest extends NbTestCase {
    
    public SourceForBinaryQueryLibraryImplTest(String testName) {
        super(testName);
        MockLookup.setInstances(
                TestLibraryProviderImpl.getDefault(),
                new JavaPlatformProviderImpl(),
                new ArchiveURLMapper(),
                new J2SELibrarySourceForBinaryQuery(),
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
        File src = new File(libPath+"/src1");
        src.mkdir();
        registerLibrary("library1", library, src);
        
        // create library2:
        libPath = dir.toString() + "/library2";
        library = createJar(new File(libPath), "library2.jar", new String[]{"Main.class"});
        src = createJar(new File(libPath), "library2src.jar", new String[]{"Main.java"});
        registerLibrary("library2", library, src);
        
        // create library3:
        libPath = dir.toString() + "/library3";
        library = new File(libPath+"/library3");
        library.mkdirs();
        src = new File(libPath+"/src3");
        src.mkdirs();
        registerLibrary("library3", library, src);
                                
        // refresh FS
        FileUtil.toFileObject(dir).getFileSystem().refresh(false);
    }
    
    private void setupLibraryForListeningTest () throws Exception {
        // create library4:
        File dir = new File(getBase());
        String libPath = dir.toString() + "/library4";
        File library = new File(libPath,"library4");
        library.mkdirs();
        File src = new File(libPath+"/src4");
        src.mkdirs();
        registerLibrary("library4", library, null);
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
    
    private void registerLibrary(final String libName, final File cp, final File src) throws Exception {
        LibraryTestUtils.registerLibrary(libName, cp, src, null);
    }
    
    private LibraryImplementation getLibrary (String name) {
        TestLibraryProviderImpl prov = TestLibraryProviderImpl.getDefault();
        LibraryImplementation[] impls = prov.getLibraries();
        for (int i=0; i< impls.length; i++) {
            if (impls[i].getName().equals (name)) {
                return impls[i];
            }
        }
        return null;
    }
    
    
    public void testQuery() throws Exception {
        setupLibraries();
        
        // library1: test that folder with javadoc is found for the jar
        File f = new File(getBase()+"/library1/library1.jar");
        URL u = Utilities.toURI(f).normalize().toURL();
        u = FileUtil.getArchiveRoot(u);
        FileObject[] fos = SourceForBinaryQuery.findSourceRoots(u).getRoots();
        assertEquals(1, fos.length);
        String base = Utilities.toURI(new File(getBase())).toString();
        assertEquals(base+"library1/src1/", fos[0].getURL().toExternalForm());
        
        // library2: test that jar with javadoc is found for the class from library jar
        f = new File(getBase()+"/library2/library2.jar");
        String us = Utilities.toURI(f).normalize().toString();
        us = "jar:" + us + "!/";
        u = new URL(us);
        fos = SourceForBinaryQuery.findSourceRoots(u).getRoots();
        assertEquals(1, fos.length);
        assertEquals("jar:"+base+"library2/library2src.jar!/", fos[0].getURL().toExternalForm());
        
        // library2: test that folder with javadoc is found for the classpath root from the library
        f = new File(getBase()+"/library3/library3");
        u = Utilities.toURI(f).normalize().toURL();
        fos = SourceForBinaryQuery.findSourceRoots(u).getRoots();
        assertEquals(1, fos.length);
        assertEquals(base+"library3/src3/", fos[0].getURL().toExternalForm());
    }
    
    public void testListening () throws Exception {
        setupLibraryForListeningTest();
        File f = new File(getBase()+"/library4");
        f = new File (f, "library4");
        URL u = Utilities.toURI(f).normalize().toURL();
        SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(u);
        assertEquals(result.getRoots().length,0);
        SFBQResultListener l = new SFBQResultListener ();        
        result.addChangeListener(l);
        LibraryImplementation impl = getLibrary("library4");
        List<URL> srcList = new ArrayList<URL>();
        File baseDir = new File(getBase());
        File libDir = new File(baseDir,"library4");
        File srcDir = new File(libDir,"src4");
        srcList.add (Utilities.toURI(srcDir).toURL());
        impl.setContent("src", srcList);
        ChangeEvent[] events = l.getEvents();
        assertEquals(1,events.length);
        l.clearEventQueue();
        assertEquals(result.getRoots().length,1);
        String base = Utilities.toURI(new File(getBase())).toString();
        assertEquals(base+"library4/src4/",result.getRoots()[0].getURL().toExternalForm());
    }
    
    private static class SFBQResultListener implements ChangeListener {
        
        private final List<ChangeEvent> queue = new ArrayList<ChangeEvent>();

        public SFBQResultListener() {}
        
        public void clearEventQueue () {
            this.queue.clear();
        }

        public void stateChanged(ChangeEvent event) {
            this.queue.add (event);
        }
        
        public ChangeEvent[] getEvents () {
            return queue.toArray(new ChangeEvent[0]);
        }                                    
    }
    
}

