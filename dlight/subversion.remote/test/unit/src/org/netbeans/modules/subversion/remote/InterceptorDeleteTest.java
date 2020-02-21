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
import java.util.logging.Handler;
import static junit.framework.Assert.assertEquals;
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
import org.openide.util.RequestProcessor;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class InterceptorDeleteTest extends RemoteVersioningTestBase {

    public InterceptorDeleteTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, InterceptorDeleteTest.class, "deleteCreateChangeCase_issue_157373");
        addTest(suite, InterceptorDeleteTest.class, "deleteNotVersionedFile");
        addTest(suite, InterceptorDeleteTest.class, "deleteVersionedFileExternally");
        addTest(suite, InterceptorDeleteTest.class, "deleteVersionedFile");
        addTest(suite, InterceptorDeleteTest.class, "deleteVersionedFolder");
        addTest(suite, InterceptorDeleteTest.class, "deleteNotVersionedFolder");
        addTest(suite, InterceptorDeleteTest.class, "deleteWCRoot");
        addTest(suite, InterceptorDeleteTest.class, "deleteVersionedFileTree");
        addTest(suite, InterceptorDeleteTest.class, "deleteNotVersionedFileTree");
        return(suite);
    }
        
    public void deleteCreateChangeCase_issue_157373 () throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        final VCSFileProxy fileA = VCSFileProxy.createFileProxy(wc, "file");
        wc.toFileObject().createData(fileA.getName());
        assertCachedStatus(fileA, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        commit(wc);
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(fileA));

        // rename
        VCSFileProxySupport.delete(fileA);
        Handler h = new SVNInterceptor();
        Subversion.LOG.addHandler(h);
        RequestProcessor.Task r = Subversion.getInstance().getParallelRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                VersioningSupport.refreshFor(new VCSFileProxy[]{fileA});
            }
        });
        r.run();
        assertFalse(fileA.exists());
        final VCSFileProxy fileB = VCSFileProxy.createFileProxy(wc, fileA.getName().toUpperCase());
        VCSFileProxySupport.createNew(fileB);
        Thread.sleep(3000);
        assertTrue(fileB.exists());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, getStatus(fileB));
        Subversion.LOG.removeHandler(h);
    }
    
    public void deleteNotVersionedFile() throws Exception {
        if (skipTest()) {
            return;
        }
        // init        
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(file);
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());

        // delete
        delete(file);

        // test
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file));
        
//        commit(wc);
    }
    
    public void deleteVersionedFileExternally() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        wc.toFileObject().createData(file.getName());
        assertCachedStatus(file, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
        commit(wc);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_UPTODATE, getStatus(file));

        String prop = System.getProperty("org.netbeans.modules.subversion.deleteMissingFiles", "");
        try {
            System.setProperty("org.netbeans.modules.subversion.deleteMissingFiles", "true");
            // delete externally
            VCSFileProxySupport.deleteExternally(file);

            // test
            assertFalse(file.exists());
            assertEquals(SVNStatusKind.MISSING, getSVNStatus(file).getTextStatus());

            // notify changes
            VersioningSupport.refreshFor(new VCSFileProxy[]{file});
            assertCachedStatus(file, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);
        } finally {
            System.setProperty("org.netbeans.modules.subversion.deleteMissingFiles", prop);
        }
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());
        commit(wc);
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
    }

    public void deleteVersionedFile() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy file = VCSFileProxy.createFileProxy(wc, "file");
        VCSFileProxySupport.createNew(file);
        commit(wc);
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file).getTextStatus());

        // delete
        delete(file);

        // test
        assertFalse(file.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file).getTextStatus());

        assertCachedStatus(file, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);

        commit(wc);

        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file).getTextStatus());
    }

    public void deleteVersionedFolder() throws Exception {
        if (skipTest()) {
            return;
        }
        // init        
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder1");
        VCSFileProxySupport.mkdirs(folder);
        commit(wc);      
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder).getTextStatus());

        // delete
        delete(folder);

        // test
        assertFalse(folder.exists());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder).getTextStatus());
        
        assertCachedStatus(folder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        
        commit(wc);
        
        assertFalse(folder.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }

    public void deleteNotVersionedFolder() throws IOException, SVNClientException {
        if (skipTest()) {
            return;
        }
        // init        
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder2");
        VCSFileProxySupport.mkdirs(folder);
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertFalse(folder.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder));
        
//        commit(wc);
    }    

    public void deleteWCRoot() throws Exception {
        if (skipTest()) {
            return;
        }
        // init        
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(wc).getTextStatus());

        // delete
        delete(wc);
        
        // test
        assertTrue(!wc.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(wc).getTextStatus());        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(wc));
    }

    public void deleteVersionedFileTree() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(folder, "folder1");
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(folder, "folder2");
        VCSFileProxySupport.mkdirs(folder2);        
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(folder1, "file1");
        VCSFileProxySupport.createNew(file11);
        VCSFileProxy file12 = VCSFileProxy.createFileProxy(folder1, "file2");
        VCSFileProxySupport.createNew(file12);
        VCSFileProxy file21 = VCSFileProxy.createFileProxy(folder2, "file1");
        VCSFileProxySupport.createNew(file21);
        VCSFileProxy file22 = VCSFileProxy.createFileProxy(folder2, "file2");
        VCSFileProxySupport.createNew(file22);
        
        commit(wc);      
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder1).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(folder2).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file11).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file12).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file21).getTextStatus());
        assertEquals(SVNStatusKind.NORMAL, getSVNStatus(file22).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertFalse(file11.exists());
        assertFalse(file12.exists());
        assertFalse(file21.exists());
        assertFalse(file22.exists());
        
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder1).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(folder2).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file11).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file12).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file21).getTextStatus());
        assertEquals(SVNStatusKind.DELETED, getSVNStatus(file22).getTextStatus());                
        
        assertCachedStatus(folder, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        assertCachedStatus(folder1, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        assertCachedStatus(folder2, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file11));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file12));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file21));        
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getStatus(file22));        
        
        
        commit(wc);
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());                
    }

    public void deleteNotVersionedFileTree() throws Exception {
        if (skipTest()) {
            return;
        }
        // init
        VCSFileProxy folder = VCSFileProxy.createFileProxy(wc, "folder");
        VCSFileProxySupport.mkdirs(folder);
        VCSFileProxy folder1 = VCSFileProxy.createFileProxy(folder, "folder1");
        VCSFileProxySupport.mkdirs(folder1);
        VCSFileProxy folder2 = VCSFileProxy.createFileProxy(folder, "folder2");
        VCSFileProxySupport.mkdirs(folder2);        
        VCSFileProxy file11 = VCSFileProxy.createFileProxy(folder1, "file1");
        VCSFileProxySupport.createNew(file11);
        VCSFileProxy file12 = VCSFileProxy.createFileProxy(folder1, "file2");
        VCSFileProxySupport.createNew(file12);
        VCSFileProxy file21 = VCSFileProxy.createFileProxy(folder2, "file1");
        VCSFileProxySupport.createNew(file21);
        VCSFileProxy file22 = VCSFileProxy.createFileProxy(folder2, "file2");
        VCSFileProxySupport.createNew(file22);
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder1).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file22).getTextStatus());

        // delete
        delete(folder);
        
        // test
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertFalse(file11.exists());
        assertFalse(file12.exists());
        assertFalse(file21.exists());
        assertFalse(file22.exists());
        
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder1).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder2).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file11).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file12).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file21).getTextStatus());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(file22).getTextStatus());
        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder1));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(folder2));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file11));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file12));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file21));        
        assertEquals(FileInformation.STATUS_UNKNOWN, getStatus(file22));        
        
        commit(wc);
        assertFalse(folder.exists());
        assertFalse(folder1.exists());
        assertFalse(folder2.exists());
        assertEquals(SVNStatusKind.UNVERSIONED, getSVNStatus(folder).getTextStatus());        
    }
}
