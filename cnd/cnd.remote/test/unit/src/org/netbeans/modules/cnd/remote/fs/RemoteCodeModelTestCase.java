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

import org.netbeans.modules.cnd.remote.test.RemoteBuildTestBase;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.RandomlyFails;
//import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
//import org.netbeans.modules.cnd.indexing.impl.TextIndexStorageManager;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
//import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
//import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
//import org.netbeans.modules.cnd.repository.support.RepositoryTestUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;

// NOTE: Some tests commented out since Apache NetBeans does not use the
// donated "cnd.api.model, cnd.indexing, cnd.modelimpl and cnd.repository modules.

/**
 *
 */
public class RemoteCodeModelTestCase extends RemoteBuildTestBase {

    private boolean testReconnect = false;

    public RemoteCodeModelTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        startupModel();
        System.setProperty("cnd.mode.unittest", "true");
        System.setProperty("org.netbeans.modules.cnd.apt.level","OFF"); // NOI18N
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.Utils").setLevel(Level.SEVERE);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown(); 
//        shutdownModel();
    }
    
    
//    private void shutdownModel() {
//        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
//        model.shutdown();
//        ModelSupport.instance().shutdown();
//        TextIndexStorageManager.shutdown();
//        RepositoryTestUtils.deleteDefaultCacheLocation();
//    }    
//
//    private void startupModel() {
//        RepositoryTestUtils.deleteDefaultCacheLocation();
//        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
//        model.startup();
//        ModelSupport.instance().startup();        
//    }

    @Override
    protected void clearRemoteSyncRoot() {
        super.clearRemoteSyncRoot();
        if (testReconnect) {
            ConnectionManager.getInstance().disconnect(getTestExecutionEnvironment());
        }
    }
    
    protected void processSample(Toolchain toolchain, String sampleName, String projectDirBase) throws Exception {
        final ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        MakeProject makeProject = prepareSampleProject(Sync.RFS, toolchain, sampleName, projectDirBase);
        if (testReconnect) {
            assertFalse("Host should be disconnected at this point", ConnectionManager.getInstance().isConnectedTo(execEnv));
        } else {
            assertTrue("Host should be connected at this point", ConnectionManager.getInstance().isConnectedTo(execEnv));
        }
        OpenProjects.getDefault().open(new Project[]{ makeProject }, false);
        changeProjectHost(makeProject, execEnv);
//        checkCodeModel(makeProject);
        if (testReconnect) {
            ConnectionManager.getInstance().connectTo(execEnv);
            assertTrue("Can not reconnect to host", ConnectionManager.getInstance().isConnectedTo(execEnv));
        }
    }

    @ForAllEnvironments
    public void testArgumentsGNU() throws Exception {
        testReconnect = false;
        processSample(Toolchain.GNU, "Arguments", "Args_01");
    }

    @ForAllEnvironments
    public void testArgumentsSolStudio() throws Exception {
        testReconnect = false;
        processSample(Toolchain.SUN, "Arguments", "Args_02");
    }

    @ForAllEnvironments
    @RandomlyFails
    public void testQuoteGNU() throws Exception {
        testReconnect = false;
        processSample(Toolchain.GNU, "Quote", "Quote_01");
    }

    @ForAllEnvironments
    @RandomlyFails
    public void testQuoteSolStudio() throws Exception {
        testReconnect = false;
        processSample(Toolchain.SUN, "Quote", "Quote_02");
    }

//    @ForAllEnvironments
//    public void testArgumentsOffline() throws Exception {
//        testReconnect = true;
//        processSample(Toolchain.GNU, "Arguments", "Args_03");
//    }

    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteCodeModelTestCase.class);
    }
}
