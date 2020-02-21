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
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.NativeexecutionApiUtilTestBridge;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class RemoteFileSystemOffilneTestCase extends RemoteFileTestBase {

    public RemoteFileSystemOffilneTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testOfflineStdlibH() throws Exception {
        String absPath = "/usr/include/stdlib.h";
        FileObject fo = getFileObject(absPath);
        assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
        String content = readFile(fo);
        String text2search = "getenv";
        assertTrue("Can not find \"" + text2search + "\" in " + getFileName(execEnv, absPath),
                content.indexOf(text2search) >= 0);
        char[] passwd = PasswordManager.getInstance().getPassword(execEnv);
        ConnectionManager.getInstance().disconnect(execEnv);
        try {
            assertFalse("Shouldn't be connected now", ConnectionManager.getInstance().isConnectedTo(execEnv));
            fo = getFileObject(absPath);
            assertTrue("File " +  getFileName(execEnv, absPath) + " does not exist", fo.isValid());
            content = readFile(fo);
            text2search = "getenv";
            assertTrue("Can not find \"" + text2search + "\" in " + getFileName(execEnv, absPath),
                    content.indexOf(text2search) >= 0);
        } finally {
            PasswordManager.getInstance().storePassword(execEnv, passwd, false);
            ConnectionManager.getInstance().connectTo(execEnv);
        }
    }

    @ForAllEnvironments
    public void testOfflineCanRead() throws Exception {
        String baseDir = null;
        char[] passwd = null;
        try {
            baseDir = mkTempAndRefreshParent(true);

            String allPath = "/usr/include/stdio.h";
            String usrPath = baseDir + '/' + "usrFile";
            //String grpPath = baseDir + '/' + "grpFile";

            //runScript(String.format("touch %s; chmod 600 %s; touch %s; chmod 060 %s", usrPath, usrPath, grpPath, grpPath));
            runScript(String.format("touch %s; chmod 600 %s;", usrPath, usrPath));

            FileObject baseDirFO = getFileObject(baseDir);
            baseDirFO.refresh();

            FileObject allFO = getFileObject(allPath);
            assertTrue(allFO.canRead());
            FileObject usrFO = getFileObject(usrPath);
            assertTrue(usrFO.canRead());
            //FileObject grpFO = getFileObject(grpPath);
            //assertTrue(grpFO.canRead());

            passwd = PasswordManager.getInstance().getPassword(execEnv);
            ConnectionManager.getInstance().disconnect(execEnv);
            NativeexecutionApiUtilTestBridge.resetHostInfoUtilsData();

            assertTrue("canRead shuld return for " + allFO.getPath() + " when disconnected", allFO.canRead());
            assertTrue("canRead shuld return for " + usrFO.getPath() + " when disconnected", usrFO.canRead());
            //assertTrue(grpFO.canRead());
        } finally {
            PasswordManager.getInstance().storePassword(execEnv, passwd, false);
            ConnectionManager.getInstance().connectTo(execEnv);
            removeRemoteDirIfNotNull(baseDir);
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(RemoteFileSystemOffilneTestCase.class);
    }
}
