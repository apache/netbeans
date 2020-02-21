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

package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 * Teste for #198129 - Error opening a project created with NB 6.5
 */
public class Project_65_Test extends CndBaseTestCase {

    public Project_65_Test(String testName) {
        super(testName);
    }

    @Override
    protected boolean addEditorSupport() {
        return false;
    }

    protected MakeProject openProject(String projectName) throws IOException, Exception, IllegalArgumentException {
        File scriptFile = new File(getTestCaseDataDir(), "pre-process.sh");
        if (scriptFile.exists()) {
            ExitStatus res = ProcessUtils.executeInDir(getTestCaseDataDir().getAbsolutePath(), 
                    ExecutionEnvironmentFactory.getLocal(), "/bin/sh", scriptFile.getAbsolutePath());
            assertTrue(res.getErrorString(), res.isOK());
        }        
        FileObject projectDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(getDataFile(projectName)));
        MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(projectDirFO);
        assertNotNull("project is null", makeProject);
        return makeProject;
    }
    
    public void testMakeProj65() throws Throwable {
        if (Utilities.isWindows()) {
            return;
        }
        MakeProject makeProject = openProject("makeproj-with-link-PRJ-65");
        OpenProjectList.getDefault().open(makeProject);
        ConfigurationDescriptorProvider cdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        assertNotNull("Null ConfigurationDescriptorProvider", cdp);
        cdp.getConfigurationDescriptor();
        assertTrue("cdp.gotDescriptor returned false", cdp.gotDescriptor());
        Throwable lastAssertion = CndUtils.getLastAssertion();
        if (lastAssertion != null) {
            throw lastAssertion;
        }
    }

    protected int getSampleBuildTimeout() throws Exception {
        int result = 180;
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String timeout = rcFile.get("makeproject", "build.timeout");
        if (timeout != null) {
            result = Integer.parseInt(timeout);
        }
        return result;
    }    
}
