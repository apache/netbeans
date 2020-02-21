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
import org.netbeans.modules.remote.impl.fs.server.FSSTransport;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class MoveTestCase extends RemoteFileTestBase {

//    static {
//        System.setProperty("remote.fs_server.verbose", "0");
//        System.setProperty("remote.fs_server.suppress.stderr", "false");
//    }

    public MoveTestCase(String testName) {
        super(testName);
    }
    
    public MoveTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }


    @ForAllEnvironments
    public void testMoveToExistent() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            RemoteFileObject baseFO = getFileObject(baseDir);
            FileObject fo1 = FileUtil.createData(baseFO, "file_1");
            FileObject fo2 = FileUtil.createData(baseFO, "file_2");
            FileLock lock = null;
            try {
                lock = fo1.lock();
                fo1.move(lock, baseFO, fo2.getName(), fo2.getExt());
                fail("move  should fire exception if file already exists");
            } catch (IOException iex) {
                /** Test passed*/
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }            
            }
        } finally {
            if (baseDir != null) {
                ProcessUtils.ExitStatus res = ProcessUtils.execute(getTestExecutionEnvironment(), "chmod", "-R", "700", baseDir);
                removeRemoteDirIfNotNull(baseDir);
            }
            FSSTransport.getInstance(execEnv).testSetCleanupUponStart(true);
        }        
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(MoveTestCase.class);
    }
}
