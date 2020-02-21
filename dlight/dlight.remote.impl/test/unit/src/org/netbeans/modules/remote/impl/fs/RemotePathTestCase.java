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
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class RemotePathTestCase extends RemoteFileTestBase {


    public RemotePathTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testPath() throws Exception {
        String absPath = "/usr/include/stdio.h";
        String[] parts = absPath.split("/");
        FileObject parent = rootFO;
        for (String name : parts) {
            FileObject child;
            if (name.length() == 0) {
                child = rootFO;
            } else {
                child = getFileObject(parent, name);
            }
            System.err.printf("Child: %s\n", child.getPath());
            if (child == null) {
                break;
            }
            parent = child;
        }
        AssertionError lastAssertion = RemoteLogger.getLastAssertion();
        if (lastAssertion != null) {
            throw lastAssertion;
        }
        String content = readRemoteFile(absPath);
        String text2search = "printf";
        assertTrue("Can not find \"" + text2search + "\" in " + getFileName(execEnv, absPath),
                content.indexOf(text2search) >= 0);
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RemotePathTestCase.class);
    }

}
