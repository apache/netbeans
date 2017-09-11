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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.versioning.spi;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.junit.NbTestCase;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.util.Lookup;
import org.netbeans.modules.versioning.spi.testvcs.TestVCS;
import org.netbeans.modules.versioning.spi.testvcs.TestVCSInterceptor;
import org.openide.filesystems.FileChangeAdapter;

/**
 * Versioning SPI unit tests of VCSInterceptor.
 * 
 * @author Maros Sandor
 */
public class VCSInterceptorTest extends NbTestCase {
    
    private File dataRootDir;
    private TestVCSInterceptor inteceptor;

    public VCSInterceptorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = getDataDir(); 
        File userdir = new File(dataRootDir + "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        if(!dataRootDir.exists()) dataRootDir.mkdirs();
        Lookup.getDefault().lookupAll(VersioningSystem.class).size();
        inteceptor = (TestVCSInterceptor) TestVCS.getInstance().getVCSInterceptor();
        File f = new File(dataRootDir, "workdir");
        deleteRecursively(f);
        f.mkdirs();
        f = new File(dataRootDir, "workdir/root-test-versioned");
        f.mkdirs();
        inteceptor.clearTestData();
    }

    public void testIsMutable() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned");
        FileObject fo = FileUtil.toFileObject(f);
        fo = fo.createData("checkme.txt");
        File file = FileUtil.toFile(fo);
        fo.canWrite();
        assertTrue(inteceptor.getBeforeCreateFiles().contains(file));
        assertTrue(inteceptor.getDoCreateFiles().contains(file));
        assertTrue(inteceptor.getCreatedFiles().contains(file));
        assertFalse(inteceptor.getIsMutableFiles().contains(file));
        
        file.setReadOnly();
        fo.canWrite();
        assertTrue(inteceptor.getIsMutableFiles().contains(file));
    }

    public void testGetAttribute() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned");
        FileObject folder = FileUtil.toFileObject(f);
        FileObject fo = folder.createData("gotattr.txt");
        File file = FileUtil.toFile(fo);
        
        String attr = (String) fo.getAttribute("whatever");
        assertNull(attr);

        attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertTrue(attr.endsWith(file.getName()));


        attr = (String) fo.getAttribute("whatever");
        assertNull(attr);
    }

    public void testRefreshRecursively() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned");
        f.mkdirs();
        FileObject fo = FileUtil.toFileObject(f);
        fo = fo.createFolder("folder");
        fo.addRecursiveListener(new FileChangeAdapter());
        assertTrue(inteceptor.getRefreshRecursivelyFiles().contains(FileUtil.toFile(fo)));     
    }

    public void testChangedFile() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned");
        FileObject fo = FileUtil.toFileObject(f);
        fo = fo.createData("deleteme.txt");
        File file = FileUtil.toFile(fo);
        OutputStream os = fo.getOutputStream();
        os.close();
        assertTrue(inteceptor.getBeforeCreateFiles().contains(file));
        assertTrue(inteceptor.getDoCreateFiles().contains(file));
        assertTrue(inteceptor.getCreatedFiles().contains(file));
        assertTrue(inteceptor.getBeforeChangeFiles().contains(file));
        assertTrue(inteceptor.getAfterChangeFiles().contains(file));
    }
    
    public void testFileProtectedAndNotDeleted() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned");
        FileObject fo = FileUtil.toFileObject(f);
        fo = fo.createData("deleteme.txt-do-not-delete");
        File file = FileUtil.toFile(fo);
        fo.delete();
        assertTrue(file.isFile());
        assertTrue(inteceptor.getBeforeCreateFiles().contains(file));
        assertTrue(inteceptor.getDoCreateFiles().contains(file));
        assertTrue(inteceptor.getCreatedFiles().contains(file));
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(file));
        assertTrue(inteceptor.getDoDeleteFiles().contains(file));
        assertTrue(inteceptor.getDeletedFiles().contains(file));
    }

    public void testFileCreatedLockedRenamedDeleted() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned");
        FileObject fo = FileUtil.toFileObject(f);
        fo = fo.createData("deleteme.txt");
        File file = FileUtil.toFile(fo);
        FileLock lock = fo.lock();
        fo.rename(lock, "deleteme", "now");
        lock.releaseLock();
        File file2 = FileUtil.toFile(fo);
        fo.delete();
        assertTrue(inteceptor.getBeforeCreateFiles().contains(file));
        assertTrue(inteceptor.getDoCreateFiles().contains(file));
        assertTrue(inteceptor.getCreatedFiles().contains(file));
        assertTrue(inteceptor.getBeforeEditFiles().contains(file));
        assertTrue(inteceptor.getBeforeMoveFiles().contains(file));
        assertTrue(inteceptor.getAfterMoveFiles().contains(file));
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(file2));
        assertTrue(inteceptor.getDoDeleteFiles().contains(file2));
        assertTrue(inteceptor.getDeletedFiles().contains(file2));
    }

    public void testFileCopied() throws IOException {
        File f = new File(dataRootDir, "workdir/root-test-versioned");
        FileObject fo = FileUtil.toFileObject(f);
        fo = fo.createData("copyme.txt");
        File from = FileUtil.toFile(fo);

        FileObject fto = fo.copy(fo.getParent(), "copymeto", "txt");

        assertTrue(inteceptor.getBeforeCopyFiles().contains(from));
        assertTrue(inteceptor.getBeforeCopyFiles().contains(FileUtil.toFile(fo)));
        assertTrue(inteceptor.getDoCopyFiles().contains(from));
        assertTrue(inteceptor.getDoCopyFiles().contains(FileUtil.toFile(fo)));
        assertTrue(inteceptor.getAfterCopyFiles().contains(from));
        assertTrue(inteceptor.getAfterCopyFiles().contains(FileUtil.toFile(fo)));
    }

    private void deleteRecursively(File f) {
        if(f.isFile()) {
            f.delete();
        } else {
            File[] files = f.listFiles();
            if(files != null) {
                for (File file : files) {
                    deleteRecursively(file);
                    file.delete();
                }
            }
        }
    }
}
