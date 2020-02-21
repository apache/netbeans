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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class LinkListenersParityTestCase extends RemoteFileTestBase {

    public LinkListenersParityTestCase(String testName) {
        super(testName);
    }
    
    public LinkListenersParityTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    private static void doTestListeners2(FileObject baseDirFO, File log, boolean recursive) throws Exception {
        PrintStream out = new PrintStream(log);
        try {
            final String childName = "child_file_1";
            final String subdirName = "child_folder";
            String childLinkName = childName + "-link";
            String subirLinkName = subdirName + "-link";
            ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(baseDirFO);
            executeInDir(baseDirFO.getPath(), env, "ln",  "-s", childName, childLinkName);
            executeInDir(baseDirFO.getPath(), env, "ln",  "-s", subdirName, subirLinkName);            
            baseDirFO.refresh();
            
            String prefix = baseDirFO.getPath();
            if (recursive) {
                FileSystemProvider.addRecursiveListener(new DumpingFileChangeListener("recursive", prefix, out, true), baseDirFO.getFileSystem(), baseDirFO.getPath());
            } else {
                baseDirFO.addFileChangeListener(new DumpingFileChangeListener("baseDir", prefix, out, true));
            }
            FileObject childFO = baseDirFO.createData(childName);
            FileObject subdirFO = baseDirFO.createFolder(subdirName);
                        
            FileObject childLinkFO = baseDirFO.getFileObject(childLinkName);
            assertNotNull(childLinkFO);
            FileObject subdirLinkFO = baseDirFO.getFileObject(subirLinkName);
            assertNotNull(subdirLinkFO);
            
            if (!recursive) {
                subdirFO.addFileChangeListener(new DumpingFileChangeListener(subdirFO.getNameExt(), prefix, out, true));
                subdirLinkFO.addFileChangeListener(new DumpingFileChangeListener(subdirLinkFO.getNameExt(), prefix, out, true));
            }
            FileObject grandChildFO = subdirFO.createData("grand_child_file");
            FileObject grandChildDirFO = subdirFO.createFolder("grand_child_dir");
            FileObject grandGrandChildFO = grandChildDirFO.createData("grand_grand_child_file");
            baseDirFO.refresh();
            FileLock lock = grandGrandChildFO.lock();
            try {
                grandGrandChildFO.rename(lock, "grand_grand_child_file_renamed", "txt");
            } finally {
                lock.releaseLock();
            }
            lock = subdirFO.lock();
            try {
                subdirFO.rename(lock, "child_folder_renamed", "dir");
            } finally {
                lock.releaseLock();
            }
            
            // baseDirFO.refresh() will break the test. TODO: investigate.
            baseDirFO.refresh();
            grandGrandChildFO.delete();
            grandChildDirFO.delete();
        } finally {
            out.close();
        }
    }
    
    private void doTestListeners1(boolean recursive) throws Throwable {
        File localTmpDir = createTempFile(getClass().getSimpleName(), ".tmp", true);
        String remoteBaseDir = null;
        try {            
            remoteBaseDir = mkTempAndRefreshParent(true);
            FileObject remoteBaseDirFO = getFileObject(remoteBaseDir);
            FileObject localBaseDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(localTmpDir));            
            File workDir = getWorkDir();
            File remoteLog = new File(workDir, "remote.dat");
            File localLog = new File(workDir, "local.dat");
            doTestListeners2(remoteBaseDirFO, remoteLog, recursive);
            doTestListeners2(localBaseDirFO, localLog, recursive);
            if (RemoteApiTest.TRACE_LISTENERS) {
                printFile(localLog, "LOCAL ", System.out);
                printFile(remoteLog, "REMOTE", System.out);
            }
            File diff = new File(workDir, "diff.diff");
            try {
                assertFile("Remote and local events differ, see diff " + remoteLog.getAbsolutePath() + " " + localLog.getAbsolutePath(), remoteLog, localLog, diff);
            } catch (Throwable ex) {
                if (diff.exists()) {
                    printFile(diff, null, System.err);
                }
                throw ex;
            }
        } finally {
            removeRemoteDirIfNotNull(remoteBaseDir);
            if (localTmpDir != null && localTmpDir.exists()) {
                removeDirectory(localTmpDir);
            }
        }    
    }
    
    @ForAllEnvironments
    public void testListeners() throws Throwable {
        if (Utilities.isWindows()) {
            System.err.printf("Skipping %s test on Windows\n", getClass().getName());
            return;
        }
        doTestListeners1(false);
    }

    @ForAllEnvironments
    public void testRecursiveListeners() throws Throwable {                
        if (Utilities.isWindows()) {
            System.err.printf("Skipping %s test on Windows\n", getClass().getName());
            return;
        }
        doTestListeners1(true);
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(LinkListenersParityTestCase.class);
    }
}
