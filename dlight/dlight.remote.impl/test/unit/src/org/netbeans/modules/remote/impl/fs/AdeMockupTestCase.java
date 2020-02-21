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

import junit.framework.Test;
import org.netbeans.modules.remote.test.RemoteApiTest;
import java.io.IOException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class AdeMockupTestCase  extends RemoteFileTestBase  {
    
    public AdeMockupTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testInstances() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);

            String fileName1 = "file1";
            String fileName2 = "file2";

            String script1 = // file1, file2 - plain files
                    "cd " + baseDir + "; " +
                    "rm -rf *; " +
                    "touch "+ fileName1 + "; " +                    
                    "touch "+ fileName2 + "; " ;

            String script2 = // file1 - plain file, file2 - link
                    "cd " + baseDir + "; " +
                    "rm -rf *; " +
                    "touch "+ fileName1 + "; " +                    
                    "ln -s " + fileName1 + ' ' + fileName2 + ";";

            ProcessUtils.ExitStatus res = ProcessUtils.execute(execEnv, "sh", "-c", script1);
            assertEquals("Error executing script \"" + script1 + "\": " + res.getErrorString(), 0, res.exitCode);
            
            FileObject baseDirFO = getFileObject(baseDir);
            
            baseDirFO.refresh();           
            FileObject fo1_1 = baseDirFO.getFileObject(fileName1);
            FileObject fo2_1 = baseDirFO.getFileObject(fileName2);
            assertNotNull(fo1_1);
            assertNotNull(fo2_1);
            
            res = ProcessUtils.execute(execEnv, "sh", "-c", script2);
            assertEquals("Error executing script \"" + script1 + "\": " + res.getErrorString(), 0, res.exitCode);
                        
            baseDirFO.refresh();
            
            FileObject fo1_2 = baseDirFO.getFileObject(fileName1);
            FileObject fo2_2 = baseDirFO.getFileObject(fileName2);
            assertNotNull(fo1_2);
            assertNotNull(fo2_2);
            assertTrue("Instances differ for " + fo1_1.getPath(), fo1_1 == fo1_2);
            assertTrue("Instances differ for " + fo2_1.getPath(), fo2_1 == fo2_2);
            
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(AdeMockupTestCase.class);
    }
    
}
