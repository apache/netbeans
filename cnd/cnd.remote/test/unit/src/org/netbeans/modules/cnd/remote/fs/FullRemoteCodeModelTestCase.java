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
package org.netbeans.modules.cnd.remote.fs;

import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.remote.test.RemoteBuildTestBase;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 */
public class FullRemoteCodeModelTestCase extends RemoteBuildTestBase {

    public FullRemoteCodeModelTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("cnd.mode.unittest", "true");
        System.setProperty("org.netbeans.modules.cnd.apt.level", "OFF"); // NOI18N
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.Utils").setLevel(Level.SEVERE);
    }

    protected void processSample(String sampleName) throws Exception {
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        String remoteTempDir = null;
        try {
            remoteTempDir = mkTempAndRefreshParent(true);
            FileObject remoteProjectDirBase = getFileObject(remoteTempDir);            
            FileObject localProjFO = prepareSampleProject(sampleName, sampleName);
            FileObject remoteProjectFO = FileUtil.copyFile(localProjFO, remoteProjectDirBase, sampleName);            
            assertTrue("Should be remote: " + remoteProjectFO, FileSystemProvider.getExecutionEnvironment(remoteProjectFO).isRemote());            
            MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(remoteProjectFO);            
            assertTrue("Should be remote: " + makeProject.getProjectDirectory(), FileSystemProvider.getExecutionEnvironment(makeProject.getProjectDirectory()).isRemote());
            assertTrue("Host should be connected at this point", ConnectionManager.getInstance().isConnectedTo(execEnv));
            OpenProjects.getDefault().open(new Project[]{makeProject}, false);
        } finally {
            if (remoteTempDir != null) {
                CommonTasksSupport.rmDir(execEnv, remoteTempDir, true, new OutputStreamWriter(System.err));
            }
        }        
    }
    
    @ForAllEnvironments
    public void testArguments() throws Exception {
        processSample("Arguments");
    }
    
    public static Test suite() {
        return new RemoteDevelopmentTest(FullRemoteCodeModelTestCase.class);
    }    
}
