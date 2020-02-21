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
