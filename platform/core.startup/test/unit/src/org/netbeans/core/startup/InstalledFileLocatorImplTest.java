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

package org.netbeans.core.startup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.api.PlacesTestUtils;
import org.openide.util.NbBundle;
import org.openide.util.test.TestFileUtils;
/**
 * Test functionality of InstalledFileLocatorImpl.
 * @author Jesse Glick
 */
public class InstalledFileLocatorImplTest extends NbTestCase {

    static {
        InstalledFileLocatorImplTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    public InstalledFileLocatorImplTest(String name) {
        super(name);
    }
    
    protected @Override Level logLevel() {
        return Level.ALL;
    }
    
    private File scratch, nbhome, nbuser, nbdir1, nbdir2;
    private InstalledFileLocator ifl;
    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        scratch = new File(getWorkDir(), "install");
        nbhome = new File(scratch, "nbhome");
        touch(file(nbhome, "a/b"));
        touch(file(nbhome, "a/c"));
        touch(file(nbhome, "d"));
        touch(file(nbhome, "e/f/g"));
        touch(file(nbhome, "loc/x.html"));
        touch(file(nbhome, "loc/x_ja.html"));
        touch(file(nbhome, "loc/x_foo.html"));
        touch(file(nbhome, "loc/x_foo_ja.html"));
        touch(file(nbhome, "loc/y.html"));
        touch(file(nbhome, "h_ja"));
        nbuser = new File(getWorkDir(), "nbuser");
        touch(file(nbuser, "a/b"));
        nbdir1 = new File(scratch, "nbdir1");
        touch(file(nbdir1, "e/f/g"));
        nbdir2 = new File(scratch, "nbdir2");
        touch(file(nbdir2, "h"));
        touch(file(nbdir2, "loc/y_foo.html"));
        File nbdirx = new File(scratch, "nbdirx"); // nonexistent dir
        System.setProperty("netbeans.home", nbhome.getAbsolutePath());
        PlacesTestUtils.setUserDirectory(nbuser);
        System.setProperty("netbeans.dirs",
            nbdir1.getAbsolutePath() + File.pathSeparatorChar +
            nbdir2.getAbsolutePath() + File.pathSeparatorChar +
            // Useless trailing separator intentional:
            nbdirx.getAbsolutePath() + File.pathSeparatorChar);
        NbBundle.setBranding("foo");
        ifl = new InstalledFileLocatorImpl();
    }
    private static File file(File dir, String path) {
        return new File(dir, path.replace('/', File.separatorChar));
    }
    private static void touch(File f) throws IOException {
        File p = f.getParentFile();
        if (!p.exists()) {
            if (!p.mkdirs()) {
                throw new IOException(p.getAbsolutePath());
            }
        }
        OutputStream os = new FileOutputStream(f);
        os.close();
    }

    /**
     * Test file locating.
     * Note that there can be only one test method because the IFLI static
     * block initializes the dir list immediately with the given system
     * properties, so another test in the same VM would not be able to change them.
     */
    public void testLocate() throws Exception {
        Locale.setDefault(Locale.JAPAN);
        try {
        InstalledFileLocatorImpl.prepareCache();
        try {
        assertEquals("[cache] found a simple file", file(nbhome, "d"), ifl.locate("d", null, false));
        assertEquals("[cache] did not find a nonexistent file", null, ifl.locate("d2", null, false));
        assertEquals("[cache] found an override in nbuser", file(nbuser, "a/b"), ifl.locate("a/b", null, false));
        assertEquals("[cache] found a non-override in nbhome", file(nbhome, "a/c"), ifl.locate("a/c", null, false));
        assertEquals("[cache] found an overridden dir in nbuser", file(nbuser, "a"), ifl.locate("a", null, false));
        assertEquals("[cache] did not find a nonexistent file in an existing dir", null, ifl.locate("a/x", null, false));
        assertEquals("[cache] found a multilevel override in an nbdirs component", file(nbdir1, "e/f/g"), ifl.locate("e/f/g", null, false));
        assertEquals("[cache] all nbdirs components scanned", file(nbdir2, "h"), ifl.locate("h", null, false));
        assertEquals("[cache] localized and branded resource can be found", file(nbhome, "loc/x_foo_ja.html"), ifl.locate("loc/x.html", null, true));
        assertEquals("[cache] nbdirs can override location of a branded resource", file(nbdir2, "loc/y_foo.html"), ifl.locate("loc/y.html", null, true));
        assertEquals("[cache] but look in all dirs for most specific resource first", file(nbhome, "h_ja"), ifl.locate("h", null, true));
        assertEquals("[cache] localized lookup a no-op for nonlocalized files", file(nbuser, "a/b"), ifl.locate("a/b", null, true));
        } finally {
            InstalledFileLocatorImpl.discardCache();
        }
        assertEquals("[no cache] found a simple file", file(nbhome, "d"), ifl.locate("d", null, false));
        assertEquals("[no cache] did not find a nonexistent file", null, ifl.locate("d2", null, false));
        touch(file(nbhome, "d2"));
        assertEquals("[no cache] but did find a newly added file", file(nbhome, "d2"), ifl.locate("d2", null, false));
        assertEquals("[no cache] found an override in nbuser", file(nbuser, "a/b"), ifl.locate("a/b", null, false));
        assertEquals("[no cache] found a non-override in nbhome", file(nbhome, "a/c"), ifl.locate("a/c", null, false));
        assertEquals("[no cache] found an overridden dir in nbuser", file(nbuser, "a"), ifl.locate("a", null, false));
        assertEquals("[no cache] did not find a nonexistent file in an existing dir", null, ifl.locate("a/x", null, false));
        assertEquals("[no cache] found a multilevel override in an nbdirs component", file(nbdir1, "e/f/g"), ifl.locate("e/f/g", null, false));
        assertEquals("[no cache] all nbdirs components scanned", file(nbdir2, "h"), ifl.locate("h", null, false));
        assertEquals("[no cache] localized and branded resource can be found", file(nbhome, "loc/x_foo_ja.html"), ifl.locate("loc/x.html", null, true));
        assertEquals("[no cache] nbdirs can override location of a branded resource", file(nbdir2, "loc/y_foo.html"), ifl.locate("loc/y.html", null, true));
        assertEquals("[no cache] but look in all dirs for most specific resource first", file(nbhome, "h_ja"), ifl.locate("h", null, true));
        assertEquals("[no cache] localized lookup a no-op for nonlocalized files", file(nbuser, "a/b"), ifl.locate("a/b", null, true));
        } finally {
            Locale.setDefault(Locale.ENGLISH);
        }
    }

    public void testLocateAll() throws Exception {
        InstalledFileLocatorImpl.prepareCache();
        try {
        doTestLocateAll();
        } finally {
            InstalledFileLocatorImpl.discardCache();
        }
        doTestLocateAll();
    }
    private void doTestLocateAll() {
        assertEquals(new HashSet<File>(Arrays.asList(file(nbuser, "a/b"), file(nbhome, "a/b"))), ifl.locateAll("a/b", null, false));
        assertEquals(Collections.emptySet(), ifl.locateAll("nonexistent", null, false));
        assertEquals(Collections.emptySet(), ifl.locateAll("nonexistent", null, true));
        assertEquals(new HashSet<File>(Arrays.asList(file(nbhome, "loc/y.html"), file(nbdir2, "loc/y_foo.html"))), ifl.locateAll("loc/y.html", null, true));
        assertEquals(new HashSet<File>(Arrays.asList(file(nbdir1, "e/f"), file(nbhome, "e/f"))), ifl.locateAll("e/f", null, false));
    }
    
    public void testLocateParallel() throws Exception {
        File x1 = new File(nbdir1, "x");
        touch(x1);
        File x2 = new File(nbdir2, "x");
        touch(x2);
        TestFileUtils.writeFile(new File(nbdir1, "update_tracking/mod-a.xml"), "<module codename=\"mod.a\">\n<file name=\"x\"/>\n</module>\n");
        TestFileUtils.writeFile(new File(nbdir2, "update_tracking/mod-b.xml"), "<module codename=\"mod.b\">\n<file name=\"x\"/>\n</module>\n");
        InstalledFileLocatorImpl.prepareCache();
        try {
        assertEquals(x1, ifl.locate("x", "mod.a", false));
        assertEquals(x2, ifl.locate("x", "mod.b", false));
        assertEquals(x1, ifl.locate("x", null, false));
        } finally {
            InstalledFileLocatorImpl.discardCache();
        }
        assertEquals(x1, ifl.locate("x", "mod.a", false));
        assertEquals(x2, ifl.locate("x", "mod.b", false));
        assertEquals(x1, ifl.locate("x", null, false));
    }

    public void testWarnings() throws Exception {
        CharSequence cs = Log.enable(InstalledFileLocatorImpl.class.getName(), Level.WARNING);
        File x = new File(nbdir1, "x");
        touch(x);
        File y = new File(nbdir1, "y");
        touch(y);
        TestFileUtils.writeFile(new File(nbdir1, "update_tracking/mod-a.xml"), "<module codename='mod.a'>\n<file name='x'/>\n</module>\n");
        assertEquals(x, ifl.locate("x", "mod.a", false));
        assertEquals(y, ifl.locate("y", "mod.a", false));
        String log = cs.toString();
        assertTrue(log, log.contains("does not own y"));
        assertFalse(log, log.contains("does not own x"));
    }    
    
}
