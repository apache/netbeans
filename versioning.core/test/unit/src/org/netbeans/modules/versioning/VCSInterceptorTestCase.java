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
package org.netbeans.modules.versioning;


import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.modules.versioning.core.DelegatingVCS;
import org.netbeans.modules.versioning.core.VersioningManager;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.netbeans.modules.versioning.core.spi.testvcs.TestVCS;

import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.modules.versioning.core.spi.testvcs.TestVCSInterceptor;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileLock;

/**
 * Versioning SPI unit tests of VCSInterceptor.
 * 
 * @author Maros Sandor
 */
public class VCSInterceptorTestCase extends AbstractFSTestCase {
    
    private TestVCSInterceptor inteceptor;
    private FSInterceptorLogHandler logHandler;

    private static final String getAttributeFormat = "getAttribute {0}, {1}";
    private static final String listFilesFormat = "listFiles {0}";
    private static final String canWriteFormat = "canWrite {0}";
    private static final String fileLockedFormat = "fileLocked {0}";
    private static final String beforeChangedFormat = "beforeChange {0}";
    private static final String fileChangedFormat = "fileChanged {0}";
    private static final String beforeCreateFormat = "beforeCreate {0}, {1}, {2}";
    private static final String createSuccessFormat = "createSuccess {0}";
    private static final String createdExternalyFormat = "createdExternally {0}";
    private static final String createFailureFormat = "createFailure {0}, {1}, {2}";
    private static final String getDeleteHandlerFormat = "getDeleteHandler {0}";
    private static final String deleteHandleFormat = "delete handle {0}";
    private static final String deletedExternalyFormat = "deletedExternaly {0}";
    private static final String deleteSuccessFormat = "deleteSuccess {0}";
    private static final String getRenameHandlerFormat = "getRenameHandler {0}, {1}";
    private static final String getMoveHandlerFormat = "getMoveHandler {0}, {1}";
    private static final String moveHandleFormat = "move handle {0} {1}";
    private static final String afterMoveFormat = "afterMove {0}, {1}";
    private static final String getCopyHandlerFormat = "getCopyHandler {0}, {1}";
    private static final String copyHandleFormat = "copy handle {0} {1}";
    private static final String copySuccessFormat = "copySuccess {0}, {1}";
    
    static String[] formats = new String[] {
        listFilesFormat,
        getAttributeFormat,
        fileLockedFormat,
        beforeChangedFormat,
        fileChangedFormat,
        getDeleteHandlerFormat,
        deleteHandleFormat,
        deletedExternalyFormat,
        deleteSuccessFormat,
        beforeCreateFormat,
        createSuccessFormat,
        canWriteFormat,
        createdExternalyFormat,
        createFailureFormat,
        getRenameHandlerFormat,
        getMoveHandlerFormat,
        moveHandleFormat,
        afterMoveFormat,
        getCopyHandlerFormat,
        copyHandleFormat,
        copySuccessFormat
    };
    
    public VCSInterceptorTestCase(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Collection<? extends VCSSystemProvider> providers = Lookup.getDefault().lookupAll(VCSSystemProvider.class);
        for (VCSSystemProvider p : providers) {
            Collection<VCSSystemProvider.VersioningSystem> systems = p.getVersioningSystems();
            for (VCSSystemProvider.VersioningSystem vs : systems) {
                if(vs instanceof DelegatingVCS) {
                    DelegatingVCS dvcs = (DelegatingVCS)vs;
                    if("TestVCSDisplay".equals(dvcs.getDisplayName())) {
                        inteceptor = (TestVCSInterceptor) dvcs.getVCSInterceptor();
                    }
                }
            }
        }
        inteceptor.clearTestData();
        logHandler = new FSInterceptorLogHandler();
        VersioningManager.LOG.addHandler(logHandler);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testFileCreateVersioned() throws IOException {
        FileObject fo = getVersionedFolder();
        logHandler.clear();
                
        fo = fo.createData("checkme.txt");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        
        assertTrue(inteceptor.getBeforeCreateFiles().contains(proxy));
        assertTrue(inteceptor.getDoCreateFiles().contains(proxy));
        assertTrue(inteceptor.getCreatedFiles().contains(proxy));
        
        assertInterceptedCalls(
            f(beforeCreateFormat, proxy.getParentFile(), proxy.getName(), false),
            f(createSuccessFormat, proxy)
        );
    }
    
    public void testFileCreateNotVersioned() throws IOException {
        FileObject fo = getNotVersionedFolder();
        logHandler.clear();
                
        fo = fo.createData("checkme.txt");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        
        assertFalse(inteceptor.getBeforeCreateFiles().contains(proxy));
        assertFalse(inteceptor.getDoCreateFiles().contains(proxy));
        assertFalse(inteceptor.getCreatedFiles().contains(proxy));
        assertInterceptedCalls(
            f(beforeCreateFormat, proxy.getParentFile(), proxy.getName(), false),
            f(createSuccessFormat, proxy)
        );
    }
    
    public void testIsMuttable() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createData("checkme.txt");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        logHandler.clear();
        
        assertTrue(fo.canWrite());
        assertEquals(0, logHandler.messages.size());
        assertFalse(inteceptor.getIsMutableFiles().contains(proxy));
    }
    
    public void testVCSDoesntOverrideReadOnly() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createData("checkme.txt");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        VCSFilesystemTestFactory.getInstance(this).setReadOnly(getRelativePath(proxy));
        logHandler.clear();
        
        assertFalse(fo.canWrite());
        assertTrue(inteceptor.getIsMutableFiles().contains(proxy));
        assertInterceptedCalls(
            f(canWriteFormat, proxy)
        );        
    }
    
    public void testVCSOverridesReadOnly() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createData(TestVCS.ALWAYS_WRITABLE_PREFIX);
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        VCSFilesystemTestFactory.getInstance(this).setReadOnly(getRelativePath(proxy));
        logHandler.clear();
        
        assertTrue(fo.canWrite());
        assertTrue(inteceptor.getIsMutableFiles().contains(proxy));
        assertInterceptedCalls(
            f(canWriteFormat, proxy)
        );   
    }

    public void testIsLockedDoesntInvokeBeforeEdit() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createData(TestVCS.ALWAYS_WRITABLE_PREFIX);
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        VCSFilesystemTestFactory.getInstance(this).setReadOnly(getRelativePath(proxy));
        logHandler.clear();
        
        assertFalse(fo.isLocked());
        List<VCSFileProxy> beforeEditFiles = inteceptor.getBeforeEditFiles();
        if(!inteceptor.getBeforeEditFiles().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Not expected beforeEdit() intercepted for file(s): ");
            for (int i = 0; i < beforeEditFiles.size(); i++) {
                VCSFileProxy file = beforeEditFiles.get(i);
                sb.append(file.getName());
                if(i < beforeEditFiles.size() - 1) {
                    sb.append(",");
                }
            }
            fail(sb.toString());
        }
    }
    
    public void testGetAttribute() throws IOException {
        FileObject folder = getVersionedFolder();
        FileObject fo = folder.createData("gotattr.txt");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        
        logHandler.clear();
        String attr = (String) fo.getAttribute("whatever");
        assertNull(attr);

        assertEquals(0, logHandler.messages.size());
        
        logHandler.clear();
        attr = (String) fo.getAttribute("ProvidedExtensions.RemoteLocation");
        assertNotNull(attr);
        assertTrue(attr.endsWith("gotattr.txt"));
        assertInterceptedCalls(
            f(getAttributeFormat, proxy, "ProvidedExtensions.RemoteLocation")
        );   
        
        fo = folder.createData("versioned.txt");
        proxy = VCSFileProxy.createFileProxy(fo);
        logHandler.clear();
    }

    public void testRefreshRecursively() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createFolder("folder");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        logHandler.clear();
        
        fo.addRecursiveListener(new FileChangeAdapter()); 
        assertTrue(inteceptor.getRefreshRecursivelyFiles().contains(VCSFileProxy.createFileProxy(fo)));     
        
        // XXX listFiles called twice on adding the listener. is this realy necessary
        assertInterceptedCalls(
            1, 2, 
            f(listFilesFormat, proxy)
        );            
    }

    public void testChangedFile() throws IOException {
        FileObject fo = getVersionedFolder();
        fo = fo.createData("deleteme.txt");
        logHandler.clear();
        
        OutputStream os = fo.getOutputStream();
        os.close();
        
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        assertTrue(inteceptor.getBeforeCreateFiles().contains(proxy));
        assertTrue(inteceptor.getDoCreateFiles().contains(proxy));
        assertTrue(inteceptor.getCreatedFiles().contains(proxy));
        assertTrue(inteceptor.getBeforeChangeFiles().contains(proxy));
        assertTrue(inteceptor.getAfterChangeFiles().contains(proxy));
        
        assertInterceptedCalls(
            f(fileLockedFormat, proxy),
            f(beforeChangedFormat, proxy),
            f(fileChangedFormat, proxy)
        );
    }
    
    public void testFileProtectedAndNotDeleted() throws IOException {
        FileObject fo = getVersionedFolder();
        logHandler.clear();
        logHandler.ignoredMessages.add(createdExternalyFormat); // XXX 
        
        fo = fo.createData("deleteme.txt-do-not-delete");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        fo.delete();
        
        assertTrue(proxy.isFile());
        assertTrue(inteceptor.getBeforeCreateFiles().contains(proxy));
        assertTrue(inteceptor.getDoCreateFiles().contains(proxy));
        assertTrue(inteceptor.getCreatedFiles().contains(proxy));
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(proxy));
        assertTrue(inteceptor.getDoDeleteFiles().contains(proxy));
        assertTrue(inteceptor.getDeletedFiles().contains(proxy));
        
        assertInterceptedCalls(
            f(beforeCreateFormat, proxy.getParentFile(), proxy.getName(), false),
            f(createSuccessFormat, proxy),
            f(fileLockedFormat, proxy),
            f(getDeleteHandlerFormat, proxy),
            f(deleteHandleFormat, proxy),
            f(deleteSuccessFormat, proxy)
        );
    }

    public void testFileCreatedLockedRenamedDeleted() throws IOException {
        inteceptor.moveHandler = moveHandler;
        inteceptor.deleteHandler = deleteHandler;
        FileObject fo = getVersionedFolder();
        logHandler.clear();
        
        fo = fo.createData("deleteme.txt");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        FileLock lock = fo.lock();
        fo.rename(lock, "deleteme", "now");
        lock.releaseLock();
        VCSFileProxy proxy2 = VCSFileProxy.createFileProxy(fo);
        fo.delete();
        assertTrue(inteceptor.getBeforeCreateFiles().contains(proxy));
        assertTrue(inteceptor.getDoCreateFiles().contains(proxy));
        assertTrue(inteceptor.getCreatedFiles().contains(proxy));
        assertTrue(inteceptor.getBeforeEditFiles().contains(proxy));
        assertTrue(inteceptor.getBeforeMoveFiles().contains(proxy));
        assertTrue(inteceptor.getAfterMoveFiles().contains(proxy));
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(proxy2));
        assertTrue(inteceptor.getDoDeleteFiles().contains(proxy2));
        assertTrue(inteceptor.getDeletedFiles().contains(proxy2));
        
        assertInterceptedCalls(
            f(beforeCreateFormat, proxy.getParentFile(), proxy.getName(), false),
            f(createSuccessFormat, proxy),
            f(fileLockedFormat, proxy),
            f(getRenameHandlerFormat, proxy, proxy2.getName()),
            f(moveHandleFormat, proxy, proxy2),
            f(afterMoveFormat, proxy, proxy2),
            f(fileLockedFormat, proxy),
            f(getDeleteHandlerFormat, proxy2),
            f(deleteHandleFormat, proxy2),
            f(deleteSuccessFormat, proxy2)
        );
    }

    public void testFileCopied() throws IOException {
        inteceptor.copyHandler = copyHandler;
        FileObject fo = getVersionedFolder();
        fo = fo.createData("copyme.txt");
        logHandler.clear();  
        logHandler.ignoredMessages.add(createdExternalyFormat); // XXX 
        
        FileObject fto = fo.copy(fo.getParent(), "copymeto", "txt");
        VCSFileProxy fromProxy = VCSFileProxy.createFileProxy(fo);
        VCSFileProxy toProxy = VCSFileProxy.createFileProxy(fto);

        assertTrue(inteceptor.getBeforeCopyFiles().contains(fromProxy));
        assertTrue(inteceptor.getBeforeCopyFiles().contains(toProxy));
        assertTrue(inteceptor.getDoCopyFiles().contains(fromProxy));
        assertTrue(inteceptor.getDoCopyFiles().contains(toProxy));
        assertTrue(inteceptor.getAfterCopyFiles().contains(fromProxy));
        assertTrue(inteceptor.getAfterCopyFiles().contains(toProxy));
        
        assertInterceptedCalls(
//              f(fileLockedFormat, fromProxy); // XXX no lock before copy ???
            f(getCopyHandlerFormat, fromProxy, toProxy),
            f(copyHandleFormat, fromProxy, toProxy),
            f(copySuccessFormat, fromProxy, toProxy)
            // XXX and this doesnt invoke createdExternaly but move does?
        );
    }
    
    public void testFolderTreeCopied() throws IOException {
        inteceptor.copyHandler = copyHandler;
        FileObject fo = getVersionedFolder();
        FileObject fromFolder = fo.createFolder("fromFolder");
        FileObject movedChild = fromFolder.createData("copiedChild");
        FileObject targetFolder = fo.createFolder("targetFolder");
        VCSFileProxy fromProxy = VCSFileProxy.createFileProxy(fromFolder);
        logHandler.clear();
        logHandler.ignoredMessages.add(createdExternalyFormat); // XXX 
        
        FileObject toFolder = fromFolder.copy(targetFolder, "copy", null);
        VCSFileProxy toProxy = VCSFileProxy.createFileProxy(toFolder);

        assertTrue(inteceptor.getBeforeCopyFiles().contains(fromProxy));
        assertTrue(inteceptor.getBeforeCopyFiles().contains(toProxy));
        assertTrue(inteceptor.getDoCopyFiles().contains(fromProxy));
        assertTrue(inteceptor.getDoCopyFiles().contains(toProxy));
        assertTrue(inteceptor.getAfterCopyFiles().contains(fromProxy));
        assertTrue(inteceptor.getAfterCopyFiles().contains(toProxy));
        
        assertInterceptedCalls(
            f(getCopyHandlerFormat, fromProxy, toProxy),
            f(copyHandleFormat, fromProxy, toProxy),
            f(copySuccessFormat, fromProxy, toProxy)
        );
    }

    public void testFileMoved() throws IOException {
        inteceptor.moveHandler = moveHandler;
        FileObject fo = getVersionedFolder();
        FileObject fromFile = fo.createData("move.txt");
        FileObject toFolder = fo.createFolder("toFolder");
        VCSFileProxy fromProxy = VCSFileProxy.createFileProxy(fromFile);
        
        logHandler.clear();
        logHandler.ignoredMessages.add(createdExternalyFormat);
        logHandler.ignoredMessages.add(deletedExternalyFormat);
        
        FileObject toFile = fromFile.move(fromFile.lock(), toFolder, fromFile.getName(), fromFile.getExt());
        VCSFileProxy toProxy = VCSFileProxy.createFileProxy(toFile);

        assertTrue(inteceptor.getBeforeMoveFiles().contains(fromProxy));
        assertTrue(inteceptor.getBeforeMoveFiles().contains(toProxy));
        assertTrue(inteceptor.getDoMoveFiles().contains(fromProxy));
        assertTrue(inteceptor.getDoMoveFiles().contains(toProxy));
        assertTrue(inteceptor.getAfterMoveFiles().contains(fromProxy));
        assertTrue(inteceptor.getAfterMoveFiles().contains(toProxy));
        
        assertInterceptedCalls(
            f(fileLockedFormat, fromProxy),
            f(getMoveHandlerFormat, fromProxy, toProxy),
            f(moveHandleFormat, fromProxy, toProxy),
    //        f(deletedExternalyFormat, toProxy); // XXX can we avoid this? sometimes deleted or created externaly 
            f(afterMoveFormat, fromProxy, toProxy)
        );
    
    }
    
    public void testFolderTreeMoved() throws IOException {
        inteceptor.moveHandler = moveHandler;
        FileObject fo = getVersionedFolder();
        FileObject fromFolder = fo.createFolder("fromFolder");
        FileObject movedChild = fromFolder.createData("movedChild");
        FileObject targetFolder = fo.createFolder("targetFolder");
        VCSFileProxy fromProxy = VCSFileProxy.createFileProxy(fromFolder);
        
        logHandler.clear();
        logHandler.ignoredMessages.add(createdExternalyFormat);
        logHandler.ignoredMessages.add(deletedExternalyFormat);
        FileObject toFile = fromFolder.move(fromFolder.lock(), targetFolder, fromFolder.getName(), fromFolder.getExt());
        VCSFileProxy toProxy = VCSFileProxy.createFileProxy(toFile);

        assertTrue(inteceptor.getBeforeMoveFiles().contains(fromProxy));
        assertTrue(inteceptor.getBeforeMoveFiles().contains(toProxy));
        assertTrue(inteceptor.getDoMoveFiles().contains(fromProxy));
        assertTrue(inteceptor.getDoMoveFiles().contains(toProxy));
        assertTrue(inteceptor.getAfterMoveFiles().contains(fromProxy));
        assertTrue(inteceptor.getAfterMoveFiles().contains(toProxy));
        
        assertInterceptedCalls(
    //        f(fileLockedFormat, fromProxy); // no lock on folder
            f(getMoveHandlerFormat, fromProxy, toProxy),
            f(moveHandleFormat, fromProxy, toProxy),
    //        f(deletedExternalyFormat, toProxy); // XXX can we avoid this? sometimes deleted or created externaly 
            f(afterMoveFormat, fromProxy, toProxy)
        );
    }
    
    public void testModifyFileOnDemand() throws Exception {
        // init
        inteceptor.deleteHandler = deleteHandler;
        FileObject fo = getVersionedFolder().createData("file");
        logHandler.clear();
        
        // modify
        OutputStream os = fo.getOutputStream();
        os.write(new byte[] { 'a', 0 });
        os.close();
        
        // test
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        assertTrue(inteceptor.getBeforeEditFiles().contains(proxy));
        assertTrue(inteceptor.getBeforeChangeFiles().contains(proxy));
        assertTrue(inteceptor.getAfterChangeFiles().contains(proxy));
        
        assertInterceptedCalls(
            f(fileLockedFormat, proxy),
            f(beforeChangedFormat, proxy),
            f(fileChangedFormat, proxy)
        );
    }

    public void testDeleteNotVersionedFile() throws Exception {
        // init     
        inteceptor.deleteHandler = deleteHandler;
        FileObject fo = getNotVersionedFolder().createData("file");
        logHandler.clear();
        
        // delete
        fo.delete();
        
        // test
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        assertFalse(fo.isValid());
        assertFalse(inteceptor.getBeforeDeleteFiles().contains(proxy));
        assertFalse(inteceptor.getDoDeleteFiles().contains(proxy));
        assertFalse(inteceptor.getDeletedFiles().contains(proxy));
        
        assertInterceptedCalls(
            f(fileLockedFormat, proxy),
            f(getDeleteHandlerFormat, proxy),
            f(deleteSuccessFormat, proxy)
        );    
    
    }
    
    public void testDeleteVersionedFile() throws Exception {
        // init     
        inteceptor.deleteHandler = deleteHandler;
        FileObject fo = getVersionedFolder().createData("file");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        logHandler.clear();

        // delete
        fo.delete();
        // test
        assertFalse(fo.isValid());
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(proxy));
        assertTrue(inteceptor.getDoDeleteFiles().contains(proxy));
        assertTrue(inteceptor.getDeletedFiles().contains(proxy));
        
        assertInterceptedCalls(
            f(fileLockedFormat, proxy),
            f(getDeleteHandlerFormat, proxy),
            f(deleteHandleFormat, proxy),
            f(deleteSuccessFormat, proxy)
        );
    }

    public void testDeleteVersionedFolder() throws Exception {
        // init       
        inteceptor.deleteHandler = deleteHandler;
        FileObject fo = getVersionedFolder().createFolder("folder");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        logHandler.clear();

        // delete
        fo.delete();

        // test
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(proxy));
        assertTrue(inteceptor.getDoDeleteFiles().contains(proxy));
        assertTrue(inteceptor.getDeletedFiles().contains(proxy));
        
        assertInterceptedCalls(
            f(getDeleteHandlerFormat, proxy),
            f(deleteHandleFormat, proxy),
            f(deleteSuccessFormat, proxy)
        );    
    }

    public void testDeleteNotVersionedFolder() throws IOException {
        // init        
        inteceptor.deleteHandler = deleteHandler;
        FileObject fo = getNotVersionedFolder().createFolder("folder");
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        logHandler.clear();
        
        // delete
        fo.delete();
        
        // test
        assertFalse(inteceptor.getBeforeDeleteFiles().contains(proxy));
        assertFalse(inteceptor.getDoDeleteFiles().contains(proxy));
        assertFalse(inteceptor.getDeletedFiles().contains(proxy));
        
        assertInterceptedCalls(
            f(getDeleteHandlerFormat, proxy),
            f(deleteSuccessFormat, proxy)
        );
    }    

    public void testDeleteVersionedFileTree() throws IOException {
        inteceptor.deleteHandler = deleteHandler;
        FileObject versionedRoot = getVersionedFolder();
        FileObject deleteFolder = versionedRoot.createFolder("deletefolder");
        VCSFileProxy deleteProxy = VCSFileProxy.createFileProxy(deleteFolder);
        
        deleteFolderTree(deleteFolder, deleteProxy);
        
        assertTrue(inteceptor.getBeforeDeleteFiles().contains(deleteProxy));
        assertTrue(inteceptor.getDoDeleteFiles().contains(deleteProxy));
        assertTrue(inteceptor.getDeletedFiles().contains(deleteProxy));
        
        assertInterceptedCalls(
            f(getDeleteHandlerFormat, deleteProxy),
            f(deleteHandleFormat, deleteProxy),
            f(deleteSuccessFormat, deleteProxy)
        );
    }
    
    public void testDeleteNotVersionedFileTree() throws IOException {
        inteceptor.deleteHandler = deleteHandler;
        FileObject versionedRoot = getNotVersionedFolder();
        FileObject deleteFolder = versionedRoot.createFolder("deletefolder");
        VCSFileProxy deleteProxy = VCSFileProxy.createFileProxy(deleteFolder);
        
        deleteFolderTree(deleteFolder, deleteProxy);
        
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(deleteProxy));
        assertTrue(!inteceptor.getDoDeleteFiles().contains(deleteProxy));
        assertTrue(!inteceptor.getDeletedFiles().contains(deleteProxy));
        
        assertInterceptedCalls(
            f(getDeleteHandlerFormat, deleteProxy),
            f(deleteSuccessFormat, deleteProxy)
        );
    }

    private void deleteFolderTree(FileObject deleteFolder, final VCSFileProxy deleteProxy) throws IOException {
        FileObject folder1 = deleteFolder.createFolder("folder1");
        FileObject folder2 = folder1.createFolder("folder2");
        FileObject folder3 = folder2.createFolder("folder3");
        
        FileObject fo3 = folder3.createData("file");
        FileObject fo2 = folder2.createData("file");
        FileObject fo1 = folder1.createData("file");
        FileObject fo0 = deleteFolder.createData("file");
        logHandler.clear();
        
        deleteFolder.delete();
        
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(folder1)));
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(folder2)));
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(folder3)));
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(fo1)));
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(fo2)));
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(fo3)));
        assertTrue(!inteceptor.getBeforeDeleteFiles().contains(VCSFileProxy.createFileProxy(fo0)));
        assertFalse(deleteFolder.isValid());
    }
    
    private String getRelativePath(VCSFileProxy proxy) throws IOException {
        String path = proxy.getPath();
        String rootPath = getRoot(path);
        if(rootPath != null) {
            path = path.substring(rootPath.length());
            if(path.startsWith("/")) {
                path = path.substring(1, path.length());
            }
            return path;
        } else {
            return null;
        }
//        VCSFilesystemTestFactory factory = VCSFilesystemTestFactory.getInstance(VCSInterceptorTestCase.this);
//        String path = proxy.getPath();
//        path = path.substring(factory.getRootPath().length());
//        if(path.startsWith("/")) {
//            path = path.substring(1, path.length());
//        }
//        return path;
    }
    
    private String getWorkdirRelativePath(VCSFileProxy proxy) throws IOException {
        String path = proxy.getPath();
        String rootPath = getRoot(path);
        if(rootPath != null) {
            rootPath = rootPath + "/" + workDirPath;
            path = path.substring(rootPath.length());
            if(path.startsWith("/")) {
                path = path.substring(1, path.length());
            }
            return path;
        } else {
            return null;
        }
    }    
    
    private TestVCSInterceptor.DeleteHandler deleteHandler = new TestVCSInterceptor.DeleteHandler() {
        @Override
        public void delete(VCSFileProxy proxy) throws IOException {
            VCSFilesystemTestFactory.getInstance(VCSInterceptorTestCase.this).delete(getRelativePath(proxy));
        }
    };
    
    private TestVCSInterceptor.MoveHandler moveHandler = new TestVCSInterceptor.MoveHandler() {
        @Override
        public void move(VCSFileProxy from, VCSFileProxy to) throws IOException {
            VCSFilesystemTestFactory.getInstance(VCSInterceptorTestCase.this).move(getRelativePath(from), getRelativePath(to));
        }
    };
    
    private TestVCSInterceptor.CopyHandler copyHandler = new TestVCSInterceptor.CopyHandler() {
        @Override
        public void copy(VCSFileProxy from, VCSFileProxy to) throws IOException {
            VCSFilesystemTestFactory.getInstance(VCSInterceptorTestCase.this).copy(getRelativePath(from), getRelativePath(to));
        }
    };
    
    private void assertInterceptedCalls(int count, String... formats) {
        assertInterceptedCalls(count, count, formats);
    }
    
    private void assertInterceptedCalls(String... formats) {
        assertInterceptedCalls(formats.length, formats.length, formats);
    }
    
    private void assertInterceptedCalls(int expectedMin, int expectedMax, String... formats) {
        if(logHandler.messages.size() < expectedMin || logHandler.messages.size() > expectedMax) {
            StringBuilder sb = new StringBuilder();
            sb.append("intercepted calls should be: \n");
            for (String f : formats) {
                sb.append(f);
                sb.append('\n');
            }
            sb.append("but where instead: \n");
            for (String m : logHandler.messages) {
                sb.append(m);
                sb.append('\n');
            }
            fail(sb.toString());
        }
        for (String f : formats) {
            assertEvent(f);
        }
    }
    
    private void assertEvent(String format) {
        boolean contains = logHandler.messages.contains(format);
        if(!contains) {
            fail(format + " should be intercepted but wasn't");
        } 
//            else if(!contains && !bl){
//                fail(new MessageFormat(format).format(proxies) + " shouldn't be intercepted but was");
//            }
    }

    private class FSInterceptorLogHandler extends Handler {
        List<String> ignoredMessages = new LinkedList<String>();
        List<String> messages = new LinkedList<String>();

        @Override
        public void publish(LogRecord record) {
            String msg = record.getMessage();
            if(msg == null) return;
            for (String format : formats) {
                String f = format.substring(0, format.indexOf(" {0}")); 
                if(msg.startsWith(f) && !ignored(f)) {
                    messages.add(f(format, record.getParameters()));
                    break;
                }
            }
        }

        @Override
        public void flush() {
            //
        }

        @Override
        public void close() throws SecurityException {
            //
        }

        void clear() {
            messages.clear();
            ignoredMessages.clear();
        }
        
        private boolean ignored(String f) {
            for (String ignored : ignoredMessages) {
                if(ignored.startsWith(f)) return true;
            }
            return false;
        }
    }

    private String f(String format, Object... args) {
        for (int i = 0; i < args.length; i++) {
            Object o = args[i];
            if(o instanceof VCSFileProxy) {
                VCSFileProxy p = (VCSFileProxy) o;
                try {
                    String path = getWorkdirRelativePath(p);
                    if(path != null) {
                        args[i] = getWorkdirRelativePath(p);
                    }
                } catch (IOException ex) {
                    fail(ex.getMessage());
                }
            }
        }
        return new MessageFormat(format).format(args);
    }
    
}
