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

import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ClassForAllEnvironments;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.Utilities;

/**
 *
 * 
 */
@ClassForAllEnvironments(section = "remote.svn")
public class MoveInterceptorTest extends  RemoteVersioningTestBase {

    public MoveInterceptorTest(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        addTest(suite, MoveInterceptorTest.class, "moveFileToIgnoredFolder_DO");
        addTest(suite, MoveInterceptorTest.class, "moveFileToIgnoredFolder_FO");
        addTest(suite, MoveInterceptorTest.class, "renameFileChangeCase_DO");
        addTest(suite, MoveInterceptorTest.class, "renameFileChangeCase_FO");
        addTest(suite, MoveInterceptorTest.class, "renameFolderChangeCase_DO");
        addTest(suite, MoveInterceptorTest.class, "renameFolderChangeCase_FO");
        return(suite);
    }
    
    public void moveFileToIgnoredFolder_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy folder = createFolder("ignoredFolder");
        HgUtils.addIgnored(folder.getParentFile(), new VCSFileProxy[] { folder });
        VCSFileProxy toFolder = createFolder(folder, "toFolder");
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFile);
        
        // move
        moveDO(fromFile, toFile);
        
        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void moveFileToIgnoredFolder_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy folder = createFolder("ignoredFolder");
        HgUtils.addIgnored(folder.getParentFile(), new VCSFileProxy[] { folder });
        VCSFileProxy toFolder = createFolder(folder, "toFolder");
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFile);
        
        // move
        moveFO(fromFile, toFile);
        
        // test
        assertFalse(fromFile.exists());
        assertTrue(toFile.exists());
        assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
        assertEquals(FileInformation.STATUS_NOTVERSIONED_EXCLUDED, getCache().refresh(toFile).getStatus());
    }
    
    public void renameFileChangeCase_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(getWorkTreeDir(), "FILE");
        commit(fromFile);
        
        // move
        renameDO(fromFile, toFile.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(isParentHasChild(toFile));
            assertFalse(isParentHasChild(fromFile));
        } else {
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void renameFileChangeCase_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy fromFile = createFile("file");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(getWorkTreeDir(), "FILE");
        commit(fromFile);
        
        // move
        renameFO(fromFile, toFile.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(isParentHasChild(toFile));
            assertFalse(isParentHasChild(fromFile));
        } else {
            assertFalse(fromFile.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void renameFolderChangeCase_DO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy fromFolder = createFolder("folder");
        VCSFileProxy fromFile = createFile(fromFolder, "file");
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(getWorkTreeDir(), "FOLDER");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFolder);
        
        // move
        renameDO(fromFolder, toFolder.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(isParentHasChild(toFolder));
            assertFalse(isParentHasChild(fromFolder));
        } else {
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
    
    public void renameFolderChangeCase_FO () throws Exception {
        if (skipTest()) {
            return;
        }
        // prepare
        VCSFileProxy fromFolder = createFolder("folder");
        VCSFileProxy fromFile = createFile(fromFolder, "file");
        VCSFileProxy toFolder = VCSFileProxy.createFileProxy(getWorkTreeDir(), "FOLDER");
        VCSFileProxy toFile = VCSFileProxy.createFileProxy(toFolder, fromFile.getName());
        commit(fromFolder);
        
        // move
        renameFO(fromFolder, toFolder.getName());
        
        // test
        if (Utilities.isWindows() || Utilities.isMac()) {
            assertTrue(isParentHasChild(toFolder));
            assertFalse(isParentHasChild(fromFolder));
        } else {
            assertFalse(fromFolder.exists());
            assertTrue(toFolder.exists());
            assertTrue(toFile.exists());
            assertEquals(FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY, getCache().refresh(fromFile).getStatus());
            assertEquals(FileInformation.STATUS_VERSIONED_ADDEDLOCALLY, getCache().refresh(toFile).getStatus());
        }
    }
}
