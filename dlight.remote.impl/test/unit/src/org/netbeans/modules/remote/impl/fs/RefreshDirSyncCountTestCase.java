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
package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class RefreshDirSyncCountTestCase extends RemoteFileTestBase {

    public RefreshDirSyncCountTestCase(String testName) {
        super(testName);
    }
    
    public RefreshDirSyncCountTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    private void recurse(FileObject fo) {
        for (FileObject child : fo.getChildren()) {
            recurse(child);
        }
    }

    @ForAllEnvironments
    public void test_iz_210439() throws Exception {
        String baseDir = null;
        try {
            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);
            baseDir = mkTempAndRefreshParent(true);
            String[] struct = new String[] {
                "d real_dir_1",
                "- real_dir_1/file_1",
//                "d real_dir_1/subdir_1",
//                "- real_dir_1/subdir_1/file_2",
                "l link_dir_1 real_dir_1",
                "l link_dir_2 real_dir_1"
            };
            createDirStructure(execEnv, baseDir, struct);
            RemoteFileObject baseFO = getFileObject(baseDir);
            baseFO.refresh();
            recurse(baseFO);
            
            int dirSyncCount_1 = fs.getDirSyncCount();
            Collection<RemoteFileObjectBase> cachedFileObjects = fs.getFactory().getCachedFileObjects();
            int cacheSize = cachedFileObjects.size();

            RefreshManager refreshManager = fs.getRefreshManager();
            long time = System.currentTimeMillis();
            refreshManager.scheduleRefresh(filterDirectories(cachedFileObjects), false);
            refreshManager.testWaitLastRefreshFinished(time);
            
            int dirSyncCount_2 = fs.getDirSyncCount();

            System.err.printf("Cache size: %d   Dir sync count during refresh: %d\n", cacheSize, dirSyncCount_2 - dirSyncCount_1);

            int expected = cacheSize - struct.length + 1; // (cacheSize - struct.length) == dirs from root to baseDir, inclusively

            assertEquals("Wrong dir sync count:", expected, dirSyncCount_2 - dirSyncCount_1);
        } finally {
            if (baseDir != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), "chmod", "-R", "700", baseDir);
                removeRemoteDirIfNotNull(baseDir);
            }
        }        
    }


    @ForAllEnvironments
    public void test_iz_258294_a() throws Exception {
        String baseDir = null;
        try {
            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);
            baseDir = mkTempAndRefreshParent(true);
            String[] struct = new String[] {
                "- file_1",
                "- file_2",
                "d dir_1",
                "l ../file_1 dir_1/link_1",
                "l ../file_2 dir_1/link_2",
            };
            createDirStructure(execEnv, baseDir, struct);
            RemoteFileObject baseFO = getFileObject(baseDir);
            baseFO.refresh();
            recurse(baseFO); // instantiate all file objects
            FileObject dirFO = getFileObject(baseFO, "dir_1");
            int cnt1 = getReadEntriesCount(dirFO);
            dirFO.refresh();
            int cnt2 = getReadEntriesCount(dirFO);
            assertEquals("Wrong dir read count:", 1, cnt2 - cnt1);
        } finally {
            if (baseDir != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), "chmod", "-R", "700", baseDir);
                removeRemoteDirIfNotNull(baseDir);
            }
        }
    }

    private int getReadEntriesCount(FileObject dirFO) {
        assertTrue(dirFO instanceof RemoteFileObject);
        RemoteFileObjectBase impl = ((RemoteFileObject) dirFO).getImplementor();
        assertTrue(impl instanceof RemoteDirectory);
        RemoteDirectory RD = (RemoteDirectory) impl;
        return RD.getReadEntriesCount();
    }

    @ForAllEnvironments
    public void test_iz_258294_b() throws Exception {
        String baseDir = null;
        try {
            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);
            baseDir = mkTempAndRefreshParent(true);
            String[] struct = new String[] {
                "d d1",
                "- d1/f1",
                "- d1/f2",
                "d d2",
                "l ../d1 d2/d1.lnk",
                "- d2/f3",
                "- d2/f4",
            };
            createDirStructure(execEnv, baseDir, struct);
            RemoteFileObject baseFO = getFileObject(baseDir);
            baseFO.refresh();
            recurse(baseFO); // instantiate all file objects
            FileObject d2 = getFileObject(baseFO, "d2");
            FileObject f1 = getFileObject(baseFO, "d1/f1");
            int rc = CommonTasksSupport.rmFile(execEnv, f1.getPath(), null).get();
            assertEquals("Error removing file " + f1, 0, rc);
            assertTrue(f1.isValid());
            //int cnt1 = getReadEntriesCount(d2);
            d2.refresh();
            //int cnt2 = getReadEntriesCount(d2);
            assertFalse("Error in test? File " + f1 + " should not exist!", HostInfoUtils.fileExists(execEnv, f1.getPath()));
            assertFalse("File " + f1 + " should be invalid", f1.isValid());
            //assertEquals("Wrong dir read count:", 2, cnt2 - cnt1);
        } finally {
            if (baseDir != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), "chmod", "-R", "700", baseDir);
                removeRemoteDirIfNotNull(baseDir);
            }
        }
    }

    private static Collection<RemoteFileObjectBase> filterDirectories(Collection<RemoteFileObjectBase> fileObjects) {
        Collection<RemoteFileObjectBase> result = new HashSet<>();
        for (RemoteFileObjectBase fo : fileObjects) {
            // Don't call isValid() or isFolder() - they might be SLOW!
            if (isDirectoryXXX(fo)) {
                result.add(fo);
            }
        }
        return result;
    }
    
    private static boolean isDirectoryXXX(RemoteFileObjectBase fo) {
        return fo != null && (/*(fo instanceof RemoteLinkBase) || */ (fo instanceof RemoteDirectory));
    }    
    
    public static Test suite() {
        return RemoteApiTest.createSuite(RefreshDirSyncCountTestCase.class);
    }
}
