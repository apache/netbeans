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
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;
import org.netbeans.modules.remote.test.RemoteApiTest;
import org.openide.filesystems.FileObject;

/**
 * There hardly is a way to unit test remote operations.
 * This is just an entry point for manual validation.
 *
 */
public class CreateDataAndFolderTestCase extends RemoteFileTestBase {

    public CreateDataAndFolderTestCase(String testName, ExecutionEnvironment execEnv) throws IOException, FormatException {
        super(testName, execEnv);
    }

    @ForAllEnvironments
    public void testCreateFolder() throws Exception {
        String baseDir = null;
        try {
            baseDir = mkTempAndRefreshParent(true);
            FileObject baseDirFO = getFileObject(baseDir);
            
            String childName;
            String childAbsPath;
            FileObject childFO;
            Exception exception;
                    
            childName = "child_folder";
            
            childFO = baseDirFO.createFolder(childName);
            assertNotNull("Null child file object", childFO.isValid());
            assertTrue("child file object isn't valid", childFO.isValid());
            assertTrue("child isn't a directory", childFO.isFolder());
            childAbsPath = baseDir + '/' + childName;
            assertTrue("Child file should exist: " + childAbsPath, HostInfoUtils.fileExists(execEnv, childAbsPath));
            
            exception = null;
            try {
                baseDirFO.createFolder(childName);
            } catch (IOException e) {  
                exception = e;
            }
            assertTrue("Creating the same directory twice; an IOException should be thrown", exception != null);
            
            childName = "child folder with a space";
            childFO = baseDirFO.createFolder(childName);
            assertNotNull("Null child file object", childFO.isValid());
            assertTrue("child file object isn't valid", childFO.isValid());
            assertTrue("child isn't a directory", childFO.isFolder());
            childAbsPath = baseDir + '/' + childName;
            assertTrue("Child file should exist: " + childAbsPath, HostInfoUtils.fileExists(execEnv, childAbsPath));
            
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }
    
    

    @ForAllEnvironments
    public void testCreateData() throws Exception {
        String baseDir = mkTempAndRefreshParent(true);
        try {
            baseDir = mkTempAndRefreshParent(true);
            FileObject baseDirFO = getFileObject(baseDir);
            
            String childName;
            String childAbsPath;
            FileObject childFO;
            Exception exception;
                       
            childName = "child_file";
            childFO = baseDirFO.createData(childName);
            assertNotNull("Null child file object", childFO.isValid());
            assertTrue("child file object isn't valid", childFO.isValid());
            assertTrue("child isn't a plain file", childFO.isData());
            childAbsPath = baseDir + '/' + childName;
            assertTrue("Child file should exist: " + childAbsPath, HostInfoUtils.fileExists(execEnv, childAbsPath));
            
            exception = null;
            try {
                childFO = baseDirFO.createData(childName);
            } catch(IOException e) {
                exception = e;
            }
            assertTrue("Creating the same file twice; an IOException should be thrown", exception != null);
            
            childName = "child file with a space";
            childFO = baseDirFO.createData(childName);
            assertNotNull("Null child file object", childFO.isValid());
            assertTrue("child file object isn't valid", childFO.isValid());
            assertTrue("child isn't a plain file", childFO.isData());
            childAbsPath = baseDir + '/' + childName;
            assertTrue("Child file should exist: " + childAbsPath, HostInfoUtils.fileExists(execEnv, childAbsPath));
        } finally {
            removeRemoteDirIfNotNull(baseDir);
        }
    }
    
    public static Test suite() {
        return RemoteApiTest.createSuite(CreateDataAndFolderTestCase.class);
    }
}
