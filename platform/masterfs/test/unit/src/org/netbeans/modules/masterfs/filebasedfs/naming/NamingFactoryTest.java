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

import java.io.File;
import java.io.IOException;
import java.security.Permission;
import junit.framework.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.CharSequences;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NamingFactoryTest extends NbTestCase {
    public NamingFactoryTest(String n) {
        super(n);
    }
    
    public static Test suite() {
        NbTestSuite s = new NbTestSuite();
        s.addTestSuite(NamingFactoryTest.class);
        s.addTest(new NamingFactoryTest("registerSecurityManager"));
        s.addTestSuite(NamingFactoryTest.class);
        return s;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        Object res = CharSequences.create("0123456789012345678901234567890123456789");
        assertTrue("Contains Byte: " + res.getClass(), res.getClass().getName().contains("Byte"));
        res = CharSequences.create("testRename-1");
        assertTrue("Contains Fixed: " + res.getClass(), res.getClass().getName().contains("Fixed"));
    }
    
    public void registerSecurityManager() {
        System.setSecurityManager(new AssertNoLockManager(NamingFactory.class));
    }

    public void testDontForciblyUnregisterFileName() throws Exception {
        File f = new File(getWorkDir(), "child.txt");
        f.createNewFile();
        
        FileNaming parent = NamingFactory.fromFile(getWorkDir());
        
        FileNaming ch1 = NamingFactory.fromFile(parent, f, true);
        FileNaming ch2 = NamingFactory.fromFile(parent, f, true);
        
        assertSame(
            "Regardless of an attempt to ignore cache, "
            + "we need to return the same object", 
            ch1, ch2
        );
    }
    
    /**
     * Test for bug 235332.
     *
     * @throws java.io.IOException
     */
    public void testRename() throws IOException {

        final File dir = getWorkDir();

        File f1 = new File(dir, "f1");
        File f2 = new File(f1, "f2");
        File d = new File(f2, "d");

        f2.mkdirs();
        d.createNewFile();

        final FileNaming nf1 = NamingFactory.fromFile(f1);
        FileNaming[] renamed = NamingFactory.rename(nf1, "f2",
                new ProvidedExtensions.IOHandler() {

                    @Override
                    public void handle() throws IOException {

                        // Simlulate that another thread has renamed the files
                        // concurrently.
                        NamingFactory.rename(nf1, "f2", null);
                        File f = new File(dir, "f2");
                        NamingFactory.fromFile(f);
                        NamingFactory.fromFile(new File(f, "d"));
                    }
                });

        assertEquals("New FileNaming should be stored at index 0",
                "f2", renamed[0].getName());
        for (int i = 1; i < renamed.length; i++) {
            FileNaming r = renamed[i];
            assertTrue("All items at index > 0 should be indirect children of "
                    + "the original FileNaming: " + r + " not below " + nf1,
                    isBelow(r, nf1));
        }
    }

    /**
     * Check that child is the same object as parent, or that child is below
     * (descendant, indirect child of) parent.
     *
     * @param child
     * @param parent
     * @return
     */
    private boolean isBelow(FileNaming child, FileNaming parent) {
        FileNaming fn = child;
        while (fn != null && fn != parent) {
            fn = fn.getParent();
        }
        return fn == parent;
    }
    
    public void testInvalidatePrevFolder() throws Exception {
        FileNaming parent = NamingFactory.fromFile(getWorkDir());
        
        File f = new File(getWorkDir(), "child");
        f.mkdir();
        
        FileObject dir = FileUtil.toFileObject(f);
        assertNotNull("Fileobject for " +f, dir);
        assertTrue("It is a directory", dir.isFolder());

        f.delete();
        f.createNewFile();
        
        FileNaming middleName = NamingFactory.fromFile(parent, f, true);
        assertFalse("No longer a folder", middleName instanceof FolderName);
        
        FileObject file = FileUtil.toFileObject(f);
        assertTrue("It is a file: " + file + " valid: " + file.isValid(), file.isData());
        
        assertFalse("Old file object is no longer valid", dir.isValid());
        assertTrue("New file object is valid", file.isValid());
        
        f.delete();
        f.mkdir();

        FileNaming newNaming = NamingFactory.fromFile(parent, f, true);
        assertFalse("No longer a file", newNaming.isFile());
        
        FileObject newDir = FileUtil.toFileObject(f);
        assertTrue("It is a dir: " + newDir + " valid: " + newDir.isValid(), newDir.isFolder());
        
        assertFalse("Oldest file object is no longer valid", dir.isValid());
        assertFalse("Middle file object is no longer valid", file.isValid());
        assertTrue("Newest is valid", newDir.isValid());
        
    }

    private static class AssertNoLockManager extends SecurityManager {
        private final Object LOCK;
        
        public AssertNoLockManager(Object lock) {
            LOCK = lock;
        }
        @Override
        public void checkRead(String string) {
            assertFalse("No lock", Thread.holdsLock(LOCK));
        }

        @Override
        public void checkRead(String string, Object o) {
            checkRead(string);
        }

        @Override
        public void checkPermission(Permission prmsn) {
        }

        @Override
        public void checkPermission(Permission prmsn, Object o) {
        }
    }
}
