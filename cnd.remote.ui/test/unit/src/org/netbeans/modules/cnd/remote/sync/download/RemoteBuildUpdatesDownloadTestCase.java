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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.sync.download;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.cnd.remote.support.RemoteProjectSupport;
import org.netbeans.modules.cnd.remote.sync.download.FileDownloadInfo.State;
import org.netbeans.modules.cnd.remote.test.RemoteBuildTestBase;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
/**
 *
 */
public class RemoteBuildUpdatesDownloadTestCase extends RemoteBuildTestBase {

    public RemoteBuildUpdatesDownloadTestCase(String testName) {
        super(testName);
    }

    public RemoteBuildUpdatesDownloadTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    private static class NameStatePair {
        public final File file;
        public final FileDownloadInfo.State state;
        public NameStatePair(File file, State state) {
            this.file = file;
            this.state = state;
        }
    
    }

    @ForAllEnvironments
    public void test_LexYacc_BuildLocalAndRemote() throws Exception {
        doTest_LexYacc_BuildLocalAndRemote(Sync.RFS);
    }

    @ForAllEnvironments
    public void test_LexYacc_BuildLocalAndRemoteFtp() throws Exception {
        doTest_LexYacc_BuildLocalAndRemote(Sync.FTP);
    }
    
    private void doTest_LexYacc_BuildLocalAndRemote(Sync sync) throws Exception {
        MakeProject makeProject = prepareSampleProject(sync, Toolchain.GNU, "LexYacc", "LexYacc_Build");
        int timeout = getSampleBuildTimeout();
        changeProjectHost(makeProject, ExecutionEnvironmentFactory.getLocal());
        buildProject(makeProject, ActionProvider.COMMAND_BUILD, timeout, TimeUnit.SECONDS);
        changeProjectHost(makeProject, getTestExecutionEnvironment());
        File token_l = new File(CndFileUtils.toFile(makeProject.getProjectDirectory()), "token.l");
        token_l.setLastModified(System.currentTimeMillis() + 2000);
        File token_y = new File(CndFileUtils.toFile(makeProject.getProjectDirectory()), "token.y");
        token_y.setLastModified(System.currentTimeMillis() + 2000);
        buildProject(makeProject, ActionProvider.COMMAND_BUILD, timeout, TimeUnit.SECONDS);
        // Bug #182762 - Second clean & build for LexYacc build on remote host fails
        buildProject(makeProject, ActionProvider.COMMAND_CLEAN, timeout, TimeUnit.SECONDS);
        buildProject(makeProject, ActionProvider.COMMAND_BUILD, timeout, TimeUnit.SECONDS);
    }

    @ForAllEnvironments
    public void test_LexYacc_Updates() throws Exception {
        doTest_LexYacc_Updates(Sync.RFS);
    }
    @ForAllEnvironments
    public void test_LexYacc_UpdatesFtp() throws Exception {
        doTest_LexYacc_Updates(Sync.FTP);
    }

    private void doTest_LexYacc_Updates(Sync sync) throws Exception {
        MakeProject makeProject = prepareSampleProject(sync, Toolchain.GNU, "LexYacc", "LexYacc_Updates");
        int timeout = getSampleBuildTimeout();
        buildProject(makeProject, ActionProvider.COMMAND_CLEAN, timeout, TimeUnit.SECONDS);
        buildProject(makeProject, ActionProvider.COMMAND_BUILD, timeout, TimeUnit.SECONDS);
        File projectDirFile = CndFileUtils.toFile(makeProject.getProjectDirectory());
        NameStatePair[] filesToCheck = new NameStatePair[] {
            new NameStatePair(new File(projectDirFile, "y.tab.c"), FileDownloadInfo.State.UNCONFIRMED),
            new NameStatePair(new File(projectDirFile, "y.tab.h"), FileDownloadInfo.State.UNCONFIRMED),
            new NameStatePair(new File(projectDirFile, "lex.yy.c"), FileDownloadInfo.State.UNCONFIRMED)
        };
        checkInfo(filesToCheck, 10000, RemoteProjectSupport.getPrivateStorage(makeProject));
    }

    @ForAllEnvironments
    public void testNonProjectUpdates() throws Exception {
        doTestNonProjectUpdates(Sync.RFS);
    }
    
    @ForAllEnvironments
    public void testNonProjectUpdatesFtp() throws Exception {
        doTestNonProjectUpdates(Sync.FTP);
    }
    
    private void doTestNonProjectUpdates(Sync sync) throws Exception {
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        MakeProject makeProject = openProject("TestNonProjectUpdates", execEnv, sync, Toolchain.GNU);
        changeProjectHost(makeProject, execEnv);
        buildProject(makeProject, ActionProvider.COMMAND_BUILD, getSampleBuildTimeout(), TimeUnit.SECONDS);
        File projectDirFile = CndFileUtils.toFile(makeProject.getProjectDirectory());
        NameStatePair[] filesToCheck = new NameStatePair[] {
            new NameStatePair(new File(projectDirFile, "file_1.c"), FileDownloadInfo.State.UNCONFIRMED),
            new NameStatePair(new File(projectDirFile, "file_1.cc"), FileDownloadInfo.State.UNCONFIRMED),
            new NameStatePair(new File(projectDirFile, "file_1.cpp"), FileDownloadInfo.State.UNCONFIRMED),
            new NameStatePair(new File(projectDirFile, "file_1.cxx"), FileDownloadInfo.State.UNCONFIRMED),
            new NameStatePair(new File(projectDirFile, "file_1.h"), FileDownloadInfo.State.UNCONFIRMED),
            new NameStatePair(new File(projectDirFile, "file_1.hpp"), FileDownloadInfo.State.UNCONFIRMED)
            //new NameStatePair(new File(projectDirFile, "Makefile"), FileDownloadInfo.State.UNCONFIRMED)
        };
        checkInfo(filesToCheck, 12000, RemoteProjectSupport.getPrivateStorage(makeProject));
    }

    @Override
    protected void buildProject(MakeProject makeProject, String command, long timeout, TimeUnit unit) throws Exception {
        try {
            super.buildProject(makeProject, command, timeout, unit);
        } catch (AssertionFailedError ex) {
            String localProjectDir = makeProject.getProjectDirectory().getPath();
            String remoteProjectDir = RemotePathMap.getPathMap(getTestExecutionEnvironment()).getRemotePath(localProjectDir, false);
            String zipName = "__rfs_remote_project__.zip";
            String remoteZip = remoteProjectDir + "/" + zipName;
            String localZip = localProjectDir + "/" + zipName;
            ExitStatus res = ProcessUtils.executeInDir(remoteProjectDir, getTestExecutionEnvironment(), "zip", "-r", "-q", remoteZip, ".");
            if (res.isOK()) {
                int rc = CommonTasksSupport.downloadFile(remoteZip, getTestExecutionEnvironment(), localZip, null).get();
                if (rc == 0) {
                    System.err.printf("Remote project content copied to %s\n", localZip);
                } else {
                    System.err.printf("Error downloading remote project content\n");
                }
            } else {
                System.err.printf("Can not download remote project: rc=%d err=%s\n", res.exitCode, res.getErrorString());
            }            
            throw ex;
        }
    }

    private void checkInfo(NameStatePair[] pairsToCheck, long timeout, FileObject privProjectStorageDir) throws IOException {
        List<NameStatePair> pairs = new ArrayList<>(Arrays.asList(pairsToCheck));
        long stopTime = System.currentTimeMillis() + timeout;
        while (true) {
            List<FileDownloadInfo> updates = HostUpdates.testGetUpdates(getTestExecutionEnvironment(), privProjectStorageDir);
            boolean success = true;
            StringBuilder notFoundMessage = new StringBuilder();
            StringBuilder wrongStateFoundMessage = new StringBuilder();
            for (Iterator<NameStatePair> iter = pairs.iterator(); iter.hasNext(); ) {
                NameStatePair pair = iter.next();
                FileDownloadInfo info = find(updates, pair.file);
                if (info == null) {
                    success = false;
                    if (notFoundMessage.length() == 0) {
                        notFoundMessage.append("Can not find FileDownloadInfo for ");
                    } else {
                        notFoundMessage.append(", ");
                    }
                    notFoundMessage.append(pair.file.getName());
                } else {
                    FileDownloadInfo.State state = info.getState();
                    if (state.equals(pair.state)) {
                        System.err.printf("\tOK state %s for %s at %s\n", info.getState(), info.getLocalFile(), getTestExecutionEnvironment());
                        iter.remove();
                    } else {
                        success = false;
                        if (wrongStateFoundMessage.length() == 0) {
                            wrongStateFoundMessage.append("Wrong state: ");
                        } else {
                            wrongStateFoundMessage.append(", ");
                        }
                        wrongStateFoundMessage.append(pair.file.getName()).append(": expected ").append(pair.state).append(" found ").append(state);
                    }
                }
            }
            if (success) {
                return;
            } else {
                if (System.currentTimeMillis() < stopTime) {
                    sleep(500);
                } else {
                    StringBuilder message = new StringBuilder(notFoundMessage);
                    if (message.length() > 0 && wrongStateFoundMessage.length() > 0) {
                        message.append(";\n");
                    }
                    message.append(wrongStateFoundMessage);
                    assertTrue(message.toString(), false);
                }
            }
        }
    }

    private FileDownloadInfo find(List<FileDownloadInfo> updates, File file) {
        for (FileDownloadInfo info : updates) {
            if (info.getLocalFile().equals(file)) {
                return info;
            }
        }
        return null;
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteBuildUpdatesDownloadTestCase.class);
    }
}
