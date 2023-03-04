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

package org.netbeans.core.startup.layers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.StringTokenizer;
import org.netbeans.core.startup.layers.NbinstURLMapper;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;

public class NbinstURLMapperTest extends NbTestCase {

    private static final String FILE_NAME = "test.txt";     //NOI18N
    private static final String FOLDER_NAME = "modules";    //NOI18N
    
    private File testFile;
    private int expectedLength;

    public NbinstURLMapperTest (String testName) throws IOException {
        super (testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockServices.setServices(
                TestInstalledFileLocator.class,
                NbinstURLMapper.class);

        org.netbeans.core.startup.Main.initializeURLFactory ();
        
        File f = this.getWorkDir();
        this.clearWorkDir();
        Lookup.Result result = Lookup.getDefault().lookupResult(InstalledFileLocator.class);
        boolean found = false;
        for (java.util.Iterator it = result.allInstances().iterator(); it.hasNext();) {
            Object locator = it.next();
            if (locator instanceof TestInstalledFileLocator) {
                ((TestInstalledFileLocator)locator).setRoot(f);
                found = true;
            }
        }
        assertTrue("No TestInstalledFileLocator can be found in " + Lookup.getDefault(), found);
        f = new File (f,FOLDER_NAME);
        f.mkdir();
        f = new File (f,FILE_NAME);
        f.createNewFile();
        testFile = f;
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(f));
            pw.println(FILE_NAME);
        } finally {
            if (pw!=null) {
                pw.close ();
            }
        }
        this.expectedLength = (int) f.length();
    }

    public void testFindFileObject () throws MalformedURLException, IOException {
        URL url = new URL ("nbinst:///modules/test.txt");  //NOI18N
        FileObject fo = URLMapper.findFileObject (url);
        assertNotNull ("The nbinst URL was not resolved.",fo);
        assertEquals("URLMapper returned wrong file.",FileUtil.toFile(fo),testFile);
        url = new URL ("nbinst://test-module/modules/test.txt");
        fo = URLMapper.findFileObject (url);
        assertNotNull ("The nbinst URL was not resolved.",fo);
        assertEquals("URLMapper returned wrong file.",FileUtil.toFile(fo),testFile);
        url = new URL ("nbinst://foo-module/modules/test.txt");
        fo = URLMapper.findFileObject (url);
        assertNull ("The nbinst URL was resolved.",fo);
        FileObject folder = FileUtil.toFileObject(testFile.getParentFile());
        assertTrue(folder.isFolder());
        assertEquals("#146173: support for folder URLs", folder, URLMapper.findFileObject(new URL("nbinst://test-module/modules/")));
    }

    public void testURLConnection() throws MalformedURLException, IOException {
        URL url = new URL ("nbinst:///modules/test.txt");                //NOI18N
        URLConnection connection = url.openConnection();
        assertEquals ("URLConnection returned wrong content length.",connection.getContentLength(),expectedLength);
        BufferedReader in = null;
        try {
            in = new BufferedReader  ( new InputStreamReader (connection.getInputStream()));
            String line = in.readLine();
            assertTrue("URLConnection returned invalid InputStream",line.equals(FILE_NAME));
        } finally {
            if (in != null) {
                in.close ();
            }
        }
    }
    
    public void testURLNoInetAccess() throws MalformedURLException, IOException {
        URL url1 = new URL ("nbinst://test-module/modules/test.txt");   // NOI18N
        URL url2 = new URL ("nbinst://foo-module/modules/test.txt");    // NOI18N
        SecurityManager defaultManager = System.getSecurityManager();
        
        System.setSecurityManager(new InetSecurityManager());
        try {
            // make sure we do not try to resolve host name
            url1.hashCode();
            url1.equals(url2);
            testURLConnection();
        } finally {
            System.setSecurityManager(defaultManager);
        }
    }

    public static class TestInstalledFileLocator extends InstalledFileLocator {

        private File root;

        public TestInstalledFileLocator() {}

        public void setRoot (File root) {
            this.root = root;
        }

        public File locate(String relativePath, String codeNameBase, boolean localized) {
            assert relativePath != null;
            if (relativePath.endsWith("/")) {
                throw new IllegalArgumentException("path " + relativePath + " ends with slash");
            }
            if (root == null) {
                return null;
            }
            if (codeNameBase!= null && !"test-module".equals(codeNameBase)) {
                return null;
            }
            StringTokenizer tk = new StringTokenizer(relativePath,"/");
            File f = this.root;
            while (tk.hasMoreTokens()) {
                String part = tk.nextToken();
                f = new File (f,part);
                if (!f.exists()) {
                    return null;
                }
            }
            return f;
        }
    }

    private static class InetSecurityManager extends SecurityManager {

        @Override
        public void checkConnect(String host, int port) {
            fail("Resolving host "+host);   // NOI18N
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
        }

        @Override
        public void checkPermission(Permission perm) {
        }

    }
}
