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
package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
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
public class RefreshNonInstantiatedTestCase extends RemoteFileTestBase {

    public RefreshNonInstantiatedTestCase(String testName) {
        super(testName);
    }
    
    public RefreshNonInstantiatedTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    private void recurse(FileObject fo) {
        for (FileObject child : fo.getChildren()) {
            recurse(child);
        }
    }

    /**
     * Check that when refreshing a directory
     * no unnecessary children are instantiated
     */
    @ForAllEnvironments
    public void testRefreshNonInstantiated() throws Exception {
        String baseDir = null;
        try {
            RemoteFileSystemManager.getInstance().resetFileSystem(execEnv, false);
            baseDir = mkTempAndRefreshParent(true);
            RemoteFileObject baseFO = getFileObject(baseDir);
            baseFO.refresh();
            recurse(baseFO);

            String[] struct = new String[] {
                "d real_dir_1",
                "d real_dir_1/subdir_1",
                "d real_dir_1/subdir_1/subsub_a",
                "d real_dir_1/subdir_1/subsub_a/subsubsub_x",
                "d real_dir_1/subdir_1/subsub_a/subsubsub_y",
                "d real_dir_1/subdir_1/subsub_a/subsubsub_z",
                "d real_dir_1/subdir_1/subsub_b",
                "d real_dir_1/subdir_2",
                "d real_dir_1/subdir_2/subsub_c",
                "d real_dir_1/subdir_2/subsub_d"
            };
            createDirStructure(execEnv, baseDir, struct);
            
            int dirSyncCount_1 = fs.getDirSyncCount();
            int cacheSize_1 = fs.getCachedFileObjectsCount();

            baseFO.refresh();

            int dirSyncCount_2 = fs.getDirSyncCount();
            int cacheSize_2 = fs.getCachedFileObjectsCount();

            System.err.printf("Cache size: %d -> %d   Dir sync count: %d -> %d\n", cacheSize_1, cacheSize_2, dirSyncCount_1, dirSyncCount_2);

            assertEquals("Wrong dir sync count:", 1, dirSyncCount_2 - dirSyncCount_1);
            assertEquals("Wrong cache size increment:", 1, cacheSize_2 - cacheSize_1);
        } finally {
            if (baseDir != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), "chmod", "-R", "700", baseDir);
                removeRemoteDirIfNotNull(baseDir);
            }
        }        
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(RefreshNonInstantiatedTestCase.class);
    }
}
