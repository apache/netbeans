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

package org.netbeans.modules.cnd.remote.full;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.AssertionFailedError;
import org.netbeans.modules.cnd.remote.test.RemoteBuildTestBase;
import junit.framework.Test;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadParameters;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport.UploadStatus;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;

/**
 */
public class FullRemoteBuildTestCase extends RemoteBuildTestBase {

    private String remoteTmpDir;
    
    static {
        System.setProperty("cnd.use.indexing.api", "false");
    }
    
    public FullRemoteBuildTestCase(String testName) {
        super(testName);
    }

    public FullRemoteBuildTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);       
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupHost();
        remoteTmpDir = createRemoteTmpDir();
    }

    @Override
    protected void tearDown() throws Exception {
        clearRemoteTmpDir(); // before disconnection!
        super.tearDown();
    }

    private MakeProject importProject(String projectName, boolean link) throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        File origProject = getDataFile(projectName);
        String origProjectCopyName = projectName + "-orig";
        String remoteProjectPath = upload(origProject, remoteTmpDir, "copy-to-private", "private", projectName, origProjectCopyName);
        if (link) {
            String symLinkPath = remoteTmpDir + '/' + projectName + "-lnk";
            runScript(String.format("cd %s; ln -s %s %s", remoteTmpDir, origProjectCopyName, symLinkPath));
            remoteProjectPath = symLinkPath;
        }
        
        FileObject remoteTmpDirFO = FileSystemProvider.getFileObject(getTestExecutionEnvironment(), remoteTmpDir);
        if (remoteTmpDirFO == null) {
            FileSystemProvider.getFileSystem(getTestExecutionEnvironment()).getRoot().refresh();
        } else {
            remoteTmpDirFO.refresh();
        }
        FileObject remoteProjectFO = FileSystemProvider.getFileObject(execEnv, remoteProjectPath);
        assertNotNull("null remote project file object for " + execEnv + ':' + remoteProjectPath, remoteProjectFO);
        assertTrue("Should be a directory: " + remoteProjectFO, remoteProjectFO.isFolder());
        MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(remoteProjectFO);
        assertNotNull("Error opening project " + remoteProjectFO, makeProject);
        return makeProject;
    }

    @ForAllEnvironments
    public void test_iz_249533() throws Exception {
        MakeProject makeProject = importProject("simple_make_project_to_import", false);
        final String origHeaderName = "change_case.h";
        final FileObject parentFO = makeProject.getProjectDirectory();
        FileObject headerFO = parentFO.getFileObject(origHeaderName);
        assertNotNull(headerFO);
        headerFO.delete();
        CsmProject csmProject = getCsmProject(makeProject);
        AtomicReference<AssertionFailedError> exRef = new AtomicReference<>();
        try {
            checkCodeModel(makeProject);
        } catch (AssertionFailedError ex) {
            exRef.set(ex);
        }
        AssertionFailedError ex = exRef.get();
        assertNotNull(ex);
        String messageStart = "Unresolved include";
        assertTrue("Unexpected exception " + ex.getMessage() + ", expected " + messageStart  , ex.getMessage().startsWith(messageStart));
        headerFO = parentFO.createData(origHeaderName);
        sleep(1);
        csmProject.waitParse();
        checkCodeModel(makeProject);
    }

    @ForAllEnvironments
    public void testFullRemoteBuildSimple() throws Exception {
        MakeProject makeProject = importProject("simple_make_project_to_import", false);
        buildProject(makeProject, ActionProvider.COMMAND_BUILD, getSampleBuildTimeout(), TimeUnit.SECONDS);
    }
    
    @ForAllEnvironments
    public void testFullRemoteBuildLink() throws Exception {
        MakeProject makeProject = importProject("simple_make_project_to_import", true);
        buildProject(makeProject, ActionProvider.COMMAND_BUILD, getSampleBuildTimeout(), TimeUnit.SECONDS);
    }
    
    @ForAllEnvironments
    public void testFullRemoteCodeModelSimple() throws Exception {
        MakeProject makeProject = importProject("simple_make_project_to_import", false);
        checkCodeModel(makeProject);
    }
    
    @ForAllEnvironments
    public void testFullRemoteCodeModelLink() throws Exception {
        MakeProject makeProject = importProject("simple_make_project_to_import", true);
        checkCodeModel(makeProject);
    }
    
    // hg filters out "nbproject/private", for that reason we have to rewname it and restore correct name while copying
    protected String upload(File file, String destination, String... replace) throws Exception {
        assertTrue(replace.length % 2 == 0);
        String fileName = file.getName();
        for (int i = 0; i < replace.length; i += 2) {
            if (fileName.equals(replace[i])) {
                fileName = replace[i+1];
            }
        }
        String remotePath = destination + '/' + fileName;        
        if (file.isFile()) {
            UploadParameters up = new CommonTasksSupport.UploadParameters(
                    file, getTestExecutionEnvironment(), remotePath, -1);
            UploadStatus status = CommonTasksSupport.uploadFile(up).get();
            if (!status.isOK()) {
                throw new IOException("Error uploading " + file.getAbsolutePath() + " to " + up.dstExecEnv + 
                        ':' + up.dstFileName + ' ' + status.getError());
            }
        } else {
            PrintWriter err = new PrintWriter(System.err);
            int rc = CommonTasksSupport.mkDir(getTestExecutionEnvironment(), remotePath, err).get();
            if (rc != 0) {
                throw new IOException("Error creating directory  " + getTestExecutionEnvironment() + ':' + remotePath);
            }
            for(File child : file.listFiles()) {
                upload(child, remotePath, replace);
            }
        }
        return remotePath;
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(FullRemoteBuildTestCase.class);
    }
}
