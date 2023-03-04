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

package org.netbeans.modules.masterfs.filebasedfs.naming;

import java.io.IOException;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import java.io.File;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.netbeans.modules.masterfs.filebasedfs.utils.Utils;
import org.openide.util.Utilities;

/**
 *
 * @author Radek Matous
 */
public class FileNameTest extends NbTestCase {
    private File f1;
    private File f2;
    private File f3;
    private FileNaming n1;
    private FileNaming n2;
    private FileNaming n3;

    public FileNameTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        f1 = getTestFile();
        f2 = new File (f1.getAbsolutePath());
        f3 = f1.getParentFile();
        n1 = NamingFactory.fromFile(f1);
        n2 = NamingFactory.fromFile(f2);
        n3 = NamingFactory.fromFile(f3);        
    }
    
    public void testCzechNames() throws Exception {
        File f1 = new File (getWorkDir(), "IMístnost.java");
        File f2 = new File (getWorkDir(), "IMístnost_115.java");
        
        n1 = NamingFactory.fromFile(f1);
        n2 = NamingFactory.fromFile(f2);
        
        assertEquals(f1.getName(), n1.getName().toString());
        assertEquals(f2.getName(), n2.getName().toString());

    }

    public void testCollision() throws Exception {
        File root = new File(getWorkDir(), "root");
        root.mkdirs();
        
        // File hash code is based on path. There are many known String collisions
        // though we need the colliding Strings to be:
        //   1. of the same length, so the collision is not broken by random prefix
        //   2. lower case, to not be affected by lower-casing the path on Win
        // For simplicity, I have chosen "y6" and "wt". They collide with xU too,
        // but that doesn't pass our 2. test on Windows
        File file1 = new File (root, "y6");
        File file2 = new File (root, "wt");
        
        // verify that the test itself is effective
        assertEquals("There should be a hash collision, nothing is tested otherwise", file1.hashCode(), file2.hashCode());

        n1 = NamingFactory.fromFile(file1);
        n2 = NamingFactory.fromFile(file2);
        n3 = NamingFactory.fromFile(file1);
        
        assertNotSame("Different files, different names", n1, n2);
        assertSame("Same file, same name", n1, n3);        
        
        Reference<?> ref = new WeakReference<Object>(n1);
        n1 = n3 = null;
        assertGC("Can GC", ref);
        
        FileNaming n4 = NamingFactory.fromFile(file2);
        assertSame("This has to remain same as before", n2, n4);
    }
    
    public void testFolderAndName() throws IOException {
        FileNaming parent = NamingFactory.fromFile(getWorkDir());
        
        File f = new File(getWorkDir(), "test");
        f.createNewFile();
        assertTrue("Is file", f.isFile());
        FileNaming first = NamingFactory.fromFile(parent, f, true);
        assertEquals("First it is file", FileName.class, first.getClass());
        f.delete();
        
        f.mkdirs();
        assertTrue("Is dir", f.isDirectory());
        
        FileNaming dir = NamingFactory.fromFile(parent, f, true);
        assertEquals("Is folder name", FolderName.class, dir.getClass());
        
        f.delete();
        f.createNewFile();
        assertTrue("Is file", f.isFile());
        
        FileNaming file = NamingFactory.fromFile(parent, f, true);
        assertEquals("Is file name", FileName.class, file.getClass());
        
        FileNaming cache = NamingFactory.fromFile(f);
        assertEquals("Is file name too", FileName.class, cache.getClass());
        
        String dump = NamingFactory.dump(Utils.hashCode(f), f);
        if (!dump.contains("References: 1")) {
            fail("We expect just one reference:\n" + dump);
        }
    }
    
    public void testListRoots() {
        for (File f : File.listRoots()) {
            String n = f.getPath();
            if (n.length() <= 1) {
                continue;
            }
            
            String withoutN;
            if (n.endsWith(File.separator)) {
                withoutN = n.substring(0, n.length() - 1);
            } else {
                continue;
            }
            FileNaming fWith = NamingFactory.fromFile(f);
            FileNaming fWithout = NamingFactory.fromFile(new File(withoutN));
            
            assertEquals("Roots should be the same", fWith, fWithout);
            if (Utilities.isWindows()) {
                FileNaming fUpper = NamingFactory.fromFile(new File(f.getPath().toUpperCase(Locale.ENGLISH)));
                FileNaming fLower = NamingFactory.fromFile(new File(f.getPath().toLowerCase(Locale.ENGLISH)));
                assertEquals("Lower and Upper case roots are equal on Windows", fUpper, fLower);
            }
        }
    }
    
    protected File getTestFile() throws Exception {
        File retVal = new File (getWorkDir(), "namingTest");
        if (!retVal.exists()) {
            retVal.createNewFile();
        }
        return retVal;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        n1 = null;
        n2 = null;
        n3 = null;
    }

    public void test69450() throws Exception {
        File fA = new File(getWorkDir(),"A");
        if (fA.exists()) {
            assertTrue(fA.delete());
        }        
        File fa = new File(getWorkDir(),"a");
        if (!fa.exists()) {
            assertTrue(fa.createNewFile());
        }        
        boolean isCaseSensitive = !Utils.equals(fa, fA);                
        FileNaming na = NamingFactory.fromFile(fa);        
        assertEquals(fa.getName(),NamingFactory.fromFile(fa).getName());        
        if (isCaseSensitive) {
            assertFalse(fa.getName().equals(NamingFactory.fromFile(fA).getName()));
            assertFalse(NamingFactory.fromFile(fa).equals(NamingFactory.fromFile(fA)));
            assertNotSame(NamingFactory.fromFile(fa),NamingFactory.fromFile(fA));            
            assertTrue(fa.delete());
            assertTrue(fA.createNewFile());
            assertFalse(fA.getName().equals(na.getName()));
            assertFalse(na.equals(NamingFactory.fromFile(fA)));
            assertFalse(fA.getName().equals(na.getName()));            
        } else {
            assertSame(na,NamingFactory.fromFile(fA));            
            assertEquals(fa.getName(),na.getName());            
            assertEquals(fa.getName(),NamingFactory.fromFile(fA).getName());
            assertEquals(NamingFactory.fromFile(fa),NamingFactory.fromFile(fA));
            assertSame(NamingFactory.fromFile(fa),NamingFactory.fromFile(fA));            
            //#69450            
            assertTrue(fa.delete());
            assertTrue(fA.createNewFile());
            assertFalse(fA.getName().equals(na.getName()));
            assertEquals(na,NamingFactory.fromFile(fA));
            assertSame(na, NamingFactory.fromFile(fA));
            assertFalse(fA.getName() + " / " + na.getName(),fA.getName().equals(na.getName()));
            FileNaming nna = NamingFactory.checkCaseSensitivity(na,fA);
            assertTrue(fA.getName() + " / " + nna.getName(),fA.getName().equals(nna.getName()));
        }
    }
    
    /**
     * Test of equals method, of class org.netbeans.modules.masterfs.pathtree.PathItem.
     */
   public void testEquals () throws Exception {
        assertEquals(n1, n2);
        assertSame(n1, n2);        
        assertNotSame(n3, n1);
        assertNotSame(n3, n2);
        assertEquals(n3, n1.getParent());
        assertEquals(n3, n2.getParent());
        assertSame(n3, n1.getParent());
        assertSame(n3, n2.getParent());                
    }    

    public void testHashcode () throws Exception {
        assertEquals(n3.hashCode(), n1.getParent().hashCode());                
        assertEquals(n3.hashCode(), n2.getParent().hashCode());                                
    }
    
    public void testWeakReferenced () throws Exception {
        List l = new ArrayList ();
        FileNaming current = n1;
        while (current != null) {
            l.add(new WeakReference (current));
            current = current.getParent();
        }
        
        current = null;        
        n1 = null;
        n2 = null;
        n3 = null;
        
        for (int i = 0; i < l.size(); i++) {
            WeakReference weakReference = (WeakReference) l.get(i);
            assertGC("Shoul be GCed: "+((FileNaming)weakReference.get()),  weakReference);
        }        
    }
    
    public void testFileConversion () throws Exception {
        FileNaming[] all = new FileNaming [] {n1, n2, n3};
        File[] files = new File [] {f1, f2, f3};
        for (int i = 0; i < all.length; i++) {
            FileNaming current = all[i];
            File currentFile = files[i];            
            
            while (current != null) {
                assertEquals (current.getFile(), currentFile);
                current = current.getParent();
                currentFile = currentFile.getParentFile();
            }            
        }        
    }

    public void testFileExist () throws Exception {
        FileNaming[] all = new FileNaming [] {n1, n2, n3};
        for (int i = 0; i < all.length; i++) {
            FileNaming current = all[i];
            while (current != null) {
                File file = current.getFile();
                assertTrue(file.getAbsolutePath(), file.exists());
                current = current.getParent();
            }            
        }        
    }

    public void testNamingIsCaseInsensitive() throws Exception {
        File f1 = new File(getWorkDir(), "Ahoj");
        File f2 = new File(getWorkDir(), "ahoJ");

        FileNaming root = NamingFactory.fromFile(getWorkDir());
        FileNaming fn1 = NamingFactory.fromFile(root, f1, false);
        FileNaming fn2 = NamingFactory.fromFile(root, f2, false);

        boolean equalF = Utils.equals(f1,f2);

        f2.createNewFile();
        NamingFactory.checkCaseSensitivity(fn2, f2);
        assertEquals("Name equals file name f2", f2.getName(), fn2.getName());

        if (equalF) {
            assertEquals("File has code", Utils.hashCode(f1), Utils.hashCode(f2));
            assertEquals("FileNaming hash code", fn1.hashCode(), fn2.hashCode());
            assertSame("namings are equal", fn1, fn2);
        } else {
            assertFalse("FileNaming shall be different", fn1.equals(fn2));
        }

        f2.delete();
        f1.createNewFile();
        NamingFactory.checkCaseSensitivity(fn1, f1);
        assertEquals("Name equals file name f1", f1.getName(), fn1.getName());
    }


    /**
     * Test of rename method, of class org.netbeans.modules.masterfs.naming.PathItem.
     */
    public void testRename() throws Exception {
        File f = f1;
        assertTrue(f.exists());
        FileNaming pi = NamingFactory.fromFile(f);
        FileNaming ni = pi.rename("renamed3", null);
        assertTrue(pi != ni);
        File f2 = ni.getFile();
        assertFalse(f.exists());
        assertTrue(f2.exists());
        assertFalse(f2.equals(f));
        assertTrue (f2.getName().equals("renamed3"));        
    }

    public void testTwoNamingsAreOnlyEqualIfTheyRepresentTheSamePath() {
        File hc1 = new HashCodeFile("/space/root/myfile", 444);
        File hc2 = new HashCodeFile("/space/myfile", 444);

        FileNaming nf1 = NamingFactory.fromFile(hc1);
        FileNaming nf2 = NamingFactory.fromFile(hc2);
        
        assertFalse("namings are different", nf1.equals(nf2));
    }


    private static final class HashCodeFile extends File {
        private final int hash;
        public HashCodeFile(String path, int hash) {
            super(path);
            this.hash = hash;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && (obj instanceof HashCodeFile) && ((HashCodeFile)obj).hash == hash;
        }

    }
}
