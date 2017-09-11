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
package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * FileLockImplTest.java
 * JUnit based test
 *
 * @author Radek Matous
 */
public class LockForFileTest extends NbTestCase {
    private Logger LOG;

    private File testFile = null;

    public LockForFileTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        LOG = Logger.getLogger("test." + getName());
        clearWorkDir();
        testFile = new File(getWorkDir(), "testLockFile.txt");
        if (!testFile.exists()) {
            testFile.createNewFile();
        }

        assertTrue(testFile.exists());
    }

    public void testLock() throws Exception {
        testLockImpl();
    }

    /**
     * Test of releaseLock method, of class org.netbeans.modules.masterfs.filebasedfs.filelocks.FileLockImpl.
     */
    public void testReleaseLock() throws Exception {
        testReleaseLockImpl();
    }

    /**
     * Test of isValid method, of class org.netbeans.modules.masterfs.filebasedfs.filelocks.FileLockImpl.
     */
    public void testIsValid() throws Exception {
        testIsValidImpl();
    }

    public void testAfterCrash() throws Exception {
        testAfterCrashImpl();
    }

    public void testHardLockExclusion() throws Throwable {
        if (Utilities.isUnix() && new File("/bin/ln").exists()) {
            File fold1 = testFile.getParentFile();
            File fold2 = new File(fold1.getParentFile(), fold1.getName() + "XXX");
            fold2.delete();
            assertTrue(fold1.exists());
            assertFalse(fold2.exists());
            ProcessBuilder pb = new ProcessBuilder("/bin/ln","-s",fold1.getAbsolutePath(), fold2.getAbsolutePath());
            try {
                pb.start().waitFor();
            } catch(Throwable th) {
                //System.out.println("");
                th.printStackTrace();
                throw th;
            }
             assertTrue(fold2.exists());
            File f1 = new File(fold1, testFile.getName());
            File f2 = new File(fold2, testFile.getName());
            FileObject fo1 = FileBasedFileSystem.getFileObject(f1);
            FileObject fo2 = FileBasedFileSystem.getFileObject(f2);
            assertNotNull(fo1);
            assertNotNull(fo2);
            for (int i = 0; i < 5; i++) {
                LockForFile lock = (LockForFile) fo1.lock();
                assertFalse(lock.isHardLocked());
                try {
                    fo2.lock();
                    fail();
                } catch (Exception iex) {
                }
                assertTrue(lock.isHardLocked());
                lock.releaseLock();
            }
        }
    }

    /**
     * Test of finalize method, of class org.netbeans.modules.masterfs.filebasedfs.filelocks.FileLockImpl.
     */
    @RandomlyFails // NB-Core-Build #2010
    public void testFinalize() throws Exception {
        testFinalizeImpl();
    }

    public void testLockImpl() throws Exception {
        for (int i = 0; i < 5; i++) {
            LockForFile lock = LockForFile.tryLock(testFile);
            try {
                assertTrue(lock.isValid());
            } finally {
                lock.releaseLock();
            }
        }
    }

    /**
     * Test of releaseLock method, of class org.netbeans.modules.masterfs.filebasedfs.filelocks.FileLockImpl.
     */
    @RandomlyFails // NB-Core-Build #2010
    public void testReleaseLockImpl() throws Exception {
        LockForFile lock = LockForFile.tryLock(testFile);
        assertFalse(lock.isHardLocked());
        assertNotNull(lock);
        try {
            LockForFile.tryLock(testFile);
            fail();
        } catch (IOException iex) {
        }

        lock.releaseLock();
        lock.releaseLock();
        lock = LockForFile.tryLock(testFile);
        assertFalse(lock.isHardLocked());
        assertNotNull(lock);
        lock.releaseLock();
    }

    /**
     * Test of isValid method, of class org.netbeans.modules.masterfs.filebasedfs.filelocks.FileLockImpl.
     */
    @RandomlyFails // NB-Core-Build #2010
    public void testIsValidImpl() throws Exception {
        LockForFile lock = LockForFile.tryLock(testFile);
        assertFalse(lock.isHardLocked());
        assertTrue(lock.isValid());
        lock.releaseLock();
        assertFalse(lock.isValid());

    }

    public void testAfterCrashImpl() throws Exception {
        LockForFile lock = LockForFile.tryLock(testFile);
        lock.releaseLock();
        assertFalse(lock.isValid());
        File lockFile = lock.getLock();
        if (!lockFile.exists()) {
            assertTrue(lockFile.createNewFile());
        }
        assertTrue(lockFile.exists());
        lock = LockForFile.tryLock(testFile);
        try {
            assertNotNull(lock);
            assertTrue(lock.isValid());
            assertTrue(lockFile.exists());
        } finally {
            lock.releaseLock();
        }

    }

    /**
     * Test of finalize method, of class org.netbeans.modules.masterfs.filebasedfs.filelocks.FileLockImpl.
     */
    @RandomlyFails // NB-Core-Build #2010
    public void testFinalizeImpl() throws Exception {
        LOG.info("testFinalizeImpl");
        LockForFile lock = LockForFile.tryLock(testFile);
        LOG.log(Level.INFO, "lock is here: {0}", lock);
        assertFalse(lock.isHardLocked());
        Reference ref = new WeakReference(lock);
        lock = null;
        LOG.log(Level.INFO, "Hard Reference is cleared: {0}", ref);
        assertGC("", ref);
        LOG.log(Level.INFO, "GCed successfully {0}", ref);
        lock = LockForFile.tryLock(testFile);
        LOG.log(Level.INFO, "Try lock: {0}", lock);
        lock.releaseLock();
        LOG.info("releaseLock");

        File lockFile = LockForFile.getLockFile(testFile);
        LOG.log(Level.INFO, "Get lock file: {0}", lockFile);
        assertFalse(lockFile.exists());
        LOG.info("OK");
    }

    public void testLockingAndReleasingLockAfterRename_82170() throws Exception {
        File folder = new File(getWorkDir(), "a/b/c/d");
        if (!folder.exists()) {
            assertTrue(folder.mkdirs());
        }
        File file = new File(folder, "file.txt");
        if (!file.exists()) {
            assertTrue(file.createNewFile());
        }
        FileObject fileFo = FileUtil.toFileObject(file);
        assertNotNull(fileFo);

        FileLock lockForFile = LockForFile.tryLock(file);
        assertTrue(lockForFile.isValid());
        FileObject parentOfFileFo = fileFo.getParent().getParent();
        FileLock lockForParentOfFileFo = parentOfFileFo.lock();
        try {
            parentOfFileFo.rename(lockForParentOfFileFo, "renamedFile", "");//NOI18N
        } finally {
            lockForParentOfFileFo.releaseLock();
        }

        assertTrue(lockForFile.isValid());//after rename is lock still valid
        file = FileUtil.toFile(fileFo);
        assertNotNull(file);
        try {
            LockForFile.tryLock(file);
            fail();
        } catch (IOException ex) {
        //after rename is still locked and there isn't possible to get other lock
        }
        File lockFile = LockForFile.getLockFile(file);
        lockForFile.releaseLock();
        assertFalse(lockFile.exists());//lock file is deleted after releasing lock
        LockForFile.tryLock(file);//there is possible to get lock after releasing lock
    }

    public java.io.File getWorkDir() throws java.io.IOException {
        return super.getWorkDir();
    }
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
}
