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
package org.netbeans.modules.subversion.remote;

import java.io.IOException;
import static junit.framework.Assert.assertTrue;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import static org.netbeans.modules.subversion.remote.RemoteVersioningTestBase.addTest;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class InterceptorCreateTest extends RemoteVersioningTestBase {

    public InterceptorCreateTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, InterceptorCreateTest.class, "createNewFile");
        addTest(suite, InterceptorCreateTest.class, "createNewFolder");
        addTest(suite, InterceptorCreateTest.class, "deleteA_CreateA");
        addTest(suite, InterceptorCreateTest.class, "deleteA_CreateAOnDemandLocking");
        addTest(suite, InterceptorCreateTest.class, "deleteA_CreateA_RunAtomic");
        addTest(suite, InterceptorCreateTest.class, "afterDelete_AfterCreate_194998");
        return(suite);
    }
    
    public void createNewFile() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        
        // create
        FileObject fo = wc.toFileObject();
        fo.createData(file.getName());
                                        
        // test 
        assertTrue(file.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());        
        assertCachedStatus(file, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
    }
    
    public void createNewFolder() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        
        // create
        FileObject fo = wc.toFileObject();
        fo.createFolder(folder.getName());
                                        
        // test 
        assertTrue(folder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
        assertCachedStatus(folder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
    }
    
    public void deleteA_CreateA() throws IOException, SVNClientException {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);        
        commit(wc);        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        
        // delete                
        FileObject fo = fileA.toFileObject();
        fo.delete();

        // test if deleted
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());

        // create        
        fo.getParent().createData(fo.getName());       
        
        // test 
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        
    }

    public void deleteA_CreateAOnDemandLocking() throws IOException, SVNClientException {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(file);
        commit(wc);
        SvnModuleConfig.getDefault(fs).setAutoLock(true);
        getClient().propertySet(file, "svn:needs-lock", "true", false);
        commit(file);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
        
        // delete
        FileObject fo = file.toFileObject();
        fo.delete();

        // test if deleted
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());

        // create        
        fo.getParent().createData(fo.getName());       
        
        // test 
        assertTrue(file.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE | FileInformation.STATUS_LOCKED, getStatus(file));                
    }

    public void deleteA_CreateA_RunAtomic() throws IOException, SVNClientException {
        if (skipTest()) {
            return;
        }
        // init
        final VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);        
        commit(wc);        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        
        final FileObject fo = fileA.toFileObject();
        FileSystem.AtomicAction a = new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {             
                fo.delete();
                fo.getParent().createData(fo.getName());
            }
        };
        fo.getFileSystem().runAtomicAction(a);        
        
        waitALittleBit(500); // after create 
        
        // test 
        assertTrue(fileA.exists());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
    }    
    
    public void afterDelete_AfterCreate_194998 () throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        
        // create
        FileObject fo = wc.toFileObject();
        fo.createData(file.getName());
        add(file);
        commit(file);
        
        // test 
        assertTrue(file.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
        
        VCSFileProxySupport.deleteExternally(file);
        VersioningSupport.refreshFor(new VCSFileProxy[]{file});
        assertEquals(SVNStatusKind.MISSING, getSVNStatus(file).getTextStatus());
        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY);
        
        TestKit.write(file, "modification");
        VersioningSupport.refreshFor(new VCSFileProxy[]{file.getParentFile()});
        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY_CONTENT);
        assertEquals(SVNStatusKind.MODIFIED, getSVNStatus(file).getTextStatus());
    }
}
