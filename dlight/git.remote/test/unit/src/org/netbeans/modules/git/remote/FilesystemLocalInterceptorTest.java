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
package org.netbeans.modules.git.remote;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class FilesystemLocalInterceptorTest extends AbstractLocalGitTestCase {

    public static final String PROVIDED_EXTENSIONS_REMOTE_LOCATION = "ProvidedExtensions.RemoteLocation";

    public FilesystemLocalInterceptorTest(String name) {
        super(name);
    }
    
    @Override
    protected boolean isFailed() {
        return Arrays.asList().contains(getName());
    }

    @Override
    protected boolean isRunAll() {return false;}

    public void testSeenRootsLogin () throws Exception {
        VCSFileProxy folderA = VCSFileProxy.createFileProxy(repositoryLocation, "folderA");
        VCSFileProxy fileA1 = VCSFileProxy.createFileProxy(folderA, "file1");
        VCSFileProxy fileA2 = VCSFileProxy.createFileProxy(folderA, "file2");
        VCSFileProxySupport.mkdirs(folderA);
        VCSFileProxySupport.createNew(fileA1);
        VCSFileProxySupport.createNew(fileA2);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repositoryLocation, "folderB");
        VCSFileProxy fileB1 = VCSFileProxy.createFileProxy(folderB, "file1");
        VCSFileProxy fileB2 = VCSFileProxy.createFileProxy(folderB, "file2");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxySupport.createNew(fileB1);
        VCSFileProxySupport.createNew(fileB2);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repositoryLocation, "folderC");
        VCSFileProxy fileC1 = VCSFileProxy.createFileProxy(folderC, "file1");
        VCSFileProxy fileC2 = VCSFileProxy.createFileProxy(folderC, "file2");
        VCSFileProxySupport.mkdirs(folderC);
        VCSFileProxySupport.createNew(fileC1);
        VCSFileProxySupport.createNew(fileC2);

        FilesystemInterceptor interceptor = Git.getInstance().getVCSInterceptor();
        Field f = FilesystemInterceptor.class.getDeclaredField("gitFolderEventsHandler");
        f.setAccessible(true);
        Object hgFolderEventsHandler = f.get(interceptor);
        f = hgFolderEventsHandler.getClass().getDeclaredField("seenRoots");
        f.setAccessible(true);
        HashMap<VCSFileProxy, Set<VCSFileProxy>> map = (HashMap) f.get(hgFolderEventsHandler);

        LogHandler handler = new LogHandler();
        Git.STATUS_LOG.addHandler(handler);
        handler.setFilesToInitializeRoots(folderA);
        interceptor.pingRepositoryRootFor(folderA);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        Set<VCSFileProxy> files = map.get(repositoryLocation);
        assertEquals(1, files.size());
        assertTrue(files.contains(folderA));
        handler.setFilesToInitializeRoots(fileA1);
        interceptor.pingRepositoryRootFor(fileA1);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        files = map.get(repositoryLocation);
        assertEquals(1, files.size());
        assertTrue(files.contains(folderA));

        handler.setFilesToInitializeRoots(fileB1);
        interceptor.pingRepositoryRootFor(fileB1);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(2, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));

        handler.setFilesToInitializeRoots(fileB2);
        interceptor.pingRepositoryRootFor(fileB2);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(3, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));
        assertTrue(files.contains(fileB2));

        handler.setFilesToInitializeRoots(folderC);
        interceptor.pingRepositoryRootFor(folderC);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(4, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));
        assertTrue(files.contains(fileB2));
        assertTrue(files.contains(folderC));

        handler.setFilesToInitializeRoots(folderB);
        interceptor.pingRepositoryRootFor(folderB);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        assertEquals(3, files.size());
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));
        assertTrue(files.contains(folderC));

        handler.setFilesToInitializeRoots(repositoryLocation);
        interceptor.pingRepositoryRootFor(repositoryLocation);
        // some time for bg threads
        assertTrue(handler.waitForFilesToInitializeRoots());
        Git.STATUS_LOG.removeHandler(handler);
        assertEquals(1, files.size());
        assertTrue(files.contains(repositoryLocation));
    }

    public void testGetWrongAttribute () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "attrfile");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.toFileObject();

        String str = (String) fo.getAttribute("peek-a-boo");
        assertNull(str);
    }

    public void testGetRemoteLocationAttribute () throws Exception {
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "attrfile");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.toFileObject();

        String str = (String) fo.getAttribute(PROVIDED_EXTENSIONS_REMOTE_LOCATION);
        // TODO implement getRemoteRepositoryURL
//        assertNotNull(str);
//        assertEquals(repositoryLocation.getAbsolutePath().toString(), str);
    }

    public void testModifyVersionedFile () throws Exception {
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        refreshHandler.setFilesToRefresh(Collections.singleton(file));
        VCSFileProxySupport.createNew(file);
        add();
        commit();
        FileObject fo = file.normalizeFile().toFileObject();
        assertEquals(EnumSet.of(Status.UPTODATE), getCache().getStatus(file).getStatus());
        assertTrue(refreshHandler.waitForFilesToRefresh());

        refreshHandler.setFilesToRefresh(Collections.singleton(file));
        PrintWriter pw = new PrintWriter(fo.getOutputStream());
        pw.println("hello new file");
        pw.close();
        assertTrue(refreshHandler.waitForFilesToRefresh());

        // test
        assertEquals(EnumSet.of(Status.MODIFIED_HEAD_WORKING_TREE, Status.MODIFIED_INDEX_WORKING_TREE), getCache().getStatus(file).getStatus());
    }
    
    public void testIsModifiedAttributeFile () throws Exception {
        // file is outside of versioned space, attribute should be unknown
        VCSFileProxy file = VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(getWorkDir()), "file");
        VCSFileProxySupport.createNew(file);
        FileObject fo = file.normalizeFile().toFileObject();
        String attributeModified = "ProvidedExtensions.VCSIsModified";
        
        Object attrValue = fo.getAttribute(attributeModified);
        assertNull(attrValue);
        
        // file inside a git repo
        file = VCSFileProxy.createFileProxy(repositoryLocation, "file");
        write(file, "init");
        fo = file.toFileObject();
        // new file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        add();
        // added file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        commit();
        
        // unmodified file, returns FALSE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
        
        write(file, "modification");
        // modified file, returns TRUE
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.TRUE, attrValue);
        
        write(file, "init");
        // back to up to date
        attrValue = fo.getAttribute(attributeModified);
        assertEquals(Boolean.FALSE, attrValue);
    }

}
