/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
