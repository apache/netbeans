/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.remote.impl.fileoperations.spi;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import junit.framework.Test;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FileProxyI;
import org.netbeans.modules.remote.impl.fileoperations.spi.MockupFilesystemInterceptorProvider.FilesystemInterceptorImpl;
import org.netbeans.modules.remote.impl.fs.RemoteFileTestBase;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.*;

/**
 *
 */
public class InterceptorTestCase extends RemoteFileTestBase {
    private String remoteDir;
    private MockupFilesystemInterceptorProvider.FilesystemInterceptorImpl interceptor;

    public static Test suite() {
        return RemoteApiTest.createSuite(InterceptorTestCase.class);
    }

    public InterceptorTestCase(String testName) {
        super(testName);
        System.setProperty("remote.vcs.suport", "true");
    }

    public InterceptorTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
        System.setProperty("remote.vcs.suport", "true");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(MockupFilesystemInterceptorProvider.class);
        if (execEnv != null) {
            remoteDir = mkTempAndRefreshParent(true);
            ProcessUtils.execute(execEnv, "umask", "0002");
        }
        interceptor = (FilesystemInterceptorImpl) FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fs);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (execEnv != null) {
            removeRemoteDirIfNotNull(remoteDir);
        }
    }

    @ForAllEnvironments
    public void testEquals() throws IOException {
        FileObject remoteDirFO = rootFO.getFileObject(remoteDir);
        FileObject fo = remoteDirFO.createData("checkme.txt");
        FileProxyI file1 = MockupFilesystemInterceptorProvider.toFileProxy(fs, fo.getPath());
        FileProxyI file2 = MockupFilesystemInterceptorProvider.toFileProxy(fs, fo.getPath());
        assertEquals(file1, file2);
        MockupFilesystemInterceptorProvider.FilesystemInterceptorImpl interceptor2 = (FilesystemInterceptorImpl) FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(fs);
        assertEquals(interceptor, interceptor2);
    }

    @ForAllEnvironments
    public void testIsMutable() throws IOException {
        FileObject remoteDirFO = rootFO.getFileObject(remoteDir);
        FileObject fo = remoteDirFO.createData("checkme.txt");
        FileProxyI file = MockupFilesystemInterceptorProvider.toFileProxy(fs, fo.getPath());
        boolean canWrite1 = fo.canWrite();
        assertTrue(canWrite1);
        assertTrue(interceptor.getBeforeCreateFiles().contains(file));
        //assertTrue(interceptor.getDoCreateFiles().contains(file));
        assertTrue(interceptor.getCreatedFiles().contains(file));
        assertFalse(interceptor.getIsMutableFiles().contains(file));
        ExitStatus execute = ProcessUtils.execute(execEnv, "chmod", "oag-w", file.getPath());
        fo.getParent().refresh(true);
        boolean canWrite2 = fo.canWrite();
        assertFalse(canWrite2);
        assertTrue(interceptor.getIsMutableFiles().contains(file));
    }

    @ForAllEnvironments
    public void testChangedFile() throws IOException {
        FileObject remoteDirFO = rootFO.getFileObject(remoteDir);
        FileObject fo = remoteDirFO.createData("deleteme.txt");
        FileProxyI file = MockupFilesystemInterceptorProvider.toFileProxy(fs, fo.getPath());
        OutputStream os = fo.getOutputStream();
        os.close();
        assertTrue(interceptor.getBeforeCreateFiles().contains(file));
        //assertTrue(interceptor.getDoCreateFiles().contains(file));
        assertTrue(interceptor.getCreatedFiles().contains(file));
        assertTrue(interceptor.getBeforeChangeFiles().contains(file));
        try {
            FileSystemProvider.waitWrites(execEnv, Collections.singleton(fo), null);
        } catch (InterruptedException ex) {
        }
        //assertTrue(interceptor.getAfterChangeFiles().contains(file));
    }

    @ForAllEnvironments
    public void testFileProtectedAndNotDeleted() throws IOException {
        FileObject remoteDirFO = rootFO.getFileObject(remoteDir);
        FileObject fo = remoteDirFO.createData("deleteme.txt-do-not-delete");
        FileProxyI file = MockupFilesystemInterceptorProvider.toFileProxy(fs, fo.getPath());
        fo.delete();
        assertTrue(interceptor.getBeforeCreateFiles().contains(file));
        //assertTrue(interceptor.getDoCreateFiles().contains(file));
        assertTrue(interceptor.getCreatedFiles().contains(file));
        //assertTrue(interceptor.getBeforeDeleteFiles().contains(file));
        assertTrue(interceptor.getDoDeleteFiles().contains(file));
        assertTrue(interceptor.getDeletedFiles().contains(file));
    }

    @ForAllEnvironments
    public void testFileCopied() throws IOException {
        FileObject remoteDirFO = rootFO.getFileObject(remoteDir);
        FileObject fo = remoteDirFO.createData("copyme.txt");
        FileProxyI file = MockupFilesystemInterceptorProvider.toFileProxy(fs, fo.getPath());
        FileObject fto = fo.copy(fo.getParent(), "copymeto", "txt");
        FileProxyI fileto = MockupFilesystemInterceptorProvider.toFileProxy(fs, fto.getPath());

        assertTrue(interceptor.getBeforeCopyFiles().contains(file));
        assertTrue(interceptor.getBeforeCopyFiles().contains(fileto));
        assertTrue(interceptor.getDoCopyFiles().contains(file));
        assertTrue(interceptor.getDoCopyFiles().contains(fileto));
        assertTrue(interceptor.getAfterCopyFiles().contains(file));
        assertTrue(interceptor.getAfterCopyFiles().contains(fileto));
    }
    
    @ForAllEnvironments
    public void testGetAttribute() throws IOException {
        FileObject folder = rootFO.getFileObject(remoteDir);
        FileObject fo = folder.createData("gotattr.txt");
        
        String attr = (String) fo.getAttribute("whatever");
        assertNull(attr);

        Boolean attrB = (Boolean) fo.getAttribute("isRemoteAndSlow");
        assertNotNull(attrB);
        assertTrue(attrB);
        
        fo.setAttribute("whatever", "done");
        attr = (String) fo.getAttribute("whatever");
        assertNotNull(attr);

        //fo = folder.createData("versioned.txt");
        //Boolean battr = (Boolean) fo.getAttribute("ProvidedExtensions.VCSManaged");
        //assertNotNull(battr);
        //assertTrue(battr);

        //f = new File(dataRootDir, "workdir");
        //folder = FileUtil.toFileObject(f);
        //fo = folder.createData("unversioned.txt");

        //fo = folder.createData("versioned.txt");
        //battr = (Boolean) fo.getAttribute("ProvidedExtensions.VCSManaged");
        //assertNotNull(battr);
        //assertFalse(battr);
    }

//    @ForAllEnvironments
//    public void testRefreshRecursively() throws IOException {
//        FileObject fo = rootFO.getFileObject(remoteDir);
//        fo = fo.createFolder("folder");
//        FileProxyI file = MockupFilesystemInterceptorProvider.toFileProxy(fs, fo.getPath());
//        fo.addRecursiveListener(new FileChangeListener(){
//
//            @Override
//            public void fileFolderCreated(FileEvent fe) {}
//
//            @Override
//            public void fileDataCreated(FileEvent fe) {}
//
//            @Override
//            public void fileChanged(FileEvent fe) {}
//
//            @Override
//            public void fileDeleted(FileEvent fe) {}
//
//            @Override
//            public void fileRenamed(FileRenameEvent fe) {}
//
//            @Override
//            public void fileAttributeChanged(FileAttributeEvent fe) {}
//            
//        });
//        assertTrue(interceptor.getRefreshRecursivelyFiles().contains(file));
//    }

//    @ForAllEnvironments
//    public void testFileCreatedLockedRenamedDeleted() throws IOException {
//        FileObject remoteDirFO = rootFO.getFileObject(remoteDir);
//        FileObject fo = remoteDirFO.createData("deleteme.txt");
//        FileProxyI file = MockupFilesystemInterceptorProvider.toFileProxy(fs, fo.getPath());
//        FileLock lockImpl = fo.lockImpl();
//        fo.rename(lockImpl, "deleteme", "now");
//        lockImpl.releaseLock();
//        FileProxyI file2 = MockupFilesystemInterceptorProvider.toFileProxy(fs, fo.getPath());
//        fo.delete();
//        assertTrue(interceptor.getBeforeCreateFiles().contains(file));
//        //assertTrue(interceptor.getDoCreateFiles().contains(file));
//        assertTrue(interceptor.getCreatedFiles().contains(file));
//        assertTrue(interceptor.getBeforeEditFiles().contains(file));
//        assertTrue(interceptor.getBeforeMoveFiles().contains(file));
//        assertTrue(interceptor.getAfterMoveFiles().contains(file));
//        assertTrue(interceptor.getBeforeDeleteFiles().contains(file2));
//        //assertTrue(interceptor.getDoDeleteFiles().contains(file2));
//        assertTrue(interceptor.getDeletedFiles().contains(file2));
//    }

}
