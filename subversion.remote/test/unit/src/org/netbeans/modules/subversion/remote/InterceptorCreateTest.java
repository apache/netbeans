/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
