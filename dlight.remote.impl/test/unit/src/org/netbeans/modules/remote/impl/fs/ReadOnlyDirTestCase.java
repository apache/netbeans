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
public class ReadOnlyDirTestCase extends RemoteFileTestBase {

    public ReadOnlyDirTestCase(String testName) {
        super(testName);
    }
    
    public ReadOnlyDirTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }


    @ForAllEnvironments
    public void testReadOnlyDirectory() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            String roDirName = "ro_dir";
            String rwDirName = "rw_sub_dir";
            String fileName1 = "file_1";
            String fileName2 = "file_2";
            String roDirPath = baseDir + '/' + roDirName;
            String rwDirPath = roDirPath + '/' + rwDirName;
            String filePath1 = rwDirPath + '/' + fileName1;
            String filePath2 = roDirPath + '/' + fileName2;
            
            String script = 
                    "mkdir -p " + roDirPath + "; " +
                    "mkdir -p " + rwDirPath + "; " +
                    "touch " + filePath1 + "; " +
                    "touch " + filePath2 + "; " +
                    "chmod a-r " + roDirPath;
            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script);
            assertEquals("Error executing sc    ript \"" + script + "\": " + res.getErrorString(), 0, res.exitCode);
            refreshParent(roDirPath);
            RemoteFileObject roDirFO = getFileObject(roDirPath);
            assertFalse("Should not be readable: " + roDirFO, roDirFO.canRead());
            FileObject rwDirFO = getFileObject(rwDirPath);
            FileObject fileFO1 = getFileObject(filePath1);
            DirectoryStorage storage;
            FileObject[] children;
            FileObject invalid;
            
            children = roDirFO.getChildren();
            assertEquals("children size for " + roDirFO.getPath(), 1, children.length);
            
            invalid = roDirFO.getFileObject("inexistent1");
            assertNull("file objject should be null for inexistent1", invalid);
            children = roDirFO.getChildren();
            assertEquals("children size for " + roDirFO.getPath(), 1, children.length);
            storage = ((RemoteDirectory) roDirFO.getImplementor()).testGetExistingDirectoryStorage();            
            assertEquals("storage.size for " + roDirFO.getPath(), 2, storage.listAll().size());
            
            FileObject fileFO2 = getFileObject(filePath2);
            children = roDirFO.getChildren();
            assertEquals("children size for " + roDirFO.getPath(), 2, children.length);
            storage = ((RemoteDirectory) roDirFO.getImplementor()).testGetExistingDirectoryStorage();
            assertEquals("storage.size", 3, storage.listAll().size());
            
            invalid = roDirFO.getFileObject("inexistent2");
            assertNull("file objject should be null for inexistent2", invalid);
            children = roDirFO.getChildren();
            assertEquals("children size for " + roDirFO.getPath(), 2, children.length);
            storage = ((RemoteDirectory) roDirFO.getImplementor()).testGetExistingDirectoryStorage();
            assertEquals("storage.size for " + roDirFO.getPath(), 4, storage.listAll().size());
            
            roDirFO.refresh();
            children = roDirFO.getChildren();
            assertEquals("children size for " + roDirFO.getPath(), 2, children.length);
            storage = ((RemoteDirectory) roDirFO.getImplementor()).testGetExistingDirectoryStorage();
            assertEquals("storage.size for " + roDirFO.getPath(), 2, storage.listAll().size());            
        } finally {
            if (baseDir != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), "chmod", "-R", "700", baseDir);
                removeRemoteDirIfNotNull(baseDir);
            }
        }        
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(ReadOnlyDirTestCase.class);
    }
}
