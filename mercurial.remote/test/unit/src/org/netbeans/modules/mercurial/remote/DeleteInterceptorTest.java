/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.mercurial.remote;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class DeleteInterceptorTest extends  RemoteVersioningTestBase {

    public DeleteInterceptorTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, DeleteInterceptorTest.class, "fullScanLimitedOnVisibleRoots");
        addTest(suite, DeleteInterceptorTest.class, "deleteFile_FO");
        addTest(suite, DeleteInterceptorTest.class, "deleteFileDO");
        addTest(suite, DeleteInterceptorTest.class, "deleteFolder_FO");
        addTest(suite, DeleteInterceptorTest.class, "deleteFolder_DO");
        return(suite);
    }
    
    public void fullScanLimitedOnVisibleRoots () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy repo = VCSFileProxy.createFileProxy(getWorkTreeDir().getParentFile(), String.valueOf(System.currentTimeMillis()));
        VCSFileProxySupport.mkdir(repo);
        HgCommand.doCreate(repo, NULL_LOGGER);

        VCSFileProxy folderA = VCSFileProxy.createFileProxy(repo, "folderA");
        VCSFileProxy fileA1 = VCSFileProxy.createFileProxy(folderA, "file1");
        VCSFileProxy fileA2 = VCSFileProxy.createFileProxy(folderA, "file2");
        VCSFileProxySupport.mkdirs(folderA);
        VCSFileProxySupport.createNew(fileA1);
        VCSFileProxySupport.createNew(fileA2);
        VCSFileProxy folderB = VCSFileProxy.createFileProxy(repo, "folderB");
        VCSFileProxy fileB1 = VCSFileProxy.createFileProxy(folderB, "file1");
        VCSFileProxy fileB2 = VCSFileProxy.createFileProxy(folderB, "file2");
        VCSFileProxySupport.mkdirs(folderB);
        VCSFileProxySupport.createNew(fileB1);
        VCSFileProxySupport.createNew(fileB2);
        VCSFileProxy folderC = VCSFileProxy.createFileProxy(repo, "folderC");
        VCSFileProxy fileC1 = VCSFileProxy.createFileProxy(folderC, "file1");
        VCSFileProxy fileC2 = VCSFileProxy.createFileProxy(folderC, "file2");
        VCSFileProxySupport.mkdirs(folderC);
        VCSFileProxySupport.createNew(fileC1);
        VCSFileProxySupport.createNew(fileC2);

        MercurialInterceptor interceptor = Mercurial.getInstance().getMercurialInterceptor();
        Field f = MercurialInterceptor.class.getDeclaredField("hgFolderEventsHandler");
        f.setAccessible(true);
        Object hgFolderEventsHandler = f.get(interceptor);
        f = hgFolderEventsHandler.getClass().getDeclaredField("seenRoots");
        f.setAccessible(true);
        HashMap<VCSFileProxy, Set<VCSFileProxy>> map = (HashMap) f.get(hgFolderEventsHandler);

        getCache().markAsSeenInUI(folderA);
        // some time for bg threads
        Thread.sleep(3000);
        Set<VCSFileProxy> files = map.get(repo);
        assertTrue(files.contains(folderA));

        getCache().markAsSeenInUI(fileB1);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(fileB1));

        getCache().markAsSeenInUI(fileB2);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));

        getCache().markAsSeenInUI(folderC);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(files.contains(folderA));
        assertTrue(files.contains(folderB));
        assertTrue(files.contains(folderC));

        getCache().markAsSeenInUI(repo);
        // some time for bg threads
        Thread.sleep(3000);
        assertTrue(files.contains(repo));

        VCSFileProxySupport.delete(repo);
    }

    public void deleteFile_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file1");
        commit(folder);
        createFile(folder, "file2");        
        deleteFO(file);
        assertFalse(file.exists());
        assertTrue(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file).getStatus());
    }
    
    public void deleteFileDO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file = createFile(folder, "file1");
        commit(folder);
        createFile(folder, "file2");        
        deleteDO(file);
        assertFalse(file.exists());
        assertTrue(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file).getStatus());
    }
    
    public void deleteFolder_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        commit(folder);
        
        deleteFO(folder);
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file2).getStatus());
    }
    
    public void deleteFolder_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        VCSFileProxy folder = createFolder("folder");
        VCSFileProxy file1 = createFile(folder, "file1");
        VCSFileProxy file2 = createFile(folder, "file2");
        commit(folder);
        
        deleteDO(folder);
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(folder.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file1).getStatus());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(file2).getStatus());
    }
}
