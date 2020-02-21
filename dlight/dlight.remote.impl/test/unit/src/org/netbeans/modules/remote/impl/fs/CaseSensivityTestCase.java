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

import java.io.File;
import junit.framework.Test;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;
/**
 *
 */
public class CaseSensivityTestCase extends RemoteFileTestBase {

    public CaseSensivityTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }


    @ForAllEnvironments
    public void testCaseSensitiveDir() throws Exception {
        String tmpDir = null;
        File file = null;
        try {
            tmpDir = mkTempAndRefreshParent(true);
            FileObject tmpDirParentFO = getFileObject(PathUtilities.getDirName(tmpDir));
            String upperDir = tmpDir + "/CC";
            String lowerDir = tmpDir + "/cc";
            String upperFile = upperDir + "/file.dat";
            String lowerFile = lowerDir + "/file.dat";
            //String commonFileU = upperDir + "/file.common";
            //String commonFileL = lowerDir + "/file.common";
            execute("mkdir", "-p", upperDir);
            execute("mkdir", "-p", lowerDir);
            file = File.createTempFile("rfs-test", ".dat");
            upload(file, lowerFile);
            upload(file, upperFile);
            tmpDirParentFO.refresh();
            FileObject lowerDirFO = getFileObject(lowerDir);
            FileObject upperDirFO = getFileObject(upperDir);
            assertNotSame("Directory file objects should differ", lowerDirFO, upperDirFO);
            FileObject lowerFO = getFileObject(lowerFile);
            FileObject upperFO = getFileObject(upperFile);
            assertNotSame("File objects should differ", lowerFO, upperFO);
        } finally {
            if (file != null) {
                file.delete();
            }
            removeRemoteDirIfNotNull(tmpDir);
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(CaseSensivityTestCase.class);
    }

}
