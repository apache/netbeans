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
