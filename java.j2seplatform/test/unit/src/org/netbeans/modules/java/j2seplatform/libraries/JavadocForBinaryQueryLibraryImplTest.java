/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.java.j2seplatform.libraries;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.core.startup.layers.ArchiveURLMapper;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seplatform.platformdefinition.JavaPlatformProviderImpl;
import org.netbeans.modules.project.libraries.DefaultLibraryImplementation;
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
        DefaultLibraryImplementation lib;
        lib = new DefaultLibraryImplementation("j2se", new String[]{"classpath", "javadoc"});
        lib.setName(libName);
        List<URL> l = new ArrayList<URL>();
        URL u = Utilities.toURI(cp).toURL();
        if (cp.getPath().endsWith(".jar")) {
            u = FileUtil.getArchiveRoot(u);
        }
        l.add(u);
        lib.setContent("classpath", l);
        l = new ArrayList<URL>();
        u = Utilities.toURI(javadoc).toURL();
        if (javadoc.getPath().endsWith(".jar")) {
            u = FileUtil.getArchiveRoot(u);
        }
        l.add(u);
        lib.setContent("javadoc", l);
        TestLibraryProviderImpl prov = TestLibraryProviderImpl.getDefault();
        prov.addLibrary(lib);
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
