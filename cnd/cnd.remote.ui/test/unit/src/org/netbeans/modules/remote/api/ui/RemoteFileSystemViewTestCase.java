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

package org.netbeans.modules.remote.api.ui;

import java.io.File;
import junit.framework.Test;
import org.netbeans.modules.cnd.remote.test.RemoteDevelopmentTest;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;

/**
 *
 */
public class RemoteFileSystemViewTestCase extends NativeExecutionBaseTestCase {

    public RemoteFileSystemViewTestCase(String name, ExecutionEnvironment env) {
        super(name, env);
    }

    public RemoteFileSystemViewTestCase(String name) {
        super(name);
    }

    @ForAllEnvironments
    public void testIZ_191613() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        RemoteFileSystemView fs = new RemoteFileSystemView("/", env);
        String dirname = "/usr";
        String filename = "include";
        File referenceFile = fs.createFileObject(dirname + '/' + filename);
        assertTrue(referenceFile.exists());
        File dir = fs.createFileObject("/usr");
        File selectedFile = fs.createFileObject(filename);
        assertFalse("isAbsolute() should return false for " + filename, selectedFile.isAbsolute());
        if(!selectedFile.isAbsolute()) {
           selectedFile = fs.getChild(dir, filename);
        }
    }

    @SuppressWarnings("unchecked")
    public static Test suite() {
        return new RemoteDevelopmentTest(RemoteFileSystemViewTestCase.class);
    }

}
