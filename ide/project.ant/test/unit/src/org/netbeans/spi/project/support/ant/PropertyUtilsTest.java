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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.test.MockChangeListener;

/**
 * Test functionality of PropertyUtils.
 * @author Jesse Glick
 */
public class PropertyUtilsTest extends NbTestCase {
    
    public PropertyUtilsTest(String name) {
        super(name);
    }
    
    @Override protected void runTest() throws Throwable {
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
            @Override public Void run() throws Exception {
                try {
                    PropertyUtilsTest.super.runTest();
                } catch (Exception x) {
                    throw x;
                } catch (Throwable x) {
                    throw new Exception(x);
                }
                return null;
            }
        });
    }

    public void testTokenizePath() throws Exception {
        assertEquals("basic tokenization works on ':'",
                Arrays.asList(new String[] {"foo", "bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("foo:bar")));
            assertEquals("basic tokenization works on ';'",
                Arrays.asList(new String[] {"foo", "bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("foo;bar")));
            assertEquals("Unix paths work",
                Arrays.asList(new String[] {"/foo/bar", "baz/quux"}),
                Arrays.asList(PropertyUtils.tokenizePath("/foo/bar:baz/quux")));
            assertEquals("empty components are stripped with ':'",
                Arrays.asList(new String[] {"foo", "bar"}),
                Arrays.asList(PropertyUtils.tokenizePath(":foo::bar:")));
            assertEquals("empty components are stripped with ';'",
                Arrays.asList(new String[] {"foo", "bar"}),
                Arrays.asList(PropertyUtils.tokenizePath(";foo;;bar;")));
            assertEquals("DOS paths are recognized with ';'",
                Arrays.asList(new String[] {"c:\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:\\foo;D:\\\\bar")));
            assertEquals("DOS paths are recognized with ':'",
                Arrays.asList(new String[] {"c:\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:\\foo:D:\\\\bar")));
            assertEquals("a..z can be drive letters",
                Arrays.asList(new String[] {"a:\\foo", "z:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("a:\\foo:z:\\\\bar")));
            assertEquals("A..Z can be drive letters",
                Arrays.asList(new String[] {"A:\\foo", "Z:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("A:\\foo:Z:\\\\bar")));
            assertEquals("non-letters are not drives with ';'",
                Arrays.asList(new String[] {"1", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("1;\\foo;D:\\\\bar")));
            assertEquals("non-letters are not drives with ':'",
                Arrays.asList(new String[] {"1", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("1:\\foo:D:\\\\bar")));
            assertEquals(">1 letters are not drives with ';'",
                Arrays.asList(new String[] {"ab", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("ab;\\foo;D:\\\\bar")));
            assertEquals(">1 letters are not drives with ':'",
                Arrays.asList(new String[] {"ab", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("ab:\\foo:D:\\\\bar")));
            assertEquals("drives use ':'",
                Arrays.asList(new String[] {"c", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c;\\foo;D:\\\\bar")));
            assertEquals("drives use only one ':'",
                Arrays.asList(new String[] {"c", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c::\\foo;D:\\\\bar")));
            assertEquals("drives use only one drive letter",
                Arrays.asList(new String[] {"c", "c:\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:c:\\foo;D:\\\\bar")));
            assertEquals("DOS paths start with '\\'",
                Arrays.asList(new String[] {"c", "foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:foo;D:\\\\bar")));
            assertEquals("DOS paths start with '/'",
                Arrays.asList(new String[] {"c", "/foo", "D:/bar", "/path"}),
                Arrays.asList(PropertyUtils.tokenizePath("c;/foo;D:/bar:/path")));
            assertEquals("empty path handled",
                Collections.EMPTY_LIST,
                Arrays.asList(PropertyUtils.tokenizePath("")));
            assertEquals("effectively empty path handled",
                Collections.EMPTY_LIST,
                Arrays.asList(PropertyUtils.tokenizePath(":;:;")));
            assertEquals("one letter directories handled",
                Arrays.asList(new String[] {"c:/foo/c", "/foo/c/bar", "c", "/foo/c", "/bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:/foo/c;/foo/c/bar;c;/foo/c:/bar")));
            assertEquals("one letter directories handled2",
                Arrays.asList(new String[] {"c"}),
                Arrays.asList(PropertyUtils.tokenizePath("c")));
    }
    
    public void testRelativizeFile() throws Exception {
        clearWorkDir();
        File tmp = getWorkDir();
        File d1 = new File(tmp, "d1");
        File d1f = new File(d1, "f");
        File d1s = new File(d1, "s p a c e");
        File d1sf = new File(d1s, "f");
        File d2 = new File(tmp, "d2");
        File d2f = new File(d2, "f");
        // Note that "/tmp/d11".startsWith("/tmp/d1"), hence this being interesting:
        File d11 = new File(tmp, "d11");
        // Note: none of these dirs/files exist yet.
        assertEquals("d1f from d1", "f", PropertyUtils.relativizeFile(d1, d1f));
        assertEquals("d1 from d1f", "..", PropertyUtils.relativizeFile(d1f, d1)); // #61687
        assertEquals("d2f from d1", "../d2/f", PropertyUtils.relativizeFile(d1, d2f));
        assertEquals("d1 from d1", ".", PropertyUtils.relativizeFile(d1, d1));
        assertEquals("d2 from d1", "../d2", PropertyUtils.relativizeFile(d1, d2));
        assertEquals("d1s from d1", "s p a c e", PropertyUtils.relativizeFile(d1, d1s));
        assertEquals("d1sf from d1", "s p a c e/f", PropertyUtils.relativizeFile(d1, d1sf));
        assertEquals("d11 from d1", "../d11", PropertyUtils.relativizeFile(d1, d11));
        // Now make them and check that the results are the same.
        assertTrue("made d1s", d1s.mkdirs());
        assertTrue("made d1f", d1f.createNewFile());
        assertTrue("made d1sf", d1sf.createNewFile());
        assertTrue("made d2", d2.mkdirs());
        assertTrue("made d2f", d2f.createNewFile());
        assertEquals("existing d1f from d1", "f", PropertyUtils.relativizeFile(d1, d1f));
        assertEquals("existing d2f from d1", "../d2/f", PropertyUtils.relativizeFile(d1, d2f));
        assertEquals("existing d1 from d1", ".", PropertyUtils.relativizeFile(d1, d1));
        assertEquals("existing d2 from d1", "../d2", PropertyUtils.relativizeFile(d1, d2));
        assertEquals("existing d1s from d1", "s p a c e", PropertyUtils.relativizeFile(d1, d1s));
        assertEquals("existing d1sf from d1", "s p a c e/f", PropertyUtils.relativizeFile(d1, d1sf));
        assertEquals("existing d11 from d1", "../d11", PropertyUtils.relativizeFile(d1, d11));
        // XXX: the below code should pass on Unix too I guess.
        if (Utilities.isWindows()) {
            // test Windows drives:
            File f1 = new File("C:\\folder\\one");
            File f2 = new File("D:\\t e m p\\two");
            assertNull("different drives cannot be relative", PropertyUtils.relativizeFile(f1, f2));
            f1 = new File("D:\\folder\\one");
            f2 = new File("D:\\t e m p\\two");
            assertEquals("relativization failed for Windows absolute paths", "../../t e m p/two", PropertyUtils.relativizeFile(f1, f2));
            
            //#231704 when 2 UNC paths have different case, they are equal.
            //better to show as non-relative rather than throw an assert.
            f1 =  new File("\\\\MKLEINT-CZ\\test11");
            f2 = new File("\\\\Mkleint-CZ\\test22");
            assertNull(PropertyUtils.relativizeFile(f1, f2));
            
        }
        assertEquals(".", PropertyUtils.relativizeFile(new File(new File(".").getAbsolutePath()), new File(".")));
    }
    
    public void testGlobalProperties() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        File ubp = new File(getWorkDir(), "build.properties");
        assertFalse("no build.properties yet", ubp.exists());
        assertEquals("no properties to start", Collections.EMPTY_MAP, PropertyUtils.getGlobalProperties());
        EditableProperties p = new EditableProperties(false);
        p.setProperty("key1", "val1");
        p.setProperty("key2", "val2");
        PropertyUtils.putGlobalProperties(p);
        assertTrue("now have build.properties", ubp.isFile());
        p = PropertyUtils.getGlobalProperties();
        assertEquals("two definitions now", 2, p.size());
        assertEquals("key1 correct", "val1", p.getProperty("key1"));
        assertEquals("key2 correct", "val2", p.getProperty("key2"));
        Properties p2 = new Properties();
        InputStream is = new FileInputStream(ubp);
        try {
            p2.load(is);
        } finally {
            is.close();
        }
        assertEquals("two definitions now from disk", 2, p2.size());
        assertEquals("key1 correct from disk", "val1", p2.getProperty("key1"));
        assertEquals("key2 correct from disk", "val2", p2.getProperty("key2"));
        // Test the property provider too.
        PropertyProvider gpp = PropertyUtils.globalPropertyProvider();
        MockChangeListener l = new MockChangeListener();
        gpp.addChangeListener(l);
        p = PropertyUtils.getGlobalProperties();
        assertEquals("correct initial definitions", p, gpp.getProperties());
        p.setProperty("key3", "val3");
        assertEquals("still have 2 defs", 2, gpp.getProperties().size());
        l.assertNoEvents();
        PropertyUtils.putGlobalProperties(p);
        l.assertEvent();
        assertEquals("now have 3 defs", 3, gpp.getProperties().size());
        assertEquals("right val", "val3", gpp.getProperties().get("key3"));
        l.msg("no spurious changes").assertNoEvents();
        // Test changes made using Filesystems API.
        p.setProperty("key1", "val1a");
        FileObject fo = FileUtil.toFileObject(ubp);
        assertNotNull("there is USER_BUILD_PROPERTIES on disk", fo);
        OutputStream os = fo.getOutputStream();
        p.store(os);
        os.close();
        l.msg("got a change from the Filesystems API").assertEvent();
        assertEquals("still have 3 defs", 3, gpp.getProperties().size());
        assertEquals("right val for key1", "val1a", gpp.getProperties().get("key1"));
        // XXX changes made on disk are not picked up... bad test, or something else?
        /*
        Thread.sleep(1000);
        p.setProperty("key2", "val2a");
        OutputStream os = new FileOutputStream(ubp);
        p.store(os);
        os.close();
        FileUtil.toFileObject(ubp).getFileSystem().refresh(false);
        Thread.sleep(1000);
        assertTrue("got a change from disk", l.expect());
        assertEquals("still have 3 defs", 3, gpp.getProperties().size());
        assertEquals("right val for key2", "val2a", gpp.getProperties().get("key2"));
         */
    }
    
    public void testFixedPropertyProvider() throws Exception {
        Map<String,String> defs = new HashMap<String,String>();
        defs.put("key1", "val1");
        defs.put("key2", "val2");
        PropertyProvider pp = PropertyUtils.fixedPropertyProvider(defs);
        assertEquals(defs, pp.getProperties());
    }
    
    public void testPropertiesFilePropertyProvider() throws Exception {
        clearWorkDir();
        final FileObject scratch = FileUtil.toFileObject(getWorkDir());
        PropertyProvider pp = PropertyUtils.propertiesFilePropertyProvider(new File(FileUtil.toFile(scratch), "test.properties"));
        MockChangeListener l = new MockChangeListener();
        pp.addChangeListener(l);
        assertEquals("no defs yet (no file)", Collections.EMPTY_MAP, pp.getProperties());
        l.assertNoEvents();
        final FileObject[] testProperties = new FileObject[1];
        scratch.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                testProperties[0] = FileUtil.createData(scratch, "test.properties");
                OutputStream os = testProperties[0].getOutputStream();
                try {
                    PrintWriter pw = new PrintWriter(os);
                    pw.println("a=aval");
                    pw.flush();
                } finally {
                    os.close();
                }
            }
        });
        l.msg("got a change when file was created").assertEvent();
        assertEquals("one key", Collections.singletonMap("a", "aval"), pp.getProperties());
        OutputStream os = testProperties[0].getOutputStream();
        try {
            PrintWriter pw = new PrintWriter(os);
            pw.println("a=aval");
            pw.println("b=bval");
            pw.flush();
        } finally {
            os.close();
        }
        Map<String,String> m = new HashMap<String,String>();
        m.put("a", "aval");
        m.put("b", "bval");
        l.msg("got a change when file was changed").assertEvent();
        assertEquals("right properties", m, pp.getProperties());
        testProperties[0].delete();
        l.msg("got a change when file was deleted").assertEvent();
        assertEquals("no defs again (file deleted)", Collections.emptyMap(), pp.getProperties());
    }
    
    private static final String ILLEGAL_CHARS = " !\"#$%&'()*+,/:;<=>?@[\\]^`{|}~";
    
    public void testIsUsablePropertyName() throws Exception {
        for (int i=0; i<ILLEGAL_CHARS.length(); i++) {
            String s = ILLEGAL_CHARS.substring(i, i+1);
            assertFalse("Not a valid property name: "+s, PropertyUtils.isUsablePropertyName(s));
        }
        for (int i=127; i<256; i++) {
            String s = ""+(char)i;
            assertFalse("Not a valid property name: "+s+" - "+i, PropertyUtils.isUsablePropertyName(s));
        }
        assertFalse("Not a valid property name", PropertyUtils.isUsablePropertyName(ILLEGAL_CHARS));
        for (int i=32; i<127; i++) {
            String s = ""+(char)i;
            if (ILLEGAL_CHARS.indexOf((char)i) == -1) {
                assertTrue("Valid property name: "+s, PropertyUtils.isUsablePropertyName(s));
            }
        }
        assertTrue("Valid property name: java.classpath", 
                PropertyUtils.isUsablePropertyName("java.classpath"));
        assertFalse("Invalid property name: java#classpath", 
                PropertyUtils.isUsablePropertyName("java#classpath"));
        assertFalse("Blank name is not valid property name", 
                PropertyUtils.isUsablePropertyName(""));
    }
    
    public void testGetUsablePropertyName() throws Exception {
        StringBuilder bad = new StringBuilder();
        StringBuilder good = new StringBuilder();
        for (int i=0; i<ILLEGAL_CHARS.length(); i++) {
            bad.append(ILLEGAL_CHARS.substring(i, i+1));
            bad.append("x");
            good.append("_");
            good.append("x");
        }
        assertEquals("Corrected property name does match", good.toString(), PropertyUtils.getUsablePropertyName(bad.toString()));
    }
    
}
