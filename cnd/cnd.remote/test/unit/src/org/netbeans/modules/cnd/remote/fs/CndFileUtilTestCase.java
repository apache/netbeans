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

import java.io.File;
import java.io.OutputStreamWriter;
import junit.framework.Test;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.cnd.remote.test.RemoteTestBase;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.spi.FileSystemCacheProvider;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public class CndFileUtilTestCase extends RemoteTestBase {

    public CndFileUtilTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ServerList.addServer(execEnv, execEnv.getDisplayName(), null, true, false);
        ConnectionManager.getInstance().connectTo(execEnv);
    }

    @ForAllEnvironments
    public void testExists() throws Exception {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        FileSystem fs = FileSystemProvider.getFileSystem(execEnv);
        String remoteTempDir = null;
        try {
            remoteTempDir = mkTempAndRefreshParent(true);
            FileObject remoteProjectDirBase = getFileObject(remoteTempDir);            
            FileObject file_1 = remoteProjectDirBase.createData("file_1");
            boolean exists = CndFileUtils.exists(fs, file_1.getPath());
            assertTrue(exists);
            file_1.delete();
            final String path_1 = file_1.getPath();
            exists = CndFileUtils.exists(fs, path_1);
            CndUtils.assertTrueInConsole(exists, "CndUtils should report that the file " + path_1 + " still exists");
            CndFileUtils.clearFileExistenceCache();
            exists = CndFileUtils.exists(fs, file_1.getPath());
            assertFalse(exists);
        } finally {
            if (remoteTempDir != null) {
                CommonTasksSupport.rmDir(execEnv, remoteTempDir, true, new OutputStreamWriter(System.err));
            }
        }        
    }

    public static Test suite() {
        return new RemoteDevelopmentTest(CndFileUtilTestCase.class);
    }

}
