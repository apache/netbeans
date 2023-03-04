/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.nativeexecution.api.util;

import java.io.File;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.Md5checker.Result;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestSuite;

/**
 *
 * @author Vladimir Kvashin
 */
public class Md5checkerTest extends NativeExecutionBaseTestCase {

    public Md5checkerTest(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
    }

    @SuppressWarnings("unchecked")
    public static Test suite() {
        return new NativeExecutionBaseTestSuite(Md5checkerTest.class);
    }

    @ForAllEnvironments(section = "remote.platforms")
    public void testChecker() throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        ConnectionManager.getInstance().connectTo(env);
        clearRemoteTmpDir();
        String remoteTmpDir = createRemoteTmpDir();
        File localFile = File.createTempFile("test_checker_", ".dat");
        localFile.deleteOnExit();
        writeFile(localFile, "1\n2\n3\n");

        String remotePath = remoteTmpDir + "/" + localFile.getName();
        assertFalse("File " + env + ":" + remotePath + " should not exist at this point", HostInfoUtils.fileExists(env, remotePath));

        Md5checker checker = new Md5checker(env);
        Result res = checker.check(localFile, remotePath);
        assertEquals("Check result", Md5checker.Result.INEXISTENT, res);

        int rc = CommonTasksSupport.uploadFile(localFile, env, remotePath, 0777).get().getExitCode();
        assertEquals("Error copying " + localFile + " file to " + env + ":" + remotePath, 0, rc);
        res = checker.check(localFile, remotePath);
        assertEquals("Check result", Md5checker.Result.UPTODATE, res);

        writeFile(localFile, "4\n5\n6\n");
        res = checker.check(localFile, remotePath);
        assertEquals("Check result", Md5checker.Result.DIFFERS, res);

        clearRemoteTmpDir();
        localFile.delete();
    }
}
