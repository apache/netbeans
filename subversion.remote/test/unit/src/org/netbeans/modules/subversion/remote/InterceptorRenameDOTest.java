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

import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import static org.netbeans.modules.subversion.remote.RemoteVersioningTestBase.addTest;
import org.netbeans.modules.subversion.remote.api.SVNStatusKind;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class InterceptorRenameDOTest extends RemoteVersioningTestBase {

    public InterceptorRenameDOTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, InterceptorRenameDOTest.class, "renameVersionedFile_DO");
        addTest(suite, InterceptorRenameDOTest.class, "renameUnversionedFile_DO");
        addTest(suite, InterceptorRenameDOTest.class, "renameUnversionedFolder_DO");
        addTest(suite, InterceptorRenameDOTest.class, "renameAddedFile_DO");
        addTest(suite, InterceptorRenameDOTest.class, "renameA2B2A_DO");
        addTest(suite, InterceptorRenameDOTest.class, "renameA2B2C_DO");
        addTest(suite, InterceptorRenameDOTest.class, "renameA2B2C2A_DO");
        addTest(suite, InterceptorRenameDOTest.class, "renameA2B_CreateA_DO");
        addTest(suite, InterceptorRenameDOTest.class, "deleteA_RenameB2A_DO_129805");
        addTest(suite, InterceptorRenameDOTest.class, "renameVersionedFolder_DO");
        addTest(suite, InterceptorRenameDOTest.class, "renameFileTree_DO"); // failed
        addTest(suite, InterceptorRenameDOTest.class, "renameA2CB2A_DO");
        addTest(suite, InterceptorRenameDOTest.class, "deleteA_renameB2A2B_DO");
        addTest(suite, InterceptorRenameDOTest.class, "deleteA_renameUnversioned2A_DO");
        return(suite);
    }
    
    public void renameVersionedFile_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        commit(wc);                       
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(wc, "toFile");
        
        // rename    
        renameDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFile).getTextStatus());
        
        assertCachedStatus(fromFile, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        assertCachedStatus(toFile, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
//        commit(wc);
    }

    public void renameUnversionedFile_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(wc, "toFile");
                
        // rename
        renameDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));                
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void renameUnversionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "fromFolder");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "toFolder");
        
        // rename
        renameDO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFolder).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFolder));                
        assertCachedStatus(toFolder, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void renameAddedFile_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init        
        VCSFileProxy fromFile = VCSFileProxy.createFileProxy(wc, "fromFile");
        VCSFileProxySupport.createNew(fromFile);
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(wc, "toFile");
        
        // add
        getClient().addFile(fromFile);                
        
        // rename
        renameDO(fromFile, toFile);
        
        // test 
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fromFile).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(toFile).getTextStatus());        

        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fromFile));
        assertCachedStatus(toFile, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);                
        
//        commit(wc);
    }
    
    public void renameA2B2A_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "to");
        
        // rename
        renameDO(fileA, fileB);
        renameDO(fileB, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
              
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        
//        commit(wc);
    }
    
    public void renameA2B2C_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);        
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(wc, "C");

        // rename
        renameDO(fileA, fileB);
        renameDO(fileB, fileC);
        
        // test 
        assertFalse(fileA.exists());
        assertFalse(fileB.exists());
        assertTrue(fileC.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());
        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertCachedStatus(fileC, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);
        
//        commit(wc);
    }
    
    public void renameA2B2C2A_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);  
        
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        VCSFileProxy fileC = VCSFileProxy.createFileProxy(wc, "C");
        
        // rename 
        renameDO(fileA, fileB);
        renameDO(fileB, fileC);
        renameDO(fileC, fileA);
        
        // test 
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertFalse(fileC.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileC).getTextStatus());  
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));                
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileC));                
        
//        commit(wc);
        
    }

    public void renameA2B_CreateA_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);  
        
        // rename
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        renameDO(fileA, fileB);
        
        // create from file
        fileA.getParentFile().toFileObject().createData(fileA.getName());
        
        // test 
        assertTrue(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileB).getTextStatus());        
        
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));                
        assertCachedStatus(fileB, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        
//        commit(wc);
    }
    
    // fixed - see issue #129805
    public void deleteA_RenameB2A_DO_129805() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        VCSFileProxySupport.createNew(fileB);
        commit(wc);  
        
        // delete A
        delete(fileA);
        // rename B to A
        renameDO(fileB, fileA);
        
        // test 
        assertFalse(fileB.exists());
        assertTrue(fileA.exists());
        
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
             
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        
        commit(wc);

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
    }
    
    public void renameVersionedFolder_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        commit(wc);               
       
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "to");
        
        // rename       
        renameDO(fromFolder, toFolder);
        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        commit(wc);
        assertFalse(fromFolder.exists());        
    }

    public void renameFileTree_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fromFolder = VCSFileProxy.createFileProxy(wc, "from");
        VCSFileProxySupport.mkdirs(fromFolder);
        VCSFileProxy fromFolder1 = VCSFileProxy.createFileProxy(fromFolder, "folder1");
        VCSFileProxySupport.mkdirs(fromFolder1);
        VCSFileProxy fromFolder2 = VCSFileProxy.createFileProxy(fromFolder, "folder2");
        VCSFileProxySupport.mkdirs(fromFolder2);
        VCSFileProxy fromFile11 = VCSFileProxy.createFileProxy(fromFolder1, "file11");
        VCSFileProxySupport.createNew(fromFile11);
        VCSFileProxy fromFile12 = VCSFileProxy.createFileProxy(fromFolder1, "file12");
        VCSFileProxySupport.createNew(fromFile12);
        VCSFileProxy fromFile21 = VCSFileProxy.createFileProxy(fromFolder2, "file21");
        VCSFileProxySupport.createNew(fromFile21);
        VCSFileProxy fromFile22 = VCSFileProxy.createFileProxy(fromFolder2, "file22");
        VCSFileProxySupport.createNew(fromFile22);
        commit(wc);               
        
        // rename
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(wc, "to");
        renameDO(fromFolder, toFolder);
                                        
        // test 
        assertFalse(fromFolder.exists());
        assertTrue(toFolder.exists());
        VCSFileProxy toFolder1 = VCSFileProxy.createFileProxy(toFolder, "folder1");
        assertTrue(toFolder1.exists());
        VCSFileProxy toFolder2 = VCSFileProxy.createFileProxy(toFolder, "folder2");
        assertTrue(toFolder2.exists());
        VCSFileProxy toFile11 = VCSFileProxy.createFileProxy(toFolder1, "file11");
        assertTrue(toFile11.exists());
        VCSFileProxy toFile12 = VCSFileProxy.createFileProxy(toFolder1, "file12");
        assertTrue(toFile12.exists());
        VCSFileProxy toFile21 = VCSFileProxy.createFileProxy(toFolder2, "file21");
        assertTrue(toFile21.exists());
        VCSFileProxy toFile22 = VCSFileProxy.createFileProxy(toFolder2, "file22");
        assertTrue(toFile22.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile11).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile12).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile21).getTextStatus());        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fromFile22).getTextStatus());        
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(toFolder).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder1).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFolder2).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile11).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile12).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile21).getTextStatus());        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(toFile22).getTextStatus());    
        
        assertCachedStatus(fromFolder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder1, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFolder2, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile11, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile12, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile21, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(fromFile22, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);                
        assertCachedStatus(toFolder, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);                
        assertCachedStatus(toFolder1, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertCachedStatus(toFolder2, FileInformation.STATUS_VERSIONED_UPTODATE);                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile11));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile12));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile21));                
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(toFile22));   
        
        commit(wc);
        assertFalse(fromFolder.exists());
    }
    
    public void renameA2CB2A_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, "B");
        VCSFileProxySupport.createNew(fileB);
        commit(wc);

        VCSFileProxy fileC = VCSFileProxy.createFileProxy(wc, "C");

        // move
        renameDO(fileA, fileC);
        Thread.sleep(500);
        renameDO(fileB, fileA);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.ADDED, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileC));

        commit(wc);

        // test
        assertTrue(fileA.exists());
        assertTrue(fileC.exists());
        assertFalse(fileB.exists());

        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(fileB).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileC).getTextStatus());

        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(fileB));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileC));
    }

    public void deleteA_renameB2A2B_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdir(folder);
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folder, "A");
        VCSFileProxySupport.createNew(fileA);
        VCSFileProxy fileB = VCSFileProxy.createFileProxy(folder, "B");
        VCSFileProxySupport.createNew(fileB);
        commit(wc);
        
        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        renameDO(fileB, fileA);
        assertTrue(fileA.exists());
        assertFalse(fileB.exists());
        assertEquals(SVNStatusKind.REPLACED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileB));
        // move second
        renameDO(fileA, fileB);
        assertFalse(fileA.exists());
        assertTrue(fileB.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileB).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileB));
    }
    
    public void deleteA_renameUnversioned2A_DO() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdir(folder);
        VCSFileProxy fileA = VCSFileProxy.createFileProxy(folder, "A");
        VCSFileProxySupport.createNew(fileA);
        commit(wc);
        
        VCSFileProxy fileUnversioned = VCSFileProxy.createFileProxy(folder, "Unversioned");
        VCSFileProxySupport.createNew(fileUnversioned);

        //delete
        delete(fileA);
        assertFalse(fileA.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(fileA));
        // move
        renameDO(fileUnversioned, fileA);

        // test
        assertTrue(fileA.exists());
        assertFalse(fileUnversioned.exists());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(fileA).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));
    }
}
